package com.example.springprojectsteganographytool.cleanup;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A scheduled task for cleaning up orphaned or expired files in the storage directory.
 * This task deletes:
 * 1. Orphaned stego files that are no longer referenced in the database.
 * 2. Extracted files that have exceeded their time-to-live (TTL).
 */
@Component
@Slf4j
public class OrphanCleanupTask {

    private static final Pattern STEGO_PATTERN = Pattern.compile("^stego-.*\\.png$"); // Pattern for identifying stego files.
    private static final Pattern EXTRACTED_PATTERN = Pattern.compile("^extracted-(\\d+)-[0-9a-fA-F\\-]+-.*$"); // Pattern for identifying extracted files.
    private final boolean enabled; // Indicates whether the cleanup task is enabled.
    private final Path basePath; // The base directory where files are stored.
    private final StegoDataRepository stegoDataRepository; // Repository for accessing stego data.
    private final long extractedTtlMs; // Time-to-live (TTL) for extracted files in milliseconds.

    /**
     * Constructor for OrphanCleanupTask.
     *
     * @param enabled             Whether the cleanup task is enabled.
     * @param basePath            The base directory for file storage.
     * @param stegoDataRepository Repository for accessing stego data.
     * @param extractedTtlMs      Time-to-live for extracted files in milliseconds.
     */
    public OrphanCleanupTask(
            @Value("${app.cleanup.enabled:false}") boolean enabled,
            @Value("${app.storage.base-path}") Path basePath,
            StegoDataRepository stegoDataRepository,
            @Value("${app.extraction.temp-ttl-ms}") long extractedTtlMs
    ) {
        this.enabled = enabled;
        this.basePath = basePath.toAbsolutePath().normalize();
        this.stegoDataRepository = stegoDataRepository;
        this.extractedTtlMs = extractedTtlMs;
    }

    /**
     * Scheduled method to perform the cleanup task.
     * Runs at a fixed interval as defined in the application properties.
     */
    @Scheduled(
            initialDelayString = "${app.cleanup.initial-delay-ms}",
            fixedDelayString = "${app.cleanup.interval-ms}"
    )
    public void runCleanup() {
        // Exit early if the cleanup task is disabled
        if (!enabled) {
            return;
        }

        try {
            // Check if the base storage path exists
            if (!Files.exists(basePath)) {
                log.debug("Cleanup skipped: base path does not exist yet: {}", basePath);
                return;
            }

            // Retrieve all referenced stego file names from the database
            var referencedStegoNames = stegoDataRepository.findAll()
                    .stream()
                    .map(StegoData::getStegoFileName)
                    .collect(Collectors.toSet());
            var now = System.currentTimeMillis();
            int deletedCount = 0;

            // List all files in the base storage path
            try (var paths = Files.list(basePath)) {

                for (var file : paths.filter(Files::isRegularFile).toList()) {
                    var name = file.getFileName().toString();

                    // 1. Orphaned stego image deletion
                    if (STEGO_PATTERN.matcher(name).matches()) {
                        if (!referencedStegoNames.contains(name)) {
                            try {
                                Files.deleteIfExists(file); // Attempt to delete the file
                                deletedCount++;
                                log.info("Deleted orphaned stego file: {}", name);
                            } catch (Exception e) {
                                log.warn("Failed to delete orphaned stego file: {}: {}", name, e.getMessage());
                            }
                        }
                        continue;
                    }

                    // 2. Expired extracted image deletion
                    var extractedMatcher = EXTRACTED_PATTERN.matcher(name);
                    if (extractedMatcher.matches()) {
                        try {
                            var created = Long.parseLong(extractedMatcher.group(1));

                            if (now - created > extractedTtlMs) {
                                Files.deleteIfExists(file); // Attempt to delete the file
                                deletedCount++;
                                log.info("Deleted expired extracted file: {}", name);
                            }
                        } catch (Exception e) {
                            log.warn("Failed evaluating extracted file {} for cleanup: {}", name, e.getMessage());
                        }
                    }
                }
            }

            // Log the total number of deleted files, if any
            if (deletedCount > 0) {
                log.info("Orphan cleanup completed, deleted {} files (orphan/expired)", deletedCount);
            }

        } catch (Exception e) {
            // Log any errors encountered during the cleanup process
            log.warn("Orphan cleanup encountered an error: {}", e.getMessage());
        }
    }

}