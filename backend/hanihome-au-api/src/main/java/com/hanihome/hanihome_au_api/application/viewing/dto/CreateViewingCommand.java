package com.hanihome.hanihome_au_api.application.viewing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateViewingCommand {
    private Long propertyId;
    private Long tenantUserId;
    private Long landlordUserId;
    private Long agentUserId; // Optional
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String tenantNotes;
    private String contactPhone;
    private String contactEmail;
}