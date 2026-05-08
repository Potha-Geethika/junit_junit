package com.carbo.admin.storage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init();

    void store(String folder, MultipartFile file) throws IOException;

    Stream<Path> loadAll();

    Path load(String fileName);

    Resource loadAsResource(String folder, String fileName) throws FileNotFoundException;

    FileSystemResource loadAsFileSystemResource(String folder, String fileName) throws FileNotFoundException;

    void deleteAll();
}
