package com.example.springprojectsteganographytool.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document
@Data
@NoArgsConstructor
public class StegoData {

    @Id
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
