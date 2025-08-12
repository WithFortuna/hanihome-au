# HaniHome AU 백엔드 테스트 전략

## 개요

이 문서는 HaniHome Australia API의 포괄적인 테스트 전략을 정의합니다. DDD(Domain-Driven Design) 아키텍처를 기반으로 한 Spring Boot 3.4 애플리케이션에 대한 단위 테스트, 통합 테스트, E2E 테스트 전략을 제시합니다.

## 현재 상태 분석

### 기존 테스트 인프라
- **테스트 프레임워크**: JUnit 5 + Spring Boot Test
- **커버리지 도구**: JaCoCo (최소 50% 요구)
- **정적 분석**: SonarQube
- **컨테이너화 테스트**: Testcontainers
- **보안 검사**: OWASP Dependency Check
- **테스트 DB**: H2 인메모리 데이터베이스

### 현재 문제점
- 테스트 클래스가 거의 없는 상태
- 커버리지가 거의 0%
- `src/test/java/integration/` 폴더만 존재하고 비어있음

## 테스트 전략

### 1. 테스트 피라미드 접근법

```
       /\
      /  \     E2E Tests (5%)
     /____\    - 전체 시스템 워크플로우
    /      \   
   /        \  Integration Tests (20%)
  /          \ - API, 서비스 레이어 통합
 /____________\
Unit Tests (75%)
- 도메인 로직, 비즈니스 규칙
```

### 2. 테스트 레벨별 전략

#### 2.1 단위 테스트 (Unit Tests)
**목표**: 개별 클래스와 메소드의 동작 검증

**테스트 대상**:
- **Domain Entities**: `Property`, `User`, `Transaction`
- **Value Objects**: `Money`, `Address`, `Email`, `PropertyId`
- **Domain Services**: `PropertyDomainService`
- **Application Services**: `PropertyApplicationService`, `UserApplicationService`
- **Validators**: `DateRangeValidator`, `PropertyTypeValidator`
- **Utilities**: JWT 토큰 제공자, 보안 컴포넌트

**테스트 도구**:
- JUnit 5
- Mockito (@Mock, @MockBean)
- AssertJ (유창한 단언문)
- @ExtendWith(MockitoExtension.class)

**커버리지 목표**: 85% 이상

#### 2.2 통합 테스트 (Integration Tests)
**목표**: 여러 컴포넌트 간의 상호작용 검증

**테스트 대상**:
- **Repository Layer**: JPA 엔티티와 데이터베이스 연동
- **Service Integration**: Application Service + Domain Service 통합
- **Security Integration**: JWT 인증, OAuth2 인가
- **External APIs**: FCM, Email 서비스
- **Cache Integration**: Redis 캐싱 동작

**테스트 도구**:
- @SpringBootTest
- Testcontainers (PostgreSQL, Redis)
- @DataJpaTest, @WebMvcTest
- @TestPropertySource

**커버리지 목표**: 70% 이상

#### 2.3 API 테스트 (Controller Tests)
**목표**: REST API 엔드포인트의 동작 검증

**테스트 대상**:
- 모든 Controller 클래스
- 인증/인가 시나리오
- 입력 검증 및 예외 처리
- HTTP 응답 형식 검증
- API 문서화 (Spring REST Docs)

**테스트 도구**:
- @WebMvcTest
- MockMvc
- @WithMockUser, @WithUserDetails
- Spring Security Test

#### 2.4 엔드투엔드 테스트 (E2E Tests)
**목표**: 전체 시스템의 비즈니스 워크플로우 검증

**주요 시나리오**:
- 사용자 가입 → 로그인 → 부동산 등록 → 검색
- 부동산 관리 워크플로우
- 알림 시스템 전체 플로우
- 결제 및 거래 프로세스

**테스트 도구**:
- @SpringBootTest(webEnvironment = RANDOM_PORT)
- TestRestTemplate
- Testcontainers (전체 스택)

## 테스트 디렉토리 구조

