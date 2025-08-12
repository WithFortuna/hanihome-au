package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;

import java.time.LocalDateTime;

/**
 * User 엔티티를 위한 테스트 데이터 빌더
 * 플루언트 인터페이스를 통해 직관적인 테스트 데이터 생성을 지원
 */
public class UserTestDataBuilder {
    
    private UserId id;
    private Email email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    public UserTestDataBuilder() {
        // 기본값 설정
        this.id = UserId.of(BaseTestDataFactory.faker.internet().uuid());
        this.email = Email.of("test@example.com");
        this.name = "Test User";
        this.phoneNumber = "010-1234-5678";
        this.role = UserRole.USER;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserTestDataBuilder withId(String id) {
        this.id = UserId.of(id);
        return this;
    }
    
    public UserTestDataBuilder withRandomId() {
        this.id = UserId.of(BaseTestDataFactory.faker.internet().uuid());
        return this;
    }
    
    public UserTestDataBuilder withEmail(String email) {
        this.email = Email.of(email);
        return this;
    }
    
    public UserTestDataBuilder withRandomEmail() {
        this.email = Email.of(BaseTestDataFactory.faker.internet().emailAddress());
        return this;
    }
    
    public UserTestDataBuilder withEmail(String prefix, String domain) {
        this.email = Email.of(prefix + "@" + domain);
        return this;
    }
    
    public UserTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public UserTestDataBuilder withRandomName() {
        this.name = BaseTestDataFactory.faker.name().fullName();
        return this;
    }
    
    public UserTestDataBuilder withKoreanName() {
        // 한국어 이름 생성
        String[] firstNames = {"민수", "영희", "철수", "지영", "현우", "수진", "동현", "은지", "준호", "미영"};
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
        
        String lastName = lastNames[BaseTestDataFactory.faker.random().nextInt(lastNames.length)];
        String firstName = firstNames[BaseTestDataFactory.faker.random().nextInt(firstNames.length)];
        
        this.name = lastName + firstName;
        return this;
    }
    
    public UserTestDataBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    public UserTestDataBuilder withRandomPhoneNumber() {
        String[] prefixes = {"010", "011", "016", "017", "018", "019"};
        String prefix = prefixes[BaseTestDataFactory.faker.random().nextInt(prefixes.length)];
        String middle = String.format("%04d", BaseTestDataFactory.faker.number().numberBetween(1000, 9999));
        String last = String.format("%04d", BaseTestDataFactory.faker.number().numberBetween(1000, 9999));
        
        this.phoneNumber = prefix + "-" + middle + "-" + last;
        return this;
    }
    
    public UserTestDataBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }
    
    public UserTestDataBuilder withRandomRole() {
        UserRole[] roles = {UserRole.USER, UserRole.LANDLORD, UserRole.AGENT};
        this.role = roles[BaseTestDataFactory.faker.random().nextInt(roles.length)];
        return this;
    }
    
    public UserTestDataBuilder withEmailVerified(boolean verified) {
        this.emailVerified = verified;
        return this;
    }
    
    public UserTestDataBuilder withPhoneVerified(boolean verified) {
        this.phoneVerified = verified;
        return this;
    }
    
    public UserTestDataBuilder withActive(boolean active) {
        this.isActive = active;
        return this;
    }
    
    public UserTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public UserTestDataBuilder withCreatedAtDaysAgo(int days) {
        this.createdAt = LocalDateTime.now().minusDays(days);
        return this;
    }
    
