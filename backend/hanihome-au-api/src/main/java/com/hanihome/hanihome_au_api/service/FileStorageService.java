package com.hanihome.hanihome_au_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * File Storage Service for handling file uploads and operations
 * Supports local file storage and image thumbnail generation
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.image.thumbnail.width:200}")
    private int thumbnailWidth;

    @Value("${app.image.thumbnail.height:150}")
    private int thumbnailHeight;

    /**
     * Upload a file to the specified path
     */
    public String uploadFile(MultipartFile file, String relativePath) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).resolve(relativePath);
        Files.createDirectories(uploadPath.getParent());
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;
        
        Path filePath = uploadPath.getParent().resolve(filename);
        
        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative URL
        String relativeUrl = relativePath.substring(0, relativePath.lastIndexOf('/') + 1) + filename;
        String fullUrl = baseUrl + "/files/" + relativeUrl;
        
        log.info("File uploaded successfully: {}", fullUrl);
        return fullUrl;
    }

    /**
     * Generate thumbnail for an image file
     */
    public String generateThumbnail(MultipartFile file, String relativePath) throws IOException {
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("File is not an image");
        }
        
        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Cannot read image file");
        }
        
        // Create thumbnail
        BufferedImage thumbnail = createThumbnail(originalImage, thumbnailWidth, thumbnailHeight);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).resolve(relativePath);
        Files.createDirectories(uploadPath.getParent());
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "_thumb." + extension;
        
        Path thumbnailPath = uploadPath.getParent().resolve(filename);
        
        // Save thumbnail
        ImageIO.write(thumbnail, extension, thumbnailPath.toFile());
        
        // Return relative URL
        String relativeUrl = relativePath.substring(0, relativePath.lastIndexOf('/') + 1) + filename;
        String fullUrl = baseUrl + "/files/" + relativeUrl;
        
        log.info("Thumbnail generated successfully: {}", fullUrl);
        return fullUrl;
    }

    /**
     * Delete a file by URL
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            log.warn("Invalid file URL for deletion: {}", fileUrl);
            return;
        }
        
        // Extract relative path from URL
        String relativePath = fileUrl.substring(baseUrl.length() + "/files/".length());
        Path filePath = Paths.get(uploadDir).resolve(relativePath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted successfully: {}", fileUrl);
        } else {
            log.warn("File not found for deletion: {}", fileUrl);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Check if file is an image
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    /**
     * Create thumbnail image with specified dimensions
     */
    private BufferedImage createThumbnail(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate scaling to maintain aspect ratio
        double scaleX = (double) targetWidth / originalWidth;
        double scaleY = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        // Create thumbnail with high quality
        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fill background with white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        
        // Center the scaled image
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;
        
        // Draw scaled image
        g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return thumbnail;
    }
}