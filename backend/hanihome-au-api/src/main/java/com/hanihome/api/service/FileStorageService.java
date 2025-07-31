package com.hanihome.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file-storage.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file-storage.base-url:http://localhost:8080/files}")
    private String baseUrl;

    public String storeFile(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, category);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = generateUniqueFilename() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Store file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        String fileUrl = baseUrl + "/" + category + "/" + uniqueFilename;
        log.info("File stored successfully: {}", fileUrl);
        
        return fileUrl;
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            log.warn("Invalid file URL for deletion: {}", fileUrl);
            return;
        }

        // Extract relative path from URL
        String relativePath = fileUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadDir, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted successfully: {}", filePath);
        } else {
            log.warn("File not found for deletion: {}", filePath);
        }
    }

    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return false;
        }

        String relativePath = fileUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.exists(filePath);
    }

    public long getFileSize(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            throw new IllegalArgumentException("Invalid file URL");
        }

        String relativePath = fileUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadDir, relativePath);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        return Files.size(filePath);
    }

    private String generateUniqueFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid;
    }
}