package com.example.springprojectsteganographytool.controllers;

import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import com.example.springprojectsteganographytool.services.LsbUtilService;
import com.example.springprojectsteganographytool.services.SteganographyService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stego")
public class StegoController {

    private final SteganographyService steganographyService;
    private final LsbUtilService lsbUtilService;

    public StegoController(
            SteganographyService steganographyService,
            LsbUtilService lsbUtilService
    ) {
        this.steganographyService = steganographyService;
        this.lsbUtilService = lsbUtilService;
    }

    // ----- Helper -----

    private static BufferedImage toBufferedImage(MultipartFile file) throws IOException {
        try (var is = file.getInputStream()) {
            var image = ImageIO.read(is);
            if (image == null) {
                throw new InvalidImageFormatException("Provided file is not a valid image.");
            }
            return image;
        }
    }

    // ----- Encode endpoints -----

    @PostMapping(path = "/encode/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> encodeText(
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("message") String message,
            @RequestParam("password") String password,
            @RequestParam(name = "lsbDepth", defaultValue = "1") int lsbDepth
    ) throws Exception {
        var image = toBufferedImage(coverImage);
        var result = steganographyService.encodeText(image, message, password, lsbDepth);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/encode/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> encodeFile(
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("embeddedFile") MultipartFile embeddedFile,
            @RequestParam("password") String password,
            @RequestParam(name = "lsbDepth", defaultValue = "1") int lsbDepth
    ) throws Exception {
        var image = toBufferedImage(coverImage);
        var originalFileName = embeddedFile.getOriginalFilename();
        var fileBytes = embeddedFile.getBytes();
        var result = steganographyService.encodeFile(image, originalFileName, fileBytes, password, lsbDepth);
        return ResponseEntity.ok(result);
    }

    // ----- Decode operations -----

    @PostMapping(path = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoDecodeResponseDTO> decode(
            @RequestParam("stegoImage") MultipartFile stegoImage,
            @RequestParam("password") String password
    ) throws Exception {
        var image = toBufferedImage(stegoImage);
        var result = steganographyService.decodeProcess(image, password);
        return ResponseEntity.ok(result);
    }

    // ----- Metadata operations -----

    @PostMapping(path = "/metadata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoMetadataDTO> extractMetadata(
            @RequestParam("stegoImage") MultipartFile stegoImage
    ) throws IOException {
        var bytes = stegoImage.getBytes();
        var meta = lsbUtilService.extractMetadata(bytes);
        if (meta == null) {
            throw new InvalidImageFormatException("No metadata found or invalid image provided.");
        }
        return ResponseEntity.ok(meta);
    }

    // ----- Retrieval operations -----

    @GetMapping(path = "/encodings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StegoEncodeResponseDTO>> listAllEncodings() {
        return ResponseEntity.ok(steganographyService.listAllEncodings());
    }

    @GetMapping(path = "/encodings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> getById(@PathVariable("id") UUID id) throws Exception {
        return ResponseEntity.ok(steganographyService.getById(id));
    }

    @DeleteMapping(path = "/encodings/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) throws Exception {
        steganographyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Download operations -----

    @GetMapping(path = "/encodings/{id}/stego-image")
    public ResponseEntity<byte[]> downloadStegoImage(@PathVariable("id") UUID id) throws Exception {
        var dto = steganographyService.downloadStegoImage(id);
        var headers = new HttpHeaders();

        headers.setContentType(MediaType.parseMediaType(dto.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(dto.fileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(dto.fileData());
    }
}
