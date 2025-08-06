package com.hanihome.hanihome_au_api.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cursor for pagination in property search")
public class PropertySearchCursor {
    
    @Schema(description = "Last property ID from previous page")
    private Long lastId;
    
    @Schema(description = "Last sort value (for cursor-based pagination)")
    private String lastSortValue;
    
    @Schema(description = "Last created timestamp")
    private LocalDateTime lastCreatedAt;
    
    @Schema(description = "Encoded cursor string")
    private String encodedCursor;
    
    @Schema(description = "Whether to use cursor-based pagination")
    @Builder.Default
    private Boolean useCursor = false;
}