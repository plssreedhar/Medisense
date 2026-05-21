package com.epam.medisense.storage;

import org.springframework.stereotype.Service;

import java.io.IOException;

// Reserved for Phase 2 - swap @Primary here and remove from LocalFileStorageService
@Service
public class S3StorageService implements StorageService {

    @Override
    public String store(String filename, byte[] content) throws IOException {
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public byte[] retrieve(String path) throws IOException {
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public void delete(String path) throws IOException {
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }
}
