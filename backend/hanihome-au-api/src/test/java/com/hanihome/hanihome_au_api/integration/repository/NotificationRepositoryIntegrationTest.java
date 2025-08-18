package com.hanihome.hanihome_au_api.integration.repository;

import com.hanihome.hanihome_au_api.domain.notification.entity.Notification;
import com.hanihome.hanihome_au_api.domain.notification.valueobject.NotificationId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.enums.NotificationType;
import com.hanihome.hanihome_au_api.domain.enums.NotificationPriority;
import com.hanihome.hanihome_au_api.infrastructure.persistence.notification.NotificationRepository;
import com.hanihome.hanihome_au_api.testutil.PostgreSQLContainerConfig;
import com.hanihome.hanihome_au_api.testutil.NotificationTestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * NotificationRepository Integration Tests
 * 
 * Tests the NotificationRepository implementation with actual PostgreSQL database
 * using Testcontainers to verify notification data access operations and queries.
 */
@DataJpaTest
@Import(PostgreSQLContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration-test")
@DisplayName("NotificationRepository Integration Tests")
class NotificationRepositoryIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private UserId userId1;
    private UserId userId2;
    private Notification unreadNotification;
    private Notification readNotification;
    private Notification highPriorityNotification;
    private Notification propertyNotification;
    private Notification systemNotification;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        
        userId1 = new UserId(UUID.randomUUID().toString());
        userId2 = new UserId(UUID.randomUUID().toString());
        
        // Create test notifications for different scenarios
        unreadNotification = NotificationTestDataFactory.createDefaultNotification()
            .withRecipient(userId1)
            .withTitle("Unread Notification")
            .withMessage("This notification has not been read yet")
            .withType(NotificationType.PROPERTY_STATUS_CHANGED)
            .withPriority(NotificationPriority.MEDIUM)
            .build();
        
        readNotification = NotificationTestDataFactory.createDefaultNotification()
            .withRecipient(userId1)
            .withTitle("Read Notification")
            .withMessage("This notification has been read")
            .withType(NotificationType.MESSAGE_RECEIVED)
            .withPriority(NotificationPriority.LOW)
            .build();
        readNotification.markAsRead(); // Mark as read
        
        highPriorityNotification = NotificationTestDataFactory.createDefaultNotification()
            .withRecipient(userId1)
            .withTitle("High Priority Alert")
            .withMessage("This is a high priority notification")
            .withType(NotificationType.SECURITY_ALERT)
            .withPriority(NotificationPriority.HIGH)
            .build();
        
        propertyNotification = NotificationTestDataFactory.createDefaultNotification()
            .withRecipient(userId2)
            .withTitle("Property Update")
            .withMessage("Your property has been approved")
            .withType(NotificationType.PROPERTY_STATUS_CHANGED)
            .withPriority(NotificationPriority.MEDIUM)
            .withRelatedEntityId("property-123")
            .build();
        
        systemNotification = NotificationTestDataFactory.createDefaultNotification()
            .withRecipient(userId2)
            .withTitle("System Maintenance")
            .withMessage("Scheduled maintenance tonight")
            .withType(NotificationType.SYSTEM_ANNOUNCEMENT)
            .withPriority(NotificationPriority.LOW)
            .build();
        
        // Save all test notifications
        notificationRepository.saveAll(List.of(
            unreadNotification, readNotification, highPriorityNotification,
            propertyNotification, systemNotification
        ));
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperationsTests {

        @Test
        @DisplayName("Should save and find notification by ID")
        void should_SaveAndFindNotificationById() {
            // Arrange
            Notification newNotification = NotificationTestDataFactory.createDefaultNotification()
                .withRecipient(userId1)
                .withTitle("New Test Notification")
                .withMessage("This is a new test notification")
                .build();

            // Act
            Notification savedNotification = notificationRepository.save(newNotification);
            Optional<Notification> foundNotification = notificationRepository.findById(savedNotification.getId());

            // Assert
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get().getId()).isEqualTo(savedNotification.getId());
            assertThat(foundNotification.get().getTitle()).isEqualTo(newNotification.getTitle());
        }

        @Test
        @DisplayName("Should update notification and maintain version control")
        void should_UpdateNotificationAndMaintainVersionControl() {
            // Arrange
            Long initialVersion = unreadNotification.getVersion();

            // Act
            unreadNotification.markAsRead();
            Notification updatedNotification = notificationRepository.save(unreadNotification);

            // Assert
            assertThat(updatedNotification.isRead()).isTrue();
            assertThat(updatedNotification.getReadAt()).isNotNull();
            assertThat(updatedNotification.getVersion()).isGreaterThan(initialVersion);
        }

        @Test
        @DisplayName("Should delete notification by ID")
        void should_DeleteNotificationById() {
            // Arrange
            NotificationId notificationId = unreadNotification.getId();

            // Act
            notificationRepository.deleteById(notificationId);
            Optional<Notification> deletedNotification = notificationRepository.findById(notificationId);

            // Assert
            assertThat(deletedNotification).isEmpty();
        }

        @Test
        @DisplayName("Should count total notifications")
        void should_CountTotalNotifications() {
            // Act
            long notificationCount = notificationRepository.count();

            // Assert
            assertThat(notificationCount).isEqualTo(5); // 5 test notifications created in setUp
        }
    }

    @Nested
    @DisplayName("User-based Query Operations")
    class UserBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find notifications by recipient")
        void should_FindNotificationsByRecipient() {
            // Act
            List<Notification> user1Notifications = notificationRepository.findByRecipientId(userId1);
            List<Notification> user2Notifications = notificationRepository.findByRecipientId(userId2);

            // Assert
            assertThat(user1Notifications).hasSize(3); // unread, read, highPriority
            assertThat(user2Notifications).hasSize(2); // property, system
            
            // Verify all notifications belong to correct user
            assertThat(user1Notifications).allMatch(n -> n.getRecipientId().equals(userId1));
            assertThat(user2Notifications).allMatch(n -> n.getRecipientId().equals(userId2));
        }

        @Test
        @DisplayName("Should find notifications by recipient with pagination")
        void should_FindNotificationsByRecipientWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

            // Act
            Page<Notification> notificationPage = notificationRepository.findByRecipientId(userId1, pageable);

            // Assert
            assertThat(notificationPage.getContent()).hasSize(2);
            assertThat(notificationPage.getTotalElements()).isEqualTo(3);
            assertThat(notificationPage.getTotalPages()).isEqualTo(2);
            
            // Verify descending order by creation date
            List<Notification> notifications = notificationPage.getContent();
            if (notifications.size() > 1) {
                assertThat(notifications.get(0).getCreatedAt())
                    .isAfterOrEqualTo(notifications.get(1).getCreatedAt());
            }
        }

        @Test
        @DisplayName("Should count notifications by recipient")
        void should_CountNotificationsByRecipient() {
            // Act
            long user1Count = notificationRepository.countByRecipientId(userId1);
            long user2Count = notificationRepository.countByRecipientId(userId2);

            // Assert
            assertThat(user1Count).isEqualTo(3);
            assertThat(user2Count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Read Status Query Operations")
    class ReadStatusQueryOperationsTests {

        @Test
        @DisplayName("Should find unread notifications")
        void should_FindUnreadNotifications() {
            // Act
            List<Notification> unreadNotifications = notificationRepository.findByIsReadFalse();

            // Assert
            assertThat(unreadNotifications).hasSize(4); // All except readNotification
            assertThat(unreadNotifications).noneMatch(Notification::isRead);
        }

        @Test
        @DisplayName("Should find read notifications")
        void should_FindReadNotifications() {
            // Act
            List<Notification> readNotifications = notificationRepository.findByIsReadTrue();

            // Assert
            assertThat(readNotifications).hasSize(1); // Only readNotification
            assertThat(readNotifications).allMatch(Notification::isRead);
            assertThat(readNotifications.get(0).getId()).isEqualTo(readNotification.getId());
        }

        @Test
        @DisplayName("Should find unread notifications by recipient")
        void should_FindUnreadNotificationsByRecipient() {
            // Act
            List<Notification> user1Unread = notificationRepository.findByRecipientIdAndIsReadFalse(userId1);
            List<Notification> user2Unread = notificationRepository.findByRecipientIdAndIsReadFalse(userId2);

            // Assert
            assertThat(user1Unread).hasSize(2); // unread, highPriority
            assertThat(user2Unread).hasSize(2); // property, system
            assertThat(user1Unread).noneMatch(Notification::isRead);
            assertThat(user2Unread).noneMatch(Notification::isRead);
        }

        @Test
        @DisplayName("Should count unread notifications by recipient")
        void should_CountUnreadNotificationsByRecipient() {
            // Act
            long user1UnreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId1);
            long user2UnreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId2);

            // Assert
            assertThat(user1UnreadCount).isEqualTo(2);
            assertThat(user2UnreadCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Should mark multiple notifications as read")
        void should_MarkMultipleNotificationsAsRead() {
            // Arrange
            List<NotificationId> notificationIds = List.of(
                unreadNotification.getId(),
                highPriorityNotification.getId()
            );

            // Act
            int updatedCount = notificationRepository.markAsReadByIds(notificationIds);

            // Assert
            assertThat(updatedCount).isEqualTo(2);
            
            // Verify changes
            Optional<Notification> updatedUnread = notificationRepository.findById(unreadNotification.getId());
            Optional<Notification> updatedHighPriority = notificationRepository.findById(highPriorityNotification.getId());
            
            assertThat(updatedUnread).isPresent();
            assertThat(updatedUnread.get().isRead()).isTrue();
            assertThat(updatedHighPriority).isPresent();
            assertThat(updatedHighPriority.get().isRead()).isTrue();
        }
    }

    @Nested
    @DisplayName("Type-based Query Operations")
    class TypeBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find notifications by type")
        void should_FindNotificationsByType() {
            // Act
            List<Notification> propertyNotifications = notificationRepository.findByType(NotificationType.PROPERTY_STATUS_CHANGED);
            List<Notification> securityNotifications = notificationRepository.findByType(NotificationType.SECURITY_ALERT);
            List<Notification> systemNotifications = notificationRepository.findByType(NotificationType.SYSTEM_ANNOUNCEMENT);

            // Assert
            assertThat(propertyNotifications).hasSize(2); // unread, property
            assertThat(securityNotifications).hasSize(1); // highPriority
            assertThat(systemNotifications).hasSize(1); // system
        }

        @Test
        @DisplayName("Should find notifications by recipient and type")
        void should_FindNotificationsByRecipientAndType() {
            // Act
            List<Notification> user1PropertyNotifications = notificationRepository.findByRecipientIdAndType(
                userId1, NotificationType.PROPERTY_STATUS_CHANGED);
            List<Notification> user2PropertyNotifications = notificationRepository.findByRecipientIdAndType(
                userId2, NotificationType.PROPERTY_STATUS_CHANGED);

            // Assert
            assertThat(user1PropertyNotifications).hasSize(1); // unread
            assertThat(user2PropertyNotifications).hasSize(1); // property
        }

        @Test
        @DisplayName("Should find notifications by multiple types")
        void should_FindNotificationsByMultipleTypes() {
            // Arrange
            List<NotificationType> importantTypes = List.of(
                NotificationType.SECURITY_ALERT,
                NotificationType.PROPERTY_STATUS_CHANGED
            );

            // Act
            List<Notification> importantNotifications = notificationRepository.findByTypeIn(importantTypes);

            // Assert
            assertThat(importantNotifications).hasSize(3); // unread, highPriority, property
            assertThat(importantNotifications).allMatch(n -> importantTypes.contains(n.getType()));
        }
    }

    @Nested
    @DisplayName("Priority-based Query Operations")
    class PriorityBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find notifications by priority")
        void should_FindNotificationsByPriority() {
            // Act
            List<Notification> highPriorityNotifications = notificationRepository.findByPriority(NotificationPriority.HIGH);
            List<Notification> mediumPriorityNotifications = notificationRepository.findByPriority(NotificationPriority.MEDIUM);
            List<Notification> lowPriorityNotifications = notificationRepository.findByPriority(NotificationPriority.LOW);

            // Assert
            assertThat(highPriorityNotifications).hasSize(1); // highPriority
            assertThat(mediumPriorityNotifications).hasSize(2); // unread, property
            assertThat(lowPriorityNotifications).hasSize(2); // read, system
        }

        @Test
        @DisplayName("Should find high priority unread notifications")
        void should_FindHighPriorityUnreadNotifications() {
            // Act
            List<Notification> urgentNotifications = notificationRepository.findByPriorityAndIsReadFalse(NotificationPriority.HIGH);

            // Assert
            assertThat(urgentNotifications).hasSize(1);
            assertThat(urgentNotifications.get(0).getId()).isEqualTo(highPriorityNotification.getId());
            assertThat(urgentNotifications.get(0).isRead()).isFalse();
        }

        @Test
        @DisplayName("Should find notifications by recipient and priority")
        void should_FindNotificationsByRecipientAndPriority() {
            // Act
            List<Notification> user1HighPriority = notificationRepository.findByRecipientIdAndPriority(
                userId1, NotificationPriority.HIGH);

            // Assert
            assertThat(user1HighPriority).hasSize(1);
            assertThat(user1HighPriority.get(0).getId()).isEqualTo(highPriorityNotification.getId());
        }
    }

    @Nested
    @DisplayName("Date Range Query Operations")
    class DateRangeQueryOperationsTests {

        @Test
        @DisplayName("Should find notifications created within date range")
        void should_FindNotificationsCreatedWithinDateRange() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // Act
            List<Notification> recentNotifications = notificationRepository.findByCreatedAtBetween(startDate, endDate);

            // Assert
            assertThat(recentNotifications).hasSize(5); // All test notifications created today
        }

        @Test
        @DisplayName("Should find notifications created after specific date")
        void should_FindNotificationsCreatedAfterSpecificDate() {
            // Arrange
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

            // Act
            List<Notification> recentNotifications = notificationRepository.findByCreatedAtAfter(yesterday);

            // Assert
            assertThat(recentNotifications).hasSize(5); // All test notifications created today
        }

        @Test
        @DisplayName("Should find notifications read within time period")
        void should_FindNotificationsReadWithinTimePeriod() {
            // Arrange
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            // Act
            List<Notification> recentlyRead = notificationRepository.findByReadAtAfter(oneHourAgo);

            // Assert
            assertThat(recentlyRead).hasSize(1); // Only readNotification
            assertThat(recentlyRead.get(0).getId()).isEqualTo(readNotification.getId());
        }
    }

    @Nested
    @DisplayName("Related Entity Query Operations")
    class RelatedEntityQueryOperationsTests {

        @Test
        @DisplayName("Should find notifications by related entity ID")
        void should_FindNotificationsByRelatedEntityId() {
            // Act
            List<Notification> propertyRelatedNotifications = notificationRepository.findByRelatedEntityId("property-123");

            // Assert
            assertThat(propertyRelatedNotifications).hasSize(1);
            assertThat(propertyRelatedNotifications.get(0).getId()).isEqualTo(propertyNotification.getId());
        }

        @Test
        @DisplayName("Should find notifications with related entity")
        void should_FindNotificationsWithRelatedEntity() {
            // Act
            List<Notification> notificationsWithEntity = notificationRepository.findByRelatedEntityIdIsNotNull();

            // Assert
            assertThat(notificationsWithEntity).hasSize(1); // Only propertyNotification has related entity
            assertThat(notificationsWithEntity.get(0).getRelatedEntityId()).isEqualTo("property-123");
        }

        @Test
        @DisplayName("Should find notifications without related entity")
        void should_FindNotificationsWithoutRelatedEntity() {
            // Act
            List<Notification> notificationsWithoutEntity = notificationRepository.findByRelatedEntityIdIsNull();

            // Assert
            assertThat(notificationsWithoutEntity).hasSize(4); // All except propertyNotification
        }
    }

    @Nested
    @DisplayName("Complex Query Operations")
    class ComplexQueryOperationsTests {

        @Test
        @DisplayName("Should find unread high priority notifications for user")
        void should_FindUnreadHighPriorityNotificationsForUser() {
            // Act
            List<Notification> urgentUnread = notificationRepository.findByRecipientIdAndPriorityAndIsReadFalse(
                userId1, NotificationPriority.HIGH);

            // Assert
            assertThat(urgentUnread).hasSize(1);
            assertThat(urgentUnread.get(0).getId()).isEqualTo(highPriorityNotification.getId());
        }

        @Test
        @DisplayName("Should find recent unread notifications by type")
        void should_FindRecentUnreadNotificationsByType() {
            // Arrange
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            // Act
            List<Notification> recentUnreadProperty = notificationRepository
                .findByTypeAndIsReadFalseAndCreatedAtAfter(
                    NotificationType.PROPERTY_STATUS_CHANGED, oneHourAgo);

            // Assert
            assertThat(recentUnreadProperty).hasSize(2); // unread, property
        }

        @Test
        @DisplayName("Should get notification summary by user")
        void should_GetNotificationSummaryByUser() {
            // Act
            long totalUser1 = notificationRepository.countByRecipientId(userId1);
            long unreadUser1 = notificationRepository.countByRecipientIdAndIsReadFalse(userId1);
            long highPriorityUser1 = notificationRepository.countByRecipientIdAndPriority(userId1, NotificationPriority.HIGH);

            // Assert
            assertThat(totalUser1).isEqualTo(3);
            assertThat(unreadUser1).isEqualTo(2);
            assertThat(highPriorityUser1).isEqualTo(1);
        }

        @Test
        @DisplayName("Should perform bulk operations efficiently")
        void should_PerformBulkOperationsEfficiently() {
            // Arrange
            List<NotificationId> allNotificationIds = List.of(
                unreadNotification.getId(),
                readNotification.getId(),
                highPriorityNotification.getId(),
                propertyNotification.getId(),
                systemNotification.getId()
            );

            // Act - Bulk mark as read
            int updatedCount = notificationRepository.markAsReadByIds(allNotificationIds);

            // Assert
            assertThat(updatedCount).isEqualTo(4); // readNotification was already read
            
            // Verify all are now read
            List<Notification> allNotifications = notificationRepository.findAll();
            assertThat(allNotifications).allMatch(Notification::isRead);
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting Operations")
    class PaginationAndSortingOperationsTests {

        @Test
        @DisplayName("Should paginate notifications correctly")
        void should_PaginateNotificationsCorrectly() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 3); // First page, 3 items per page

            // Act
            Page<Notification> notificationPage = notificationRepository.findAll(pageable);

            // Assert
            assertThat(notificationPage.getContent()).hasSize(3);
            assertThat(notificationPage.getTotalElements()).isEqualTo(5);
            assertThat(notificationPage.getTotalPages()).isEqualTo(2);
            assertThat(notificationPage.isFirst()).isTrue();
            assertThat(notificationPage.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should sort notifications by priority")
        void should_SortNotificationsByPriority() {
            // Arrange - Custom sorting by priority (HIGH > MEDIUM > LOW)
            Sort sort = Sort.by(Sort.Direction.DESC, "priority");
            Pageable pageable = PageRequest.of(0, 10, sort);

            // Act
            Page<Notification> sortedNotifications = notificationRepository.findAll(pageable);

            // Assert
            List<Notification> notifications = sortedNotifications.getContent();
            assertThat(notifications).hasSize(5);
            
            // Find high priority notification should be first
            assertThat(notifications.get(0).getPriority()).isEqualTo(NotificationPriority.HIGH);
        }

        @Test
        @DisplayName("Should paginate user notifications with custom sorting")
        void should_PaginateUserNotificationsWithCustomSorting() {
            // Arrange
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("priority"));
            Pageable pageable = PageRequest.of(0, 2, sort);

            // Act
            Page<Notification> userNotifications = notificationRepository.findByRecipientId(userId1, pageable);

            // Assert
            assertThat(userNotifications.getContent()).hasSize(2);
            assertThat(userNotifications.getTotalElements()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Data Cleanup Operations")
    class DataCleanupOperationsTests {

        @Test
        @DisplayName("Should delete old read notifications")
        void should_DeleteOldReadNotifications() {
            // Arrange
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

            // Act
            int deletedCount = notificationRepository.deleteByIsReadTrueAndReadAtBefore(cutoffDate);

            // Assert - Since our test data is recent, should not delete any
            assertThat(deletedCount).isEqualTo(0);
            
            // Verify read notification still exists
            Optional<Notification> stillExists = notificationRepository.findById(readNotification.getId());
            assertThat(stillExists).isPresent();
        }

        @Test
        @DisplayName("Should delete notifications by recipient")
        void should_DeleteNotificationsByRecipient() {
            // Act
            int deletedCount = notificationRepository.deleteByRecipientId(userId1);

            // Assert
            assertThat(deletedCount).isEqualTo(3); // user1 had 3 notifications
            
            // Verify user1 notifications are gone
            List<Notification> remainingUser1Notifications = notificationRepository.findByRecipientId(userId1);
            assertThat(remainingUser1Notifications).isEmpty();
            
            // Verify user2 notifications still exist
            List<Notification> user2Notifications = notificationRepository.findByRecipientId(userId2);
            assertThat(user2Notifications).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large notification sets efficiently")
        void should_HandleLargeNotificationSetsEfficiently() {
            // Arrange - Create many notifications
            List<Notification> manyNotifications = createManyTestNotifications(100);
            notificationRepository.saveAll(manyNotifications);

            // Act
            long startTime = System.currentTimeMillis();
            long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(userId1);
            long endTime = System.currentTimeMillis();

            // Assert
            assertThat(unreadCount).isGreaterThan(0);
            assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
        }

        @Test
        @DisplayName("Should optimize notification queries")
        void should_OptimizeNotificationQueries() {
            // Arrange
            List<Notification> manyNotifications = createManyTestNotifications(50);
            notificationRepository.saveAll(manyNotifications);
            
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            long startTime = System.currentTimeMillis();
            Page<Notification> page = notificationRepository.findByRecipientIdAndIsReadFalse(userId1, pageable);
            long endTime = System.currentTimeMillis();

            // Assert
            assertThat(page.getContent()).hasSizeGreaterThan(0);
            assertThat(endTime - startTime).isLessThan(500); // Should be fast for filtered pagination
        }

        private List<Notification> createManyTestNotifications(int count) {
            List<Notification> notifications = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                Notification notification = NotificationTestDataFactory.createDefaultNotification()
                    .withRecipient(userId1)
                    .withTitle("Test Notification " + i)
                    .withMessage("Test message " + i)
                    .withType(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .build();
                notifications.add(notification);
            }
            return notifications;
        }
    }

    @Test
    @Sql("/cleanup-data.sql")
    @DisplayName("Should clean up test data after SQL operations")
    void should_CleanUpTestDataAfterSqlOperations() {
        // This test verifies that SQL cleanup scripts work correctly
        long notificationCount = notificationRepository.count();
        assertThat(notificationCount).isEqualTo(0);
    }
}