package com.hanihome.hanihome_au_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanihome.hanihome_au_api.dto.request.GeographicSearchRequest;
import com.hanihome.hanihome_au_api.service.GeographicSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Geographic Search functionality
 */
@SpringBootTest
@AutoConfigureTestMockMvc
@ActiveProfiles("test")
@Transactional
public class GeographicSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeographicSearchService geographicSearchService;

    @Test
    public void testRadiusSearchEndpoint() throws Exception {
        // Melbourne CBD coordinates
        GeographicSearchRequest request = GeographicSearchRequest.builder()
            .latitude(new BigDecimal("-37.8136"))
            .longitude(new BigDecimal("144.9631"))
            .radiusKm(new BigDecimal("5.0"))
            .page(0)
            .size(10)
            .build();

        mockMvc.perform(post("/api/v1/properties/search/geographic/radius")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    public void testBoundsSearchEndpoint() throws Exception {
        // Melbourne area bounding box
        GeographicSearchRequest request = GeographicSearchRequest.builder()
            .northLatitude(new BigDecimal("-37.7000"))
            .southLatitude(new BigDecimal("-37.9000"))
            .eastLongitude(new BigDecimal("145.1000"))
            .westLongitude(new BigDecimal("144.8000"))
            .page(0)
            .size(10)
            .build();

        mockMvc.perform(post("/api/v1/properties/search/geographic/bounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    public void testNearestPropertiesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/properties/search/geographic/nearest")
                .param("latitude", "-37.8136")
                .param("longitude", "144.9631")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testPropertyClustersEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/properties/search/geographic/clusters")
                .param("northLat", "-37.7000")
                .param("southLat", "-37.9000")
                .param("eastLng", "145.1000")
                .param("westLng", "144.8000")
                .param("zoomLevel", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testDistanceCalculationEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/properties/search/geographic/distance")
                .param("lat1", "-37.8136")
                .param("lng1", "144.9631")
                .param("lat2", "-37.8398")
                .param("lng2", "144.9889"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.distanceKm").exists())
                .andExpected(jsonPath("$.data.bearing").exists());
    }

    @Test
    public void testInvalidRadiusSearchRequest() throws Exception {
        // Invalid request with missing required fields
        GeographicSearchRequest request = GeographicSearchRequest.builder()
            .latitude(new BigDecimal("-37.8136"))
            // Missing longitude and radius
            .build();

        mockMvc.perform(post("/api/v1/properties/search/geographic/radius")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidCoordinateRange() throws Exception {
        mockMvc.perform(get("/api/v1/properties/search/geographic/nearest")
                .param("latitude", "91.0") // Invalid latitude > 90
                .param("longitude", "144.9631")
                .param("limit", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDistanceCalculation() {
        // Test Haversine formula accuracy
        // Distance between Melbourne CBD and South Yarra (known distance ~3.5km)
        BigDecimal distance = geographicSearchService.calculateDistance(
            new BigDecimal("-37.8136"), new BigDecimal("144.9631"), // Melbourne CBD
            new BigDecimal("-37.8398"), new BigDecimal("144.9889")  // South Yarra
        );

        // Assert distance is approximately 3.5km (allowing 0.5km tolerance)
        assert distance.doubleValue() > 3.0;
        assert distance.doubleValue() < 4.0;
    }

    @Test
    public void testBearingCalculation() {
        // Test bearing calculation
        Integer bearing = geographicSearchService.calculateBearing(
            new BigDecimal("-37.8136"), new BigDecimal("144.9631"), // Melbourne CBD
            new BigDecimal("-37.8398"), new BigDecimal("144.9889")  // South Yarra
        );

        // South Yarra is southeast of Melbourne CBD
        assert bearing != null;
        assert bearing >= 120 && bearing <= 180; // Southeast quadrant
    }
}