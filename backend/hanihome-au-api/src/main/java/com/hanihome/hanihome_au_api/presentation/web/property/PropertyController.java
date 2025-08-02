package com.hanihome.hanihome_au_api.presentation.web.property;

import com.hanihome.hanihome_au_api.application.property.dto.CreatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.application.property.service.PropertyApplicationService;
import com.hanihome.hanihome_au_api.presentation.dto.ApiResponse;
import com.hanihome.hanihome_au_api.presentation.dto.CreatePropertyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Property Management", description = "Property management operations")
public class PropertyController {
    
    private final PropertyApplicationService propertyApplicationService;

    public PropertyController(PropertyApplicationService propertyApplicationService) {
        this.propertyApplicationService = propertyApplicationService;
    }

    @PostMapping
    @Operation(summary = "Create a new property", description = "Creates a new property listing")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> createProperty(
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
            ApiResponse<PropertyResponseDto> response = ApiResponse.success("Property created successfully", propertyResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<PropertyResponseDto> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ApiResponse<PropertyResponseDto> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property by ID", description = "Retrieves property details by ID")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> getProperty(@PathVariable Long id) {
        try {
            PropertyResponseDto property = propertyApplicationService.getProperty(id);
            ApiResponse<PropertyResponseDto> response = ApiResponse.success(property);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<PropertyResponseDto> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            ApiResponse<PropertyResponseDto> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Get available properties", description = "Retrieves all available properties")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getAvailableProperties() {
        try {
            List<PropertyResponseDto> properties = propertyApplicationService.getAvailableProperties();
            ApiResponse<List<PropertyResponseDto>> response = ApiResponse.success(properties);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<PropertyResponseDto>> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/my-properties")
    @Operation(summary = "Get user's properties", description = "Retrieves properties owned by the authenticated user")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getMyProperties(Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            List<PropertyResponseDto> properties = propertyApplicationService.getPropertiesByOwner(ownerId);
            ApiResponse<List<PropertyResponseDto>> response = ApiResponse.success(properties);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<PropertyResponseDto>> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate property", description = "Activates a property for rental")
    public ResponseEntity<ApiResponse<Void>> activateProperty(@PathVariable Long id, Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            propertyApplicationService.activateProperty(id, ownerId);
            ApiResponse<Void> response = ApiResponse.success("Property activated successfully", null);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate property", description = "Deactivates a property from rental")
    public ResponseEntity<ApiResponse<Void>> deactivateProperty(@PathVariable Long id, Authentication authentication) {
        try {
            Long ownerId = extractUserIdFromAuthentication(authentication);
            propertyApplicationService.deactivateProperty(id, ownerId);
            ApiResponse<Void> response = ApiResponse.success("Property deactivated successfully", null);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        return 1L;
    }
}