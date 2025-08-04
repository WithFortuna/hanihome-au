package com.hanihome.hanihome_au_api.application.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@hanihome.com.au}")
    private String fromEmail;

    @Value("${app.mail.from-name:HaniHome Australia}")
    private String fromName;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 뷰잉 예약 확인 이메일 전송
     */
    @Async
    public CompletableFuture<Boolean> sendViewingConfirmationEmail(
            String toEmail, 
            String recipientName, 
            Long viewingId,
            String propertyTitle,
            LocalDateTime scheduledAt,
            Integer durationMinutes,
            String landlordName,
            String contactPhone) {
        
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping confirmation email.");
            return CompletableFuture.completedFuture(true);
        }

        try {
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("propertyTitle", propertyTitle);
            context.setVariable("scheduledAt", scheduledAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
            context.setVariable("durationMinutes", durationMinutes);
            context.setVariable("landlordName", landlordName);
            context.setVariable("contactPhone", contactPhone);
            context.setVariable("frontendUrl", frontendUrl);
            context.setVariable("viewingId", viewingId);

            String htmlContent = templateEngine.process("email/viewing-confirmation", context);

            return sendEmail(
                toEmail,
                "뷰잉 예약이 확정되었습니다 - " + propertyTitle,
                htmlContent
            );

        } catch (Exception e) {
            log.error("Failed to send viewing confirmation email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 뷰잉 예약 취소 이메일 전송
     */
    @Async
    public CompletableFuture<Boolean> sendViewingCancellationEmail(
            String toEmail,
            String recipientName,
            String propertyTitle,
            LocalDateTime scheduledAt,
            String cancellationReason,
            String cancelledByName) {
        
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping cancellation email.");
            return CompletableFuture.completedFuture(true);
        }

        try {
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("propertyTitle", propertyTitle);
            context.setVariable("scheduledAt", scheduledAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
            context.setVariable("cancellationReason", cancellationReason);
            context.setVariable("cancelledByName", cancelledByName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("email/viewing-cancellation", context);

            return sendEmail(
                toEmail,
                "뷰잉 예약이 취소되었습니다 - " + propertyTitle,
                htmlContent
            );

        } catch (Exception e) {
            log.error("Failed to send viewing cancellation email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 뷰잉 예약 리마인더 이메일 전송
     */
    @Async
    public CompletableFuture<Boolean> sendViewingReminderEmail(
            String toEmail,
            String recipientName,
            String propertyTitle,
            LocalDateTime scheduledAt,
            String contactPhone,
            String address) {
        
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping reminder email.");
            return CompletableFuture.completedFuture(true);
        }

        try {
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("propertyTitle", propertyTitle);
            context.setVariable("scheduledAt", scheduledAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
            context.setVariable("contactPhone", contactPhone);
            context.setVariable("address", address);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("email/viewing-reminder", context);

            return sendEmail(
                toEmail,
                "뷰잉 예약 알림 - " + propertyTitle + " (내일 예정)",
                htmlContent
            );

        } catch (Exception e) {
            log.error("Failed to send viewing reminder email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 새로운 뷰잉 요청 알림 이메일 전송 (임대인/중개인용)
     */
    @Async
    public CompletableFuture<Boolean> sendNewViewingRequestEmail(
            String toEmail,
            String recipientName,
            String propertyTitle,
            LocalDateTime scheduledAt,
            String tenantName,
            String tenantPhone,
            String tenantNotes,
            Long viewingId) {
        
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping new viewing request email.");
            return CompletableFuture.completedFuture(true);
        }

        try {
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("propertyTitle", propertyTitle);
            context.setVariable("scheduledAt", scheduledAt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
            context.setVariable("tenantName", tenantName);
            context.setVariable("tenantPhone", tenantPhone);
            context.setVariable("tenantNotes", tenantNotes);
            context.setVariable("frontendUrl", frontendUrl);
            context.setVariable("viewingId", viewingId);

            String htmlContent = templateEngine.process("email/new-viewing-request", context);

            return sendEmail(
                toEmail,
                "새로운 뷰잉 요청 - " + propertyTitle,
                htmlContent
            );

        } catch (Exception e) {
            log.error("Failed to send new viewing request email to {}: {}", toEmail, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 일반적인 이메일 전송 메서드
     */
    private CompletableFuture<Boolean> sendEmail(String toEmail, String subject, String htmlContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, fromName);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Email sent successfully to: {}, subject: {}", toEmail, subject);
                return true;

            } catch (MessagingException e) {
                log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
                return false;
            } catch (Exception e) {
                log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * 이메일 발송 상태 확인
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}