```
src/test/java/com/hanihome/hanihome_au_api/
├── unit/                           # 단위 테스트
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── PropertyTest.java
│   │   │   ├── UserTest.java
│   │   │   └── TransactionTest.java
│   │   ├── service/
│   │   │   └── PropertyDomainServiceTest.java
│   │   └── valueobject/
│   │       ├── MoneyTest.java
│   │       ├── AddressTest.java
│   │       └── EmailTest.java
│   ├── application/
│   │   ├── property/
│   │   │   ├── PropertyApplicationServiceTest.java
│   │   │   └── PropertySearchServiceTest.java
│   │   ├── user/
│   │   │   └── UserApplicationServiceTest.java
│   │   └── notification/
│   │       ├── FCMNotificationServiceTest.java
│   │       └── EmailNotificationServiceTest.java
│   ├── security/
│   │   ├── jwt/
│   │   │   └── JwtTokenProviderTest.java
│   │   └── oauth2/
│   │       └── CustomOAuth2UserServiceTest.java
│   └── validation/
│       ├── DateRangeValidatorTest.java
│       └── PropertyTypeValidatorTest.java
│
├── integration/                    # 통합 테스트
│   ├── repository/
│   │   ├── PropertyRepositoryTest.java
│   │   ├── UserRepositoryTest.java
│   │   └── TransactionRepositoryTest.java
│   ├── service/
│   │   ├── PropertyIntegrationTest.java
│   │   ├── UserIntegrationTest.java
│   │   └── NotificationIntegrationTest.java
│   ├── security/
│   │   └── SecurityIntegrationTest.java
│   └── external/
│       ├── FCMIntegrationTest.java
│       └── EmailIntegrationTest.java
│
├── controller/                     # API 테스트
│   ├── property/
│   │   └── PropertyControllerTest.java
│   ├── user/
│   │   └── UserControllerTest.java
│   ├── admin/
│   │   └── AdminDashboardControllerTest.java
│   └── auth/
│       └── AuthControllerTest.java
│
├── e2e/                           # 엔드투엔드 테스트
│   ├── PropertyManagementE2ETest.java
│   ├── UserJourneyE2ETest.java
│   └── NotificationE2ETest.java
│
└── testutil/                      # 테스트 유틸리티
    ├── TestDataFactory.java      # 테스트 데이터 생성
    ├── MockFactory.java          # Mock 객체 생성
    ├── TestContainerConfig.java  # Testcontainer 설정
    └── TestSecurityConfig.java   # 테스트용 보안 설정
```

## 테스트 환경 설정

### 테스트 프로파일 (`application-test.yml`)
- H2 인메모리 데이터베이스
- 테스트용 Redis (임베디드)
- JWT 시크릿 키 (테스트 전용)
- 로깅 레벨 조정

### Testcontainers 설정
```yaml
# 통합 테스트용 PostgreSQL
postgres:
  image: postgres:15
  environment:
    POSTGRES_DB: hanihome_test
    POSTGRES_USER: test
    POSTGRES_PASSWORD: test

# 통합 테스트용 Redis
redis:
  image: redis:7
```

## CI/CD 파이프라인 통합

### GitHub Actions 워크플로우

```yaml
name: Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
          
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
        
    - name: Run Unit Tests
      run: ./gradlew test
      
    - name: Run Integration Tests
      run: ./gradlew integrationTest
      
    - name: Generate Coverage Report
      run: ./gradlew jacocoTestReport
      
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: build/reports/jacoco/test/jacocoTestReport.xml
        
    - name: SonarQube Analysis
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: ./gradlew sonar
      
    - name: Security Scan
      run: ./gradlew dependencyCheckAnalyze
```

### 테스트 실행 전략

1. **Pre-commit Hook**: 
   - 단위 테스트 실행
   - 코드 포맷팅 검사

2. **Pull Request**:
   - 전체 테스트 스위트 실행
   - 커버리지 검증 (최소 80%)
   - 정적 분석

3. **배포 전**:
   - E2E 테스트 실행
   - 보안 스캔
   - 성능 테스트

4. **프로덕션 배포 후**:
   - 스모크 테스트
   - 헬스 체크

## 구현 로드맵

### Phase 1: 핵심 도메인 단위 테스트 (1-2주)
**우선순위**: 높음

