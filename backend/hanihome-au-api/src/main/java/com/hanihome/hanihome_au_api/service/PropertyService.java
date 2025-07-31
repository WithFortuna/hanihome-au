package com.hanihome.hanihome_au_api.service;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.entity.PropertyImage;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.dto.request.PropertyCreateRequest;
import com.hanihome.hanihome_au_api.dto.request.PropertySearchCriteria;
import com.hanihome.hanihome_au_api.dto.request.PropertyUpdateRequest;
import com.hanihome.hanihome_au_api.dto.response.PropertyDetailResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyListResponse;
import com.hanihome.hanihome_au_api.exception.PropertyException;
import com.hanihome.hanihome_au_api.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageService propertyImageService;

    public PropertyDetailResponse createProperty(PropertyCreateRequest request, Long landlordId) {
        validatePropertyCreateRequest(request);

        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .zipCode(request.getZipCode())
                .city(request.getCity())
                .district(request.getDistrict())
                .propertyType(request.getPropertyType())
                .rentalType(request.getRentalType())
                .deposit(request.getDeposit())
                .monthlyRent(request.getMonthlyRent())
                .maintenanceFee(request.getMaintenanceFee())
                .area(request.getArea())
                .rooms(request.getRooms())
                .bathrooms(request.getBathrooms())
                .floor(request.getFloor())
                .totalFloors(request.getTotalFloors())
                .availableDate(request.getAvailableDate())
                .landlordId(landlordId)
                .agentId(request.getAgentId())
                .options(request.getOptions())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .parkingAvailable(request.getParkingAvailable())
                .petAllowed(request.getPetAllowed())
                .furnished(request.getFurnished())
                .shortTermAvailable(request.getShortTermAvailable())
                .status(PropertyStatus.PENDING_APPROVAL)
                .build();

        Property savedProperty = propertyRepository.save(property);
        log.info("Created property: {} by landlord: {}", savedProperty.getId(), landlordId);

        return convertToDetailResponse(savedProperty);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "propertyDetails", key = "#propertyId")
    public PropertyDetailResponse getProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        return convertToDetailResponse(property);
    }

    @Transactional(readOnly = true)
    public PropertyListResponse searchProperties(PropertySearchCriteria criteria, Pageable pageable) {
        Page<Property> properties = propertyRepository.searchPropertiesWithCriteria(criteria, pageable);
        
        return PropertyListResponse.builder()
                .properties(properties.getContent().stream()
                        .map(this::convertToListItem)
                        .toList())
                .totalElements(properties.getTotalElements())
                .totalPages(properties.getTotalPages())
                .currentPage(properties.getNumber())
                .pageSize(properties.getSize())
                .hasNext(properties.hasNext())
                .hasPrevious(properties.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public PropertyListResponse getPropertiesByLandlord(Long landlordId, Pageable pageable) {
        Page<Property> properties = propertyRepository.findByLandlordId(landlordId, pageable);
        
        return PropertyListResponse.builder()
                .properties(properties.getContent().stream()
                        .map(this::convertToListItem)
                        .toList())
                .totalElements(properties.getTotalElements())
                .totalPages(properties.getTotalPages())
                .currentPage(properties.getNumber())
                .pageSize(properties.getSize())
                .hasNext(properties.hasNext())
                .hasPrevious(properties.hasPrevious())
                .build();
    }

    @CacheEvict(value = "propertyDetails", key = "#propertyId")
    public PropertyDetailResponse updateProperty(Long propertyId, PropertyUpdateRequest request, Long landlordId) {
        Property property = propertyRepository.findByIdAndLandlordId(propertyId, landlordId)
                .orElseThrow(() -> new PropertyException.PropertyAccessDeniedException(propertyId));

        updatePropertyFields(property, request);
        Property updatedProperty = propertyRepository.save(property);
        
        log.info("Updated property: {} by landlord: {}", propertyId, landlordId);
        return convertToDetailResponse(updatedProperty);
    }

    public void deleteProperty(Long propertyId, Long landlordId) {
        Property property = propertyRepository.findByIdAndLandlordId(propertyId, landlordId)
                .orElseThrow(() -> new PropertyException.PropertyAccessDeniedException(propertyId));

        try {
            propertyImageService.deleteAllPropertyImages(propertyId);
        } catch (Exception e) {
            log.warn("Failed to delete images for property: {}", propertyId, e);
        }

        propertyRepository.delete(property);
        log.info("Deleted property: {} by landlord: {}", propertyId, landlordId);
    }

    public PropertyDetailResponse approveProperty(Long propertyId, Long approvedBy) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        if (!property.isPendingApproval()) {
            throw new PropertyException.PropertyStatusException("Property is not pending approval: " + propertyId);
        }

        property.approve(approvedBy);
        Property approvedProperty = propertyRepository.save(property);
        
        log.info("Approved property: {} by agent: {}", propertyId, approvedBy);
        return convertToDetailResponse(approvedProperty);
    }

    public PropertyDetailResponse rejectProperty(Long propertyId, String reason) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        if (!property.isPendingApproval()) {
            throw new PropertyException.PropertyStatusException("Property is not pending approval: " + propertyId);
        }

        property.reject();
        if (reason != null) {
            property.setAdminNotes(reason);
        }
        Property rejectedProperty = propertyRepository.save(property);
        
        log.info("Rejected property: {} with reason: {}", propertyId, reason);
        return convertToDetailResponse(rejectedProperty);
    }

    public PropertyDetailResponse changePropertyStatus(Long propertyId, PropertyStatus newStatus, Long userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        switch (newStatus) {
            case ACTIVE -> property.activate();
            case INACTIVE -> property.deactivate();
            case COMPLETED -> property.complete();
            case SUSPENDED -> property.suspend();
            default -> throw new IllegalArgumentException("Invalid status change: " + newStatus);
        }

        Property updatedProperty = propertyRepository.save(property);
        log.info("Changed property {} status to {} by user: {}", propertyId, newStatus, userId);
        
        return convertToDetailResponse(updatedProperty);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "similarProperties", key = "#propertyId + '_' + #limit")
    public List<Property> findSimilarProperties(Long propertyId, int limit) {
        return propertyRepository.findSimilarProperties(propertyId, limit);
    }

    @Transactional(readOnly = true)
    public List<Property> findNearbyProperties(Double latitude, Double longitude, Double radiusKm, int limit) {
        return propertyRepository.findPropertiesNearby(latitude, longitude, radiusKm, limit);
    }

    private void validatePropertyCreateRequest(PropertyCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new PropertyException.PropertyValidationException("Property title is required");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new PropertyException.PropertyValidationException("Property address is required");
        }
        if (request.getPropertyType() == null) {
            throw new PropertyException.PropertyValidationException("Property type is required");
        }
        if (request.getRentalType() == null) {
            throw new PropertyException.PropertyValidationException("Rental type is required");
        }
    }

    private void updatePropertyFields(Property property, PropertyUpdateRequest request) {
        if (request.getTitle() != null) {
            property.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            property.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            property.setAddress(request.getAddress());
        }
        if (request.getDetailAddress() != null) {
            property.setDetailAddress(request.getDetailAddress());
        }
        if (request.getDeposit() != null) {
            property.setDeposit(request.getDeposit());
        }
        if (request.getMonthlyRent() != null) {
            property.setMonthlyRent(request.getMonthlyRent());
        }
        if (request.getMaintenanceFee() != null) {
            property.setMaintenanceFee(request.getMaintenanceFee());
        }
        if (request.getArea() != null) {
            property.setArea(request.getArea());
        }
        if (request.getRooms() != null) {
            property.setRooms(request.getRooms());
        }
        if (request.getBathrooms() != null) {
            property.setBathrooms(request.getBathrooms());
        }
        if (request.getFloor() != null) {
            property.setFloor(request.getFloor());
        }
        if (request.getTotalFloors() != null) {
            property.setTotalFloors(request.getTotalFloors());
        }
        if (request.getAvailableDate() != null) {
            property.setAvailableDate(request.getAvailableDate());
        }
        if (request.getOptions() != null) {
            property.setOptions(request.getOptions());
        }
        if (request.getParkingAvailable() != null) {
            property.setParkingAvailable(request.getParkingAvailable());
        }
        if (request.getPetAllowed() != null) {
            property.setPetAllowed(request.getPetAllowed());
        }
        if (request.getFurnished() != null) {
            property.setFurnished(request.getFurnished());
        }
        if (request.getShortTermAvailable() != null) {
            property.setShortTermAvailable(request.getShortTermAvailable());
        }
    }

    private PropertyDetailResponse convertToDetailResponse(Property property) {
        List<PropertyImage> images = propertyImageService.getPropertyImages(property.getId());
        
        return PropertyDetailResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .address(property.getAddress())
                .detailAddress(property.getDetailAddress())
                .zipCode(property.getZipCode())
                .city(property.getCity())
                .district(property.getDistrict())
                .propertyType(property.getPropertyType())
                .rentalType(property.getRentalType())
                .deposit(property.getDeposit())
                .monthlyRent(property.getMonthlyRent())
                .maintenanceFee(property.getMaintenanceFee())
                .area(property.getArea())
                .rooms(property.getRooms())
                .bathrooms(property.getBathrooms())
                .floor(property.getFloor())
                .totalFloors(property.getTotalFloors())
                .availableDate(property.getAvailableDate())
                .status(property.getStatus())
                .landlordId(property.getLandlordId())
                .agentId(property.getAgentId())
                .options(property.getOptions())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .parkingAvailable(property.getParkingAvailable())
                .petAllowed(property.getPetAllowed())
                .furnished(property.getFurnished())
                .shortTermAvailable(property.getShortTermAvailable())
                .images(images)
                .createdDate(property.getCreatedDate())
                .modifiedDate(property.getModifiedDate())
                .approvedAt(property.getApprovedAt())
                .approvedBy(property.getApprovedBy())
                .build();
    }

    private PropertyListResponse.PropertyListItem convertToListItem(Property property) {
        PropertyImage mainImage = propertyImageService.getMainImage(property.getId());
        
        return PropertyListResponse.PropertyListItem.builder()
                .id(property.getId())
                .title(property.getTitle())
                .address(property.getAddress())
                .city(property.getCity())
                .district(property.getDistrict())
                .propertyType(property.getPropertyType())
                .rentalType(property.getRentalType())
                .deposit(property.getDeposit())
                .monthlyRent(property.getMonthlyRent())
                .area(property.getArea())
                .rooms(property.getRooms())
                .bathrooms(property.getBathrooms())
                .floor(property.getFloor())
                .availableDate(property.getAvailableDate())
                .status(property.getStatus())
                .parkingAvailable(property.getParkingAvailable())
                .petAllowed(property.getPetAllowed())
                .furnished(property.getFurnished())
                .mainImageUrl(mainImage != null ? mainImage.getImageUrl() : null)
                .thumbnailUrl(mainImage != null ? mainImage.getThumbnailUrl() : null)
                .createdDate(property.getCreatedDate())
                .build();
    }
}