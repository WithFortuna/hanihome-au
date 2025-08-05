package com.hanihome.hanihome_au_api.application.moderation.service;

import com.hanihome.hanihome_au_api.domain.entity.Report;
import com.hanihome.hanihome_au_api.domain.entity.ReportAction;
import com.hanihome.hanihome_au_api.domain.enums.ReportStatus;
import com.hanihome.hanihome_au_api.domain.enums.ReportType;
import com.hanihome.hanihome_au_api.repository.ReportActionRepository;
import com.hanihome.hanihome_au_api.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoModerationService {

    private final ReportRepository reportRepository;
    private final ReportActionRepository reportActionRepository;
    private final ModerationActionService moderationActionService;

    // Auto-moderation rules and patterns
    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
        "무료", "공짜", "돈벌기", "투자", "수익", "클릭", "즉시", "보장", "100%", "급매"
    );

    private static final List<Pattern> INAPPROPRIATE_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)\\b(욕설|비방|모욕|차별|혐오)\\b"),
        Pattern.compile("(?i)\\b(사기|가짜|속임|거짓)\\b"),
        Pattern.compile("(?i)\\b(개인정보|연락처|전화번호|카카오톡)\\b")
    );

    private static final Map<ReportType, Integer> AUTO_ACTION_THRESHOLDS = Map.of(
        ReportType.SPAM_USER, 3,
        ReportType.FAKE_LISTING, 2,
        ReportType.INAPPROPRIATE_CONTENT, 5,
        ReportType.SPAM_REVIEW, 3
    );

    @Async
    @Transactional
    public void processReportForAutoModeration(Report report) {
        log.info("Processing report {} for auto-moderation", report.getId());

        try {
            // Check if report type is eligible for auto-moderation
            if (!isAutoModerationEligible(report.getReportType())) {
                log.debug("Report type {} not eligible for auto-moderation", report.getReportType());
                return;
            }

            // Analyze report content
            ModerationDecision decision = analyzeReportContent(report);

            // Apply auto-moderation rules
            if (decision.isAutoActionRequired()) {
                applyAutoModerationAction(report, decision);
            }

            // Check for mass reporting patterns
            checkMassReportingPattern(report);

            report.markAsAutoModerated();
            reportRepository.save(report);

        } catch (Exception e) {
            log.error("Error during auto-moderation processing for report {}", report.getId(), e);
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void runScheduledAutoModeration() {
        log.info("Running scheduled auto-moderation");

        try {
            List<ReportType> autoModeratedTypes = Arrays.asList(
                ReportType.SPAM_USER, ReportType.FAKE_LISTING, 
                ReportType.INAPPROPRIATE_CONTENT, ReportType.SPAM_REVIEW
            );

            List<Report> candidates = reportRepository.findCandidatesForAutoModeration(
                autoModeratedTypes, ReportStatus.PENDING
            );

            log.info("Found {} reports for auto-moderation", candidates.size());

            for (Report report : candidates) {
                processReportForAutoModeration(report);
            }

        } catch (Exception e) {
            log.error("Error during scheduled auto-moderation", e);
        }
    }

    private boolean isAutoModerationEligible(ReportType reportType) {
        return AUTO_ACTION_THRESHOLDS.containsKey(reportType);
    }

    private ModerationDecision analyzeReportContent(Report report) {
        ModerationDecision.ModerationDecisionBuilder decision = ModerationDecision.builder()
                .reportId(report.getId())
                .confidence(0.0);

        double confidence = 0.0;
        List<String> reasons = new ArrayList<>();

        // Content analysis
        if (containsSpamKeywords(report.getDescription())) {
            confidence += 0.3;
            reasons.add("Contains spam keywords");
        }

        if (containsInappropriateContent(report.getDescription())) {
            confidence += 0.4;
            reasons.add("Contains inappropriate content");
        }

        // Historical analysis
        Long reportCount = getReportCountForTarget(report.getTargetType(), report.getTargetId());
        if (reportCount >= AUTO_ACTION_THRESHOLDS.getOrDefault(report.getReportType(), 5)) {
            confidence += 0.5;
            reasons.add(String.format("Target has %d reports", reportCount));
        }

        // Reporter credibility
        double reporterCredibility = calculateReporterCredibility(report.getReporterId());
        confidence += reporterCredibility * 0.2;

        decision.confidence(confidence)
                .reasons(reasons)
                .autoActionRequired(confidence >= 0.7)
                .recommendedAction(determineRecommendedAction(report, confidence));

        return decision.build();
    }

    private boolean containsSpamKeywords(String content) {
        if (content == null) return false;
        String lowerContent = content.toLowerCase();
        return SPAM_KEYWORDS.stream().anyMatch(lowerContent::contains);
    }

    private boolean containsInappropriateContent(String content) {
        if (content == null) return false;
        return INAPPROPRIATE_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(content).find());
    }

    private Long getReportCountForTarget(String targetType, Long targetId) {
        List<com.hanihome.hanihome_au_api.domain.enums.ReportStatus> excludeStatuses = Arrays.asList(
            ReportStatus.DISMISSED, ReportStatus.DUPLICATE
        );
        return reportRepository.countReportsForTarget(targetType, targetId, excludeStatuses);
    }

    private double calculateReporterCredibility(Long reporterId) {
        // Calculate reporter credibility based on their reporting history
        // Higher credibility for users with accurate reports
        // Lower credibility for users with frequently dismissed reports
        
        // Placeholder implementation
        return 0.5; // Neutral credibility
    }

    private RecommendedAction determineRecommendedAction(Report report, double confidence) {
        if (confidence >= 0.9) {
            return RecommendedAction.IMMEDIATE_ACTION;
        } else if (confidence >= 0.7) {
            return RecommendedAction.WARNING;
        } else if (confidence >= 0.5) {
            return RecommendedAction.REVIEW_REQUIRED;
        } else {
            return RecommendedAction.NO_ACTION;
        }
    }

    @Transactional
    public void applyAutoModerationAction(Report report, ModerationDecision decision) {
        log.info("Applying auto-moderation action for report {} with confidence {}", 
                 report.getId(), decision.getConfidence());

        ReportAction.ActionType actionType;
        String actionDescription;

        switch (decision.getRecommendedAction()) {
            case IMMEDIATE_ACTION:
                if (report.getReportType() == ReportType.SPAM_USER) {
                    moderationActionService.suspendUser(report.getTargetId(), "Auto-moderation: Spam user detected", null);
                    actionType = ReportAction.ActionType.USER_SUSPENDED;
                    actionDescription = "User automatically suspended due to spam reports";
                    report.updateStatus(ReportStatus.USER_SUSPENDED);
                } else if (report.getReportType() == ReportType.FAKE_LISTING || 
                          report.getReportType() == ReportType.INAPPROPRIATE_CONTENT) {
                    moderationActionService.removeContent(report.getTargetType(), report.getTargetId(), 
                                                         "Auto-moderation: Inappropriate content", null);
                    actionType = ReportAction.ActionType.CONTENT_REMOVED;
                    actionDescription = "Content automatically removed due to inappropriate content reports";
                    report.updateStatus(ReportStatus.CONTENT_REMOVED);
                } else {
                    actionType = ReportAction.ActionType.AUTOMATED_ACTION;
                    actionDescription = "Automated action applied";
                }
                break;

            case WARNING:
                moderationActionService.sendWarning(report.getTargetType(), report.getTargetId(), 
                                                   "Warning: Multiple reports received", null);
                actionType = ReportAction.ActionType.WARNING_SENT;
                actionDescription = "Warning automatically sent due to multiple reports";
                report.updateStatus(ReportStatus.WARNING_ISSUED);
                break;

            case REVIEW_REQUIRED:
                report.updateStatus(ReportStatus.UNDER_REVIEW);
                actionType = ReportAction.ActionType.AUTOMATED_ACTION;
                actionDescription = "Flagged for manual review by auto-moderation";
                break;

            default:
                actionType = ReportAction.ActionType.AUTOMATED_ACTION;
                actionDescription = "Auto-moderation analysis completed - no action required";
                break;
        }

        // Record the auto-moderation action
        ReportAction action = ReportAction.builder()
                .report(report)
                .actionType(actionType)
                .description(actionDescription)
                .performedBy(null) // null indicates automated action
                .additionalDetails(String.format("Auto-moderation confidence: %.2f, Reasons: %s", 
                                                decision.getConfidence(), 
                                                String.join(", ", decision.getReasons())))
                .build();

        reportActionRepository.save(action);
        reportRepository.save(report);

        log.info("Auto-moderation action applied successfully for report {}", report.getId());
    }

    private void checkMassReportingPattern(Report report) {
        // Check if the same target is being mass-reported
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Long recentReportsCount = reportRepository.countByCreatedAtBetween(last24Hours, LocalDateTime.now());

        if (recentReportsCount >= 10) { // Threshold for mass reporting
            log.warn("Mass reporting pattern detected for target {}:{}", 
                     report.getTargetType(), report.getTargetId());
            
            // Escalate to human moderator
            report.escalate(null, "Mass reporting pattern detected");
            reportRepository.save(report);
        }
    }

    public enum RecommendedAction {
        NO_ACTION,
        REVIEW_REQUIRED,
        WARNING,
        IMMEDIATE_ACTION
    }

    public static class ModerationDecision {
        private final Long reportId;
        private final double confidence;
        private final List<String> reasons;
        private final boolean autoActionRequired;
        private final RecommendedAction recommendedAction;

        public ModerationDecision(Long reportId, double confidence, List<String> reasons, 
                                boolean autoActionRequired, RecommendedAction recommendedAction) {
            this.reportId = reportId;
            this.confidence = confidence;
            this.reasons = reasons != null ? reasons : new ArrayList<>();
            this.autoActionRequired = autoActionRequired;
            this.recommendedAction = recommendedAction;
        }

        public static ModerationDecisionBuilder builder() {
            return new ModerationDecisionBuilder();
        }

        // Getters
        public Long getReportId() { return reportId; }
        public double getConfidence() { return confidence; }
        public List<String> getReasons() { return reasons; }
        public boolean isAutoActionRequired() { return autoActionRequired; }
        public RecommendedAction getRecommendedAction() { return recommendedAction; }

        public static class ModerationDecisionBuilder {
            private Long reportId;
            private double confidence;
            private List<String> reasons;
            private boolean autoActionRequired;
            private RecommendedAction recommendedAction;

            public ModerationDecisionBuilder reportId(Long reportId) {
                this.reportId = reportId;
                return this;
            }

            public ModerationDecisionBuilder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }

            public ModerationDecisionBuilder reasons(List<String> reasons) {
                this.reasons = reasons;
                return this;
            }

            public ModerationDecisionBuilder autoActionRequired(boolean autoActionRequired) {
                this.autoActionRequired = autoActionRequired;
                return this;
            }

            public ModerationDecisionBuilder recommendedAction(RecommendedAction recommendedAction) {
                this.recommendedAction = recommendedAction;
                return this;
            }

            public ModerationDecision build() {
                return new ModerationDecision(reportId, confidence, reasons, autoActionRequired, recommendedAction);
            }
        }
    }
}