**구현 대상**:
- [ ] Property, User, Transaction 엔티티 테스트
- [ ] Money, Address, Email 값 객체 테스트
- [ ] PropertyDomainService 테스트
- [ ] 기본 검증 로직 테스트

**성공 기준**:
- 도메인 레이어 커버리지 85% 이상
- 모든 비즈니스 규칙 테스트 커버

### Phase 2: 애플리케이션 레이어 테스트 (2-3주)
**우선순위**: 높음

**구현 대상**:
- [ ] PropertyApplicationService 테스트
- [ ] UserApplicationService 테스트
- [ ] 알림 서비스 테스트
- [ ] Repository 통합 테스트
- [ ] 보안 컴포넌트 테스트

**성공 기준**:
- 애플리케이션 레이어 커버리지 80% 이상
- 외부 의존성 모킹 완료

### Phase 3: API 및 통합 테스트 (2-3주)
**우선순위**: 중간

**구현 대상**:
- [ ] 모든 Controller 테스트
- [ ] 보안 인증/인가 테스트
- [ ] 외부 API 통합 테스트
- [ ] 캐시 통합 테스트

**성공 기준**:
- API 레이어 커버리지 75% 이상
- 모든 엔드포인트 테스트 완료

### Phase 4: E2E 테스트 및 최적화 (1-2주)
**우선순위**: 중간

**구현 대상**:
- [ ] 주요 사용자 시나리오 E2E 테스트
- [ ] 성능 테스트
- [ ] CI/CD 파이프라인 최적화
- [ ] 테스트 병렬 실행 설정

**성공 기준**:
- 전체 커버리지 80% 이상 달성
- CI/CD 파이프라인 안정화

## 커버리지 목표

### 전체 목표
- **라인 커버리지**: 80% 이상
- **브랜치 커버리지**: 75% 이상
- **메소드 커버리지**: 85% 이상

### 레이어별 목표
- **Domain Layer**: 85% 이상
- **Application Layer**: 80% 이상
- **Infrastructure Layer**: 70% 이상
- **Presentation Layer**: 75% 이상

## 테스트 실행 명령어

```bash
# 단위 테스트만 실행
./gradlew test

# 통합 테스트만 실행
./gradlew integrationTest

# 모든 테스트 실행
./gradlew check

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 정적 분석 실행
./gradlew sonar

# 보안 스캔 실행
./gradlew dependencyCheckAnalyze

# 특정 테스트 클래스 실행
./gradlew test --tests PropertyTest

# 특정 패키지 테스트 실행
./gradlew test --tests "com.hanihome.*.domain.*"
```

## 베스트 프랙티스

### 1. 테스트 작성 원칙
- **FIRST 원칙**: Fast, Independent, Repeatable, Self-Validating, Timely
- **AAA 패턴**: Arrange, Act, Assert
- **Given-When-Then** BDD 스타일 활용

### 2. 명명 규칙
```java
// 메소드명: should_ExpectedBehavior_When_StateUnderTest
@Test
void should_ReturnValidProperty_When_ValidDataProvided() { }

// 테스트 클래스명: [대상클래스명]Test
class PropertyDomainServiceTest { }
```

### 3. Mock 사용 가이드라인
- 외부 의존성에만 Mock 사용
- 도메인 객체는 실제 인스턴스 사용
- @MockBean은 통합 테스트에서만 사용

### 4. 테스트 데이터 관리
- TestDataFactory를 통한 일관된 테스트 데이터 생성
- 각 테스트는 독립적인 데이터 사용
- 테스트 간 데이터 공유 금지

## 모니터링 및 리포팅

### 1. 지속적 모니터링
- 커버리지 추세 모니터링
- 테스트 실행 시간 추적
- 실패율 모니터링

### 2. 품질 게이트
- PR 머지 전 최소 커버리지 검증
- 모든 테스트 통과 필수
- 정적 분석 기준 만족

### 3. 리포팅
- 주간 테스트 품질 리포트
- 커버리지 트렌드 분석
- 테스트 부채 식별

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-01-06  
**담당자**: Backend Team  