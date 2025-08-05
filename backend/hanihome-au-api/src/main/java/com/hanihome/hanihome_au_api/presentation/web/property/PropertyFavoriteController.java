package com.hanihome.hanihome_au_api.presentation.web.property;

import com.hanihome.hanihome_au_api.application.property.dto.*;
import com.hanihome.hanihome_au_api.application.property.service.PropertyFavoriteService;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/properties/favorites")
@RequiredArgsConstructor
@Tag(name = "Property Favorites", description = "매물 즐겨찾기 관리 API")
public class PropertyFavoriteController {

    private final PropertyFavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "매물을 즐겨찾기에 추가", description = "사용자가 매물을 즐겨찾기에 추가합니다.")
    public ResponseEntity<ApiResponse<PropertyFavoriteResponseDto>> addToFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddToFavoritesRequest request) {
        
        AddToFavoritesCommand command = new AddToFavoritesCommand(
                userPrincipal.getId(),
                request.getPropertyId(),
                request.getCategory(),
                request.getNotes(),
                request.getNotificationEnabled()
        );

        PropertyFavoriteResponseDto response = favoriteService.addToFavorites(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "즐겨찾기에서 매물 제거", description = "사용자가 즐겨찾기에서 매물을 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> removeFromFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long propertyId) {
        
        favoriteService.removeFromFavorites(userPrincipal.getId(), propertyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{favoriteId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "즐겨찾기 정보 수정", description = "즐겨찾기의 카테고리, 메모, 알림 설정을 수정합니다.")
    public ResponseEntity<ApiResponse<PropertyFavoriteResponseDto>> updateFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long favoriteId,
            @Valid @RequestBody UpdateFavoriteRequest request) {
        
        UpdateFavoriteCommand command = new UpdateFavoriteCommand(
                favoriteId,
                userPrincipal.getId(),
                request.getCategory(),
                request.getNotes(),
                request.getNotificationEnabled()
        );

        PropertyFavoriteResponseDto response = favoriteService.updateFavorite(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자의 즐겨찾기 목록 조회", description = "사용자의 즐겨찾기 매물 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PropertyFavoriteResponseDto>>> getUserFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PropertyFavoriteResponseDto> favorites = favoriteService.getUserFavorites(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "카테고리별 즐겨찾기 조회", description = "특정 카테고리의 즐겨찾기 매물을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PropertyFavoriteResponseDto>>> getFavoritesByCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<PropertyFavoriteResponseDto> favorites = favoriteService.getUserFavoritesByCategory(
                userPrincipal.getId(), category, pageable);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자의 즐겨찾기 카테고리 목록", description = "사용자가 사용하는 즐겨찾기 카테고리 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<String>>> getUserFavoriteCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<String> categories = favoriteService.getUserFavoriteCategories(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "즐겨찾기 통계", description = "사용자의 즐겨찾기 통계 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFavoriteStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        long totalCount = favoriteService.getUserFavoriteCount(userPrincipal.getId());
        Map<String, Long> categoryStats = favoriteService.getCategoryStats(userPrincipal.getId());
        
        Map<String, Object> stats = Map.of(
                "totalCount", totalCount,
                "categoryStats", categoryStats
        );
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/check/{propertyId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "즐겨찾기 여부 확인", description = "특정 매물이 즐겨찾기에 추가되었는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkIsFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long propertyId) {
        
        boolean isFavorite = favoriteService.isFavorite(userPrincipal.getId(), propertyId);
        return ResponseEntity.ok(ApiResponse.success(isFavorite));
    }

    @GetMapping("/property-ids")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "즐겨찾기 매물 ID 목록", description = "사용자의 즐겨찾기 매물 ID 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Long>>> getFavoritePropertyIds(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<Long> propertyIds = favoriteService.getUserFavoritePropertyIds(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(propertyIds));
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "알림 활성화된 즐겨찾기 조회", description = "알림이 활성화된 즐겨찾기 매물을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PropertyFavoriteResponseDto>>> getNotificationEnabledFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<PropertyFavoriteResponseDto> favorites = favoriteService.getNotificationEnabledFavorites(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
}