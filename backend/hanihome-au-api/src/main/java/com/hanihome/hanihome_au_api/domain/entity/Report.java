package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.ReportStatus;
import com.hanihome.hanihome_au_api.domain.enums.ReportType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports", indexes = {
    @Index(name = "idx_report_status", columnList = "status"),
    @Index(name = "idx_report_type", columnList = "reportType"),
    @Index(name = "idx_report_reporter", columnList = "reporterId"),
    @Index(name = "idx_report_target", columnList = "targetType, targetId"),
    @Index(name = "idx_report_created_at", columnList = "createdAt"),
    @Index(name = "idx_report_assigned_admin", columnList = "assignedAdminId")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String targetType; // USER, PROPERTY, TRANSACTION, REVIEW 등

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column
    private Long assignedAdminId;

    @Column(length = 2000)
    private String adminNotes;

    @Column(length = 1000)
    private String resolution;

    @Column
    private LocalDateTime resolvedAt;

    @Column
    private LocalDateTime assignedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReportAction> actions = new ArrayList<>();

    @Column
    private Integer priority = 1; // 1: Low, 2: Medium, 3: High, 4: Critical

    @Column
    private Boolean autoModerated = false;

    @Column(length = 500)
    private String evidenceUrls; // JSON array of evidence URLs

    @Builder
    public Report(Long reporterId, ReportType reportType, String description, 
                  String targetType, Long targetId, Integer priority, String evidenceUrls) {
        this.reporterId = reporterId;
        this.reportType = reportType;
        this.description = description;
        this.targetType = targetType;
        this.targetId = targetId;
        this.priority = priority != null ? priority : 1;
        this.evidenceUrls = evidenceUrls;
        this.status = ReportStatus.PENDING;
        this.autoModerated = false;
    }

    public void assignToAdmin(Long adminId) {
        this.assignedAdminId = adminId;
        this.assignedAt = LocalDateTime.now();
        updateStatus(ReportStatus.UNDER_REVIEW);
    }

    public void updateStatus(ReportStatus newStatus) {
        ReportStatus oldStatus = this.status;
        this.status = newStatus;
        
        if (newStatus.isCompleted()) {
            this.resolvedAt = LocalDateTime.now();
        }
        
        // Add action record
        ReportAction action = ReportAction.builder()
                .report(this)
                .actionType(ReportAction.ActionType.STATUS_CHANGE)
                .description(String.format("Status changed from %s to %s", oldStatus, newStatus))
                .performedBy(this.assignedAdminId)
                .build();
        
        this.actions.add(action);
    }

    public void addAdminNotes(String notes, Long adminId) {
        this.adminNotes = notes;
        
        ReportAction action = ReportAction.builder()
                .report(this)
                .actionType(ReportAction.ActionType.NOTES_ADDED)
                .description("Admin notes added")
                .performedBy(adminId)
                .build();
        
        this.actions.add(action);
    }

    public void resolve(String resolution, Long adminId) {
        this.resolution = resolution;
        this.resolvedAt = LocalDateTime.now();
        updateStatus(ReportStatus.RESOLVED);
        
        ReportAction action = ReportAction.builder()
                .report(this)
                .actionType(ReportAction.ActionType.RESOLVED)
                .description(resolution)
                .performedBy(adminId)
                .build();
        
        this.actions.add(action);
    }

    public void dismiss(String reason, Long adminId) {
        this.resolution = reason;
        this.resolvedAt = LocalDateTime.now();
        updateStatus(ReportStatus.DISMISSED);
        
        ReportAction action = ReportAction.builder()
                .report(this)
                .actionType(ReportAction.ActionType.DISMISSED)
                .description(reason)
                .performedBy(adminId)
                .build();
        
        this.actions.add(action);
    }

    public void escalate(Long adminId, String reason) {
        updateStatus(ReportStatus.ESCALATED);
        
        ReportAction action = ReportAction.builder()
                .report(this)
                .actionType(ReportAction.ActionType.ESCALATED)
                .description("Escalated: " + reason)
                .performedBy(adminId)
                .build();
        
        this.actions.add(action);
    }

    public void markAsAutoModerated() {
        this.autoModerated = true;
    }

    public boolean isHighPriority() {
        return this.priority >= 3;
    }

    public boolean isOverdue() {
        if (this.status.isCompleted()) {
            return false;
        }
        
        // Consider a report overdue if it's pending for more than 24 hours for high priority,
        // or more than 72 hours for normal priority
        LocalDateTime threshold = isHighPriority() ? 
                this.createdAt.plusHours(24) : 
                this.createdAt.plusHours(72);
        
        return LocalDateTime.now().isAfter(threshold);
    }
}