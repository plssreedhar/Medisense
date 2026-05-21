package com.epam.medisense.storage;

import java.io.IOException;

public interface StorageService {

    String store(String filename, byte[] content) throws IOException;

    byte[] retrieve(String path) throws IOException;

    void delete(String path) throws IOException;
}
