package com.hanihome.hanihome_au_api.controller;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.dto.request.PropertyCreateRequest;
import com.hanihome.hanihome_au_api.dto.request.PropertySearchCriteria;
import com.hanihome.hanihome_au_api.dto.request.PropertyUpdateRequest;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyDetailResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyListResponse;
import com.hanihome.hanihome_au_api.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:create')")
    @Operation(
        summary = "Create a new property",
        description = "Creates a new property listing with the provided details. Property will be in PENDING_APPROVAL status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<com.hanihome.hanihome_au_api.dto.response.ApiResponse<PropertyDetailResponse>> createProperty(
            @Valid @RequestBody PropertyCreateRequest request) {
        log.info("Creating new property: {}", request.getTitle());
        
        Long landlordId = getCurrentUserId();
        PropertyDetailResponse property = propertyService.createProperty(request, landlordId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(com.hanihome.hanihome_au_api.dto.response.ApiResponse.success("Property created successfully", property));
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

    private Long getCurrentUserId() {
        // This would be implemented to get current user ID from security context
        // For now, return a placeholder
        return 1L;
    }
}