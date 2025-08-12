package com.hanihome.hanihome_au_api.presentation.web.property;

import com.hanihome.hanihome_au_api.application.property.service.SearchPerformanceService;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/search-performance")
@RequiredArgsConstructor
@Tag(name = "Search Performance", description = "검색 성능 모니터링 및 관리 API")
@PreAuthorize("hasRole('ADMIN')")
public class SearchPerformanceController {

    private final SearchPerformanceService searchPerformanceService;

    @GetMapping("/stats")
    @Operation(summary = "검색 성능 통계 조회", description = "검색 성능 통계 및 캐시 히트율을 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStats() {
        Map<String, Object> stats = searchPerformanceService.getSearchStatsSummary();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/cache-hit-rate")
    @Operation(summary = "캐시 히트율 조회", description = "현재 캐시 히트율을 조회합니다.")
    public ResponseEntity<ApiResponse<Double>> getCacheHitRate() {
        double hitRate = searchPerformanceService.getCacheHitRate();
        return ResponseEntity.ok(ApiResponse.success(hitRate));
    }

    @GetMapping("/slow-queries")
    @Operation(summary = "슬로우 쿼리 목록 조회", description = "느린 검색 쿼리 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Object>>> getSlowQueries(
            @RequestParam(defaultValue = "50") int limit) {
        List<Object> slowQueries = searchPerformanceService.getSlowQueries(limit);
        return ResponseEntity.ok(ApiResponse.success(slowQueries));
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "캐시 초기화", description = "검색 캐시를 초기화합니다.")
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        searchPerformanceService.clearOldCacheEntries();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/cache/preload")
    @Operation(summary = "인기 검색 캐시 프리로드", description = "인기 검색어를 캐시에 미리 로드합니다.")
    public ResponseEntity<ApiResponse<Void>> preloadCache() {
        searchPerformanceService.preloadPopularSearches();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}