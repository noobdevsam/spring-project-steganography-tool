package com.example.springprojectsteganographytool.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class StegoData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalFileName;
    private String embeddedFileName;
    private String message;

    private byte[] stegoImageBytes;
    private byte[] embeddedFileBytes;

    private String encryptionKeyHash;
    private Instant createdDate;

    private boolean hasText;
    private boolean hasFile;

    @Builder
    public StegoData(
            String originalFileName,
            String embeddedFileName,
            String message,
            byte[] stegoImageBytes,
            byte[] embeddedFileBytes,
            String encryptionKeyHash,
            boolean hasText,
            boolean hasFile
    ) {
        this.id = UUID.randomUUID();
        this.originalFileName = originalFileName;
        this.embeddedFileName = embeddedFileName;
        this.message = message;
        this.stegoImageBytes = stegoImageBytes;
        this.embeddedFileBytes = embeddedFileBytes;
        this.encryptionKeyHash = encryptionKeyHash;
        this.createdDate = Instant.now();
        this.hasText = hasText;
        this.hasFile = hasFile;
    }

}
