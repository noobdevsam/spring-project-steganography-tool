package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.common.OperationNotAllowedException;
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
import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import com.example.springprojectsteganographytool.services.AesUtilService;
import com.example.springprojectsteganographytool.services.LsbUtilService;
import com.example.springprojectsteganographytool.services.SteganographyService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class SteganographyServiceImpl implements SteganographyService {

    private final AesUtilService aesUtilService;
    private final LsbUtilService lsbUtilService;
    private final StegoDataRepository stegoDataRepository;

    public SteganographyServiceImpl(AesUtilService aesUtilService, LsbUtilService lsbUtilService, StegoDataRepository stegoDataRepository) {
        this.aesUtilService = aesUtilService;
        this.lsbUtilService = lsbUtilService;
        this.stegoDataRepository = stegoDataRepository;
    }

    @Override
    public StegoEncodeResponseDTO encodeText(BufferedImage coverImage, String message, String password, int lsbDepth) throws InvalidLsbDepthException, MessageTooLargeException, InvalidEncryptionKeyException, LsbEncodingException, AesOperationException, MetadataEncodingException, StorageException {
        return null;
    }

    @Override
    public StegoEncodeResponseDTO encodeFile(BufferedImage coverImage, String originalFileName, byte[] fileBytes, String password, int lsbDepth) throws InvalidLsbDepthException, FileTooLargeException, InvalidEncryptionKeyException, LsbEncodingException, AesOperationException, MetadataEncodingException, StorageException {
        return null;
    }

    @Override
    public StegoDecodeResponseDTO decodeProcess(BufferedImage stegoImage, String password) throws InvalidEncryptionKeyException, MetadataNotFoundException, StegoDataNotFoundException, LsbDecodingException, AesOperationException, MetadataDecodingException {
        return null;
    }

    @Override
    public byte[] encodeTextToBytes(BufferedImage coverImage, String message, String password, int lsbDepth) throws Exception {
        return new byte[0];
    }

    @Override
    public byte[] encodeFileToBytes(BufferedImage coverImage, String originalFileName, byte[] fileBytes, String password, int lsbDepth) throws Exception {
        return new byte[0];
    }

    @Override
    public List<StegoEncodeResponseDTO> listAllEncodings() throws StorageException, StegoImageNotFoundException {
        return List.of();
    }

    @Override
    public StegoEncodeResponseDTO getById(UUID id) throws StorageException, StegoImageNotFoundException {
        return null;
    }

    @Override
    public void deleteById(UUID id) throws StorageException, StegoImageNotFoundException, OperationNotAllowedException {

    }

    // --- helpers ---
    private byte[] bufferedImageToBytes(BufferedImage image, String format)
            throws Exception {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

}
