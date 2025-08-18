package com.hanihome.hanihome_au_api.integration.repository;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.enums.UserStatus;
import com.hanihome.hanihome_au_api.domain.enums.Role;
import com.hanihome.hanihome_au_api.infrastructure.persistence.user.UserRepository;
import com.hanihome.hanihome_au_api.testutil.PostgreSQLContainerConfig;
import com.hanihome.hanihome_au_api.testutil.UserTestDataFactory;

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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * UserRepository Integration Tests
 * 
 * Tests the UserRepository implementation with actual PostgreSQL database
 * using Testcontainers to verify data access operations and custom queries.
 */
@DataJpaTest
@Import(PostgreSQLContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration-test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User activeUser;
    private User pendingUser;
    private User suspendedUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create test users with different states
        activeUser = UserTestDataFactory.createDefaultUser()
            .withEmail(new Email("active@example.com"))
            .withFirstName("Active")
            .withLastName("User")
            .withPhoneNumber("+61-400-111-111")
            .withStatus(UserStatus.ACTIVE)
            .build();
        
        pendingUser = UserTestDataFactory.createDefaultUser()
            .withEmail(new Email("pending@example.com"))
            .withFirstName("Pending")
            .withLastName("User")
            .withPhoneNumber("+61-400-222-222")
            .withStatus(UserStatus.PENDING_VERIFICATION)
            .build();
        
        suspendedUser = UserTestDataFactory.createDefaultUser()
            .withEmail(new Email("suspended@example.com"))
            .withFirstName("Suspended")
            .withLastName("User")
            .withPhoneNumber("+61-400-333-333")
            .withStatus(UserStatus.SUSPENDED)
            .build();
        
        adminUser = UserTestDataFactory.createDefaultUser()
            .withEmail(new Email("admin@example.com"))
            .withFirstName("Admin")
            .withLastName("User")
            .withPhoneNumber("+61-400-444-444")
            .withStatus(UserStatus.ACTIVE)
            .withRole(Role.ADMIN)
            .build();
        
        // Save all test users
        userRepository.saveAll(List.of(activeUser, pendingUser, suspendedUser, adminUser));
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperationsTests {

        @Test
        @DisplayName("Should save and find user by ID")
        void should_SaveAndFindUserById() {
            // Arrange
            User newUser = UserTestDataFactory.createDefaultUser()
                .withEmail(new Email("newuser@example.com"))
                .build();

            // Act
            User savedUser = userRepository.save(newUser);
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
            assertThat(foundUser.get().getEmail()).isEqualTo(newUser.getEmail());
        }

        @Test
        @DisplayName("Should update user and maintain version control")
        void should_UpdateUserAndMaintainVersionControl() {
            // Arrange
            Long initialVersion = activeUser.getVersion();
            String newFirstName = "Updated First Name";

            // Act
            activeUser.updateProfile(newFirstName, activeUser.getLastName(), activeUser.getPhoneNumber());
            User updatedUser = userRepository.save(activeUser);

            // Assert
            assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
            assertThat(updatedUser.getVersion()).isGreaterThan(initialVersion);
            assertThat(updatedUser.getUpdatedAt()).isAfter(activeUser.getCreatedAt());
        }

        @Test
        @DisplayName("Should delete user by ID")
        void should_DeleteUserById() {
            // Arrange
            UserId userId = activeUser.getId();

            // Act
            userRepository.deleteById(userId);
            Optional<User> deletedUser = userRepository.findById(userId);

            // Assert
            assertThat(deletedUser).isEmpty();
        }

        @Test
        @DisplayName("Should count total users")
        void should_CountTotalUsers() {
            // Act
            long userCount = userRepository.count();

            // Assert
            assertThat(userCount).isEqualTo(4); // 4 test users created in setUp
        }
    }

    @Nested
    @DisplayName("Email-based Query Operations")
    class EmailBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find user by email successfully")
        void should_FindUserByEmailSuccessfully() {
            // Act
            Optional<User> foundUser = userRepository.findByEmail(activeUser.getEmail());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getId()).isEqualTo(activeUser.getId());
            assertThat(foundUser.get().getEmail()).isEqualTo(activeUser.getEmail());
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void should_ReturnEmptyWhenEmailNotFound() {
            // Arrange
            Email nonExistentEmail = new Email("nonexistent@example.com");

            // Act
            Optional<User> foundUser = userRepository.findByEmail(nonExistentEmail);

            // Assert
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should check if email exists")
        void should_CheckIfEmailExists() {
            // Act & Assert
            assertThat(userRepository.existsByEmail(activeUser.getEmail())).isTrue();
            assertThat(userRepository.existsByEmail(new Email("nonexistent@example.com"))).isFalse();
        }

        @Test
        @DisplayName("Should find users by email domain")
        void should_FindUsersByEmailDomain() {
            // Act
            List<User> exampleUsers = userRepository.findByEmailContaining("@example.com");

            // Assert
            assertThat(exampleUsers).hasSize(4);
            assertThat(exampleUsers).extracting(User::getEmail)
                .allSatisfy(email -> assertThat(email.getValue()).contains("@example.com"));
        }
    }

    @Nested
    @DisplayName("Status-based Query Operations")
    class StatusBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find users by status")
        void should_FindUsersByStatus() {
            // Act
            List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
            List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING_VERIFICATION);
            List<User> suspendedUsers = userRepository.findByStatus(UserStatus.SUSPENDED);

            // Assert
            assertThat(activeUsers).hasSize(2); // activeUser and adminUser
            assertThat(pendingUsers).hasSize(1); // pendingUser
            assertThat(suspendedUsers).hasSize(1); // suspendedUser
        }

        @Test
        @DisplayName("Should find active users only")
        void should_FindActiveUsersOnly() {
            // Act
            List<User> activeUsers = userRepository.findActiveUsers();

            // Assert
            assertThat(activeUsers).hasSize(2);
            assertThat(activeUsers).extracting(User::getStatus)
                .allSatisfy(status -> assertThat(status).isEqualTo(UserStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should count users by status")
        void should_CountUsersByStatus() {
            // Act
            long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
            long pendingCount = userRepository.countByStatus(UserStatus.PENDING_VERIFICATION);
            long suspendedCount = userRepository.countByStatus(UserStatus.SUSPENDED);

            // Assert
            assertThat(activeCount).isEqualTo(2);
            assertThat(pendingCount).isEqualTo(1);
            assertThat(suspendedCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Role-based Query Operations")
    class RoleBasedQueryOperationsTests {

        @Test
        @DisplayName("Should find users by role")
        void should_FindUsersByRole() {
            // Act
            List<User> adminUsers = userRepository.findByRole(Role.ADMIN);
            List<User> regularUsers = userRepository.findByRole(Role.USER);

            // Assert
            assertThat(adminUsers).hasSize(1);
            assertThat(adminUsers.get(0).getId()).isEqualTo(adminUser.getId());
            assertThat(regularUsers).hasSize(3); // Other users default to USER role
        }

        @Test
        @DisplayName("Should find users with any of specified roles")
        void should_FindUsersWithAnyOfSpecifiedRoles() {
            // Arrange
            Set<Role> adminRoles = Set.of(Role.ADMIN, Role.MODERATOR);

            // Act
            List<User> adminUsers = userRepository.findByRoleIn(adminRoles);

            // Assert
            assertThat(adminUsers).hasSize(1);
            assertThat(adminUsers.get(0).hasRole(Role.ADMIN)).isTrue();
        }

        @Test
        @DisplayName("Should find active users with specific role")
        void should_FindActiveUsersWithSpecificRole() {
            // Act
            List<User> activeAdmins = userRepository.findByStatusAndRole(UserStatus.ACTIVE, Role.ADMIN);

            // Assert
            assertThat(activeAdmins).hasSize(1);
            assertThat(activeAdmins.get(0).getId()).isEqualTo(adminUser.getId());
            assertThat(activeAdmins.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(activeAdmins.get(0).hasRole(Role.ADMIN)).isTrue();
        }
    }

    @Nested
    @DisplayName("Date Range Query Operations")
    class DateRangeQueryOperationsTests {

        @Test
        @DisplayName("Should find users created within date range")
        void should_FindUsersCreatedWithinDateRange() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // Act
            List<User> recentUsers = userRepository.findByCreatedAtBetween(startDate, endDate);

            // Assert
            assertThat(recentUsers).hasSize(4); // All test users created today
        }

        @Test
        @DisplayName("Should find users created after specific date")
        void should_FindUsersCreatedAfterSpecificDate() {
            // Arrange
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

            // Act
            List<User> recentUsers = userRepository.findByCreatedAtAfter(yesterday);

            // Assert
            assertThat(recentUsers).hasSize(4); // All test users created today
        }

        @Test
        @DisplayName("Should find recently updated users")
        void should_FindRecentlyUpdatedUsers() {
            // Arrange - Update one user
            activeUser.updateProfile("Updated Name", activeUser.getLastName(), activeUser.getPhoneNumber());
            userRepository.save(activeUser);
            
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

            // Act
            List<User> recentlyUpdated = userRepository.findByUpdatedAtAfter(fiveMinutesAgo);

            // Assert
            assertThat(recentlyUpdated).hasSizeGreaterThanOrEqualTo(1);
            assertThat(recentlyUpdated).anyMatch(user -> user.getId().equals(activeUser.getId()));
        }
    }

    @Nested
    @DisplayName("Text Search Operations")
    class TextSearchOperationsTests {

        @Test
        @DisplayName("Should find users by first name containing text")
        void should_FindUsersByFirstNameContainingText() {
            // Act
            List<User> usersWithActive = userRepository.findByFirstNameContainingIgnoreCase("active");
            List<User> usersWithUser = userRepository.findByFirstNameContainingIgnoreCase("user");

            // Assert
            assertThat(usersWithActive).hasSize(1);
            assertThat(usersWithActive.get(0).getId()).isEqualTo(activeUser.getId());
            assertThat(usersWithUser).hasSize(3); // Pending, Suspended, Admin users have "User" in last name
        }

        @Test
        @DisplayName("Should find users by last name containing text")
        void should_FindUsersByLastNameContainingText() {
            // Act
            List<User> usersWithUser = userRepository.findByLastNameContainingIgnoreCase("user");

            // Assert
            assertThat(usersWithUser).hasSize(4); // All test users have "User" in last name
        }

        @Test
        @DisplayName("Should find users by full name search")
        void should_FindUsersByFullNameSearch() {
            // Act
            List<User> matchingUsers = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("admin", "admin");

            // Assert
            assertThat(matchingUsers).hasSize(1);
            assertThat(matchingUsers.get(0).getId()).isEqualTo(adminUser.getId());
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting Operations")
    class PaginationAndSortingOperationsTests {

        @Test
        @DisplayName("Should paginate users correctly")
        void should_PaginateUsersCorrectly() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 2); // First page, 2 items per page

            // Act
            Page<User> userPage = userRepository.findAll(pageable);

            // Assert
            assertThat(userPage.getContent()).hasSize(2);
            assertThat(userPage.getTotalElements()).isEqualTo(4);
            assertThat(userPage.getTotalPages()).isEqualTo(2);
            assertThat(userPage.isFirst()).isTrue();
            assertThat(userPage.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should sort users by creation date")
        void should_SortUsersByCreationDate() {
            // Arrange
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(0, 10, sort);

            // Act
            Page<User> sortedUsers = userRepository.findAll(pageable);

            // Assert
            List<User> users = sortedUsers.getContent();
            assertThat(users).hasSize(4);
            
            // Verify descending order by creation date
            for (int i = 0; i < users.size() - 1; i++) {
                assertThat(users.get(i).getCreatedAt())
                    .isAfterOrEqualTo(users.get(i + 1).getCreatedAt());
            }
        }

        @Test
        @DisplayName("Should paginate users by status")
        void should_PaginateUsersByStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 1);

            // Act
            Page<User> activeUsersPage = userRepository.findByStatus(UserStatus.ACTIVE, pageable);

            // Assert
            assertThat(activeUsersPage.getContent()).hasSize(1);
            assertThat(activeUsersPage.getTotalElements()).isEqualTo(2);
            assertThat(activeUsersPage.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("Complex Query Operations")
    class ComplexQueryOperationsTests {

        @Test
        @DisplayName("Should find users with complex criteria")
        void should_FindUsersWithComplexCriteria() {
            // Act - Find active users created today
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            List<User> result = userRepository.findByStatusAndCreatedAtAfter(UserStatus.ACTIVE, today);

            // Assert
            assertThat(result).hasSize(2); // activeUser and adminUser
        }

        @Test
        @DisplayName("Should perform bulk status update")
        void should_PerformBulkStatusUpdate() {
            // Arrange
            List<UserId> userIds = List.of(pendingUser.getId(), suspendedUser.getId());

            // Act
            int updatedCount = userRepository.updateStatusByIds(userIds, UserStatus.ACTIVE);

            // Assert
            assertThat(updatedCount).isEqualTo(2);
            
            // Verify changes
            Optional<User> updatedPending = userRepository.findById(pendingUser.getId());
            Optional<User> updatedSuspended = userRepository.findById(suspendedUser.getId());
            
            assertThat(updatedPending).isPresent();
            assertThat(updatedPending.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(updatedSuspended).isPresent();
            assertThat(updatedSuspended.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find users by phone number pattern")
        void should_FindUsersByPhoneNumberPattern() {
            // Act
            List<User> usersWithPattern = userRepository.findByPhoneNumberContaining("+61-400");

            // Assert
            assertThat(usersWithPattern).hasSize(4); // All test users have this pattern
        }

        @Test
        @DisplayName("Should get user statistics")
        void should_GetUserStatistics() {
            // Act
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
            long pendingUsers = userRepository.countByStatus(UserStatus.PENDING_VERIFICATION);
            long suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);

            // Assert
            assertThat(totalUsers).isEqualTo(4);
            assertThat(activeUsers).isEqualTo(2);
            assertThat(pendingUsers).isEqualTo(1);
            assertThat(suspendedUsers).isEqualTo(1);
            assertThat(activeUsers + pendingUsers + suspendedUsers).isEqualTo(totalUsers);
        }
    }

    @Nested
    @DisplayName("Data Integrity and Constraint Tests")
    class DataIntegrityAndConstraintTests {

        @Test
        @DisplayName("Should enforce email uniqueness constraint")
        void should_EnforceEmailUniquenessConstraint() {
            // Arrange
            User duplicateEmailUser = UserTestDataFactory.createDefaultUser()
                .withEmail(activeUser.getEmail()) // Same email as existing user
                .build();

            // Act & Assert
            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateEmailUser))
                .isInstanceOf(Exception.class); // Will be a constraint violation exception
        }

        @Test
        @DisplayName("Should handle null and empty values correctly")
        void should_HandleNullAndEmptyValuesCorrectly() {
            // Act & Assert
            assertThatThrownBy(() -> userRepository.findByEmail(null))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should maintain referential integrity on delete")
        void should_MaintainReferentialIntegrityOnDelete() {
            // This test would be expanded when there are foreign key relationships
            // For now, verify basic delete operation
            
            // Arrange
            UserId userIdToDelete = activeUser.getId();

            // Act
            userRepository.delete(activeUser);

            // Assert
            Optional<User> deletedUser = userRepository.findById(userIdToDelete);
            assertThat(deletedUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large result sets efficiently")
        void should_HandleLargeResultSetsEfficiently() {
            // Arrange - Create additional users for testing
            List<User> manyUsers = createManyTestUsers(50);
            userRepository.saveAll(manyUsers);

            // Act
            long startTime = System.currentTimeMillis();
            List<User> allUsers = userRepository.findAll();
            long endTime = System.currentTimeMillis();

            // Assert
            assertThat(allUsers).hasSizeGreaterThanOrEqualTo(54); // 4 original + 50 new
            assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
        }

        @Test
        @DisplayName("Should optimize pagination queries")
        void should_OptimizePaginationQueries() {
            // Arrange
            List<User> manyUsers = createManyTestUsers(20);
            userRepository.saveAll(manyUsers);
            
            Pageable pageable = PageRequest.of(0, 10);

            // Act
            long startTime = System.currentTimeMillis();
            Page<User> page = userRepository.findAll(pageable);
            long endTime = System.currentTimeMillis();

            // Assert
            assertThat(page.getContent()).hasSize(10);
            assertThat(endTime - startTime).isLessThan(500); // Should be fast for pagination
        }

        private List<User> createManyTestUsers(int count) {
            List<User> users = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                User user = UserTestDataFactory.createDefaultUser()
                    .withEmail(new Email("user" + i + "@test.com"))
                    .withFirstName("User" + i)
                    .withLastName("Test")
                    .build();
                users.add(user);
            }
            return users;
        }
    }

    @Test
    @Sql("/cleanup-data.sql")
    @DisplayName("Should clean up test data after SQL operations")
    void should_CleanUpTestDataAfterSqlOperations() {
        // This test verifies that SQL cleanup scripts work correctly
        long userCount = userRepository.count();
        assertThat(userCount).isEqualTo(0);
    }
}