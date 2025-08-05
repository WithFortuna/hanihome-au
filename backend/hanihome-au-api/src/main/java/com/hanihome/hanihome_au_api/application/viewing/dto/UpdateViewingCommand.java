package com.hanihome.hanihome_au_api.application.viewing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateViewingCommand {
    private Long viewingId;
    private Long userId; // User making the update
    private LocalDateTime scheduledAt; // For rescheduling
    private Integer durationMinutes;
    private String tenantNotes;
    private String landlordNotes;
    private String agentNotes;
    private String contactPhone;
    private String contactEmail;
}