    public UserTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public UserTestDataBuilder withLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        return this;
    }
    
    public UserTestDataBuilder withLastLoginAtHoursAgo(int hours) {
        this.lastLoginAt = LocalDateTime.now().minusHours(hours);
        return this;
    }
    
    public UserTestDataBuilder withRecentLogin() {
        this.lastLoginAt = LocalDateTime.now().minusMinutes(BaseTestDataFactory.faker.number().numberBetween(5, 120));
        return this;
    }
    
    // Role-specific convenience methods
    public UserTestDataBuilder asTenant() {
        return withRole(UserRole.USER)
            .withEmail("tenant." + BaseTestDataFactory.faker.name().username() + "@example.com")
            .withRandomName()
            .withRandomPhoneNumber();
    }
    
    public UserTestDataBuilder asLandlord() {
        return withRole(UserRole.LANDLORD)
            .withEmail("landlord." + BaseTestDataFactory.faker.name().username() + "@example.com")
            .withRandomName()
            .withRandomPhoneNumber()
            .withEmailVerified(true)
            .withPhoneVerified(true);
    }
    
    public UserTestDataBuilder asAgent() {
        return withRole(UserRole.AGENT)
            .withEmail("agent." + BaseTestDataFactory.faker.name().username() + "@realestate.com")
            .withRandomName()
            .withRandomPhoneNumber()
            .withEmailVerified(true)
            .withPhoneVerified(true);
    }
    
    public UserTestDataBuilder asAdmin() {
        return withRole(UserRole.ADMIN)
            .withEmail("admin." + BaseTestDataFactory.faker.name().username() + "@hanihome.com")
            .withName("Admin " + BaseTestDataFactory.faker.name().fullName())
            .withRandomPhoneNumber()
            .withEmailVerified(true)
            .withPhoneVerified(true)
            .withRecentLogin();
    }
    
    // Verification status convenience methods
    public UserTestDataBuilder asVerified() {
        return withEmailVerified(true)
            .withPhoneVerified(true);
    }
    
    public UserTestDataBuilder asUnverified() {
        return withEmailVerified(false)
            .withPhoneVerified(false);
    }
    
    public UserTestDataBuilder asPartiallyVerified() {
        return withEmailVerified(true)
            .withPhoneVerified(false);
    }
    
    // Activity status convenience methods
    public UserTestDataBuilder asActive() {
        return withActive(true)
            .withRecentLogin();
    }
    
    public UserTestDataBuilder asInactive() {
        return withActive(false)
            .withLastLoginAtHoursAgo(BaseTestDataFactory.faker.number().numberBetween(168, 8760)); // 1주-1년 전
    }
    
    public UserTestDataBuilder asNewUser() {
        return withCreatedAtDaysAgo(0)
            .withUpdatedAt(LocalDateTime.now())
            .withLastLoginAt(null)
            .withEmailVerified(false)
            .withPhoneVerified(false);
    }
    
    // Domain-specific convenience methods
    public UserTestDataBuilder forPropertyTesting() {
        return withRandomId()
            .withRandomEmail()
            .withRandomName()
            .withRandomPhoneNumber()
            .withRandomRole()
            .asVerified()
            .asActive();
    }
    
    public UserTestDataBuilder forSecurityTesting() {
        return withRandomId()
            .withEmail("security.test@example.com")
            .withName("Security Test User")
            .withRandomPhoneNumber()
            .withRole(UserRole.USER)
            .asVerified()
            .asActive()
            .withRecentLogin();
    }
    
    public UserTestDataBuilder forIntegrationTesting() {
        return withId("integration-test-user-" + System.currentTimeMillis())
            .withEmail("integration.test+" + System.currentTimeMillis() + "@example.com")
            .withName("Integration Test User")
            .withPhoneNumber("010-0000-0000")
            .withRole(UserRole.USER)
            .asVerified()
            .asActive();
    }
    
    // Random data generation
    public UserTestDataBuilder allRandom() {
        return withRandomId()
            .withRandomEmail()
            .withRandomName()
            .withRandomPhoneNumber()
            .withRandomRole()
            .withEmailVerified(BaseTestDataFactory.faker.bool().bool())
            .withPhoneVerified(BaseTestDataFactory.faker.bool().bool())
            .withActive(BaseTestDataFactory.faker.bool().bool())
            .withCreatedAtDaysAgo(BaseTestDataFactory.faker.number().numberBetween(1, 365))
            .withLastLoginAtHoursAgo(BaseTestDataFactory.faker.number().numberBetween(1, 8760));
    }
    
    // Invalid data for negative testing
    public UserTestDataBuilder withInvalidData() {
        return withEmail("invalid-email") // Invalid email format
            .withName("") // Empty name
            .withPhoneNumber("123") // Invalid phone format
            .withRole(null); // Null role
    }
    
    public User build() {
        User user = User.create(id, email, name, role);
        
        // Set optional fields through the user object methods if they exist
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
        
        if (emailVerified) {
            user.verifyEmail();
        }
        
        if (phoneVerified) {
            user.verifyPhone();
        }
        
        if (!isActive) {
            user.deactivate();
        }
        
        if (lastLoginAt != null) {
            user.updateLastLogin();
        }
        
        return user;
    }
    
    // Static factory methods for common scenarios
    public static UserTestDataBuilder aTenant() {
        return new UserTestDataBuilder().asTenant();
    }
    
    public static UserTestDataBuilder aLandlord() {
        return new UserTestDataBuilder().asLandlord();
    }
    
    public static UserTestDataBuilder anAgent() {
        return new UserTestDataBuilder().asAgent();
    }
    
    public static UserTestDataBuilder anAdmin() {
        return new UserTestDataBuilder().asAdmin();
    }
    
    public static UserTestDataBuilder aRandomUser() {
        return new UserTestDataBuilder().allRandom();
    }
    
    public static UserTestDataBuilder aVerifiedUser() {
        return new UserTestDataBuilder().asVerified();
    }
    
    public static UserTestDataBuilder anUnverifiedUser() {
        return new UserTestDataBuilder().asUnverified();
    }
    
    public static UserTestDataBuilder anActiveUser() {
        return new UserTestDataBuilder().asActive();
    }
    
    public static UserTestDataBuilder anInactiveUser() {
        return new UserTestDataBuilder().asInactive();
    }
}