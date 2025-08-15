package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.exceptions.file.FileTooLargeException;
import com.example.springprojectsteganographytool.exceptions.file.StegoImageNotFoundException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for performing steganography operations such as encoding and decoding
 * messages or files into images, as well as managing encoded data.
 */
public interface SteganographyService {

    /**
     * Encodes a text message into a cover image using the specified LSB depth.
     *
     * @param coverImage The image to be used as the cover for encoding.
     * @param message    The text message to encode.
     * @param password   The password for encrypting the message.
     * @param lsbDepth   The number of least significant bits per channel to use (1 or 2).
     * @return A DTO containing details of the encoding process.
     * @throws InvalidLsbDepthException      If the specified LSB depth is invalid.
     * @throws MessageTooLargeException      If the message is too large to fit in the image.
     * @throws InvalidEncryptionKeyException If the encryption key is invalid.
     * @throws LsbEncodingException          If an error occurs during the encoding process.
     * @throws AesOperationException         If an error occurs during AES encryption.
     * @throws MetadataEncodingException     If an error occurs while encoding metadata.
     * @throws StorageException              If an error occurs while storing the encoded data.
     */
    StegoEncodeResponseDTO encodeText(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth
    ) throws InvalidLsbDepthException,
            MessageTooLargeException,
            InvalidEncryptionKeyException,
            LsbEncodingException,
            AesOperationException,
            MetadataEncodingException,
            StorageException;

    /**
     * Encodes a file into a cover image using the specified LSB depth.
     *
     * @param coverImage       The image to be used as the cover for encoding.
     * @param originalFileName The original name of the file being encoded.
     * @param fileBytes        The byte array of the file to encode.
     * @param password         The password for encrypting the file.
     * @param lsbDepth         The number of least significant bits per channel to use (1 or 2).
     * @return A DTO containing details of the encoding process.
     * @throws InvalidLsbDepthException      If the specified LSB depth is invalid.
     * @throws FileTooLargeException         If the file is too large to fit in the image.
     * @throws InvalidEncryptionKeyException If the encryption key is invalid.
     * @throws LsbEncodingException          If an error occurs during the encoding process.
     * @throws AesOperationException         If an error occurs during AES encryption.
     * @throws MetadataEncodingException     If an error occurs while encoding metadata.
     * @throws StorageException              If an error occurs while storing the encoded data.
     */
    StegoEncodeResponseDTO encodeFile(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) throws InvalidLsbDepthException,
            FileTooLargeException,
            InvalidEncryptionKeyException,
            LsbEncodingException,
            AesOperationException,
            MetadataEncodingException,
            StorageException;

    /**
     * Decodes a stego image to extract the hidden message or file.
     *
     * @param stegoImage The image containing the hidden data.
     * @param password   The password used to decrypt the hidden data.
     * @return A DTO containing the decoded data.
     * @throws InvalidEncryptionKeyException If the decryption key is invalid.
     * @throws MetadataNotFoundException     If no metadata is found in the image.
     * @throws StegoDataNotFoundException    If no stego data is found in the image.
     * @throws LsbDecodingException          If an error occurs during the decoding process.
     * @throws AesOperationException         If an error occurs during AES decryption.
     * @throws MetadataDecodingException     If an error occurs while decoding metadata.
     */
    StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws InvalidEncryptionKeyException,
            MetadataNotFoundException,
            StegoDataNotFoundException,
            LsbDecodingException,
            AesOperationException,
            MetadataDecodingException;

    /**
     * Encodes a text message into a byte array representation of the stego image.
     *
     * @param coverImage The image to be used as the cover for encoding.
     * @param message    The text message to encode.
     * @param password   The password for encrypting the message.
     * @param lsbDepth   The number of least significant bits per channel to use (1 or 2).
     * @return A byte array representing the stego image.
     * @throws Exception If an error occurs during encoding.
     */
    byte[] encodeTextToBytes(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth
    ) throws Exception;

    /**
     * Encodes a file into a byte array representation of the stego image.
     *
     * @param coverImage       The image to be used as the cover for encoding.
     * @param originalFileName The original name of the file being encoded.
     * @param fileBytes        The byte array of the file to encode.
     * @param password         The password for encrypting the file.
     * @param lsbDepth         The number of least significant bits per channel to use (1 or 2).
     * @return A byte array representing the stego image.
     * @throws Exception If an error occurs during encoding.
     */
    byte[] encodeFileToBytes(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) throws Exception;

    /**
     * Retrieves a list of all encodings performed.
     *
     * @return A list of DTOs containing details of all encodings.
     * @throws StorageException            If an error occurs while retrieving the encodings.
     * @throws StegoImageNotFoundException If no stego images are found.
     */
    List<StegoEncodeResponseDTO> listAllEncodings() throws StorageException, StegoImageNotFoundException;

    /**
     * Retrieves the details of a specific encoding by its ID.
     *
     * @param id The unique identifier of the encoding.
     * @return A DTO containing details of the encoding.
     * @throws StorageException            If an error occurs while retrieving the encoding.
     * @throws StegoImageNotFoundException If the specified stego image is not found.
     */
    StegoEncodeResponseDTO getById(UUID id) throws StorageException, StegoImageNotFoundException;

    /**
     * Deletes a specific encoding by its ID.
     *
     * @param id The unique identifier of the encoding to delete.
     * @throws StegoDataNotFoundException If the specified stego data is not found.
     */
    void deleteById(UUID id) throws StegoDataNotFoundException;

}