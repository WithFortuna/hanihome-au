package com.hanihome.hanihome_au_api.presentation.web.search;

import com.hanihome.hanihome_au_api.application.search.dto.SaveSearchCommand;
import com.hanihome.hanihome_au_api.application.search.dto.SearchHistoryResponseDto;
import com.hanihome.hanihome_au_api.application.search.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search History", description = "Search history management APIs")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping
    @Operation(summary = "Get search history", description = "Retrieve user's search history with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<SearchHistoryResponseDto>> getSearchHistory(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Filter saved searches only") @RequestParam(defaultValue = "false") boolean savedOnly,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Long userId = Long.valueOf(jwt.getSubject());
        Pageable pageable = PageRequest.of(page, size);

        Page<SearchHistoryResponseDto> searchHistory = searchHistoryService.getSearchHistory(userId, savedOnly, pageable);

        return ResponseEntity.ok(searchHistory);
    }

    @GetMapping("/frequent")
    @Operation(summary = "Get frequent searches", description = "Retrieve user's most frequent searches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Frequent searches retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SearchHistoryResponseDto>> getFrequentSearches(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "10") int limit) {

        Long userId = Long.valueOf(jwt.getSubject());
        List<SearchHistoryResponseDto> frequentSearches = searchHistoryService.getFrequentSearches(userId, limit);

        return ResponseEntity.ok(frequentSearches);
    }

    @PostMapping("/{searchHistoryId}/save")
    @Operation(summary = "Save search", description = "Save a search with a custom name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or search name already exists"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Search history not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SearchHistoryResponseDto> saveSearch(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Search history ID to save") @PathVariable Long searchHistoryId,
            @Valid @RequestBody SaveSearchRequest request) {

        Long userId = Long.valueOf(jwt.getSubject());

        SaveSearchCommand command = SaveSearchCommand.builder()
                .userId(userId)
                .searchHistoryId(searchHistoryId)
                .searchName(request.getSearchName())
                .build();

        SearchHistoryResponseDto savedSearch = searchHistoryService.saveSearch(command);

        return ResponseEntity.ok(savedSearch);
    }

    @DeleteMapping("/{searchHistoryId}")
    @Operation(summary = "Delete search history", description = "Delete a specific search history entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Search history deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Search history not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteSearchHistory(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Search history ID to delete") @PathVariable Long searchHistoryId) {

        Long userId = Long.valueOf(jwt.getSubject());
        searchHistoryService.deleteSearchHistory(userId, searchHistoryId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear search history", description = "Clear all non-saved search history for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search history cleared successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ClearHistoryResponse> clearSearchHistory(@AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());
        int deletedCount = searchHistoryService.clearAllHistory(userId);

        return ResponseEntity.ok(new ClearHistoryResponse(deletedCount));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get search statistics", description = "Get search statistics for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SearchHistoryService.SearchHistoryStats> getSearchStats(@AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());
        SearchHistoryService.SearchHistoryStats stats = searchHistoryService.getSearchStats(userId);

        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SaveSearchRequest {
        @jakarta.validation.constraints.NotBlank(message = "Search name cannot be blank")
        @jakarta.validation.constraints.Size(max = 100, message = "Search name cannot exceed 100 characters")
        private String searchName;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ClearHistoryResponse {
        private int deletedCount;
    }
}