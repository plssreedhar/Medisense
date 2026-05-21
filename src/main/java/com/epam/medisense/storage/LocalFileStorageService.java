package com.epam.medisense.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Primary
public class LocalFileStorageService implements StorageService {

    private final Path baseDir;

    public LocalFileStorageService(@Value("${storage.local.base-path:./uploads}") String basePath) throws IOException {
        this.baseDir = Paths.get(basePath);
        Files.createDirectories(this.baseDir);
    }

    @Override
    public String store(String filename, byte[] content) throws IOException {
        Path target = baseDir.resolve(filename);
        Files.write(target, content);
        return target.toAbsolutePath().toString();
    }

    @Override
    public byte[] retrieve(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    @Override
    public void delete(String path) throws IOException {
        Path target = Paths.get(path);
        if (Files.exists(target)) {
            Files.delete(target);
        }
    }
}
