package com.hanihome.hanihome_au_api.application.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDto {
    
    // User Statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Map<String, Long> usersByRole;
    private List<TimeSeriesData> userGrowthData;
    
    // Property Statistics
    private Long totalProperties;
    private Long activeProperties;
    private Long newPropertiesToday;
    private Long newPropertiesThisWeek;
    private Long newPropertiesThisMonth;
    private Map<String, Long> propertiesByStatus;
    private Map<String, Long> propertiesByType;
    private List<TimeSeriesData> propertyGrowthData;
    
    // Transaction Statistics
    private Long totalTransactions;
    private Long completedTransactions;
    private Long pendingTransactions;
    private Long transactionsToday;
    private Long transactionsThisWeek;
    private Long transactionsThisMonth;
    private Map<String, Long> transactionsByStatus;
    private List<TimeSeriesData> transactionGrowthData;
    
    // Viewing Statistics
    private Long totalViewings;
    private Long scheduledViewings;
    private Long completedViewings;
    private Long viewingsToday;
    private Long viewingsThisWeek;
    private Long viewingsThisMonth;
    private Map<String, Long> viewingsByStatus;
    private List<TimeSeriesData> viewingGrowthData;
    
    // System Statistics
    private LocalDateTime lastUpdated;
    private Map<String, Object> systemMetrics;
    
    @Data
    @Builder
    public static class TimeSeriesData {
        private String period;
        private Long value;
        private LocalDateTime timestamp;
    }
}