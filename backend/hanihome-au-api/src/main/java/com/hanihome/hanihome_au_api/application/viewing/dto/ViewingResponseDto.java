package com.hanihome.hanihome_au_api.application.viewing.dto;

import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingResponseDto {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private String propertyAddress;
    private Long tenantUserId;
    private String tenantName;
    private String tenantEmail;
    private Long landlordUserId;
    private String landlordName;
    private String landlordEmail;
    private Long agentUserId;
    private String agentName;
    private String agentEmail;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private ViewingStatus status;
    private String tenantNotes;
    private String landlordNotes;
    private String agentNotes;
    private String contactPhone;
    private String contactEmail;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Long cancelledByUserId;
    private LocalDateTime completedAt;
    private Integer feedbackRating;
    private String feedbackComment;
    private Long rescheduledFromViewingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private LocalDateTime scheduledEndTime;
    private boolean canBeCancelled;
    private boolean canBeRescheduled;
    private boolean isInPast;
    private boolean requiresFeedback;
}