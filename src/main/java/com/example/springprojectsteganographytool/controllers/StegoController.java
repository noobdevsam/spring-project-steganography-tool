package com.example.springprojectsteganographytool.controllers;

import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.services.CapacityUtilService;
import com.example.springprojectsteganographytool.services.LsbUtilService;
import com.example.springprojectsteganographytool.services.SteganographyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stego")
@Slf4j
public class StegoController {

    private final SteganographyService steganographyService;
    private final LsbUtilService lsbUtilService;
    private final CapacityUtilService capacityUtilService;
    private final long streamThreshold;

    public StegoController(
            SteganographyService steganographyService,
            LsbUtilService lsbUtilService,
            CapacityUtilService capacityUtilService,
            @Value("${app.stream.threshold-bytes}") long streamThreshold
    ) {
        this.steganographyService = steganographyService;
        this.lsbUtilService = lsbUtilService;
        this.capacityUtilService = capacityUtilService;
        this.streamThreshold = streamThreshold;
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

    // ----- Capacity estimation endpoint (Phase 1) -----

    @GetMapping(path = "/estimate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> estimateCapacity(
            @RequestParam int width,
            @RequestParam int height,
            @RequestParam int lsbDepth,
            @RequestParam long plainLength,
            @RequestParam(name = "metadataJsonLength", required = false, defaultValue = "120") int metadataJsonLength
    ) {

        log.info("In Controller- Estimating capacity for image. ");

        if (width <= 0 || height <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "width and height must be > 0"));
        }
        if (lsbDepth != 1 && lsbDepth != 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "lsbDepth must be 1 or 2"));
        }
        if (plainLength < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "plainLength must be >= 0"));
        }
        if (metadataJsonLength <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "metadataJsonLength must be > 0"));
        }

        var result = capacityUtilService.estimate(width, height, lsbDepth, metadataJsonLength, plainLength);

        return ResponseEntity.ok(
                Map.of(
                        "capacityBytes", result.capacityBytes(),
                        "overheadBytes", result.overheadBytes(),
                        "encryptedBytesEstimate", result.encryptedBytes(),
                        "requiredBytesEstimate", result.requiredBytes(),
                        "fits", result.fits(),
                        "streamThresholdBytes", streamThreshold
                )
        );
    }

    // ----- Encode endpoints -----

    @PostMapping(path = "/encode/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> encodeText(
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("message") String message,
            @RequestParam("password") String password,
            @RequestParam(name = "lsbDepth", defaultValue = "1") int lsbDepth
    ) throws Exception {

        log.info("In Controller- Encoding text into image. ");

        var image = toBufferedImage(coverImage);
        var coverImageName = coverImage.getOriginalFilename();
        var result = steganographyService.encodeText(image, coverImageName, message, password, lsbDepth);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/encode/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> encodeFile(
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("fileToEmbed") MultipartFile fileToEmbed,
            @RequestParam("password") String password,
            @RequestParam(name = "lsbDepth", defaultValue = "1") int lsbDepth
    ) throws Exception {

        log.info("In Controller- Encoding file into image. ");

        var image = toBufferedImage(coverImage);
        var coverImageName = coverImage.getOriginalFilename();
        var nameOfFileToEmbed = fileToEmbed.getOriginalFilename();
        var sizeOfFileToEmbed = fileToEmbed.getSize();


        if (sizeOfFileToEmbed > streamThreshold) {

            log.info(">>>Using stream-based encoding.");

            try (var input = fileToEmbed.getInputStream()) {
                return ResponseEntity.ok(
                        steganographyService.encodeFileStream(
                                image, coverImageName, nameOfFileToEmbed, input, sizeOfFileToEmbed, password, lsbDepth
                        )
                );
            }

        } else {

            log.info(">>>Using byte-array-based encoding.");

            var fileBytes = fileToEmbed.getBytes();

            return ResponseEntity.ok(
                    steganographyService.encodeFile(image, coverImageName, nameOfFileToEmbed, fileBytes, password, lsbDepth)
            );
        }
    }

    // ----- Decode operations -----

    @PostMapping(path = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoDecodeResponseDTO> decode(
            @RequestParam("stegoImage") MultipartFile stegoImage,
            @RequestParam("password") String password
    ) throws Exception {

        log.info("In Controller- Decoding data from image. ");

        var image = toBufferedImage(stegoImage);
        var result = steganographyService.decodeProcess(image, password);
        return ResponseEntity.ok(result);
    }

    // ----- Metadata operations -----

    @PostMapping(path = "/metadata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> extractMetadata(
            @RequestParam("stegoImage") MultipartFile stegoImage
    ) throws IOException {

        log.info("In Controller- Extracting metadata from image. ");

        var meta = lsbUtilService.extractMetadata(toBufferedImage(stegoImage));
        if (meta == null) {
            throw new InvalidImageFormatException("No metadata found or invalid image provided.");
        }
        return ResponseEntity.ok(
                Map.of(
                        "lsbDepth", meta.lsbDepth(),
                        "hasText", meta.hasText(),
                        "hasFile", meta.hasFile()
                )
        );
    }

    // ----- Retrieval operations -----

    @GetMapping(path = "/encodings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StegoEncodeResponseDTO>> listAllEncodings() {
        log.info("In Controller- Listing all encodings in DB. ");
        return ResponseEntity.ok(steganographyService.listAllEncodings());
    }

    @GetMapping(path = "/encodings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StegoEncodeResponseDTO> getById(@PathVariable("id") UUID id) {
        log.info("In Controller- Getting encoding by ID from DB. ");
        return ResponseEntity.ok(steganographyService.getById(id));
    }

    @DeleteMapping(path = "/encodings/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        log.info("In Controller- Deleting encoding by ID from DB. ");
        steganographyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
