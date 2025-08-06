package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.repository.*;
import com.hanihome.hanihome_au_api.application.notification.service.EmailNotificationService;
import com.hanihome.hanihome_au_api.application.notification.service.FCMNotificationService;
import com.hanihome.hanihome_au_api.security.jwt.JwtTokenProvider;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * 테스트용 Mock 객체 생성을 위한 팩토리 클래스
 */
public class MockFactory {

    // Repository Mocks
    public static class Repositories {
        
        public static PropertyRepository createPropertyRepository() {
            return mock(PropertyRepository.class);
        }

        public static UserRepository createUserRepository() {
            return mock(UserRepository.class);
        }

        public static TransactionRepository createTransactionRepository() {
            return mock(TransactionRepository.class);
        }

        public static PropertyFavoriteRepository createPropertyFavoriteRepository() {
            return mock(PropertyFavoriteRepository.class);
        }

        public static SearchHistoryRepository createSearchHistoryRepository() {
            return mock(SearchHistoryRepository.class);
        }

        public static ViewingRepository createViewingRepository() {
            return mock(ViewingRepository.class);
        }

        public static FCMTokenRepository createFCMTokenRepository() {
            return mock(FCMTokenRepository.class);
        }

        public static ReportRepository createReportRepository() {
            return mock(ReportRepository.class);
        }
    }

    // Service Mocks
    public static class Services {
        
        public static EmailNotificationService createEmailNotificationService() {
            return mock(EmailNotificationService.class);
        }

        public static FCMNotificationService createFCMNotificationService() {
            return mock(FCMNotificationService.class);
        }

        public static PasswordEncoder createPasswordEncoder() {
            PasswordEncoder encoder = mock(PasswordEncoder.class);
            when(encoder.encode(any(CharSequence.class))).thenReturn("encoded-password");
            when(encoder.matches(any(CharSequence.class), any(String.class))).thenReturn(true);
            return encoder;
        }
    }

    // Security Mocks
    public static class Security {
        
        public static JwtTokenProvider createJwtTokenProvider() {
            JwtTokenProvider provider = mock(JwtTokenProvider.class);
            when(provider.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
            when(provider.validateToken(any(String.class))).thenReturn(true);
            when(provider.getUsernameFromToken(any(String.class))).thenReturn("test@example.com");
            return provider;
        }

        public static OAuth2User createOAuth2User() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getName()).thenReturn("123456789");
            when(oAuth2User.getAttributes()).thenReturn(Map.of(
                "email", "test@example.com",
                "name", "Test User",
                "given_name", "Test",
                "family_name", "User"
            ));
            return oAuth2User;
        }

        public static UserDetails createUserDetails() {
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn("test@example.com");
            when(userDetails.getPassword()).thenReturn("encoded-password");
            when(userDetails.isEnabled()).thenReturn(true);
            when(userDetails.isAccountNonExpired()).thenReturn(true);
            when(userDetails.isAccountNonLocked()).thenReturn(true);
            when(userDetails.isCredentialsNonExpired()).thenReturn(true);
            when(userDetails.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
            return userDetails;
        }

        public static UserDetails createAdminUserDetails() {
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn("admin@hanihome.com");
            when(userDetails.getPassword()).thenReturn("encoded-password");
            when(userDetails.isEnabled()).thenReturn(true);
            when(userDetails.isAccountNonExpired()).thenReturn(true);
            when(userDetails.isAccountNonLocked()).thenReturn(true);
            when(userDetails.isCredentialsNonExpired()).thenReturn(true);
            when(userDetails.getAuthorities()).thenReturn(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
            ));
            return userDetails;
        }

