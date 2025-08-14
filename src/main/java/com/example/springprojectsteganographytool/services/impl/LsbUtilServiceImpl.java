package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import com.example.springprojectsteganographytool.services.LsbUtilService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Service
public class LsbUtilServiceImpl implements LsbUtilService {

    private static final byte[] STEGO_MAGIC = new byte[]{'S', 'T', 'E', 'G'};
    private static final byte STEGO_VERSION = 1;

    private static final int HEADER_MAGIC_LEN = 4;
    private static final int HEADER_VERSION_LEN = 1;
    private static final int HEADER_TOTAL_LEN = HEADER_MAGIC_LEN + HEADER_VERSION_LEN; // 5

    private static final int META_LEN_BYTES = 4;
    private static final int PAYLOAD_LEN_BYTES = 8;

    private final ObjectMapper mapper = new ObjectMapper();


    /**
     * Encodes a payload into an image using LSB steganography.
     * <p>
     * This method delegates the encoding process to the `encodeWithMetadata` method,
     * which embeds both metadata and payload data into the least significant bits
     * of the image's pixels.
     *
     * @param imageBytes   The byte array representing the original image.
     * @param payloadBytes The byte array representing the payload to encode.
     * @param metadata     Metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded payload.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws MessageTooLargeException    If the payload is too large to fit in the image.
     * @throws LsbEncodingException        If an error occurs during encoding.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    @Override
    public byte[] encode(byte[] imageBytes, byte[] payloadBytes, StegoMetadataDTO metadata) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException {
        return encodeWithMetadata(imageBytes, payloadBytes, metadata);
    }

    /**
     * Decodes a payload from a stego image using LSB steganography.
     * <p>
     * This method extracts metadata from the stego image if the LSB depth is not provided,
     * and then decodes the payload using the specified or extracted LSB depth.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @param lsbDepth        The LSB depth used during encoding, or null to extract it from metadata.
     * @return A byte array representing the decoded payload.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws LsbDecodingException        If an error occurs during decoding.
     * @throws StegoDataNotFoundException  If no stego data is found in the image.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    @Override
    public byte[] decode(byte[] stegoImageBytes, Integer lsbDepth) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException {
        try {
            if (lsbDepth == null) {
                var metadata = extractMetadata(stegoImageBytes);
                return extractPayloadUsingDepth(stegoImageBytes, metadata.lsbDepth());
            } else {
                return extractPayloadUsingDepth(stegoImageBytes, lsbDepth);
            }
        } catch (InvalidLsbDepthException | InvalidImageFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new LsbDecodingException(e.getMessage());
        }
    }


    // ----- Private High-Level Helper Methods -----

    /**
     * Extracts metadata from a stego image.
     * <p>
     * This method reads the header and metadata length from the provided stego image byte array.
     * It then extracts the metadata JSON from the image and deserializes it into a `StegoMetadataDTO` object.
     * If the header is invalid, the metadata is not found, or an error occurs during deserialization,
     * appropriate exceptions are thrown.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @return A `StegoMetadataDTO` object containing the extracted metadata.
     * @throws MetadataNotFoundException   If the metadata length is invalid or zero.
     * @throws MetadataDecodingException   If an error occurs during metadata deserialization.
     * @throws InvalidImageFormatException If the image does not contain a valid LSB header.
     */

    private StegoMetadataDTO extractMetadata(byte[] stegoImageBytes) throws MetadataNotFoundException, MetadataDecodingException, InvalidImageFormatException {
        try {

            // Read header and metadata length
            var info = readHeaderAndMetaLength(stegoImageBytes);

            // 3) Read metadata JSON: [META_JSON] at LSB=1
            var metaJsonStartPixel = bytesToPixelCount(HEADER_TOTAL_LEN + META_LEN_BYTES, 1);
            var metaJsonBytes = readBytesFromImage(info.image(), metaJsonStartPixel, 1, info.metaLength());

            // 4) Deserialize metadata JSON and return
            return mapper.readValue(metaJsonBytes, StegoMetadataDTO.class);

            // Note: We intentionally do not validate fields such as lsbDepth here,
            // because extraction/decoding paths validate them when needed.

        } catch (InvalidImageFormatException | MetadataNotFoundException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            throw new MetadataDecodingException("Failed to decode metadata from image", e);
        }

    }

