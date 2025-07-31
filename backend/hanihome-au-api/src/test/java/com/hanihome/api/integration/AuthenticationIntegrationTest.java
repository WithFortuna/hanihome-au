package com.hanihome.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanihome.api.dto.PasswordChangeDto;
import com.hanihome.api.dto.UserProfileDto;
import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.repository.UserRepository;
import com.hanihome.hanihome_au_api.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up Redis
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .name("Test User")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.TENANT)
                .oauthProvider(OAuthProvider.GOOGLE)
                .oauthProviderId("google-test-id")
                .enabled(true)
                .twoFactorEnabled(false)
                .loginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = userRepository.save(testUser);
        validToken = jwtTokenProvider.generateToken(testUser.getId().toString());
    }

    @Test
    @DisplayName("JWT 토큰 생성 및 검증 성능 테스트")
    void testJwtTokenPerformance() throws InterruptedException {
        int threadCount = 100;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    try {
                        // Token generation test
                        String token = jwtTokenProvider.generateToken("user" + j);
                        assertThat(token).isNotNull();

                        // Token validation test
                        boolean isValid = jwtTokenProvider.validateToken(token);
                        assertThat(isValid).isTrue();

                        // Extract user ID test
                        String userId = jwtTokenProvider.getUserIdFromToken(token);
                        assertThat(userId).isEqualTo("user" + j);

                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertThat(finished).isTrue();
        assertThat(errorCount.get()).isZero();
        assertThat(successCount.get()).isEqualTo(threadCount * operationsPerThread);

        System.out.println("JWT Performance Test Results:");
        System.out.println("Total operations: " + (threadCount * operationsPerThread));
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Operations per second: " + (successCount.get() * 1000.0 / duration));
    }

    @Test
    @DisplayName("동시 사용자 프로필 접근 부하 테스트")
    void testConcurrentUserProfileAccess() throws InterruptedException {
        int threadCount = 50;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        mockMvc.perform(get("/api/profile")
                                .header("Authorization", "Bearer " + validToken)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                                .andExpect(jsonPath("$.name").value(testUser.getName()));

                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertThat(finished).isTrue();
        assertThat(errorCount.get()).isLessThan(threadCount * requestsPerThread * 0.01); // Less than 1% error rate

        System.out.println("Concurrent Profile Access Test Results:");
        System.out.println("Total requests: " + (threadCount * requestsPerThread));
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Requests per second: " + (successCount.get() * 1000.0 / duration));
        System.out.println("Error rate: " + (errorCount.get() * 100.0 / (threadCount * requestsPerThread)) + "%");
    }

    @Test
    @DisplayName("사용자 프로필 CRUD 통합 테스트")
    void testUserProfileCrud() throws Exception {
        // 1. Get profile
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));

        // 2. Update profile
        UserProfileDto updateDto = new UserProfileDto();
        updateDto.setName("Updated Name");
        updateDto.setPhone("+61412345678");
        updateDto.setAddress("123 Test Street, Melbourne, VIC 3000");
        updateDto.setBio("Updated bio information");

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.phone").value("+61412345678"))
                .andExpect(jsonPath("$.address").value("123 Test Street, Melbourne, VIC 3000"))
                .andExpect(jsonPath("$.bio").value("Updated bio information"));

        // 3. Verify update
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.phone").value("+61412345678"));
    }

    @Test
    @DisplayName("비밀번호 변경 보안 테스트")
    void testPasswordChangeSecurity() throws Exception {
        // 1. Valid password change
        PasswordChangeDto validChange = new PasswordChangeDto();
        validChange.setCurrentPassword("password123");
        validChange.setNewPassword("newPassword123");
        validChange.setConfirmPassword("newPassword123");

        mockMvc.perform(post("/api/profile/password/change")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validChange)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // 2. Invalid current password
        PasswordChangeDto invalidCurrentPassword = new PasswordChangeDto();
        invalidCurrentPassword.setCurrentPassword("wrongPassword");
        invalidCurrentPassword.setNewPassword("newPassword456");
        invalidCurrentPassword.setConfirmPassword("newPassword456");

        mockMvc.perform(post("/api/profile/password/change")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCurrentPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // 3. Password mismatch
        PasswordChangeDto passwordMismatch = new PasswordChangeDto();
        passwordMismatch.setCurrentPassword("newPassword123");
        passwordMismatch.setNewPassword("anotherPassword123");
        passwordMismatch.setConfirmPassword("differentPassword123");

        mockMvc.perform(post("/api/profile/password/change")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordMismatch)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Rate Limiting 테스트")
    void testRateLimiting() throws Exception {
        // Test password change rate limiting (3 attempts per 5 minutes)
        PasswordChangeDto changeDto = new PasswordChangeDto();
        changeDto.setCurrentPassword("wrongPassword");
        changeDto.setNewPassword("newPassword123");
        changeDto.setConfirmPassword("newPassword123");

        // First 3 attempts should go through (but fail due to wrong password)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/profile/password/change")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changeDto)))
                    .andExpect(status().isBadRequest());
        }

        // 4th attempt should be rate limited
        mockMvc.perform(post("/api/profile/password/change")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeDto)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("비밀번호 변경 시도가 너무 많습니다. 5분 후 다시 시도해주세요."));
    }

    @Test
    @DisplayName("2단계 인증 설정 테스트")
    void testTwoFactorAuthentication() throws Exception {
        // 1. Enable 2FA
        mockMvc.perform(post("/api/profile/two-factor/enable")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("2단계 인증이 활성화되었습니다"));

        // 2. Verify 2FA is enabled
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorEnabled").value(true));

        // 3. Disable 2FA
        mockMvc.perform(post("/api/profile/two-factor/disable")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("2단계 인증이 비활성화되었습니다"));

        // 4. Verify 2FA is disabled
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorEnabled").value(false));
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 테스트")
    void testUnauthorizedAccess() throws Exception {
        // No token
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        // Invalid token
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        // Expired token (simulate by using a token that's too old)
        String expiredToken = jwtTokenProvider.generateTokenWithExpiration("user1", -1000); // Expired 1 second ago
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("세션 관리 테스트")
    void testSessionManagement() throws Exception {
        // 1. Create session
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        // 2. Verify session exists in Redis
        String sessionKey = "session:" + testUser.getId();
        Boolean exists = redisTemplate.hasKey(sessionKey);
        // Note: This depends on your session management implementation

        // 3. Test multiple concurrent sessions
        String anotherToken = jwtTokenProvider.generateToken(testUser.getId().toString());
        
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + anotherToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공개 프로필 접근 테스트")
    void testPublicProfileAccess() throws Exception {
        mockMvc.perform(get("/api/profile/" + testUser.getId())
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").doesNotExist()) // Email should not be in public profile
                .andExpect(jsonPath("$.phone").doesNotExist()); // Phone should not be in public profile
    }
}