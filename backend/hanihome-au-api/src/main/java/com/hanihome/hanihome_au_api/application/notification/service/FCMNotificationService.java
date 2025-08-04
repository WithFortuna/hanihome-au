package com.hanihome.hanihome_au_api.application.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 단일 토큰으로 알림 전송
     */
    public CompletableFuture<String> sendNotification(String token, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("Firebase messaging not initialized. Skipping notification send.");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());

                // 데이터 페이로드 추가
                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                // 웹 푸시 설정
                messageBuilder.setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setIcon("/icon-192x192.png")
                                .setBadge("/badge-72x72.png")
                                .build())
                        .build());

                Message message = messageBuilder.build();
                String response = firebaseMessaging.send(message);
                
                log.info("Successfully sent FCM notification. Message ID: {}", response);
                return response;
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM notification to token: {}, error: {}", token, e.getMessage(), e);
                throw new RuntimeException("FCM notification send failed", e);
            }
        });
    }

    /**
     * 여러 토큰으로 알림 전송 (배치)
     */
    public CompletableFuture<BatchResponse> sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("Firebase messaging not initialized. Skipping multicast notification send.");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                        .addAllTokens(tokens)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());

                // 데이터 페이로드 추가
                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                // 웹 푸시 설정
                messageBuilder.setWebpushConfig(WebpushConfig.builder()
                        .setNotification(WebpushNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setIcon("/icon-192x192.png")
                                .setBadge("/badge-72x72.png")
                                .build())
                        .build());

                MulticastMessage message = messageBuilder.build();
                BatchResponse response = firebaseMessaging.sendMulticast(message);
                
                log.info("Successfully sent FCM multicast notification. Success count: {}, Failure count: {}", 
                    response.getSuccessCount(), response.getFailureCount());
                
                return response;
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM multicast notification, error: {}", e.getMessage(), e);
                throw new RuntimeException("FCM multicast notification send failed", e);
            }
        });
    }

    /**
     * 뷰잉 예약 관련 알림 전송
     */
    public CompletableFuture<String> sendViewingNotification(String token, String notificationType, Long viewingId, String propertyTitle) {
        String title = getViewingNotificationTitle(notificationType);
        String body = getViewingNotificationBody(notificationType, propertyTitle);
        
        Map<String, String> data = Map.of(
                "type", "viewing",
                "action", notificationType,
                "viewingId", viewingId.toString(),
                "redirectUrl", "/viewing/" + viewingId
        );

        return sendNotification(token, title, body, data);
    }

    private String getViewingNotificationTitle(String notificationType) {
        return switch (notificationType) {
            case "CREATED" -> "뷰잉 예약 확인";
            case "CONFIRMED" -> "뷰잉 예약 확정";
            case "CANCELLED" -> "뷰잉 예약 취소";
            case "REMINDER" -> "뷰잉 예약 알림";
            case "UPDATED" -> "뷰잉 예약 변경";
            default -> "뷰잉 알림";
        };
    }

    private String getViewingNotificationBody(String notificationType, String propertyTitle) {
        return switch (notificationType) {
            case "CREATED" -> propertyTitle + " 매물의 뷰잉 예약이 접수되었습니다.";
            case "CONFIRMED" -> propertyTitle + " 매물의 뷰잉 예약이 확정되었습니다.";
            case "CANCELLED" -> propertyTitle + " 매물의 뷰잉 예약이 취소되었습니다.";
            case "REMINDER" -> propertyTitle + " 매물의 뷰잉 예약 시간이 다가오고 있습니다.";
            case "UPDATED" -> propertyTitle + " 매물의 뷰잉 예약 정보가 변경되었습니다.";
            default -> propertyTitle + " 매물에 대한 뷰잉 알림이 있습니다.";
        };
    }
}