package com.example.springprojectsteganographytool.repos;

import com.example.springprojectsteganographytool.entities.StegoData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StegoDataRepository extends JpaRepository<StegoData, UUID> {

    Optional<StegoData> findByOriginalFileName(String originalFileName);

    Optional<StegoData> findByEmbeddedFileName(String embeddedFileName);

}
