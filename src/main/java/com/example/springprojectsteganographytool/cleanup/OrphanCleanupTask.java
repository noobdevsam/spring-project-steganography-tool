package com.example.springprojectsteganographytool.cleanup;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrphanCleanupTask {

    private final boolean enabled;
    private final Path basePath;
    private final StegoDataRepository stegoDataRepository;

    public OrphanCleanupTask(
            @Value("${app.cleanup.enabled.false}") boolean enabled,
            @Value("${app.storage.base-path}") Path basePath,
            StegoDataRepository stegoDataRepository
    ) {
        this.enabled = enabled;
        this.basePath = basePath.toAbsolutePath().normalize();
        this.stegoDataRepository = stegoDataRepository;
    }

    @Scheduled(
            initialDelayString = "${app.cleanup.initial-delay-ms: 60000}",
            fixedDelayString = "${app.cleanup.interval-ms: 300000}"
    )
    public void runCleanup() {
        if (!enabled) {
            return;
        }

        try {
            if (!Files.exists(basePath)) {
                log.debug("Cleanup skipped: base path does not exist yet: {}", basePath);
                return;
            }

            var referenced = stegoDataRepository.findAll()
                    .stream()
                    .map(StegoData::getStegoFileName)
                    .collect(Collectors.toSet());

            try (var paths = Files.list(basePath)) {
                var candidates = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().matches("^stego-.*\\.png$"))
                        .toList();

                int deletedCount = 0;
                for (var file : candidates) {
                    var name = file.getFileName().toString();

                    if (!referenced.contains(name)) {
                        try {
                            Files.deleteIfExists(file);
                            deletedCount++;
                            log.info("Deleted orphaned stego file: {}", name);
                        } catch (Exception e) {
                            log.warn("Failed to delete orphaned stego file: {}: {}", name, e.getMessage());
                        }
                    }
                }

                if (deletedCount > 0) {
                    log.info("Orphan cleanup completed, deleted {} files", deletedCount);
                }
            }
        } catch (Exception e) {
            log.warn("Orphan cleanup encountered an error: {}", e.getMessage());
        }
    }

}
