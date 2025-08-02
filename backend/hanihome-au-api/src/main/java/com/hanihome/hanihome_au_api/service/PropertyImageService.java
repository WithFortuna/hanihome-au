package com.hanihome.hanihome_au_api.service;

import com.hanihome.hanihome_au_api.service.FileStorageService;
import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.entity.PropertyImage;
import com.hanihome.hanihome_au_api.repository.PropertyImageRepository;
import com.hanihome.hanihome_au_api.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final PropertyRepository propertyRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.property.image.max-count:10}")
    private int maxImageCount;

    @Value("${app.property.image.max-size:5242880}") // 5MB
    private long maxImageSize;

    public PropertyImage uploadImage(Long propertyId, MultipartFile file, String description, boolean isMain) throws IOException {
        validateFile(file);
        validatePropertyExists(propertyId);
        validateImageCount(propertyId);

        if (isMain) {
            unsetCurrentMainImage(propertyId);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = generateFileName(propertyId, fileExtension);
        
        String imageUrl = fileStorageService.uploadFile(file, "properties/" + propertyId + "/images/" + fileName);
        String thumbnailUrl = fileStorageService.generateThumbnail(file, "properties/" + propertyId + "/thumbnails/" + fileName);

        int nextOrder = getNextImageOrder(propertyId);

        PropertyImage propertyImage = PropertyImage.builder()
                .propertyId(propertyId)
                .imageUrl(imageUrl)
                .thumbnailUrl(thumbnailUrl)
                .imageOrder(nextOrder)
                .description(description)
                .isMain(isMain)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .originalFileName(originalFileName)
                .build();

        PropertyImage savedImage = propertyImageRepository.save(propertyImage);
        log.info("Uploaded image for property {}: {}", propertyId, imageUrl);
        
        return savedImage;
    }

    public List<PropertyImage> uploadMultipleImages(Long propertyId, List<MultipartFile> files) throws IOException {
        validatePropertyExists(propertyId);
        
        if (files.size() > maxImageCount) {
            throw new IllegalArgumentException("Cannot upload more than " + maxImageCount + " images");
        }

        List<PropertyImage> uploadedImages = new java.util.ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            boolean isMain = i == 0 && !hasMainImage(propertyId);
            PropertyImage image = uploadImage(propertyId, file, null, isMain);
            uploadedImages.add(image);
        }
        
        return uploadedImages;
    }

    @Transactional(readOnly = true)
    public List<PropertyImage> getPropertyImages(Long propertyId) {
        return propertyImageRepository.findByPropertyIdOrderByImageOrder(propertyId);
    }

    @Transactional(readOnly = true)
    public PropertyImage getMainImage(Long propertyId) {
        return propertyImageRepository.findByPropertyIdAndIsMainTrue(propertyId);
    }

    public void deleteImage(Long imageId) throws IOException {
        PropertyImage image = propertyImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        fileStorageService.deleteFile(image.getImageUrl());
        if (image.getThumbnailUrl() != null) {
            fileStorageService.deleteFile(image.getThumbnailUrl());
        }

        propertyImageRepository.delete(image);
        log.info("Deleted image: {}", image.getImageUrl());

        // If deleted image was main, set first remaining image as main
        if (image.getIsMain()) {
            PropertyImage firstImage = propertyImageRepository
                .findFirstByPropertyIdOrderByImageOrder(image.getPropertyId());
            if (firstImage != null) {
                firstImage.setAsMain();
                propertyImageRepository.save(firstImage);
            }
        }
    }

    public void deleteAllPropertyImages(Long propertyId) throws IOException {
        List<PropertyImage> images = propertyImageRepository.findByPropertyId(propertyId);
        
        for (PropertyImage image : images) {
            fileStorageService.deleteFile(image.getImageUrl());
            if (image.getThumbnailUrl() != null) {
                fileStorageService.deleteFile(image.getThumbnailUrl());
            }
        }
        
        propertyImageRepository.deleteByPropertyId(propertyId);
        log.info("Deleted all images for property: {}", propertyId);
    }

    public PropertyImage setMainImage(Long imageId) {
        PropertyImage image = propertyImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        unsetCurrentMainImage(image.getPropertyId());
        image.setAsMain();
        
        return propertyImageRepository.save(image);
    }

    public void reorderImages(Long propertyId, List<Long> imageIds) {
        List<PropertyImage> images = propertyImageRepository.findByPropertyIdAndIdIn(propertyId, imageIds);
        
        if (images.size() != imageIds.size()) {
            throw new IllegalArgumentException("Some images not found for property: " + propertyId);
        }

        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            PropertyImage image = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
            
            image.setImageOrder(i + 1);
        }

        propertyImageRepository.saveAll(images);
        log.info("Reordered images for property: {}", propertyId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxImageSize) {
            throw new IllegalArgumentException("File size exceeds limit: " + maxImageSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        List<String> allowedTypes = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");
        if (!allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }
    }

    private void validatePropertyExists(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new IllegalArgumentException("Property not found: " + propertyId);
        }
    }

    private void validateImageCount(Long propertyId) {
        long currentCount = propertyImageRepository.countByPropertyId(propertyId);
        if (currentCount >= maxImageCount) {
            throw new IllegalArgumentException("Cannot upload more than " + maxImageCount + " images per property");
        }
    }

    private boolean hasMainImage(Long propertyId) {
        return propertyImageRepository.existsByPropertyIdAndIsMainTrue(propertyId);
    }

    private void unsetCurrentMainImage(Long propertyId) {
        PropertyImage currentMain = propertyImageRepository.findByPropertyIdAndIsMainTrue(propertyId);
        if (currentMain != null) {
            currentMain.unsetAsMain();
            propertyImageRepository.save(currentMain);
        }
    }

    private int getNextImageOrder(Long propertyId) {
        Integer maxOrder = propertyImageRepository.findMaxImageOrderByPropertyId(propertyId);
        return (maxOrder == null) ? 1 : maxOrder + 1;
    }

    private String generateFileName(Long propertyId, String extension) {
        return "property_" + propertyId + "_" + UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}