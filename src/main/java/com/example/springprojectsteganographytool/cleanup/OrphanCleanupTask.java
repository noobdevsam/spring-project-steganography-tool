package com.example.springprojectsteganographytool.cleanup;

import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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

}
