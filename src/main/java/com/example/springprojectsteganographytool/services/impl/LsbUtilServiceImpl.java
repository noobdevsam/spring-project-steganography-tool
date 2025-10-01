package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import com.example.springprojectsteganographytool.models.lsb.HeaderInfoDTO;
import com.example.springprojectsteganographytool.models.lsb.MetadataBlockDTO;
import com.example.springprojectsteganographytool.services.LsbUtilService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Service
@Slf4j
public class LsbUtilServiceImpl implements LsbUtilService {

    private static final byte[] STEGO_MAGIC = new byte[]{'S', 'T', 'E', 'G'};
    private static final byte STEGO_VERSION = 1;

    private static final int HEADER_MAGIC_LEN = 4;
    private static final int HEADER_VERSION_LEN = 1;
    private static final int HEADER_TOTAL_LEN = HEADER_MAGIC_LEN + HEADER_VERSION_LEN; // 5

    private static final int META_LEN_BYTES = 4;
    private static final int PAYLOAD_LEN_BYTES = 8;

    private final ObjectMapper mapper = new ObjectMapper();

    public LsbUtilServiceImpl() {

    }

    // ------- New  BufferedImage-based public API -------

    @Override
    public byte[] encode(
            byte[] imageBytes,
            byte[] payloadBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException {

        // Writes: [MAGIC(4)][VERSION(1)] at LSB=1, then [META_LEN(4)][META_JSON] at LSB=1,
        // then [PAYLOAD_LEN(8)] at LSB=1 and [PAYLOAD] at LSB=metadata.lsbDepth()

        try {
            log.info("Encoding payload into image with metadata");

            var result = getResultForStartingEncoding(imageBytes, metadata); // Prepare the image and metadata block

            // Check capacity for payload: [PAYLOAD_LEN(8)][PAYLOAD]
            var requiredPayloadBytes = PAYLOAD_LEN_BYTES + payloadBytes.length;
            if (requiredPayloadBytes > result.payloadCapacityBytes()) {
                throw new MessageTooLargeException("Payload is too large for the image with the given LSB depth");
            }

            // Step 1: Write metadata block at LSB=1
            writeBytesToImage(result.working(), 0, 1, result.metaBlock());

            // Step 2: Write payload length  at LSB=1
            var payloadLengthBytes = ByteBuffer
                    .allocate(PAYLOAD_LEN_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(payloadBytes.length)
                    .array(); // Convert the payload length to an 8-byte array

            var payloadLenPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, 1); // Calculate pixels used by payload length at LSB=1
            writeBytesToImage(result.working(), result.metaPixelCount(), 1, payloadLengthBytes); // Write the payload length to the image using LSB depth of 1

            // Step 3: Write actual payload data at metadata.lsbDepth()
            var payloadStartPixel = result.metaPixelCount() + payloadLenPixels; // Calculate where the payload data starts
            writeBytesToImage(result.working(), payloadStartPixel, metadata.lsbDepth(), payloadBytes); // Write the actual payload data to the image using the specified LSB depth

            // Step 4: Return the modified image as a byte array in lossless PNG format
            return imageToBytes(result.working()); // Convert the modified image back to a byte array in lossless PNG format
        } catch (MessageTooLargeException | InvalidLsbDepthException | MetadataNotFoundException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            log.error("Error during LSB encoding", e);
            throw new LsbEncodingException("Failed to encode payload into image", e);
        }

    }

    @Override
    public byte[] encodeStream(
            byte[] imageBytes,
            InputStream payloadStream,
            long payloadLength,
            StegoMetadataDTO metadata
    ) throws Exception {

        try {
            var result = getResultForStartingEncoding(imageBytes, metadata); // Prepare the image and metadata block

            var requiredPayloadBytes = PAYLOAD_LEN_BYTES + payloadLength;
            if (requiredPayloadBytes > result.payloadCapacityBytes()) {
                throw new MessageTooLargeException("Payload is too large for the image with the given LSB depth");
            }

            // write metadata block
            writeBytesToImage(result.working(), 0, 1, result.metaBlock()); // Write the metadata block to the image using LSB depth of 1

            //Write payload length at LSB=1
            var payloadLengthBytes = ByteBuffer
                    .allocate(PAYLOAD_LEN_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(payloadLength)
                    .array(); // Convert the payload length to an 8-byte array

            var payloadLenPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, 1); // Calculate pixels used by payload length at LSB=1
            writeBytesToImage(result.working(), result.metaPixelCount(), 1, payloadLengthBytes); // Write the payload length to the image using LSB depth of 1

            var payloadStartPixel = result.metaPixelCount() + payloadLenPixels; // Calculate where the payload data starts
            writeStreamToImage(result.working(), payloadStartPixel, metadata.lsbDepth(), payloadStream, payloadLength); // Stream the payload data into the image using the specified LSB depth

            return imageToBytes(result.working()); // Convert the modified image back to a byte array in lossless PNG format
        } catch (Exception e) {
            throw new LsbEncodingException("LSB stream encoding failed", e);
        }

    }

    @Override
    public StegoMetadataDTO extractMetadata(BufferedImage stegoImage) throws InvalidImageFormatException {
        try {
            return extractMetadataFromImage(
                    convertForLsb(stegoImage)
            );
        } catch (Exception e) {
            throw new InvalidImageFormatException("Failed extracting metadata: " + e.getMessage());
        }
    }

    @Override
    public byte[] decode(BufferedImage stegoImage, Integer lsbDepth) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException {
        try {
            return decodeFromImage(convertForLsb(stegoImage), lsbDepth);
        } catch (Exception e) {
            throw new LsbDecodingException("Decoding failed: " + e.getMessage());
        }
    }

    // ----- Private High-Level Helper Methods -----

    private byte[] decodeFromImage(BufferedImage bufferedImage, Integer lsbDepth) throws Exception {

        StegoMetadataDTO meta = null;

        if (lsbDepth == null) {
            meta = extractMetadataFromImage(bufferedImage);
            lsbDepth = meta.lsbDepth();
        }

        if (lsbDepth != 1 && lsbDepth != 2) {
            throw new InvalidLsbDepthException("Invalid LSB depth: " + lsbDepth);
        }

        // 1) Read and validate header + meta length (at LSB=1)
        var headerInfo = readHeaderAndMetaLength(bufferedImage);

        // 2) Derive meta pixel usages by [MAGIC|VERSION|META_LEN|META_JSON] (all at LSB=1)
        var metaTotalBytes = HEADER_TOTAL_LEN + META_LEN_BYTES + headerInfo.metaLength();
        var metaPixelCount = bytesToPixelCount(metaTotalBytes, 1);

        // 3) Read payload length at LSB=1
        var payloadLenBytes = readBytesFromImage(bufferedImage, metaPixelCount, 1, PAYLOAD_LEN_BYTES);
        var payloadLength = ByteBuffer
                .wrap(payloadLenBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .getLong();
        if (payloadLength < 0 || payloadLength > Integer.MAX_VALUE) {
            throw new LsbDecodingException("Payload length invalid or too large. Got: " + payloadLength);
        }

        // 4) Capacity check
        var totalPixels = (long) bufferedImage.getWidth() * bufferedImage.getHeight();

        // Calculate payload length pixels at LSB=1
        var payloadLenPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, 1);

        // Remaining pixels after metadata and payload length header
        var payloadDataPixels = totalPixels - metaPixelCount - payloadLenPixels;
        var maxPayloadBytes = ((payloadDataPixels * 3L * lsbDepth) / 8L);

        if (payloadLength > maxPayloadBytes) {
            throw new LsbDecodingException("Payload length " + payloadLength + " exceeds capacity" + maxPayloadBytes);
        }

        // 5) Read payload at chosen depth by user
        var payloadStartPixel = metaPixelCount + payloadLenPixels;

        return readBytesFromImage(bufferedImage, payloadStartPixel, lsbDepth, (int) payloadLength);
    }

//    private MetadataBlockDTO getResultForStartingEncoding(byte[] imageBytes, StegoMetadataDTO metadata) throws Exception {
//        if (metadata == null) {
//            throw new MetadataNotFoundException("Metadata cannot be null");
//        }
//
//        if (metadata.lsbDepth() != 1 && metadata.lsbDepth() != 2) {
//            throw new InvalidLsbDepthException("LSB depth must be 1 or 2");
//        }
//
//        var working = deepCopy(bytesToImage(imageBytes));
//
//        //Build metadata block
//        var metaJson = mapper.writeValueAsBytes(metadata); // Convert metadata to JSON bytes
//        var metaLength = metaJson.length; // Get the length of the metadata in bytes
//        var metaLengthBytes = ByteBuffer
//                .allocate(META_LEN_BYTES)
//                .order(ByteOrder.BIG_ENDIAN)
//                .putInt(metaLength)
//                .array(); // Convert the length to a 4-byte array
//        var metaBlockLength = HEADER_TOTAL_LEN + META_LEN_BYTES + metaLength; // Calculate the total length of the metadata block
//        var metaBlock = new byte[metaBlockLength]; // Create a byte array for the metadata block
//
//        // After creating metaBlock
//        log.info("=== META BLOCK HEX DUMP ===");
//        log.info("Full metaBlock (hex): {}", java.util.HexFormat.of().formatHex(metaBlock));
//        log.info("MAGIC bytes: {}", java.util.HexFormat.of().formatHex(java.util.Arrays.copyOfRange(metaBlock, 0, 4)));
//        log.info("VERSION byte: {}", metaBlock[4]);
//        log.info("META_LEN bytes (hex): {}", java.util.HexFormat.of().formatHex(java.util.Arrays.copyOfRange(metaBlock, 5, 9)));
//        log.info("META_LEN value: {}", metaLength);
//
//        // [MAGIC(4)]
//        System.arraycopy(STEGO_MAGIC, 0, metaBlock, 0, HEADER_MAGIC_LEN); // Copy the magic bytes to the metadata block
//        // [VERSION(1)]
//        metaBlock[HEADER_MAGIC_LEN] = STEGO_VERSION; // Set the version byte in the metadata block
//        // [META_LENGTH(4)]
//        System.arraycopy(metaLengthBytes, 0, metaBlock, HEADER_TOTAL_LEN, META_LEN_BYTES); // Copy the metadata length bytes to the metadata block
//        // [META_JSON]
//        System.arraycopy(metaJson, 0, metaBlock, (HEADER_TOTAL_LEN + META_LEN_BYTES), metaLength); // Copy the metadata JSON bytes to the metadata block
//
//        //Check capacity for metadata
//        var totalPixels = (long) working.getWidth() * working.getHeight();
//        var metaPixelCount = bytesToPixelCount(metaBlock.length, 1);
//
//        log.info("=== ENCODING META DEBUG ===");
//        log.info("metaBlock.length: {}", metaBlock.length);
//        log.info("metaPixelCount: {}", metaPixelCount);
//        log.info("metaJson.length: {}", metaLength);
//
//        if (metaPixelCount > totalPixels) {
//            throw new MessageTooLargeException("Metadata is too large for the image with the given LSB depth");
//        }
//
//        //Calculate payload capacity in pixels and bytes
//        // Payload length is stored at LSB=1
//        var payloadLenPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, 1);
//
//        // Remaining pixels after metadata and payload length header
//        var remainingPixels = totalPixels - metaPixelCount - payloadLenPixels;
//
//        // Actual payload capacity at metadata.lsbDepth()
//        var payloadCapacityBits = remainingPixels * 3L * metadata.lsbDepth();
//        var payloadCapacityBytes = payloadCapacityBits / 8L;
//
//        // Return the prepared data
//        return new MetadataBlockDTO(working, metaBlock, metaPixelCount, payloadCapacityBytes);
//    }

    private MetadataBlockDTO getResultForStartingEncoding(byte[] imageBytes, StegoMetadataDTO metadata) throws Exception {
        if (metadata == null) {
            throw new MetadataNotFoundException("Metadata cannot be null");
        }

        if (metadata.lsbDepth() != 1 && metadata.lsbDepth() != 2) {
            throw new InvalidLsbDepthException("LSB depth must be 1 or 2");
        }

        var working = deepCopy(bytesToImage(imageBytes));

        // Build metadata components (but don't combine them yet)
        var metaJson = mapper.writeValueAsBytes(metadata);
        var metaLength = metaJson.length;
        var metaLengthBytes = ByteBuffer
                .allocate(META_LEN_BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(metaLength)
                .array();

        // Calculate total metadata size for capacity checking
        var metaTotalBytes = HEADER_TOTAL_LEN + META_LEN_BYTES + metaLength;

        // Check capacity for metadata
        var totalPixels = (long) working.getWidth() * working.getHeight();
        var metaPixelCount = bytesToPixelCount(metaTotalBytes, 1);
        if (metaPixelCount > totalPixels) {
            throw new MessageTooLargeException("Metadata is too large for the image with the given LSB depth");
        }

        // Calculate payload capacity
        var payloadLenPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, 1);
        var remainingPixels = totalPixels - metaPixelCount - payloadLenPixels;
        var payloadCapacityBits = remainingPixels * 3L * metadata.lsbDepth();
        var payloadCapacityBytes = payloadCapacityBits / 8L;

        // Create the combined metaBlock for backward compatibility with the DTO
        var metaBlock = new byte[metaTotalBytes];
        System.arraycopy(STEGO_MAGIC, 0, metaBlock, 0, HEADER_MAGIC_LEN);
        metaBlock[HEADER_MAGIC_LEN] = STEGO_VERSION;
        System.arraycopy(metaLengthBytes, 0, metaBlock, HEADER_TOTAL_LEN, META_LEN_BYTES);
        System.arraycopy(metaJson, 0, metaBlock, HEADER_TOTAL_LEN + META_LEN_BYTES, metaLength);

        return new MetadataBlockDTO(working, metaBlock, metaPixelCount, payloadCapacityBytes);
    }

    private StegoMetadataDTO extractMetadataFromImage(BufferedImage image) throws Exception {

        // 1. Read just the header and meta length to know the total size of the metadata
        var info = readHeaderAndMetaLength(image);
        var metaTotalBytes = HEADER_TOTAL_LEN + META_LEN_BYTES + info.metaLength();

        // 2. Read the full metadata block at LSB=1
        var metaBlockBytes = readBytesFromImage(image, 0, 1, metaTotalBytes);

        // 3. Extract the JSON part from it
        var metaJsonStart = HEADER_TOTAL_LEN + META_LEN_BYTES;
        var metaJsonBytes = new byte[info.metaLength()];
        System.arraycopy(metaBlockBytes, metaJsonStart, metaJsonBytes, 0, info.metaLength());

        // 4. Deserialize and return
        return mapper.readValue(metaJsonBytes, StegoMetadataDTO.class);
    }

    // ----- Private Mid-Level Header / Metadata helpers -----

    private HeaderInfoDTO readHeaderAndMetaLength(BufferedImage image) throws Exception {

        // 1) Validate header: [MAGIC(4)][VERSION(1)] at LSB=1
        var header = readBytesFromImage(image, 0, 1, HEADER_TOTAL_LEN);
        if (
                header.length != HEADER_TOTAL_LEN
                        || header[0] != STEGO_MAGIC[0]
                        || header[1] != STEGO_MAGIC[1]
                        || header[2] != STEGO_MAGIC[2]
                        || header[3] != STEGO_MAGIC[3]
                        || header[4] != STEGO_VERSION
        ) {
            throw new InvalidImageFormatException("Image does not contain valid LSB header");
        }

        // 2) Read metadata length: [META_LEN(4)] at LSB=1
        var headerPixels = bytesToPixelCount(HEADER_TOTAL_LEN, 1);
        var metaLengthBytes = readBytesFromImage(image, headerPixels, 1, META_LEN_BYTES);
        var metaLength = ByteBuffer
                .wrap(metaLengthBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
        if (metaLength <= 0) {
            throw new MetadataNotFoundException("Metadata length is invalid or zero");
        }

        return new HeaderInfoDTO(image, headerPixels, metaLength);
    }

    // ----- Private Low-Level Helper Methods -----

    private BufferedImage convertForLsb(BufferedImage source) {

        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source; // Already in the desired format
        }

        return deepCopy(source);
    }


    private BufferedImage bytesToImage(
            byte[] imageBytes
    ) throws Exception {

        try (
                // Create an input stream from the byte array
                var byteArrayInputStream = new ByteArrayInputStream(imageBytes)
        ) {
            // Convert byte array to BufferedImage
            var image = ImageIO.read(byteArrayInputStream);

            // Check if the image is null, which indicates an unsupported format or corrupted data
            if (image == null) {
                throw new LsbEncodingException("Unsupported image format or corrupted image data.");
            }

            // Convert the image to a format suitable for LSB encoding
            // Convert to TYPE_INT_ARGB to ensure consistent pixel operations
            var convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // Draw the original image onto the converted image
            convertedImage.getGraphics().drawImage(image, 0, 0, null);

            // Return the converted image
            return convertedImage;
        }

    }

    private byte[] imageToBytes(
            BufferedImage image
    ) throws Exception {

        try (
                var byteArrayOutputStream = new ByteArrayOutputStream()
        ) {

            // Write the image to the output stream in the specified format
            ImageIO.write(image, "png", byteArrayOutputStream);

            // Convert the output stream to a byte array
            return byteArrayOutputStream.toByteArray();

        }
    }

    private BufferedImage deepCopy(BufferedImage source) {

        // Create a new BufferedImage with the same dimensions and type as the source
        var copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        // Draw the source image onto the new BufferedImage
        copy.getGraphics().drawImage(source, 0, 0, null);

        // Return the deep copy of the image
        return copy;
    }

    private int bytesToPixelCount(
            int numberOfBytes,
            int lsbDepth
    ) {

        // Convert bytes to bits
        var bits = (long) numberOfBytes * 8L;

        // 3 color channels (RGB) times lsbDepth bits per channel
        var bitsPerPixel = 3L * lsbDepth;

        // Return the ceil of bits divided by bits per pixel
        // Round up to the nearest whole pixel
        return (int) ((bits + bitsPerPixel - 1) / bitsPerPixel);

    }

    private void writeBytesToImage(
            BufferedImage image,
            int startPixel,
            int lsbDepth,
            byte[] dataBytes
    ) throws MessageTooLargeException {
        var width = image.getWidth(); // get image width
        var height = image.getHeight(); // get image height
        var totalPixels = width * height; // calculate total number of pixels in the image
        var bitPointer = 0; // bit pointer to track the current bit in the byte
        var bytePointer = 0; // byte pointer to track the current byte in the dataBytes array
        var totalBits = dataBytes.length * 8; // total bits in the dataBytes array
        var pixelIndex = startPixel; // start pixel index to begin writing data

        // label for breaking out of nested loops
        outer:
        while (bytePointer < dataBytes.length) { // while there are still bytes to write

            if (pixelIndex >= totalPixels) { // check if pixel index exceeds total pixels
                throw new MessageTooLargeException("Not enough image capacity while writing payload");
            }

            var x = pixelIndex % width; // calculate x coordinate of the pixel
            var y = pixelIndex / width; // calculate y coordinate of the pixel
            var rgb = image.getRGB(x, y); // get the RGB value of the pixel
            var alpha = (rgb >> 24) & 0xFF; // extract the alpha channel from the RGB value

            var red = (rgb >> 16) & 0xFF; // extract the red channel from the RGB value
            var green = (rgb >> 8) & 0xFF; // extract the green channel from the RGB value
            var blue = rgb & 0xFF; // extract the blue channel from the RGB value
            var channels = new int[]{red, green, blue}; // create an array to hold the RGB channels

            for (var c = 0; c < 3; c++) { // iterate over each color channel (R, G, B)

                //get next lsbDepth bits from dataBytes
                var bitsToWrite = 0; // variable to hold the bits to write into the channel

                for (var bit = 0; bit < lsbDepth; bit++) { // iterate over the number of bits to write

                    var globalBitIndex = (bytePointer * 8) + bitPointer; // calculate the global bit index in the dataBytes array
                    var bitValue = 0; // variable to hold the bit value to write

                    if (globalBitIndex < totalBits) { // check if the global bit index is within the bounds of the dataBytes array
                        var currentByte = dataBytes[bytePointer] & 0xFF; // get the current byte from the dataBytes array and ensure it's treated as unsigned
                        var shift = 7 - (bitPointer); // calculate the shift amount to get the correct bit from the byte
                        bitValue = (currentByte >> shift) & 0x01; // extract the bit value from the current byte
                    } else { // if the global bit index exceeds total bits, set bitValue to 0
                        bitValue = 0; // default to 0 if we run out of bits in dataBytes
                    }

                    bitsToWrite = (bitsToWrite << 1) | bitValue; // shift the bitsToWrite left by 1 and add the current bit value
                    bitPointer++; // increment the bit pointer

                    if (bitPointer == 8) { // if we have read 8 bits (1 byte)
                        bitPointer = 0; // reset the bit pointer to 0
                        bytePointer++; // increment the byte pointer to move to the next byte
                    }
                }

                // set lsbDepth bits in the channel
                var mask = ~((1 << lsbDepth) - 1); //use bitwise NOT to create a mask
                channels[c] = (channels[c] & mask) | (bitsToWrite & ((1 << lsbDepth) - 1)); // clear the lsbDepth bits in the channel and set them to bitsToWrite

                if (bytePointer >= dataBytes.length && ((bytePointer * 8) + bitPointer) >= totalBits) { // if we have written all bytes and bits, we can stop
                    // done writing; still to update pixel and break
                    var newRgb = (alpha << 24) | ((channels[0] & 0xFF) << 16) | ((channels[1] & 0xFF) << 8) | (channels[2] & 0xFF); // create a new RGB value with the modified channels
                    image.setRGB(x, y, newRgb); // set the new RGB value to the pixel
                    break outer; // break out of the outer loop
                }
            }

            var newRgb = (alpha << 24) | ((channels[0] & 0xFF) << 16) | ((channels[1] & 0xFF) << 8) | (channels[2] & 0xFF); // create a new RGB value with the modified channels
            image.setRGB(x, y, newRgb); // set the new RGB value to the pixel
            pixelIndex++; // move to the next pixel
        }

    }

    private void writeStreamToImage(
            BufferedImage working,
            int payloadStartPixel,
            int lsbDepth,
            InputStream payloadStream,
            long payloadLength
    ) throws Exception {

        var width = working.getWidth();
        var height = working.getHeight();
        var totalPixels = (long) width * height;
        var pixelIndex = payloadStartPixel;

        var bytesWritten = 0L;
        var bitPointer = 0;
        var currentByte = -1;  // if -1 means need to read new byte
        var channels = new int[3];

        // Buffer read
        var buffer = new byte[8192];
        var bufferPos = 0;
        var bufferLimit = 0;

        // Helper to fetch next bit
        while (bytesWritten < payloadLength) {
            if (pixelIndex >= totalPixels) {
                throw new MessageTooLargeException("Ran out of pixels while streaming payload");
            }

            var x = (pixelIndex % width);
            var y = (pixelIndex / width);
            var rgb = working.getRGB(x, y);
            var alpha = (rgb >> 24) & 0xFF;
            channels[0] = (rgb >> 16) & 0xFF;
            channels[1] = (rgb >> 8) & 0xFF;
            channels[2] = rgb & 0xFF;

            for (var c = 0; c < 3 && bytesWritten < payloadLength; c++) {

                var bitsToWrite = 0;

                for (var bit = 0; bit < lsbDepth; bit++) {

                    if (currentByte == -1) {

                        if (bufferPos >= bufferLimit) {
                            bufferLimit = payloadStream.read(buffer);
                            bufferPos = 0;

                            if (bufferLimit == -1) {
                                // End prematurely (should not happen)
                                bitsToWrite <<= (lsbDepth - bit);
                                bytesWritten = payloadLength;
                                break;
                            }

                        }

                        currentByte = buffer[bufferPos++] & 0xFF;

                    }

                    var shift = 7 - bitPointer;
                    var bitVal = (currentByte >> shift) & 0x01;

                    bitsToWrite = (bitsToWrite << 1) | bitVal;
                    bitPointer++;

                    if (bitPointer == 8) {
                        bitPointer = 0;
                        currentByte = -1;
                        bytesWritten++;

                        if (bytesWritten >= payloadLength) {
                            // If done but still mid-channel fill remaining bits with zeros
                            // (not strictly necessary; final partial group is fine)
                            break;
                        }

                    }

                }

                int mask = ~((1 << lsbDepth) - 1);
                channels[c] = (channels[c] & mask) | (bitsToWrite & ((1 << lsbDepth) - 1));
            }

            var newRgb = (alpha << 24)
                    | ((channels[0] & 0xFF) << 16)
                    | ((channels[1] & 0xFF) << 8)
                    | (channels[2] & 0xFF);
            working.setRGB(x, y, newRgb);
            pixelIndex++;
        }

    }

    private byte[] readBytesFromImage(
            BufferedImage image,
            int startPixel,
            int lsbDepth,
            int numberOfBytes
    ) throws LsbDecodingException {
        int width = image.getWidth(); // get image width
        int height = image.getHeight(); // get image height
        int totalPixels = width * height; // calculate total number of pixels in the image
        int bitPointer = 0; // bit pointer to track the current bit in the byte
        int bytePointer = 0; // byte pointer to track the current byte in the dataBytes array
        int totalBits = numberOfBytes * 8; // total bits to read from the image
        int pixelIndex = startPixel; // start pixel index to begin reading data
        int filledBits = 0; // filled bits to track how many bits have been read
        int currentByte = 0; // current byte to hold the bits being read

        byte[] outputBytes = new byte[numberOfBytes]; // create an output byte array to hold the read data

        outer:
        while (filledBits < totalBits) { // while we have bits to read

            if (pixelIndex >= totalPixels) { // check if pixel index exceeds total pixels
                throw new LsbDecodingException("Not enough pixels while reading payload");
            }

            var x = pixelIndex % width; // calculate x coordinate of the pixel
            var y = pixelIndex / width; // calculate y coordinate of the pixel
            var rgb = image.getRGB(x, y); // get the RGB value of the pixel
            var channels = new int[]{
                    (rgb >> 16) & 0xFF,
                    (rgb >> 8) & 0xFF,
                    rgb & 0xFF
            }; // create an array to hold the RGB channels

            for (var c = 0; c < 3; c++) { // iterate over each color channel (R, G, B)
                var bits = channels[c] & ((1 << lsbDepth) - 1); // extract the lsbDepth bits from the channel

                // append bits to the currentByte from left
                for (var bit = lsbDepth - 1; bit >= 0; bit--) { // iterate over the bits in reverse order

                    var bitValue = (bits >> bit) & 0x01; // extract the bit value from the channel
                    currentByte = (currentByte << 1) | bitValue; // shift the currentByte left by 1 and add the bit value
                    bitPointer++; // increment the bit pointer
                    filledBits++; // increment the filled bits counter

                    if (bitPointer == 8) { // if we have read 8 bits (1 byte)
                        outputBytes[bytePointer++] = (byte) (currentByte & 0xFF); // store the current byte in the output array
                        bitPointer = 0; // reset the bit pointer to 0
                        currentByte = 0; // reset the current byte to 0
                        if (bytePointer >= numberOfBytes) { // if we have filled the output byte array, we can stop
                            break outer; // break out of the outer loop
                        }
                    }

                    if (filledBits >= totalBits) { // check if we have read enough bits
                        // if we have read enough bits, we can stop
                        break outer;
                    }
                }

            }

            pixelIndex++; // move to the next pixel
        }

        return outputBytes; // return the output bytes containing the read data
    }

}
