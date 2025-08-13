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

    @Override
    public byte[] encodeMessage(byte[] imageBytes, byte[] messageBytes, StegoMetadataDTO metadata) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException {
        return new byte[0];
    }

    @Override
    public byte[] decodeMessage(byte[] stegoImageBytes, int lsbDepth) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException {
        return new byte[0];
    }

    @Override
    public byte[] encodeFile(byte[] imageBytes, byte[] fileBytes, StegoMetadataDTO metadata) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException {
        return new byte[0];
    }

    @Override
    public byte[] decodeFile(byte[] stegoImageBytes, int lsbDepth) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException {
        return new byte[0];
    }

    @Override
    public StegoMetadataDTO extractMetadata(byte[] stegoImageBytes) throws MetadataNotFoundException, MetadataDecodingException, InvalidImageFormatException {
        return null;
    }

    // ----- Private High-Level Helper Methods -----

    /**
     * Encodes a payload with metadata into an image using LSB encoding.
     * <p>
     * This method embeds metadata and payload data into the least significant bits
     * of the pixels of the provided image. The metadata is encoded first, followed
     * by the payload. The method ensures that the image has sufficient capacity
     * to store both the metadata and the payload. If the payload is too large for
     * the image, a MessageTooLargeException is thrown.
     *
     * @param imageBytes       The byte array representing the image to encode into.
     * @param payloadDataBytes The byte array containing the payload data to encode.
     * @param metadata         The metadata object containing encoding details such as LSB depth.
     * @return A byte array representing the encoded image in PNG format.
     * @throws LsbEncodingException If an error occurs during the encoding process or the payload is too large.
     */
    private byte[] encodeWithMetadata(
            byte[] imageBytes,
            byte[] payloadDataBytes,
            StegoMetadataDTO metadata
    ) throws LsbEncodingException {

        // Writes: [MAGIC(4)][VERSION(1)] at LSB=1, then [META_LEN(4)][META_JSON] at LSB=1,
        // then [PAYLOAD_LEN(8)][PAYLOAD] at LSB=metadata.lsbDepth()

        try {

            if (metadata == null) {
                throw new LsbEncodingException("Metadata cannot be null");
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

            return imageToBytes(working, "png"); // Convert the modified image back to a byte array in lossless PNG format

        } catch (MessageTooLargeException e) {
            throw new LsbEncodingException("Not enough image capacity while writing data", e);
        } catch (LsbEncodingException e) {
            throw e;
        } catch (Exception e) {
            throw new LsbEncodingException("LSB encoding failed", e);
        }
    }

    /**
     * Extracts the payload data from a stego image using metadata.
     * <p>
     * This method decodes the payload embedded in the least significant bits of the
     * pixels of the provided stego image. It first validates the header, extracts the
     * metadata length, calculates the metadata region size, and retrieves the payload
     * length and the actual payload. If the payload length exceeds the maximum allowed
     * size or the image does not contain valid metadata, an exception is thrown.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @param metadata        The metadata object containing encoding details such as LSB depth.
     * @return A byte array containing the extracted payload data.
     * @throws Exception If an error occurs during the decoding process, such as invalid metadata,
     *                   invalid LSB depth, or insufficient image capacity.
     */
    private byte[] extractPayloadUsingMetadata(
            byte[] stegoImageBytes,
            StegoMetadataDTO metadata
    ) throws Exception {

        // Reads and validates header
        // then finds payload using computed metadata region length without parsing JSON.
        // Expects metadata.lsbDepth() to be correct for the payload region.

        var image = bytesToImage(stegoImageBytes);

        // Read and validate the header
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

        // Calculate the number of pixels used for the header
        var headerPixels = bytesToPixelCount(HEADER_TOTAL_LEN, 1);

        // Read the metadata length
        var metaLengthBytes = readBytesFromImage(image, headerPixels, 1, META_LEN_BYTES);

        // Convert the metadata length bytes to an integer
        var metaLength = ByteBuffer
                .wrap(metaLengthBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
        if (metaLength <= 0) {
            throw new MetadataNotFoundException("Metadata length is invalid or zero");
        }

        // Calculate the total metadata size (header + metadata length + metadata content)
        var metaTotalBytes = HEADER_TOTAL_LEN + META_LEN_BYTES + metaLength;
        var metaPixelCount = bytesToPixelCount(metaTotalBytes, 1);

        // Validate the LSB depth in the metadata
        if (metadata.lsbDepth() != 1 && metadata.lsbDepth() != 2) {
            throw new InvalidLsbDepthException("Invalid LSB depth in metadata: " + metadata.lsbDepth());
        }

        // Read the payload length
        var payloadLengthHeaderBytes = readBytesFromImage(image, metaPixelCount, metadata.lsbDepth(), PAYLOAD_LEN_BYTES);
        var payloadLength = ByteBuffer
                .wrap(payloadLengthHeaderBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .getLong();

        // Validate the payload length
        if (payloadLength < 0 || payloadLength > Integer.MAX_VALUE) {
            throw new LsbDecodingException("Payload length is invalid or too large");
        }

        // Calculate the maximum payload size based on the remaining pixels
        var totalPixels = (long) image.getWidth() * image.getHeight();
        var remainingPixels = totalPixels - metaPixelCount;
        var maxPayloadBytes = ((remainingPixels * 3L * metadata.lsbDepth()) / 8L) - PAYLOAD_LEN_BYTES;
        if (payloadLength > maxPayloadBytes) {
            throw new LsbDecodingException("Payload length exceeds the maximum allowed size for the image");
        }

        // Calculate the starting pixel for the payload
        var payloadHeaderPixels = bytesToPixelCount(PAYLOAD_LEN_BYTES, metadata.lsbDepth());
        var payloadStartPixel = metaPixelCount + payloadHeaderPixels;

        // Read and return the payload data
        return readBytesFromImage(image, payloadStartPixel, metadata.lsbDepth(), (int) (payloadLength));
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
     * using the specified image format (e.g., "png", "jpg") and returns the
     * resulting byte array. If an error occurs during the writing process,
     * an exception is thrown.
     *
     * @param image  The BufferedImage to be converted.
     * @param format The format in which the image should be written (e.g., "png", "jpg").
     * @return A byte array representing the image in the specified format.
     * @throws Exception If an error occurs during the image writing process.
     */
    private byte[] imageToBytes(
            BufferedImage image,
            String format
    ) throws Exception {

        try (
                var byteArrayOutputStream = new ByteArrayOutputStream()
        ) {

            // Write the image to the output stream in the specified format
            ImageIO.write(image, format, byteArrayOutputStream);

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
