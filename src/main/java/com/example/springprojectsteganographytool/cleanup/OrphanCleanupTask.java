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

    /**
     * This method is scheduled to run periodically to clean up orphaned stego files
     * from the storage directory. Orphaned files are those that exist in the storage
     * directory but are not referenced in the database.
     * <p>
     * The method performs the following steps:
     * 1. Checks if the cleanup task is enabled. If not, it exits early.
     * 2. Verifies if the base storage path exists. If not, logs a debug message and exits.
     * 3. Retrieves all referenced stego file names from the database.
     * 4. Lists all files in the base storage path that match the naming pattern for stego files.
     * 5. Deletes files that are not referenced in the database and logs the deletion.
     * 6. Logs the total number of deleted files, if any.
     * <p>
     * If any errors occur during the process, they are logged as warnings.
     */
    @Scheduled(
            initialDelayString = "${app.cleanup.initial-delay-ms: 60000}",
            fixedDelayString = "${app.cleanup.interval-ms: 300000}"
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
            var referenced = stegoDataRepository.findAll()
                    .stream()
                    .map(StegoData::getStegoFileName)
                    .collect(Collectors.toSet());

            // List all files in the base storage path
            try (var paths = Files.list(basePath)) {
                var candidates = paths
                        .filter(Files::isRegularFile) // Filter regular files
                        .filter(path -> path.getFileName().toString().matches("^stego-.*\\.png$")) // Match stego file pattern
                        .toList();

                int deletedCount = 0;

                // Iterate over candidate files and delete unreferenced ones
                for (var file : candidates) {
                    var name = file.getFileName().toString();

                    if (!referenced.contains(name)) {
                        try {
                            Files.deleteIfExists(file); // Attempt to delete the file
                            deletedCount++;
                            log.info("Deleted orphaned stego file: {}", name);
                        } catch (Exception e) {
                            log.warn("Failed to delete orphaned stego file: {}: {}", name, e.getMessage());
                        }
                    }
                }

                // Log the total number of deleted files, if any
                if (deletedCount > 0) {
                    log.info("Orphan cleanup completed, deleted {} files", deletedCount);
                }
            }
        } catch (Exception e) {
            // Log any errors encountered during the cleanup process
            log.warn("Orphan cleanup encountered an error: {}", e.getMessage());
        }
    }

}
