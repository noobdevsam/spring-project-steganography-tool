package com.example.springprojectsteganographytool.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity class representing steganographic data stored in the database.
 * This class is used to persist information about stego files, their metadata,
 * and associated properties.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class StegoData {

    /**
     * Unique identifier for the stego data entry.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Version field for optimistic locking.
     * Automatically managed by JPA.
     */
    @Version
    private Long version;

    /**
     * Name of the cover image used for steganographic encoding.
     */
    private String coverImageName;

    /**
     * Name of the file embedded within the stego image, if any.
     */
    private String fileNameOfEmbeddedData;

    /**
     * Name of the generated stego file.
     */
    private String stegoFileName;

    /**
     * Size of the stego file in bytes.
     */
    private Long stegoFileSize;

    /**
     * Indicates whether the stego file contains embedded text.
     */
    private boolean hasText;

    /**
     * Indicates whether the stego file contains an embedded file.
     */
    private boolean hasFile;

    /**
     * Builder constructor for creating a StegoData instance.
     *
     * @param coverImageName         Name of the cover image.
     * @param fileNameOfEmbeddedData Name of the embedded file.
     * @param stegoFileName          Name of the stego file.
     * @param stegoFileSize          Size of the stego file in bytes.
     * @param hasText                Whether the stego file contains text.
     * @param hasFile                Whether the stego file contains a file.
     */
    @Builder
    public StegoData(
            String coverImageName,
            String fileNameOfEmbeddedData,
            String stegoFileName,
            Long stegoFileSize,
            boolean hasText,
            boolean hasFile
    ) {
        this.coverImageName = coverImageName;
        this.fileNameOfEmbeddedData = fileNameOfEmbeddedData;
        this.stegoFileName = stegoFileName;
        this.stegoFileSize = stegoFileSize;
        this.hasText = hasText;
        this.hasFile = hasFile;
    }

}