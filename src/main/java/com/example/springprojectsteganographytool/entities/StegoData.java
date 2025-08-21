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

    private String stegoFileName;
    private Long stegoFileSize;


    private String encryptionKeyHash;
    private Instant createdDate;

    private boolean hasText;
    private boolean hasFile;

    @Builder
    public StegoData(
            String originalFileName,
            String embeddedFileName,
            String stegoFileName,
            Long stegoFileSize,
            String encryptionKeyHash,
            boolean hasText,
            boolean hasFile
    ) {
        this.originalFileName = originalFileName;
        this.embeddedFileName = embeddedFileName;
        this.stegoFileName = stegoFileName;
        this.stegoFileSize = stegoFileSize;
        this.encryptionKeyHash = encryptionKeyHash;
        this.createdDate = Instant.now();
        this.hasText = hasText;
        this.hasFile = hasFile;
    }

}
