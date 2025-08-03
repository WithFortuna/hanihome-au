package com.hanihome.hanihome_au_api.presentation.dto;

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
@Schema(description = "Autocomplete search response")
public class AutocompleteResponse {
    
    @Schema(description = "List of autocomplete suggestions")
    private List<AutocompleteSuggestion> suggestions;
    
    @Schema(description = "Query that was searched")
    private String query;
    
    @Schema(description = "Total number of suggestions")
    private Integer totalSuggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutocompleteSuggestion {
        
        @Schema(description = "Suggestion text")
        private String text;
        
        @Schema(description = "Type of suggestion", allowableValues = {"location", "property_title", "popular_search"})
        private String type;
        
        @Schema(description = "Highlighted version of suggestion with query matches")
        private String highlighted;
        
        @Schema(description = "Frequency or relevance score")
        private Integer score;
        
        @Schema(description = "Additional context or details")
        private String context;
    }
}