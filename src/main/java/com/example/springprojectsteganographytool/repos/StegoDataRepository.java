package com.example.springprojectsteganographytool.repos;

import com.example.springprojectsteganographytool.documents.StegoData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface StegoDataRepository extends MongoRepository<StegoData, UUID> {

    Optional<StegoData> findByOriginalFileName(String originalFileName);

    Optional<StegoData> findByEmbeddedFileName(String embeddedFileName);

}
