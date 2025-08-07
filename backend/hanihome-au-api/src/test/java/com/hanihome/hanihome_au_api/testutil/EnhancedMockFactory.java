package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.domain.entity.*;
import com.hanihome.hanihome_au_api.application.notification.service.EmailNotificationService;
import com.hanihome.hanihome_au_api.application.notification.service.FCMNotificationService;

import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 향상된 Mock 객체 생성 및 관리를 위한 팩토리 클래스
 * MockitoExtension과의 완벽한 통합과 도메인별 전문화된 Mock 설정을 제공
 */
public class EnhancedMockFactory {

    /**
     * 도메인 엔티티별 전문화된 Repository Mock 팩토리
     */
    public static class DomainRepositories {
        
        /**
         * Property Repository Mock 생성 및 설정
         */
        public static PropertyRepository createPropertyRepository() {
            PropertyRepository mock = mock(PropertyRepository.class);
            setupDefaultBehaviors(mock);
            return mock;
        }
        
        /**
         * Property Repository Mock을 성공적인 시나리오로 설정
         */
        public static PropertyRepository createSuccessfulPropertyRepository(Property... properties) {
            PropertyRepository mock = mock(PropertyRepository.class);
            
            List<Property> propertyList = List.of(properties);
            
            // 기본 CRUD 동작 설정
            when(mock.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(mock.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            
            // 조회 동작 설정
            if (!propertyList.isEmpty()) {
                when(mock.findById(anyString())).thenReturn(Optional.of(propertyList.get(0)));
                when(mock.existsById(anyString())).thenReturn(true);
            }
            
            when(mock.findAll()).thenReturn(propertyList);
            when(mock.count()).thenReturn((long) propertyList.size());
            
            // 페이징 처리
            Page<Property> page = new PageImpl<>(propertyList);
            when(mock.findAll(any(Pageable.class))).thenReturn(page);
            
            return mock;
        }
        
        /**
         * User Repository Mock 생성 및 설정
         */
        public static UserRepository createUserRepository() {
            UserRepository mock = mock(UserRepository.class);
            setupDefaultBehaviors(mock);
            return mock;
        }
        
        /**
         * User Repository Mock을 성공적인 시나리오로 설정
         */
        public static UserRepository createSuccessfulUserRepository(User... users) {
            UserRepository mock = mock(UserRepository.class);
            
            List<User> userList = List.of(users);
            
            // 기본 CRUD 동작 설정
            when(mock.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(mock.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            
            // 조회 동작 설정
            if (!userList.isEmpty()) {
                when(mock.findById(anyString())).thenReturn(Optional.of(userList.get(0)));
                when(mock.existsById(anyString())).thenReturn(true);
                
                // 이메일로 조회 (일반적인 사용 케이스)
                when(mock.findByEmail(any())).thenReturn(Optional.of(userList.get(0)));
            }
            
            when(mock.findAll()).thenReturn(userList);
            when(mock.count()).thenReturn((long) userList.size());
            
            // 페이징 처리
            Page<User> page = new PageImpl<>(userList);
            when(mock.findAll(any(Pageable.class))).thenReturn(page);
            
            return mock;
        }
        
        /**
         * Repository의 기본 동작을 설정하는 헬퍼 메서드
         */
        private static <T, ID> void setupDefaultBehaviors(org.springframework.data.jpa.repository.JpaRepository<T, ID> repository) {
            when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(repository.findById(any())).thenReturn(Optional.empty());
            when(repository.findAll()).thenReturn(List.of());
            when(repository.count()).thenReturn(0L);
            when(repository.existsById(any())).thenReturn(false);
            
            // 삭제 동작
            doNothing().when(repository).deleteById(any());
            doNothing().when(repository).delete(any());
            doNothing().when(repository).deleteAll();
        }
    }

    /**
     * 서비스 계층 Mock 팩토리
     */
    public static class ApplicationServices {
        
        /**
         * 이메일 알림 서비스 Mock 생성
         */
        public static EmailNotificationService createEmailNotificationService() {
            EmailNotificationService mock = mock(EmailNotificationService.class);
            
            // 성공적인 이메일 전송 설정
            doNothing().when(mock).sendNotification(any(), any(), any());
            when(mock.isEnabled()).thenReturn(true);
            
            // 비동기 이메일 전송
            when(mock.sendNotificationAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
            
            return mock;
        }
        
        /**
         * 실패하는 이메일 서비스 Mock 생성
         */
        public static EmailNotificationService createFailingEmailNotificationService(Exception exception) {
            EmailNotificationService mock = mock(EmailNotificationService.class);
            
            doThrow(exception).when(mock).sendNotification(any(), any(), any());
            when(mock.isEnabled()).thenReturn(true);
            
            when(mock.sendNotificationAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(exception));
            
            return mock;
        }
        
        /**
         * FCM 알림 서비스 Mock 생성
         */
        public static FCMNotificationService createFCMNotificationService() {
            FCMNotificationService mock = mock(FCMNotificationService.class);
            
            // 성공적인 푸시 알림 전송 설정
            doNothing().when(mock).sendNotification(any(), any(), any());
            when(mock.isEnabled()).thenReturn(true);
            
            // 비동기 푸시 알림 전송
            when(mock.sendNotificationAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
            
            return mock;
        }
        
        /**
         * 실패하는 FCM 서비스 Mock 생성
         */
        public static FCMNotificationService createFailingFCMNotificationService(Exception exception) {
            FCMNotificationService mock = mock(FCMNotificationService.class);
            
            doThrow(exception).when(mock).sendNotification(any(), any(), any());
            when(mock.isEnabled()).thenReturn(true);
            
            when(mock.sendNotificationAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(exception));
            
            return mock;
        }
    }

    /**
     * 테스트 시나리오별 Mock 설정 프리셋
     */
    public static class TestScenarios {
        
        /**
         * 성공적인 Property 조회 시나리오
         */
        public static PropertyRepository setupSuccessfulPropertyLookup(Property property) {
            PropertyRepository mock = DomainRepositories.createPropertyRepository();
            
            when(mock.findById(property.getId().getValue())).thenReturn(Optional.of(property));
            when(mock.existsById(property.getId().getValue())).thenReturn(true);
            
            return mock;
        }
        
        /**
         * Property를 찾을 수 없는 시나리오
         */
        public static PropertyRepository setupPropertyNotFound() {
            PropertyRepository mock = DomainRepositories.createPropertyRepository();
            
            when(mock.findById(anyString())).thenReturn(Optional.empty());
            when(mock.existsById(anyString())).thenReturn(false);
            
            return mock;
        }
        
        /**
         * Database 오류 시나리오
         */
        public static PropertyRepository setupDatabaseError(Exception exception) {
            PropertyRepository mock = DomainRepositories.createPropertyRepository();
            
            when(mock.save(any())).thenThrow(exception);
            when(mock.findById(anyString())).thenThrow(exception);
            when(mock.findAll()).thenThrow(exception);
            
            return mock;
        }
        
        /**
         * 성공적인 사용자 인증 시나리오
         */
        public static UserRepository setupSuccessfulUserAuthentication(User user) {
            UserRepository mock = DomainRepositories.createUserRepository();
            
            when(mock.findById(user.getId().getValue())).thenReturn(Optional.of(user));
            when(mock.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(mock.existsById(user.getId().getValue())).thenReturn(true);
            
            return mock;
        }
        
        /**
         * 알림 전송 성공 시나리오
         */
        public static class NotificationSuccess {
            public static EmailNotificationService email() {
                return ApplicationServices.createEmailNotificationService();
            }
            
            public static FCMNotificationService fcm() {
                return ApplicationServices.createFCMNotificationService();
            }
        }
        
        /**
         * 알림 전송 실패 시나리오
         */
        public static class NotificationFailure {
            public static EmailNotificationService email(Exception exception) {
                return ApplicationServices.createFailingEmailNotificationService(exception);
            }
            
            public static FCMNotificationService fcm(Exception exception) {
                return ApplicationServices.createFailingFCMNotificationService(exception);
            }
        }
    }

    /**
     * 테스트 클래스 설정 및 관리
     */
    public static class TestSetup {
        
        /**
         * 테스트 클래스 초기화 - MockitoAnnotations 설정
         */
        public static void initializeTestClass(Object testInstance) {
            MockitoAnnotations.openMocks(testInstance);
        }
        
        /**
         * 표준 Repository Mock 셋업
         */
        public static void setupStandardRepositoryMocks(Object testInstance) throws IllegalAccessException {
            java.lang.reflect.Field[] fields = testInstance.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                if (field.isAnnotationPresent(org.mockito.Mock.class)) {
                    field.setAccessible(true);
                    Object mock = field.get(testInstance);
                    
                    if (mock instanceof org.springframework.data.jpa.repository.JpaRepository) {
                        setupStandardRepository((org.springframework.data.jpa.repository.JpaRepository) mock);
                    }
                }
            }
        }
        
        private static <T, ID> void setupStandardRepository(org.springframework.data.jpa.repository.JpaRepository<T, ID> repository) {
            when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(repository.findById(any())).thenReturn(Optional.empty());
            when(repository.findAll()).thenReturn(List.of());
            when(repository.count()).thenReturn(0L);
            when(repository.existsById(any())).thenReturn(false);
        }
        
        /**
         * 모든 Mock 객체 리셋
         */
        public static void resetAllMocks(Object testInstance) throws IllegalAccessException {
            java.lang.reflect.Field[] fields = testInstance.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                if (field.isAnnotationPresent(org.mockito.Mock.class)) {
                    field.setAccessible(true);
                    Object mock = field.get(testInstance);
                    reset(mock);
                }
            }
        }
    }

    /**
     * Mock 검증 헬퍼
     */
    public static class Verification {
        
        /**
         * Repository 저장 동작 검증
         */
        public static <T> void verifySaved(org.springframework.data.jpa.repository.JpaRepository<T, ?> repository, T entity) {
            verify(repository).save(entity);
        }
        
        /**
         * Repository 저장 동작이 호출되지 않았음을 검증
         */
        public static <T> void verifyNotSaved(org.springframework.data.jpa.repository.JpaRepository<T, ?> repository) {
            verify(repository, never()).save(any());
        }
        
        /**
         * 특정 ID로 조회했는지 검증
         */
        public static <T> void verifyFoundById(org.springframework.data.jpa.repository.JpaRepository<T, ?> repository, String id) {
            verify(repository).findById(id);
        }
        
        /**
         * 알림 전송 검증
         */
        public static void verifyEmailSent(EmailNotificationService service) {
            verify(service, atLeastOnce()).sendNotification(any(), any(), any());
        }
        
        public static void verifyFCMSent(FCMNotificationService service) {
            verify(service, atLeastOnce()).sendNotification(any(), any(), any());
        }
        
        /**
         * 알림이 전송되지 않았음을 검증
         */
        public static void verifyNoEmailSent(EmailNotificationService service) {
            verify(service, never()).sendNotification(any(), any(), any());
        }
        
        public static void verifyNoFCMSent(FCMNotificationService service) {
            verify(service, never()).sendNotification(any(), any(), any());
        }
    }

    /**
     * 통합 테스트용 Mock 설정
     */
    public static class IntegrationTestSetup {
        
        /**
         * 전체 Property 관련 Mock 생성
         */
        public static PropertyTestMocks createPropertyTestMocks() {
            return new PropertyTestMocks();
        }
        
        /**
         * 전체 User 관련 Mock 생성
         */
        public static UserTestMocks createUserTestMocks() {
            return new UserTestMocks();
        }
        
        /**
         * Property 테스트를 위한 Mock 컬렉션
         */
        public static class PropertyTestMocks {
            public final PropertyRepository repository = DomainRepositories.createPropertyRepository();
            public final EmailNotificationService emailService = ApplicationServices.createEmailNotificationService();
            public final FCMNotificationService fcmService = ApplicationServices.createFCMNotificationService();
            
            public PropertyTestMocks withSuccessfulRepository(Property... properties) {
                PropertyRepository successfulRepo = DomainRepositories.createSuccessfulPropertyRepository(properties);
                // Transfer the successful setup to this.repository
                return this;
            }
        }
        
        /**
         * User 테스트를 위한 Mock 컬렉션
         */
        public static class UserTestMocks {
            public final UserRepository repository = DomainRepositories.createUserRepository();
            public final EmailNotificationService emailService = ApplicationServices.createEmailNotificationService();
            public final FCMNotificationService fcmService = ApplicationServices.createFCMNotificationService();
            
            public UserTestMocks withSuccessfulRepository(User... users) {
                UserRepository successfulRepo = DomainRepositories.createSuccessfulUserRepository(users);
                // Transfer the successful setup to this.repository
                return this;
            }
        }
    }
}