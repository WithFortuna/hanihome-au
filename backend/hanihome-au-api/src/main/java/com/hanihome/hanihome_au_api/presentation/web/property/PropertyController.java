package com.hanihome.hanihome_au_api.presentation.web.property;

import com.hanihome.hanihome_au_api.application.property.dto.CreatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.application.property.service.PropertyApplicationService;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.dto.request.PropertyCreateRequest;
import com.hanihome.hanihome_au_api.dto.request.PropertySearchCriteria;
import com.hanihome.hanihome_au_api.dto.request.PropertyUpdateRequest;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyDetailResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyListResponse;
import com.hanihome.hanihome_au_api.presentation.dto.CreatePropertyRequest;
import com.hanihome.hanihome_au_api.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class PropertyController {
    
    private final PropertyApplicationService propertyApplicationService;
    private final PropertyService propertyService;

    // DDD-based endpoints using PropertyApplicationService
    @PostMapping("/ddd")
    @Operation(summary = "Create property (DDD)", description = "Creates a new property using DDD architecture")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> createPropertyDDD(
            @Valid @RequestBody CreatePropertyRequest request,
            Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            
            CreatePropertyCommand command = new CreatePropertyCommand(
                ownerId,
                request.getTitle(),
                request.getDescription(),
                request.getPropertyType(),
                request.getRentalType(),
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getCountry(),
                request.getPostalCode(),
                request.getLatitude(),
                request.getLongitude(),
                request.getBedrooms(),
                request.getBathrooms(),
                request.getFloorArea(),
                request.getFloor(),
                request.getTotalFloors(),
                request.isHasParking(),
                request.isHasPet(),
                request.isHasElevator(),
                request.getRentPrice(),
                request.getDepositAmount(),
                request.getCurrency()
            );

            PropertyResponseDto propertyResponse = propertyApplicationService.createProperty(command);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Property created successfully", propertyResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/ddd/{id}")
    @Operation(summary = "Get property by ID (DDD)", description = "Retrieves property using DDD architecture")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> getPropertyDDD(@PathVariable Long id) {
        try {
            PropertyResponseDto property = propertyApplicationService.getProperty(id);
            return ResponseEntity.ok(ApiResponse.success("Property retrieved successfully", property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/ddd")
    @Operation(summary = "Get available properties (DDD)", description = "Retrieves all available properties using DDD")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getAvailablePropertiesDDD() {
        try {
            List<PropertyResponseDto> properties = propertyApplicationService.getAvailableProperties();
            return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/ddd/my-properties")
    @Operation(summary = "Get user's properties (DDD)", description = "Retrieves properties owned by authenticated user using DDD")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getMyPropertiesDDD(Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            List<PropertyResponseDto> properties = propertyApplicationService.getPropertiesByOwner(ownerId);
            return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PutMapping("/ddd/{id}/activate")
    @Operation(summary = "Activate property (DDD)", description = "Activates a property for rental using DDD")
    public ResponseEntity<ApiResponse<Void>> activatePropertyDDD(@PathVariable Long id, Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            propertyApplicationService.activateProperty(id, ownerId);
            return ResponseEntity.ok(ApiResponse.success("Property activated successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PutMapping("/ddd/{id}/deactivate")
    @Operation(summary = "Deactivate property (DDD)", description = "Deactivates a property from rental using DDD")
    public ResponseEntity<ApiResponse<Void>> deactivatePropertyDDD(@PathVariable Long id, Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            propertyApplicationService.deactivateProperty(id, ownerId);
            return ResponseEntity.ok(ApiResponse.success("Property deactivated successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    // Legacy endpoints using PropertyService - with full functionality
    @PostMapping
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:create')")
    @Operation(
        summary = "Create a new property",
        description = "Creates a new property listing with the provided details. Property will be in PENDING_APPROVAL status."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> createProperty(
            @Valid @RequestBody PropertyCreateRequest request) {
        log.info("Creating new property: {}", request.getTitle());
        
        Long landlordId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.createProperty(request, landlordId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Property created successfully", property));
    }

    @GetMapping("/{propertyId}")
    @PreAuthorize("@securityExpressionHandler.canViewProperty(#propertyId)")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> getProperty(@PathVariable Long propertyId) {
        log.info("Fetching property with ID: {}", propertyId);
        
        PropertyDetailResponse property = propertyService.getProperty(propertyId);
        return ResponseEntity.ok(ApiResponse.success("Property retrieved successfully", property));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PropertyListResponse>> searchProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @ModelAttribute PropertySearchCriteria criteria) {
        
        log.info("Searching properties with criteria: {}", criteria);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));
        
        // Set default status to ACTIVE if not specified
        if (criteria.getStatus() == null) {
            criteria.setStatus(PropertyStatus.ACTIVE);
        }
        
        PropertyListResponse properties = propertyService.searchProperties(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
    }

    @PutMapping("/{propertyId}")
    @PreAuthorize("@securityExpressionHandler.canManageProperty(#propertyId) and " +
                  "@securityExpressionHandler.hasPermission('property:update')")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> updateProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyUpdateRequest request) {
        log.info("Updating property with ID: {}", propertyId);
        
        Long landlordId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.updateProperty(propertyId, request, landlordId);
        
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", property));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("@securityExpressionHandler.canManageProperty(#propertyId) and " +
                  "@securityExpressionHandler.hasPermission('property:delete')")
    public ResponseEntity<ApiResponse<String>> deleteProperty(@PathVariable Long propertyId) {
        log.info("Deleting property with ID: {}", propertyId);
        
        Long landlordId = getCurrentUserId();
        propertyService.deleteProperty(propertyId, landlordId);
        
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", 
            "Property " + propertyId + " has been deleted"));
    }

    @PostMapping("/{propertyId}/approve")
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:approve')")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> approveProperty(@PathVariable Long propertyId) {
        log.info("Approving property with ID: {}", propertyId);
        
        Long agentId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.approveProperty(propertyId, agentId);
        
        return ResponseEntity.ok(ApiResponse.success("Property approved successfully", property));
    }

    @PostMapping("/{propertyId}/reject")
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:approve')")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> rejectProperty(
            @PathVariable Long propertyId,
            @RequestParam(required = false) String reason) {
        log.info("Rejecting property with ID: {} for reason: {}", propertyId, reason);
        
        PropertyDetailResponse property = propertyService.rejectProperty(propertyId, reason);
        return ResponseEntity.ok(ApiResponse.success("Property rejected successfully", property));
    }

    @PatchMapping("/{propertyId}/status")
    @PreAuthorize("@securityExpressionHandler.canManageProperty(#propertyId) or " +
                  "@securityExpressionHandler.hasPermission('property:manage')")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> changePropertyStatus(
            @PathVariable Long propertyId,
            @RequestParam PropertyStatus status) {
        log.info("Changing property {} status to: {}", propertyId, status);
        
        Long userId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.changePropertyStatus(propertyId, status, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Property status updated successfully", property));
    }

    // Landlord-specific endpoints
    @GetMapping("/my-properties")
    @PreAuthorize("@securityExpressionHandler.canAccessLandlordFeatures()")
    public ResponseEntity<ApiResponse<PropertyListResponse>> getMyProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long landlordId = getCurrentUserId();
        log.info("Fetching properties for landlord: {}", landlordId);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));
        
        PropertyListResponse properties = propertyService.getPropertiesByLandlord(landlordId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Landlord properties retrieved successfully", properties));
    }

    // Agent-specific endpoints
    @GetMapping("/pending-approval")
    @PreAuthorize("@securityExpressionHandler.canAccessAgentFeatures()")
    public ResponseEntity<ApiResponse<PropertyListResponse>> getPendingApprovalProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching properties pending approval");
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.ASC, "createdDate"));
        PropertySearchCriteria criteria = PropertySearchCriteria.builder()
                .status(PropertyStatus.PENDING_APPROVAL)
                .build();
        
        PropertyListResponse properties = propertyService.searchProperties(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending approval properties retrieved successfully", properties));
    }

    // Admin-specific endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("@securityExpressionHandler.canAccessAdminFeatures()")
    public ResponseEntity<ApiResponse<PropertyListResponse>> getAllPropertiesAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) PropertyStatus status) {
        log.info("Admin fetching all properties");
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));
        
        PropertySearchCriteria criteria = PropertySearchCriteria.builder()
                .status(status) // null = all statuses
                .build();
        
        PropertyListResponse properties = propertyService.searchProperties(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Admin: All properties retrieved successfully", properties));
    }

    @PostMapping("/admin/{propertyId}/suspend")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> suspendProperty(@PathVariable Long propertyId) {
        log.info("Admin suspending property: {}", propertyId);
        
        Long adminId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.changePropertyStatus(propertyId, PropertyStatus.SUSPENDED, adminId);
        
        return ResponseEntity.ok(ApiResponse.success("Property suspended successfully", property));
    }

    // Utility endpoints
    @GetMapping("/{propertyId}/similar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSimilarProperties(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Fetching similar properties for property: {}", propertyId);
        
        var similarProperties = propertyService.findSimilarProperties(propertyId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Similar properties retrieved successfully", 
            Map.of("properties", similarProperties)));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNearbyProperties(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching nearby properties for location: {}, {}", latitude, longitude);
        
        var nearbyProperties = propertyService.findNearbyProperties(latitude, longitude, radiusKm, Math.min(limit, 50));
        return ResponseEntity.ok(ApiResponse.success("Nearby properties retrieved successfully", 
            Map.of("properties", nearbyProperties)));
    }

    @GetMapping("/distance-filter")
    @Operation(summary = "Search properties with distance-based filtering and sorting",
            description = "Advanced property search with precise distance calculations, multiple filter options, and distance-based sorting")
    public ResponseEntity<ApiResponse<PropertyListResponse>> searchPropertiesWithDistanceFilter(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double maxDistanceKm,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) RentalType rentalType,
            @RequestParam(required = false) Integer minRooms,
            @RequestParam(required = false) Integer maxRooms,
            @RequestParam(required = false) Boolean parkingRequired,
            @RequestParam(required = false) Boolean petAllowed,
            @RequestParam(required = false) Boolean furnished,
            @RequestParam(defaultValue = "distance") String sortBy, // distance, price, area, date
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Distance-based property search - Location: {}, {}, Max Distance: {}km", 
                latitude, longitude, maxDistanceKm);
        
        // Build search criteria with location filter
        PropertySearchCriteria criteria = PropertySearchCriteria.builder()
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .radiusKm(maxDistanceKm)
                .minMonthlyRent(minPrice)
                .maxMonthlyRent(maxPrice)
                .propertyType(propertyType)
                .rentalType(rentalType)
                .minRooms(minRooms)
                .maxRooms(maxRooms)
                .parkingRequired(parkingRequired)
                .petAllowed(petAllowed)
                .furnished(furnished)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .status(PropertyStatus.ACTIVE)
                .build();
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 50), Sort.by(direction, sortBy));
        
        PropertyListResponse properties = propertyService.searchProperties(criteria, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d properties within %.1fkm", 
                        properties.getTotalElements(), maxDistanceKm), 
                properties));
    }

    @GetMapping("/distance-ranges")
    @Operation(summary = "Get property count by distance ranges",
            description = "Returns the number of properties within different distance ranges from a given location")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPropertiesCountByDistance(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) RentalType rentalType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        log.info("Getting property count by distance ranges for location: {}, {}", latitude, longitude);
        
        Map<String, Integer> distanceRanges = propertyService.getPropertyCountByDistanceRanges(
                latitude, longitude, propertyType, rentalType, minPrice, maxPrice);
        
        return ResponseEntity.ok(ApiResponse.success("Distance ranges retrieved successfully", 
            Map.of("distanceRanges", distanceRanges)));
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        return 1L;
    }

    private Long getCurrentUserId() {
        // This would be implemented to get current user ID from security context
        // For now, return a placeholder
        return 1L;
    }
}