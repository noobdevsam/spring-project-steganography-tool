package com.example.springprojectsteganographytool.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceImplDeleteTest {

    @TempDir
    Path tempDir;

    @Test
    void deleteExistingFile() throws Exception {
        var service = new StorageServiceImpl(tempDir);
        var saved = service.save("stego-test-file.png", "hello".getBytes());
        assertTrue(Files.exists(saved));

        boolean deleted = service.delete("stego-test-file.png");
        assertTrue(deleted);
        assertFalse(Files.exists(saved));

        // deleting again returns false
        assertFalse(service.delete("stego-test-file.png"));
    }

    @Test
    void deleteNonExistentReturnsFalse() throws Exception {
        var service = new StorageServiceImpl(tempDir);
        assertFalse(service.delete("nope.png"));
    }

    @Test
    void resolveAfterDeleteThrows() throws Exception {
        var service = new StorageServiceImpl(tempDir);
        service.save("file.png", new byte[]{1, 2, 3});
        assertTrue(service.delete("file.png"));
        assertThrows(Exception.class, () -> service.resolve("file.png"));
    }
}