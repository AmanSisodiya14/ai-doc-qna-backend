package com.tech.aidocqna.service;

import com.tech.aidocqna.config.AppProperties;
import com.tech.aidocqna.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "mp3", "wav", "mp4");

    private final AppProperties appProperties;

    public FileStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Path.of(appProperties.getStoragePath()));
    }

    public Path store(MultipartFile multipartFile) {
        validateFile(multipartFile);
        try {
            String original = multipartFile.getOriginalFilename() == null ? "file" : multipartFile.getOriginalFilename();
            String extension = extensionOf(original);
            String safeName = UUID.randomUUID() + "." + extension;
            Path destination = Path.of(appProperties.getStoragePath()).resolve(safeName).normalize();
            Files.copy(multipartFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store uploaded file");
        }
    }

    public String extensionOf(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx <= 0 || idx == fileName.length() - 1) {
            throw new BadRequestException("File extension is required");
        }
        return fileName.substring(idx + 1).toLowerCase();
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        if (file.getSize() > appProperties.getMaxFileSizeBytes()) {
            throw new BadRequestException("File exceeds maximum size limit");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(name);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type");
        }
    }
}
