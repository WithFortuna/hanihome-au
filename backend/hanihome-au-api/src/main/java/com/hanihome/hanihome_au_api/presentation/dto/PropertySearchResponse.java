package com.hanihome.hanihome_au_api.presentation.dto;

import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Property search response with pagination and metadata")
public class PropertySearchResponse {
    
    @Schema(description = "List of properties matching search criteria")
    private List<PropertyResponseDto> properties;
    
    @Schema(description = "Current page number (0-based)")
    private int currentPage;
    
    @Schema(description = "Total number of pages")
    private int totalPages;
    
    @Schema(description = "Total number of elements matching criteria")
    private long totalElements;
    
    @Schema(description = "Number of elements in current page")
    private int numberOfElements;
    
    @Schema(description = "Page size")
    private int size;
    
    @Schema(description = "Whether this is the first page")
    private boolean first;
    
    @Schema(description = "Whether this is the last page")
    private boolean last;
    
    @Schema(description = "Whether there is a next page")
    private boolean hasNext;
    
    @Schema(description = "Whether there is a previous page")
    private boolean hasPrevious;
    
    @Schema(description = "Applied filters summary")
    private SearchFilterSummary filterSummary;
    
    @Schema(description = "Next cursor for pagination")
    private PropertySearchCursor nextCursor;
    
    @Schema(description = "Previous cursor for pagination")
    private PropertySearchCursor previousCursor;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilterSummary {
        private String keyword;
        private String priceRange;
        private String locationSummary;
        private String amenitiesSummary;
        private int totalFiltersApplied;
    }
}