package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import com.hanihome.hanihome_au_api.domain.shared.entity.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "viewings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viewing extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;

    @Column(name = "landlord_user_id", nullable = false)
    private Long landlordUserId;

    @Column(name = "agent_user_id")
    private Long agentUserId; // Optional - if viewing is arranged through agent

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60; // Default 1 hour

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ViewingStatus status = ViewingStatus.REQUESTED;

    @Column(name = "tenant_notes", length = 1000)
    private String tenantNotes; // Notes from tenant when requesting viewing

    @Column(name = "landlord_notes", length = 1000)
    private String landlordNotes; // Notes from landlord

    @Column(name = "agent_notes", length = 1000)
    private String agentNotes; // Notes from agent if involved

    @Column(name = "contact_phone", length = 20)
    private String contactPhone; // Primary contact phone for viewing

    @Column(name = "contact_email", length = 255)
    private String contactEmail; // Primary contact email for viewing

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_by_user_id")
    private Long cancelledByUserId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "feedback_rating")
    private Integer feedbackRating; // 1-5 rating after viewing

    @Column(name = "feedback_comment", length = 1000)
    private String feedbackComment;

    @Column(name = "rescheduled_from_viewing_id")
    private Long rescheduledFromViewingId; // If this viewing was rescheduled from another

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Override
    public Long getId() {
        return id;
    }

    // Business logic methods
    public void confirm(Long confirmedByUserId) {
        if (this.status != ViewingStatus.REQUESTED) {
            throw new IllegalStateException("Can only confirm requested viewings");
        }
        this.status = ViewingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(Long cancelledByUserId, String reason) {
        if (this.status == ViewingStatus.CANCELLED || this.status == ViewingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel viewing with status: " + this.status);
        }
        this.status = ViewingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledByUserId = cancelledByUserId;
        this.cancellationReason = reason;
    }

    public void complete() {
        if (this.status != ViewingStatus.CONFIRMED) {
            throw new IllegalStateException("Can only complete confirmed viewings");
        }
        this.status = ViewingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void reschedule(LocalDateTime newScheduledAt) {
        if (this.status == ViewingStatus.CANCELLED || this.status == ViewingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule viewing with status: " + this.status);
        }
        this.scheduledAt = newScheduledAt;
        this.status = ViewingStatus.REQUESTED; // Reset to requested for re-confirmation
        this.confirmedAt = null;
    }

    public void addFeedback(Integer rating, String comment) {
        if (this.status != ViewingStatus.COMPLETED) {
            throw new IllegalStateException("Can only add feedback to completed viewings");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.feedbackRating = rating;
        this.feedbackComment = comment;
    }

    public boolean isInPast() {
        return this.scheduledAt.isBefore(LocalDateTime.now());
    }

    public boolean canBeCancelled() {
        return this.status == ViewingStatus.REQUESTED || this.status == ViewingStatus.CONFIRMED;
    }

    public boolean canBeRescheduled() {
        return this.status == ViewingStatus.REQUESTED || this.status == ViewingStatus.CONFIRMED;
    }

    public LocalDateTime getScheduledEndTime() {
        return this.scheduledAt.plusMinutes(this.durationMinutes);
    }

    public boolean overlaps(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime thisEndTime = getScheduledEndTime();
        return !this.scheduledAt.isAfter(endTime) && !thisEndTime.isBefore(startTime);
    }
}