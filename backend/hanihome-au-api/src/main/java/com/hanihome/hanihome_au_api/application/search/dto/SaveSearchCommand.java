package com.hanihome.hanihome_au_api.application.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Command to save a search with a name")
public class SaveSearchCommand {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user saving the search")
    private Long userId;

    @NotNull(message = "Search history ID is required")
    @Schema(description = "ID of the search history entry to save")
    private Long searchHistoryId;

    @NotBlank(message = "Search name is required")
    @Size(max = 100, message = "Search name cannot exceed 100 characters")
    @Schema(description = "Name for the saved search")
    private String searchName;
}