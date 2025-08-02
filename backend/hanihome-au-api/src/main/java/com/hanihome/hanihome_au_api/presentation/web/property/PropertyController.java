package com.hanihome.hanihome_au_api.presentation.web.property;

import com.hanihome.hanihome_au_api.application.property.dto.CreatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.application.property.service.PropertyApplicationService;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.presentation.dto.CreatePropertyRequest;
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

    @PostMapping
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:create')")
    @Operation(
        summary = "Create a new property",
        description = "Creates a new property listing with the provided details. Property will be in PENDING_APPROVAL status."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Property created successfully",
            content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<ApiResponse<PropertyResponseDto>> createProperty(
            @Valid @RequestBody CreatePropertyRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new property: {}", request.getTitle());
            
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

    @GetMapping("/{propertyId}")
    @PreAuthorize("@securityExpressionHandler.canViewProperty(#propertyId)")
    @Operation(summary = "Get property by ID", description = "Retrieves property details by ID")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> getProperty(@PathVariable Long propertyId) {
        try {
            log.info("Fetching property with ID: {}", propertyId);
            PropertyResponseDto property = propertyApplicationService.getProperty(propertyId);
            return ResponseEntity.ok(ApiResponse.success("Property retrieved successfully", property));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping
    @Operation(summary = "Get available properties", description = "Retrieves all available properties")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getAvailableProperties() {
        try {
            List<PropertyResponseDto> properties = propertyApplicationService.getAvailableProperties();
            return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @GetMapping("/my-properties")
    @PreAuthorize("@securityExpressionHandler.canAccessLandlordFeatures()")
    @Operation(summary = "Get user's properties", description = "Retrieves properties owned by authenticated user")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getMyProperties(Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            log.info("Fetching properties for owner: {}", ownerId);
            List<PropertyResponseDto> properties = propertyApplicationService.getPropertiesByOwner(ownerId);
            return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("@securityExpressionHandler.canManageProperty(#id) and @securityExpressionHandler.hasPermission('property:update')")
    @Operation(summary = "Activate property", description = "Activates a property for rental")
    public ResponseEntity<ApiResponse<Void>> activateProperty(@PathVariable Long id, Authentication authentication) {
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

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("@securityExpressionHandler.canManageProperty(#id) and @securityExpressionHandler.hasPermission('property:update')")
    @Operation(summary = "Deactivate property", description = "Deactivates a property from rental")
    public ResponseEntity<ApiResponse<Void>> deactivateProperty(@PathVariable Long id, Authentication authentication) {
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

    @PostMapping("/{propertyId}/approve")
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:approve')")
    @Operation(summary = "Approve property", description = "Approves a property for listing")
    public ResponseEntity<ApiResponse<Void>> approveProperty(@PathVariable Long propertyId, Authentication authentication) {
        try {
            log.info("Approving property with ID: {}", propertyId);
            
            Long agentId = extractUserIdFromAuthentication(authentication);
            propertyApplicationService.approveProperty(propertyId, agentId);
            
            return ResponseEntity.ok(ApiResponse.success("Property approved successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PostMapping("/{propertyId}/reject")
    @PreAuthorize("@securityExpressionHandler.hasPermission('property:approve')")
    @Operation(summary = "Reject property", description = "Rejects a property listing")
    public ResponseEntity<ApiResponse<Void>> rejectProperty(
            @PathVariable Long propertyId,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Rejecting property with ID: {} for reason: {}", propertyId, reason);
            
            propertyApplicationService.rejectProperty(propertyId, reason);
            return ResponseEntity.ok(ApiResponse.success("Property rejected successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }


    private Long extractUserIdFromAuthentication(Authentication authentication) {
        return 1L;
    }
}