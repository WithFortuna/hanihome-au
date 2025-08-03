package com.hanihome.hanihome_au_api.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Autocomplete search request")
public class AutocompleteRequest {
    
    @NotBlank(message = "Query cannot be empty")
    @Size(min = 1, max = 100, message = "Query must be between 1 and 100 characters")
    @Schema(description = "Search query for autocomplete", example = "apt")
    private String query;
    
    @Schema(description = "Type of autocomplete", allowableValues = {"location", "property_title", "all"})
    private String type = "all";
    
    @Schema(description = "Maximum number of suggestions", example = "10")
    private Integer limit = 10;
}