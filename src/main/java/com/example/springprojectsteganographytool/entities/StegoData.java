package com.example.springprojectsteganographytool.entities;

import jakarta.persistence.*;
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

    @Version
    private Long version;

    private String originalFileName;
    private String embeddedFileName;
    private String message;

    @Lob
    private byte[] stegoImageBytes;

    @Lob
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
