package com.example.springprojectsteganographytool.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Document
public class StegoData {

    @Id
    private UUID id;

    private String fileName;
    private String fileType; // e.g., "image/png", "image/jpeg" etc.


    private byte[] originalImage;
    private byte[] stegoImage;

    private String embeddedText;
    private byte[] embeddedFile;

    private boolean encrypted;
    private String encryptionKey;

    private LocalDateTime creationDate;

    private String possHint;

    public StegoData() {
    }

    public StegoData(
            String fileName,
            String fileType,
            byte[] originalImage,
            byte[] embeddedFile,
            byte[] stegoImage,
            String embeddedText,
            boolean encrypted,
            String encryptionKey,
            LocalDateTime creationDate,
            String possHint
    ) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.fileType = fileType;
        this.originalImage = originalImage;
        this.embeddedFile = embeddedFile;
        this.stegoImage = stegoImage;
        this.embeddedText = embeddedText;
        this.encrypted = encrypted;
        this.encryptionKey = encryptionKey;
        this.creationDate = creationDate;
        this.possHint = possHint;
    }

    public StegoData(
            String id,
            String fileName,
            String fileType,
            byte[] originalImage,
            String embeddedText,
            byte[] stegoImage,
            boolean encrypted,
            byte[] embeddedFile,
            String encryptionKey,
            LocalDateTime creationDate,
            String possHint
    ) {
        this.id = UUID.fromString(id);
        this.fileName = fileName;
        this.fileType = fileType;
        this.originalImage = originalImage;
        this.embeddedText = embeddedText;
        this.stegoImage = stegoImage;
        this.encrypted = encrypted;
        this.embeddedFile = embeddedFile;
        this.encryptionKey = encryptionKey;
        this.creationDate = creationDate;
        this.possHint = possHint;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public byte[] getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(byte[] originalImage) {
        this.originalImage = originalImage;
    }

    public byte[] getStegoImage() {
        return stegoImage;
    }

    public void setStegoImage(byte[] stegoImage) {
        this.stegoImage = stegoImage;
    }

    public String getEmbeddedText() {
        return embeddedText;
    }

    public void setEmbeddedText(String embeddedText) {
        this.embeddedText = embeddedText;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public byte[] getEmbeddedFile() {
        return embeddedFile;
    }

    public void setEmbeddedFile(byte[] embeddedFile) {
        this.embeddedFile = embeddedFile;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getPossHint() {
        return possHint;
    }

    public void setPossHint(String possHint) {
        this.possHint = possHint;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StegoData stegoData)) return false;

        return isEncrypted() == stegoData.isEncrypted() && Objects.equals(getId(), stegoData.getId()) && Objects.equals(getFileName(), stegoData.getFileName()) && Objects.equals(getFileType(), stegoData.getFileType()) && Arrays.equals(getOriginalImage(), stegoData.getOriginalImage()) && Arrays.equals(getStegoImage(), stegoData.getStegoImage()) && Objects.equals(getEmbeddedText(), stegoData.getEmbeddedText()) && Arrays.equals(getEmbeddedFile(), stegoData.getEmbeddedFile()) && Objects.equals(getEncryptionKey(), stegoData.getEncryptionKey()) && Objects.equals(getCreationDate(), stegoData.getCreationDate()) && Objects.equals(getPossHint(), stegoData.getPossHint());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getFileName());
        result = 31 * result + Objects.hashCode(getFileType());
        result = 31 * result + Arrays.hashCode(getOriginalImage());
        result = 31 * result + Arrays.hashCode(getStegoImage());
        result = 31 * result + Objects.hashCode(getEmbeddedText());
        result = 31 * result + Arrays.hashCode(getEmbeddedFile());
        result = 31 * result + Boolean.hashCode(isEncrypted());
        result = 31 * result + Objects.hashCode(getEncryptionKey());
        result = 31 * result + Objects.hashCode(getCreationDate());
        result = 31 * result + Objects.hashCode(getPossHint());
        return result;
    }

}