    /**
     * Encodes a payload and metadata into an image using LSB steganography.
     * <p>
     * This method embeds metadata and payload data into the least significant bits
     * of the image's pixels. The metadata is stored at an LSB depth of 1, while the
     * payload is stored at the LSB depth specified in the metadata.
     * <p>
     * The encoding process involves:
     * - Validating the metadata and LSB depth.
     * - Serializing the metadata into a JSON block.
     * - Calculating the capacity of the image to store metadata and payload.
     * - Writing the metadata and payload into the image.
     *
     * @param imageBytes       The byte array representing the original image.
     * @param payloadDataBytes The byte array representing the payload to encode.
     * @param metadata         Metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded payload and metadata.
     * @throws InvalidLsbDepthException  If the specified LSB depth is invalid.
     * @throws MetadataNotFoundException If the metadata is null or invalid.
     * @throws MessageTooLargeException  If the metadata or payload is too large to fit in the image.
     * @throws LsbEncodingException      If an error occurs during the encoding process.
     */
    private byte[] encodeWithMetadata(
            byte[] imageBytes,
            byte[] payloadDataBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MetadataNotFoundException, MessageTooLargeException, LsbEncodingException {

        // Writes: [MAGIC(4)][VERSION(1)] at LSB=1, then [META_LEN(4)][META_JSON] at LSB=1,
        // then [PAYLOAD_LEN(8)][PAYLOAD] at LSB=metadata.lsbDepth()

        try {

            if (metadata == null) {
                throw new MetadataNotFoundException("Metadata cannot be null");
            }

            // Validate the LSB depth in the metadata
            if (metadata.lsbDepth() != 1 && metadata.lsbDepth() != 2) {
                throw new InvalidLsbDepthException("LSB depth must be 1 or 2");
            }

            var working = deepCopy(bytesToImage(imageBytes)); // Create a deep copy of the image to avoid modifying the original

            // Serialize the metadata to JSON and prepare the metadata block
            var metaJson = mapper.writeValueAsBytes(metadata); // Convert metadata to JSON bytes
            var metaLength = metaJson.length; // Get the length of the metadata in bytes
            var metaLengthBytes = ByteBuffer
                    .allocate(META_LEN_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putInt(metaLength)
                    .array(); // Convert the length to a 4-byte array

            var metaBlockLength = HEADER_TOTAL_LEN + META_LEN_BYTES + metaLength; // Calculate the total length of the metadata block
            var metaBlock = new byte[metaBlockLength]; // Create a byte array for the metadata block

            // [MAGIC(4)]
            System.arraycopy(STEGO_MAGIC, 0, metaBlock, 0, HEADER_MAGIC_LEN); // Copy the magic bytes to the metadata block
            // [VERSION(1)]
            metaBlock[HEADER_MAGIC_LEN] = STEGO_VERSION; // Set the version byte in the metadata block
            // [META_LENGTH(4)]
            System.arraycopy(metaLengthBytes, 0, metaBlock, HEADER_TOTAL_LEN, META_LEN_BYTES); // Copy the metadata length bytes to the metadata block
            // [META_JSON]
            System.arraycopy(metaJson, 0, metaBlock, (HEADER_TOTAL_LEN + META_LEN_BYTES), metaLength); // Copy the metadata JSON bytes to the metadata block

            // Check if the image has enough capacity to store the metadata
            var totalPixels = (long) working.getWidth() * working.getHeight();
            var metaPixelCount = bytesToPixelCount(metaBlock.length, 1);
            if (metaPixelCount > totalPixels) {
                throw new MessageTooLargeException("Metadata is too large for the image with the given LSB depth");
            }

            // Calculate the payload capacity in pixels and bytes
            var remainingPixels = totalPixels - metaPixelCount;
            var payloadCapacityBits = remainingPixels * 3L * metadata.lsbDepth();
            var payloadCapacityBytes = payloadCapacityBits / 8L;

            var payloadLengthBytes = ByteBuffer
                    .allocate(PAYLOAD_LEN_BYTES)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(payloadDataBytes.length)
                    .array(); // Convert the payload length to an 8-byte array

            var payloadBlock = new byte[PAYLOAD_LEN_BYTES + payloadDataBytes.length]; // Create a byte array for the payload block
            System.arraycopy(payloadLengthBytes, 0, payloadBlock, 0, PAYLOAD_LEN_BYTES); // Copy the payload length bytes to the payload block
            System.arraycopy(payloadDataBytes, 0, payloadBlock, PAYLOAD_LEN_BYTES, payloadDataBytes.length); // Copy the actual payload data to the payload block

            // Check if the payload fits within the image capacity
            if ((long) payloadBlock.length > payloadCapacityBytes) {
                throw new MessageTooLargeException("Payload is too large for the image with the given LSB depth");
            }

            writeBytesToImage(working, 0, 1, metaBlock); // Write the metadata block to the image using LSB depth of 1
            writeBytesToImage(working, metaPixelCount, metadata.lsbDepth(), payloadBlock); // Write the payload block to the image using the specified LSB depth

            return imageToBytes(working); // Convert the modified image back to a byte array in lossless PNG format

        } catch (MessageTooLargeException | InvalidLsbDepthException | MetadataNotFoundException e) {
            throw e; // Re-throw specific exceptions
        } catch (Exception e) {
            throw new LsbEncodingException("LSB encoding failed", e);
        }
    }

    /**
     * Extracts the payload from a stego image using the specified LSB depth.
     * <p>
     * This method validates the LSB depth, reads and validates the header and metadata length,
     * calculates the number of pixels used for metadata, and extracts the payload length.
     * It then performs capacity checks and reads the payload bytes from the image.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @param lsbDepth        The LSB depth used during encoding (must be 1 or 2).
     * @return A byte array containing the extracted payload.
     * @throws InvalidLsbDepthException If the specified LSB depth is invalid.
     * @throws LsbDecodingException     If the payload length is invalid or exceeds capacity.
     * @throws Exception                If an error occurs during the extraction process.
     */
    private byte[] extractPayloadUsingDepth(byte[] stegoImageBytes, int lsbDepth) throws Exception {
        if (lsbDepth != 1 && lsbDepth != 2) {
            throw new InvalidLsbDepthException("Invalid LSB depth: " + lsbDepth);
        }

        // 1) Read and validate header + metadata length (both at LSB=1)
        var info = readHeaderAndMetaLength(stegoImageBytes);

        // 2) Compute how many pixels were used by [MAGIC|VERSION|META_LEN|META_JSON] (all at LSB=1)
        var metaTotalBytes = HEADER_TOTAL_LEN + META_LEN_BYTES + info.metaLength();
        var metaPixelCount = bytesToPixelCount(metaTotalBytes, 1);

        // 3) Read payload length (at caller-provided LSB depth)
        var payloadLenBytes = readBytesFromImage(info.image(), metaPixelCount, lsbDepth, PAYLOAD_LEN_BYTES);
        var payloadLength = ByteBuffer.wrap(payloadLenBytes).order(ByteOrder.BIG_ENDIAN).getLong();

        if (payloadLength < 0 || payloadLength > Integer.MAX_VALUE) {
            throw new LsbDecodingException("Payload length is invalid or too large");
        }

        // 4) Capacity check for remaining pixels at the chosen depth
        var totalPixels = (long) info.image().getWidth() * info.image().getHeight();
        var remainingPixels = totalPixels - metaPixelCount;
        var maxPayloadBytes = ((remainingPixels * 3L * lsbDepth) / 8L) - PAYLOAD_LEN_BYTES;
        if (payloadLength > maxPayloadBytes) {
            throw new LsbDecodingException("Payload length exceeds the maximum allowed size for the image");
        }

        // 5) Read payload bytes (at caller-provided LSB depth)
        var payloadHeaderPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, lsbDepth);
        var payloadStartPixel = metaPixelCount + payloadHeaderPixels;

        return readBytesFromImage(info.image(), payloadStartPixel, lsbDepth, (int) payloadLength);
    }

    /**
     * Reads and validates the header and metadata length from a stego image.
     * <p>
     * This method extracts the header and metadata length from the provided stego image byte array.
     * It validates the header to ensure it contains the correct magic bytes and version.
     * It also validates the metadata length to ensure it is greater than zero.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @return A `HeaderInfo` object containing the image, header pixel count, and metadata length.
     * @throws InvalidImageFormatException If the image does not contain a valid LSB header.
     * @throws MetadataNotFoundException   If the metadata length is invalid or zero.
     * @throws Exception                   If an error occurs during the header reading process.
     */
    private HeaderInfo readHeaderAndMetaLength(byte[] stegoImageBytes) throws Exception {

        var image = bytesToImage(stegoImageBytes);

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

        return new HeaderInfo(image, headerPixels, metaLength);
    }

    /**
     * A record that encapsulates header information extracted from a stego image.
     * <p>
     * This record is used to store the image, the number of pixels used for the header,
     * and the length of the metadata. It is primarily used as a return type for methods
     * that parse the header and metadata length from a stego image.
     *
     * @param image        The `BufferedImage` representation of the stego image.
     * @param headerPixels The number of pixels used to store the header information.
     * @param metaLength   The length of the metadata in bytes.
     */
    private record HeaderInfo(
            BufferedImage image,
            int headerPixels,
            int metaLength
    ) {
    }

    // ----- Private Low-Level Helper Methods -----

    /**
     * Converts a byte array representing an image into a BufferedImage.
     * <p>
     * This method reads the image data from the provided byte array and converts it
     * into a BufferedImage. If the image format is unsupported or the data is corrupted,
     * an exception is thrown. The resulting image is converted to the TYPE_INT_ARGB format
     * to ensure consistent pixel operations for LSB encoding.
     *
     * @param imageBytes The byte array containing the image data.
     * @return A BufferedImage object representing the image in TYPE_INT_ARGB format.
     * @throws Exception If the image format is unsupported, the data is corrupted, or an I/O error occurs.
     */
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

    /**
     * Converts a BufferedImage into a byte array in the specified format.
     * <p>
     * This method writes the provided BufferedImage to a ByteArrayOutputStream
     * using the PNG image format and returns the
     * resulting byte array. If an error occurs during the writing process,
     * an exception is thrown.
     *
     * @param image The BufferedImage to be converted.
     * @return A byte array representing the image in PNG format.
     * @throws Exception If an error occurs during the image writing process.
     */
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

    /**
     * Creates a deep copy of the provided BufferedImage.
     * <p>
     * This method generates a new BufferedImage with the same dimensions and type
     * as the source image. It then draws the source image onto the new BufferedImage,
     * ensuring that the returned image is an independent copy of the original.
     *
     * @param source The BufferedImage to be copied.
     * @return A new BufferedImage that is a deep copy of the source image.
     */
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

    /**
     * Calculates the number of pixels required to store a given number of bytes using LSB encoding.
     * <p>
     * This method determines the minimum number of pixels needed to encode the specified number
     * of bytes at a given LSB depth. The calculation is based on the number of bits per byte
     * and the number of bits available per pixel for encoding.
     *
     * @param numberOfBytes The number of bytes to be encoded.
     * @param lsbDepth      The LSB depth, representing the number of least significant bits used per color channel.
     * @return The minimum number of pixels required to store the given number of bytes.
     */
    private int bytesToPixelCount(
            int numberOfBytes,
            int lsbDepth
    ) {

        // Convert bytes to bits
        var bits = (long) numberOfBytes * 8L;

        // 3 color channels (RGB) times the LSB depth
        var bitsPerPixel = 3L * lsbDepth;

        // Return the ceil of bits divided by bits per pixel
        // Round up to the nearest whole pixel
        return (int) ((bits + bitsPerPixel - 1) / bitsPerPixel);

    }

    /**
     * Writes a byte array into the pixels of a BufferedImage using LSB encoding.
     * <p>
     * This method encodes the provided data bytes into the least significant bits
     * of the image's pixel color channels, starting from the specified pixel index.
     * The encoding process uses the specified LSB depth to determine how many bits
     * per color channel are used for encoding. If the image does not have enough
     * capacity to store the data, a MessageTooLargeException is thrown.
     *
     * @param image      The BufferedImage into which the data bytes will be encoded.
     * @param startPixel The index of the pixel to start encoding from.
     * @param lsbDepth   The number of least significant bits used per color channel for encoding.
     * @param dataBytes  The byte array containing the data to be encoded.
     * @throws MessageTooLargeException If the image does not have enough capacity to store the data.
     */
    private void writeBytesToImage(
            BufferedImage image,
            int startPixel,
            int lsbDepth,
            byte[] dataBytes
    ) throws MessageTooLargeException {
        int width = image.getWidth(); // get image width
        int height = image.getHeight(); // get image height
        int totalPixels = width * height; // calculate total number of pixels in the image
        int bitPointer = 0; // bit pointer to track the current bit in the byte
        int bytePointer = 0; // byte pointer to track the current byte in the dataBytes array
        int totalBits = dataBytes.length * 8; // total bits in the dataBytes array
        int pixelIndex = startPixel; // start pixel index to begin writing data

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

    /**
     * Reads a byte array from the pixels of a BufferedImage using LSB decoding.
     * <p>
     * This method extracts data encoded in the least significant bits of the image's
     * pixel color channels, starting from the specified pixel index. The decoding
     * process uses the specified LSB depth to determine how many bits per color
     * channel are used for decoding. If the image does not contain enough pixels
     * to extract the required number of bytes, an LsbDecodingException is thrown.
     *
     * @param image         The BufferedImage from which the data bytes will be decoded.
     * @param startPixel    The index of the pixel to start decoding from.
     * @param lsbDepth      The number of least significant bits used per color channel for decoding.
     * @param numberOfBytes The number of bytes to be decoded from the image.
     * @return A byte array containing the decoded data.
     * @throws LsbDecodingException If the image does not have enough pixels to extract the required data.
     */
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