        public static UsernamePasswordAuthenticationToken createAuthentication() {
            return new UsernamePasswordAuthenticationToken(
                createUserDetails(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        public static UsernamePasswordAuthenticationToken createAdminAuthentication() {
            return new UsernamePasswordAuthenticationToken(
                createAdminUserDetails(),
                null,
                List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
                )
            );
        }
    }

    // Common Mock Behaviors
    public static class Behaviors {
        
        /**
         * Repository가 엔티티를 저장할 때 ID를 설정하여 반환하도록 설정
         */
        public static <T> void setupSaveAndReturn(org.springframework.data.jpa.repository.JpaRepository<T, String> repository) {
            when(repository.save(any())).thenAnswer(invocation -> {
                T entity = invocation.getArgument(0);
                return entity;
            });
        }

        /**
         * Repository가 ID로 엔티티를 찾을 때 Optional.empty() 반환하도록 설정
         */
        public static <T> void setupFindByIdEmpty(org.springframework.data.jpa.repository.JpaRepository<T, String> repository) {
            when(repository.findById(any(String.class))).thenReturn(java.util.Optional.empty());
        }

        /**
         * Repository가 모든 엔티티를 찾을 때 빈 리스트 반환하도록 설정
         */
        public static <T> void setupFindAllEmpty(org.springframework.data.jpa.repository.JpaRepository<T, String> repository) {
            when(repository.findAll()).thenReturn(List.of());
        }

        /**
         * 페이징된 결과 Mock 설정
         */
        public static <T> void setupPageableResult(
            org.springframework.data.jpa.repository.JpaRepository<T, String> repository,
            List<T> content) {
            org.springframework.data.domain.Page<T> page = new org.springframework.data.domain.PageImpl<>(content);
            when(repository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        }
    }

    // External Service Mocks
    public static class External {
        
        public static org.springframework.mail.javamail.JavaMailSender createMailSender() {
            return mock(org.springframework.mail.javamail.JavaMailSender.class);
        }

        public static org.springframework.data.redis.core.RedisTemplate<String, Object> createRedisTemplate() {
            @SuppressWarnings("unchecked")
            org.springframework.data.redis.core.RedisTemplate<String, Object> template = 
                mock(org.springframework.data.redis.core.RedisTemplate.class);
            
            @SuppressWarnings("unchecked")
            org.springframework.data.redis.core.ValueOperations<String, Object> valueOps = 
                mock(org.springframework.data.redis.core.ValueOperations.class);
            
            when(template.opsForValue()).thenReturn(valueOps);
            return template;
        }

        public static com.google.firebase.messaging.FirebaseMessaging createFirebaseMessaging() {
            return mock(com.google.firebase.messaging.FirebaseMessaging.class);
        }
    }

    // Test Configuration
    public static class Configuration {
        
        /**
         * 모든 Mock 객체들을 초기화하는 헬퍼 메소드
         */
        public static void resetAllMocks(Object... mocks) {
            for (Object mock : mocks) {
                Mockito.reset(mock);
            }
        }

        /**
         * Mock 객체의 상호작용 검증 헬퍼
         */
        public static void verifyNoMoreInteractions(Object... mocks) {
            Mockito.verifyNoMoreInteractions(mocks);
        }

        /**
         * Mock 객체의 메소드 호출 횟수 검증
         */
        public static <T> org.mockito.verification.VerificationMode times(int wantedNumberOfInvocations) {
            return Mockito.times(wantedNumberOfInvocations);
        }

        /**
         * Mock 객체의 메소드가 한 번도 호출되지 않았는지 검증
         */
        public static org.mockito.verification.VerificationMode never() {
            return Mockito.never();
        }

        /**
         * Mock 객체의 메소드가 정확히 한 번 호출되었는지 검증
         */
        public static org.mockito.verification.VerificationMode once() {
            return Mockito.times(1);
        }
    }

    // Argument Matchers
    public static class Matchers {
        
        public static <T> T any(Class<T> type) {
            return Mockito.any(type);
        }

        public static String anyString() {
            return Mockito.anyString();
        }

        public static Long anyLong() {
            return Mockito.anyLong();
        }

        public static Integer anyInt() {
            return Mockito.anyInt();
        }

        public static Boolean anyBoolean() {
            return Mockito.anyBoolean();
        }

        public static <T> List<T> anyList() {
            return Mockito.anyList();
        }

        public static <K, V> Map<K, V> anyMap() {
            return Mockito.anyMap();
        }

        public static <T> Collection<T> anyCollection() {
            return Mockito.anyCollection();
        }
    }

    // Stubbing Helpers
    public static class Stubbing {
        
        /**
         * 메소드 호출 시 예외를 던지도록 설정
         */
        public static <T> org.mockito.stubbing.OngoingStubbing<T> whenThenThrow(
            T methodCall, Throwable throwable) {
            return when(methodCall).thenThrow(throwable);
        }

        /**
         * 메소드 호출 시 값을 반환하도록 설정
         */
        public static <T> org.mockito.stubbing.OngoingStubbing<T> whenThenReturn(
            T methodCall, T returnValue) {
            return when(methodCall).thenReturn(returnValue);
        }

        /**
         * void 메소드 호출 시 예외를 던지도록 설정
         */
        public static org.mockito.stubbing.Stubber doThrowWhen(Throwable throwable) {
            return Mockito.doThrow(throwable);
        }

        /**
         * void 메소드 호출 시 아무것도 하지 않도록 설정
         */
        public static org.mockito.stubbing.Stubber doNothingWhen() {
            return Mockito.doNothing();
        }
    }
}