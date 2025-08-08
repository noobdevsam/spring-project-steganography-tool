package com.example.springprojectsteganographytool.repos;

import com.example.springprojectsteganographytool.documents.StegoData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface StegoDataRepository extends MongoRepository<StegoData, UUID> {

    StegoData findByOriginalFileName(String originalFileName);

    StegoData findByEmbeddedFileName(String embeddedFileName);

}
