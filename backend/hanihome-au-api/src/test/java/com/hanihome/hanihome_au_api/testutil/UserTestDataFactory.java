package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * User 엔티티용 TestDataFactory
 */
public class UserTestDataFactory extends BaseTestDataFactory<User> {

    @Override
    public User createDefault() {
        return User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("test.user@example.com"),
            "Test User",
            UserRole.USER
        );
    }

    @Override
    public User createMinimal() {
        return User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("minimal@example.com"),
            "U", // Minimal name
            UserRole.USER
        );
    }

    @Override
    public User createMaximal() {
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("premium.user@hanihome.com"),
            "Premium Test User with Long Name",
            UserRole.LANDLORD
        );
        
        // Set all optional fields
        user.setPhoneNumber(generateKoreanPhoneNumber());
        user.verifyEmail();
        user.verifyPhone();
        user.updateLastLogin();
        
        return user;
    }

    @Override
    public User createInvalid() {
        // This would typically throw exceptions during creation
        try {
            return User.create(
                null, // Invalid: null ID
                null, // Invalid: null email
                "", // Invalid: empty name
                null // Invalid: null role
            );
        } catch (Exception e) {
            // Return a user that would fail validation later
            return User.create(
                UserId.of("invalid-id"),
                Email.of("invalid-email"), // This should fail Email validation
                "", // Empty name should fail
                UserRole.USER
            );
        }
    }

    @Override
    public User createRandom() {
        UserRole[] roles = {UserRole.USER, UserRole.LANDLORD, UserRole.AGENT};
        UserRole randomRole = roles[faker.random().nextInt(roles.length)];
        
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of(faker.internet().emailAddress()),
            generateKoreanName(),
            randomRole
        );
        
        // Randomly set optional fields
        if (faker.bool().bool()) {
            user.setPhoneNumber(generateKoreanPhoneNumber());
        }
        
        if (faker.bool().bool()) {
            user.verifyEmail();
        }
        
        if (faker.bool().bool()) {
            user.verifyPhone();
        }
        
        if (faker.bool().bool()) {
            user.updateLastLogin();
        }
        
        return user;
    }

    @Override
    public List<User> createBulk(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createRandom())
            .toList();
    }

    // Role-specific factory methods
    public User createTenant() {
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("tenant." + faker.name().username() + "@example.com"),
            generateKoreanName(),
            UserRole.USER
        );
        
        user.setPhoneNumber(generateKoreanPhoneNumber());
        return user;
    }

    public User createLandlord() {
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("landlord." + faker.name().username() + "@example.com"),
            generateKoreanName(),
            UserRole.LANDLORD
        );
        
        user.setPhoneNumber(generateKoreanPhoneNumber());
        user.verifyEmail();
        user.verifyPhone();
        return user;
    }

    public User createAgent() {
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("agent." + faker.name().username() + "@realestate.com"),
            generateKoreanName(),
            UserRole.AGENT
        );
        
        user.setPhoneNumber(generateKoreanPhoneNumber());
        user.verifyEmail();
        user.verifyPhone();
        return user;
    }

    public User createAdmin() {
        User user = User.create(
            UserId.of(faker.internet().uuid()),
            Email.of("admin." + faker.name().username() + "@hanihome.com"),
            "Admin " + generateKoreanName(),
            UserRole.ADMIN
        );
        
        user.setPhoneNumber(generateKoreanPhoneNumber());
        user.verifyEmail();
        user.verifyPhone();
        user.updateLastLogin();
        
        return user;
    }

    // Status-specific factory methods
    public User createVerifiedUser() {
        User user = createDefault();
        user.verifyEmail();
        user.verifyPhone();
        return user;
    }

    public User createUnverifiedUser() {
        return createDefault(); // Default users are unverified
    }

    public User createInactiveUser() {
        User user = createDefault();
        user.deactivate();
        return user;
    }

    public User createActiveUser() {
        User user = createDefault();
        user.activate();
        user.updateLastLogin();
        return user;
    }

    // Testing-specific factory methods
    public User createUserWithEmail(String email) {
        return User.create(
            UserId.of(faker.internet().uuid()),
            Email.of(email),
            generateKoreanName(),
            UserRole.USER
        );
    }

    public User createUserWithRole(UserRole role) {
        return User.create(
            UserId.of(faker.internet().uuid()),
            Email.of(faker.internet().emailAddress()),
            generateKoreanName(),
            role
        );
    }

    public User createUserWithPhoneNumber(String phoneNumber) {
        User user = createDefault();
        user.setPhoneNumber(phoneNumber);
        return user;
    }

    // Bulk creation with specific criteria
    public List<User> createVerifiedUsers(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createVerifiedUser())
            .toList();
    }

    public List<User> createUsersWithRole(UserRole role, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createUserWithRole(role))
            .toList();
    }

    public List<User> createTenants(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createTenant())
            .toList();
    }

    public List<User> createLandlords(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createLandlord())
            .toList();
    }
}