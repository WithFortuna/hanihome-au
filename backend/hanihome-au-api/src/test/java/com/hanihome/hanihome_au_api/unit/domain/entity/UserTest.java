package com.hanihome.hanihome_au_api.unit.domain.entity;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import com.hanihome.hanihome_au_api.domain.user.event.UserRegisteredEvent;
import com.hanihome.hanihome_au_api.domain.user.event.UserRoleChangedEvent;
import com.hanihome.hanihome_au_api.domain.user.exception.UserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private UserId userId;
    private Email userEmail;
    private String userName;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        userId = new UserId(UUID.randomUUID().toString());
        userEmail = new Email("test@example.com");
        userName = "John Doe";
        userRole = UserRole.USER;
    }

    @Nested
    @DisplayName("User Creation")
    class UserCreationTests {

        @Test
        @DisplayName("Should create user with valid data")
        void should_CreateUser_When_ValidDataProvided() {
            // Act
            User user = User.create(userId, userEmail, userName, userRole);

            // Assert
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getEmail()).isEqualTo(userEmail);
            assertThat(user.getName()).isEqualTo(userName);
            assertThat(user.getRole()).isEqualTo(userRole);
            assertThat(user.isEmailVerified()).isFalse();
            assertThat(user.isPhoneVerified()).isFalse();
            assertThat(user.isFullyVerified()).isFalse();
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
            assertThat(user.getLastLoginAt()).isNull();
        }

        @Test
        @DisplayName("Should raise UserRegisteredEvent when user is created")
        void should_RaiseUserRegisteredEvent_When_UserIsCreated() {
            // Act
            User user = User.create(userId, userEmail, userName, userRole);

            // Assert
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegisteredEvent.class);
            
            UserRegisteredEvent event = (UserRegisteredEvent) user.getDomainEvents().get(0);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getEmail()).isEqualTo(userEmail);
            assertThat(event.getName()).isEqualTo(userName);
            assertThat(event.getRole()).isEqualTo(userRole);
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void should_ThrowException_When_UserIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> User.create(null, userEmail, userName, userRole))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void should_ThrowException_When_EmailIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> User.create(userId, null, userName, userRole))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Email cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when role is null")
        void should_ThrowException_When_RoleIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> User.create(userId, userEmail, userName, null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("User role cannot be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when name is invalid")
        void should_ThrowException_When_NameIsInvalid(String invalidName) {
            // Act & Assert
            assertThatThrownBy(() -> User.create(userId, userEmail, invalidName, userRole))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when name exceeds 100 characters")
        void should_ThrowException_When_NameTooLong() {
            // Arrange
            String longName = "a".repeat(101);

            // Act & Assert
            assertThatThrownBy(() -> User.create(userId, userEmail, longName, userRole))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Name cannot exceed 100 characters");
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void should_TrimWhitespace_FromName() {
            // Arrange
            String nameWithWhitespace = "  John Doe  ";

            // Act
            User user = User.create(userId, userEmail, nameWithWhitespace, userRole);

            // Assert
            assertThat(user.getName()).isEqualTo("John Doe");
        }
    }

    @Nested
    @DisplayName("User Profile Management")
    class UserProfileManagementTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
            user.clearDomainEvents(); // Clear creation event for cleaner tests
        }

        @Test
        @DisplayName("Should update profile with valid data")
        void should_UpdateProfile_When_ValidDataProvided() {
            // Arrange
            String newName = "Jane Smith";
            String newPhoneNumber = "0412345678";
            LocalDateTime beforeUpdate = user.getUpdatedAt();

            // Act
            user.updateProfile(newName, newPhoneNumber);

            // Assert
            assertThat(user.getName()).isEqualTo(newName);
            assertThat(user.getPhoneNumber()).isEqualTo(newPhoneNumber);
            assertThat(user.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Should update name only")
        void should_UpdateNameOnly_When_PhoneNumberIsNull() {
            // Arrange
            String newName = "Jane Smith";

            // Act
            user.updateProfile(newName, null);

            // Assert
            assertThat(user.getName()).isEqualTo(newName);
            assertThat(user.getPhoneNumber()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid name")
        void should_ThrowException_When_UpdatingWithInvalidName() {
            // Act & Assert
            assertThatThrownBy(() -> user.updateProfile("", "0412345678"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Name cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("User Role Management")
    class UserRoleManagementTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
            user.clearDomainEvents();
        }

        @Test
        @DisplayName("Should change role successfully")
        void should_ChangeRole_When_NewRoleProvided() {
            // Arrange
            UserRole newRole = UserRole.LANDLORD;

            // Act
            user.changeRole(newRole);

            // Assert
            assertThat(user.getRole()).isEqualTo(newRole);
        }

        @Test
        @DisplayName("Should raise UserRoleChangedEvent when role changes")
        void should_RaiseRoleChangedEvent_When_RoleChanges() {
            // Arrange
            UserRole newRole = UserRole.ADMIN;

            // Act
            user.changeRole(newRole);

            // Assert
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRoleChangedEvent.class);
            
            UserRoleChangedEvent event = (UserRoleChangedEvent) user.getDomainEvents().get(0);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getOldRole()).isEqualTo(userRole);
            assertThat(event.getNewRole()).isEqualTo(newRole);
        }

        @Test
        @DisplayName("Should not raise event when role is the same")
        void should_NotRaiseEvent_When_RoleIsTheSame() {
            // Act
            user.changeRole(userRole); // Same role

            // Assert
            assertThat(user.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when new role is null")
        void should_ThrowException_When_NewRoleIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> user.changeRole(null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("New role cannot be null");
        }

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("Should change to any valid role")
        void should_ChangeToValidRole_When_ValidRoleProvided(UserRole newRole) {
            // Act
            user.changeRole(newRole);

            // Assert
            assertThat(user.getRole()).isEqualTo(newRole);
        }
    }

    @Nested
    @DisplayName("User Verification")
    class UserVerificationTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
        }

        @Test
        @DisplayName("Should verify email successfully")
        void should_VerifyEmail_When_EmailNotVerified() {
            // Arrange
            assertThat(user.isEmailVerified()).isFalse();
            LocalDateTime beforeUpdate = user.getUpdatedAt();

            // Act
            user.verifyEmail();

            // Assert
            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Should not update timestamp when email already verified")
        void should_NotUpdateTimestamp_When_EmailAlreadyVerified() {
            // Arrange
            user.verifyEmail();
            LocalDateTime afterFirstVerification = user.getUpdatedAt();

            // Act
            user.verifyEmail(); // Second verification

            // Assert
            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getUpdatedAt()).isEqualTo(afterFirstVerification);
        }

        @Test
        @DisplayName("Should verify phone successfully")
        void should_VerifyPhone_When_PhoneNotVerified() {
            // Arrange
            assertThat(user.isPhoneVerified()).isFalse();
            LocalDateTime beforeUpdate = user.getUpdatedAt();

            // Act
            user.verifyPhone();

            // Assert
            assertThat(user.isPhoneVerified()).isTrue();
            assertThat(user.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Should not update timestamp when phone already verified")
        void should_NotUpdateTimestamp_When_PhoneAlreadyVerified() {
            // Arrange
            user.verifyPhone();
            LocalDateTime afterFirstVerification = user.getUpdatedAt();

            // Act
            user.verifyPhone(); // Second verification

            // Assert
            assertThat(user.isPhoneVerified()).isTrue();
            assertThat(user.getUpdatedAt()).isEqualTo(afterFirstVerification);
        }

        @Test
        @DisplayName("Should be fully verified when both email and phone verified")
        void should_BeFullyVerified_When_BothEmailAndPhoneVerified() {
            // Act
            user.verifyEmail();
            user.verifyPhone();

            // Assert
            assertThat(user.isFullyVerified()).isTrue();
        }

        @Test
        @DisplayName("Should not be fully verified when only email verified")
        void should_NotBeFullyVerified_When_OnlyEmailVerified() {
            // Act
            user.verifyEmail();

            // Assert
            assertThat(user.isFullyVerified()).isFalse();
        }

        @Test
        @DisplayName("Should not be fully verified when only phone verified")
        void should_NotBeFullyVerified_When_OnlyPhoneVerified() {
            // Act
            user.verifyPhone();

            // Assert
            assertThat(user.isFullyVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("User Login Tracking")
    class UserLoginTrackingTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
        }

        @Test
        @DisplayName("Should record login time")
        void should_RecordLoginTime_When_UserLogsIn() {
            // Arrange
            assertThat(user.getLastLoginAt()).isNull();

            // Act
            user.recordLogin();

            // Assert
            assertThat(user.getLastLoginAt()).isNotNull();
            assertThat(user.getLastLoginAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("Should update login time on subsequent logins")
        void should_UpdateLoginTime_OnSubsequentLogins() {
            // Arrange
            user.recordLogin();
            LocalDateTime firstLogin = user.getLastLoginAt();

            // Act - Wait a bit and login again
            try {
                Thread.sleep(10); // Small delay to ensure different timestamps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            user.recordLogin();

            // Assert
            assertThat(user.getLastLoginAt()).isAfter(firstLogin);
        }
    }

    @Nested
    @DisplayName("User Permissions")
    class UserPermissionsTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
        }

        @Test
        @DisplayName("Should check property management permission for different roles")
        void should_CheckPropertyManagementPermission_ForDifferentRoles() {
            // Test USER role
            user.changeRole(UserRole.USER);
            assertThat(user.canManageProperty()).isFalse();

            // Test LANDLORD role
            user.changeRole(UserRole.LANDLORD);
            assertThat(user.canManageProperty()).isTrue();

            // Test AGENT role
            user.changeRole(UserRole.AGENT);
            assertThat(user.canManageProperty()).isTrue();

            // Test ADMIN role
            user.changeRole(UserRole.ADMIN);
            assertThat(user.canManageProperty()).isTrue();
        }

        @Test
        @DisplayName("Should delegate permission check to role")
        void should_DelegatePermissionCheck_ToRole() {
            // Arrange
            user.changeRole(UserRole.ADMIN);
            String permission = "MANAGE_USERS";

            // Act
            boolean hasPermission = user.hasPermission(permission);

            // Assert
            // This test verifies that the permission check is delegated to the UserRole
            // The actual permission logic is tested in UserRole tests
            assertThat(hasPermission).isEqualTo(userRole.hasPermission(permission));
        }
    }

    @Nested
    @DisplayName("User Domain Events")
    class UserDomainEventsTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = User.create(userId, userEmail, userName, userRole);
        }

        @Test
        @DisplayName("Should return defensive copy of domain events")
        void should_ReturnDefensiveCopy_OfDomainEvents() {
            // Act
            var events = user.getDomainEvents();

            // Modify the returned list
            events.clear();

            // Assert - Original events should not be affected
            assertThat(user.getDomainEvents()).hasSize(1); // Still has creation event
        }

        @Test
        @DisplayName("Should clear domain events successfully")
        void should_ClearDomainEvents_Successfully() {
            // Arrange
            assertThat(user.getDomainEvents()).hasSize(1);

            // Act
            user.clearDomainEvents();

            // Assert
            assertThat(user.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Should accumulate multiple domain events")
        void should_AccumulateMultipleDomainEvents_WhenMultipleOperationsPerformed() {
            // Arrange
            user.clearDomainEvents();

            // Act
            user.changeRole(UserRole.LANDLORD);
            user.changeRole(UserRole.ADMIN);

            // Assert
            assertThat(user.getDomainEvents()).hasSize(2);
            assertThat(user.getDomainEvents())
                .allMatch(event -> event instanceof UserRoleChangedEvent);
        }
    }

    @Nested
    @DisplayName("User Invariants")
    class UserInvariantsTests {

        @Test
        @DisplayName("Should maintain data consistency after updates")
        void should_MaintainDataConsistency_AfterUpdates() {
            // Arrange
            User user = User.create(userId, userEmail, userName, userRole);
            LocalDateTime creationTime = user.getCreatedAt();

            // Act - Multiple updates
            user.updateProfile("Updated Name", "0412345678");
            user.changeRole(UserRole.LANDLORD);
            user.verifyEmail();
            user.verifyPhone();
            user.recordLogin();

            // Assert - Invariants maintained
            assertThat(user.getId()).isEqualTo(userId); // ID never changes
            assertThat(user.getEmail()).isEqualTo(userEmail); // Email never changes
            assertThat(user.getCreatedAt()).isEqualTo(creationTime); // Creation time never changes
            assertThat(user.getUpdatedAt()).isAfter(creationTime); // Updated time advances
            assertThat(user.getName()).isEqualTo("Updated Name"); // Updates applied
            assertThat(user.getRole()).isEqualTo(UserRole.LANDLORD);
            assertThat(user.isFullyVerified()).isTrue();
        }

        @Test
        @DisplayName("Should not allow email changes after creation")
        void should_NotAllowEmailChanges_AfterCreation() {
            // Arrange
            User user = User.create(userId, userEmail, userName, userRole);

            // Assert - No method should exist to change email
            // This is enforced by not having a setter or update method for email
            assertThat(user.getEmail()).isEqualTo(userEmail);
            
            // Email should remain constant throughout user lifecycle
            user.updateProfile("New Name", "0412345678");
            user.changeRole(UserRole.ADMIN);
            user.verifyEmail();
            
            assertThat(user.getEmail()).isEqualTo(userEmail); // Still the same
        }

        @Test
        @DisplayName("Should not allow ID changes after creation")
        void should_NotAllowIdChanges_AfterCreation() {
            // Arrange
            User user = User.create(userId, userEmail, userName, userRole);

            // Assert - No method should exist to change ID
            assertThat(user.getId()).isEqualTo(userId);
            
            // ID should remain constant throughout user lifecycle
            user.updateProfile("New Name", "0412345678");
            user.changeRole(UserRole.ADMIN);
            
            assertThat(user.getId()).isEqualTo(userId); // Still the same
        }
    }
}