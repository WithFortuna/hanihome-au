package com.hanihome.hanihome_au_api.controller;

import com.hanihome.hanihome_au_api.dto.request.GeographicSearchRequest;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.dto.response.PropertyWithDistanceResponse;
import com.hanihome.hanihome_au_api.service.GeographicSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for geographic-based property searches
 * Provides endpoints for location-based property search functionality
 */
@RestController
@RequestMapping("/api/v1/properties/search/geographic")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geographic Search", description = "Location-based property search operations")
public class GeographicSearchController {

    private final GeographicSearchService geographicSearchService;

    @PostMapping("/radius")
    @Operation(summary = "Search properties within radius", 
               description = "Find properties within a specified radius from a center point")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Properties found successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid search parameters"
        )
    })
    public ResponseEntity<ApiResponse<Page<PropertyWithDistanceResponse>>> searchWithinRadius(
            @Valid @RequestBody GeographicSearchRequest request) {
        
        log.info("Geographic radius search request: center=({}, {}), radius={}km", 
            request.getLatitude(), request.getLongitude(), request.getRadiusKm());

        Page<PropertyWithDistanceResponse> results = geographicSearchService.searchPropertiesWithinRadius(request);
        
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Found %d properties within %s km", results.getTotalElements(), request.getRadiusKm())));
    }

    @PostMapping("/bounds")
    @Operation(summary = "Search properties within bounds", 
               description = "Find properties within a specified bounding box")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Properties found successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid bounding box parameters"
        )
    })
    public ResponseEntity<ApiResponse<Page<PropertyWithDistanceResponse>>> searchWithinBounds(
            @Valid @RequestBody GeographicSearchRequest request) {
        
        log.info("Geographic bounds search request: bounds=({},{}) to ({},{})", 
            request.getSouthLatitude(), request.getWestLongitude(),
            request.getNorthLatitude(), request.getEastLongitude());

        Page<PropertyWithDistanceResponse> results = geographicSearchService.searchPropertiesWithinBounds(request);
        
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Found %d properties within specified bounds", results.getTotalElements())));
    }

    @GetMapping("/nearest")
    @Operation(summary = "Find nearest properties", 
               description = "Find the nearest properties to a specific location")
    public ResponseEntity<ApiResponse<List<PropertyWithDistanceResponse>>> findNearestProperties(
            @Parameter(description = "Latitude coordinate", required = true)
            @RequestParam @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
            
            @Parameter(description = "Longitude coordinate", required = true)
            @RequestParam @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude,
            
            @Parameter(description = "Maximum number of properties to return", required = false)
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("Finding {} nearest properties to location ({}, {})", limit, latitude, longitude);

        List<PropertyWithDistanceResponse> results = geographicSearchService.findNearestProperties(
            latitude, longitude, limit);
        
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Found %d nearest properties", results.size())));
    }

    @GetMapping("/clusters")
    @Operation(summary = "Get property clusters", 
               description = "Get clustered properties for map display optimization")
    public ResponseEntity<ApiResponse<List<GeographicSearchService.PropertyClusterResponse>>> getPropertyClusters(
            @Parameter(description = "North boundary latitude", required = true)
            @RequestParam @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal northLat,
            
            @Parameter(description = "South boundary latitude", required = true)
            @RequestParam @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal southLat,
            
            @Parameter(description = "East boundary longitude", required = true)
            @RequestParam @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal eastLng,
            
            @Parameter(description = "West boundary longitude", required = true)
            @RequestParam @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal westLng,
            
            @Parameter(description = "Map zoom level (affects cluster size)", required = false)
            @RequestParam(defaultValue = "12") @Min(1) @Max(20) int zoomLevel) {
        
        log.info("Getting property clusters for zoom level {} within bounds: ({},{}) to ({},{})", 
            zoomLevel, southLat, westLng, northLat, eastLng);

        List<GeographicSearchService.PropertyClusterResponse> results = 
            geographicSearchService.getPropertyClusters(northLat, southLat, eastLng, westLng, zoomLevel);
        
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Generated %d property clusters", results.size())));
    }

    @PostMapping("/comprehensive")
    @Operation(summary = "Comprehensive geographic search", 
               description = "Perform radius or bounding box search based on request parameters")
    public ResponseEntity<ApiResponse<Page<PropertyWithDistanceResponse>>> performGeographicSearch(
            @Valid @RequestBody GeographicSearchRequest request) {
        
        log.info("Comprehensive geographic search request: radius={}, bounds={}", 
            request.isRadiusSearch(), request.isBoundingBoxSearch());

        Page<PropertyWithDistanceResponse> results = geographicSearchService.performGeographicSearch(request);
        
        return ResponseEntity.ok(ApiResponse.success(results, 
            String.format("Found %d properties matching geographic criteria", results.getTotalElements())));
    }

    @GetMapping("/distance")
    @Operation(summary = "Calculate distance", 
               description = "Calculate distance between two geographic points")
    public ResponseEntity<ApiResponse<DistanceCalculationResponse>> calculateDistance(
            @Parameter(description = "Start point latitude", required = true)
            @RequestParam @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal lat1,
            
            @Parameter(description = "Start point longitude", required = true)
            @RequestParam @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal lng1,
            
            @Parameter(description = "End point latitude", required = true)
            @RequestParam @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal lat2,
            
            @Parameter(description = "End point longitude", required = true)
            @RequestParam @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal lng2) {
        
        BigDecimal distance = geographicSearchService.calculateDistance(lat1, lng1, lat2, lng2);
        Integer bearing = geographicSearchService.calculateBearing(lat1, lng1, lat2, lng2);
        
        DistanceCalculationResponse response = DistanceCalculationResponse.builder()
            .startLatitude(lat1)
            .startLongitude(lng1)
            .endLatitude(lat2)
            .endLongitude(lng2)
            .distanceKm(distance)
            .bearing(bearing)
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, 
            String.format("Distance calculated: %.2f km", distance)));
    }

    /**
     * Response DTO for distance calculations
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "Distance calculation result")
    public static class DistanceCalculationResponse {
        @Schema(description = "Starting point latitude")
        private BigDecimal startLatitude;
        
        @Schema(description = "Starting point longitude")
        private BigDecimal startLongitude;
        
        @Schema(description = "Ending point latitude")
        private BigDecimal endLatitude;
        
        @Schema(description = "Ending point longitude")
        private BigDecimal endLongitude;
        
        @Schema(description = "Distance in kilometers")
        private BigDecimal distanceKm;
        
        @Schema(description = "Bearing in degrees (0-359)")
        private Integer bearing;
    }
}