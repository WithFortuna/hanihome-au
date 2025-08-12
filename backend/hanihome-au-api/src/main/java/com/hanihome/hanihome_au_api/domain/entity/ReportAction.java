package com.hanihome.hanihome_au_api.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_actions", indexes = {
    @Index(name = "idx_report_action_report", columnList = "reportId"),
    @Index(name = "idx_report_action_performed_by", columnList = "performedBy"),
    @Index(name = "idx_report_action_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportId", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column
    private Long performedBy; // Admin ID who performed the action

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 2000)
    private String additionalDetails; // JSON or text with additional context

    public enum ActionType {
        CREATED("생성됨"),
        STATUS_CHANGE("상태 변경"),
        ASSIGNED("담당자 배정"),
        NOTES_ADDED("메모 추가"),
        EVIDENCE_ADDED("증거 추가"),
        RESOLVED("해결됨"),
        DISMISSED("기각됨"),
        ESCALATED("상급자 전달"),
        WARNING_SENT("경고 발송"),
        CONTENT_REMOVED("콘텐츠 삭제"),
        USER_SUSPENDED("사용자 정지"),
        ACCOUNT_BANNED("계정 차단"),
        AUTOMATED_ACTION("자동 조치");

        private final String displayName;

        ActionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Builder
    public ReportAction(Report report, ActionType actionType, String description, 
                       Long performedBy, String additionalDetails) {
        this.report = report;
        this.actionType = actionType;
        this.description = description;
        this.performedBy = performedBy;
        this.additionalDetails = additionalDetails;
    }

    public boolean isAutomatedAction() {
        return this.actionType == ActionType.AUTOMATED_ACTION;
    }

    public boolean isSignificantAction() {
        return this.actionType == ActionType.RESOLVED || 
               this.actionType == ActionType.DISMISSED ||
               this.actionType == ActionType.WARNING_SENT ||
               this.actionType == ActionType.CONTENT_REMOVED ||
               this.actionType == ActionType.USER_SUSPENDED ||
               this.actionType == ActionType.ACCOUNT_BANNED;
    }
}