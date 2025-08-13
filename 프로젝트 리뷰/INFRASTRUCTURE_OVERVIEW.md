# HaniHome AU - 인프라 구조 개요

## 📋 프로젝트 개요

**HaniHome AU**는 호주 부동산 렌탈 플랫폼으로, 현대적인 클라우드 네이티브 아키텍처를 기반으로 구축된 풀스택 웹 애플리케이션입니다.

## 🏗️ 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    HaniHome AU Platform                    │
├─────────────────────────────────────────────────────────────┤
│  Frontend (Next.js)  │  Backend (Spring Boot)  │  Infra    │
│  ┌─────────────────┐ │ ┌──────────────────────┐ │ ┌───────┐ │
│  │ • React 19      │ │ │ • Java 21           │ │ │ AWS   │ │
│  │ • TypeScript    │ │ │ • Spring Boot 3.4   │ │ │ Cloud │ │
│  │ • Tailwind CSS  │ │ │ • PostgreSQL        │ │ │       │ │
│  │ • NextAuth.js   │ │ │ • Redis             │ │ │       │ │
│  │ • Google Maps   │ │ │ • JWT/OAuth2        │ │ │       │ │
│  └─────────────────┘ │ └──────────────────────┘ │ └───────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🏢 환경별 인프라 구성

### 개발 환경 (Development)
```yaml
services:
  ├── PostgreSQL 16     # 메인 데이터베이스
  ├── Backend API       # Spring Boot 애플리케이션
  ├── Frontend          # Next.js 애플리케이션  
  └── PgAdmin           # 데이터베이스 관리 도구
```

### 프로덕션 환경 (Production)
```yaml
services:
  ├── Nginx             # 리버스 프록시 & SSL 종료
  ├── Frontend          # Next.js (정적 파일 서빙)
  ├── Backend API       # Spring Boot (비즈니스 로직)
  ├── Redis             # 세션 & 캐시 저장소
  └── Fluentd           # 로그 수집 및 집계
```

## 🛠️ 기술 스택

### Frontend Stack
- **Framework**: Next.js 15.4.4 (App Router)
- **Runtime**: React 19.1.0
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS v4
- **Authentication**: NextAuth.js v5
- **Maps**: Google Maps JavaScript API
- **Form**: React Hook Form + Zod
- **Build Tool**: Turbopack (개발), Webpack (프로덕션)

### Backend Stack
- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21 (LTS)
- **Database**: PostgreSQL 16 (Primary), H2 (Testing)
- **Cache**: Redis 7
- **ORM**: Spring Data JPA + QueryDSL
- **Migration**: Flyway
- **Security**: Spring Security + JWT + OAuth2
- **Testing**: JUnit 5, Testcontainers, WireMock
- **Documentation**: OpenAPI 3 (Swagger)

### Infrastructure & DevOps
- **Containerization**: Docker + Docker Compose
- **Cloud Provider**: AWS (ap-southeast-2)
- **Infrastructure as Code**: Terraform
- **CI/CD**: GitHub Actions
- **Monitoring**: AWS CloudWatch + Fluentd
- **Reverse Proxy**: Nginx
- **SSL/TLS**: Let's Encrypt (자동 갱신)

## 🗂️ 프로젝트 구조

```
hanihome-au/
├── frontend/hanihome-au/          # Next.js 애플리케이션
│   ├── src/
│   │   ├── app/                   # App Router 페이지
│   │   ├── components/            # 재사용 가능한 컴포넌트
│   │   ├── hooks/                 # 커스텀 React 훅
│   │   ├── lib/                   # 유틸리티 라이브러리
│   │   └── middleware.ts          # Next.js 미들웨어
│   ├── public/                    # 정적 파일
│   └── package.json               # 의존성 관리
│
├── backend/hanihome-au-api/       # Spring Boot 애플리케이션
│   ├── src/main/java/             # 메인 소스코드
│   │   └── com/hanihome/
│   │       ├── application/       # 애플리케이션 서비스
│   │       ├── controller/        # REST API 컨트롤러
│   │       ├── repository/        # 데이터 접근 계층
│   │       ├── security/          # 보안 설정
│   │       ├── config/            # 스프링 설정
│   │       └── dto/               # 데이터 전송 객체
│   ├── src/main/resources/        # 설정 파일
│   ├── src/test/                  # 테스트 코드
│   └── build.gradle               # 빌드 설정
│
├── infrastructure/terraform/      # 인프라 코드 (IaC)
│   ├── main.tf                    # 메인 리소스 정의
│   ├── vpc.tf                     # 네트워크 설정
│   ├── rds.tf                     # 데이터베이스
│   ├── s3.tf                      # 스토리지
│   └── security-groups.tf         # 보안 그룹
│
├── .github/workflows/             # CI/CD 파이프라인
│   ├── ci-frontend.yml            # 프론트엔드 CI
│   ├── ci-backend.yml             # 백엔드 CI
│   ├── cd-production.yml          # 프로덕션 배포
│   └── dependency-update.yml      # 의존성 자동 업데이트
│
├── docker-compose.dev.yml         # 개발 환경 오케스트레이션
├── docker-compose.production.yml  # 프로덕션 환경 오케스트레이션
└── scripts/                       # 자동화 스크립트
```

## 🔄 데이터 플로우

```
User Request → Nginx → Frontend (Next.js) → Backend API (Spring Boot) → PostgreSQL
                ↓                                     ↓
            Static Files                        Redis (Session/Cache)
```

## 🔐 보안 아키텍처

- **Frontend**: NextAuth.js 세션 관리
- **Backend**: JWT 토큰 기반 인증
- **Database**: 연결 풀링 및 암호화된 연결
- **Network**: VPC, Security Groups, HTTPS 강제
- **Secrets**: AWS Secrets Manager + Environment Variables

## 📊 모니터링 & 로깅

- **Application Logs**: Fluentd → AWS CloudWatch
- **Performance Metrics**: Spring Boot Actuator
- **Health Checks**: Kubernetes Liveness/Readiness Probes
- **Error Tracking**: 구조화된 로깅 + CloudWatch Alarms

## 🔍 코드 품질 & 보안 검사 시스템

### 🏗️ CI/CD 파이프라인 구성

```
┌─────────────────────────────────────────────────────────────────┐
│                    GitHub Actions Workflows                    │
├─────────────────────────────────────────────────────────────────┤
│  Frontend CI                     Backend CI                    │
│  ┌─────────────────────────────┐ ┌─────────────────────────────┐ │
│  │ 1. Lint & Type Check       │ │ 1. Unit Tests               │ │
│  │ 2. Tests & Coverage         │ │ 2. Integration Tests        │ │
│  │ 3. Build & Bundle Analysis  │ │ 3. Code Quality (SonarCloud)│ │
│  │ 4. Security Scan (Snyk)     │ │ 4. Security Scan (OWASP)   │ │
│  │ 5. Lighthouse Performance  │ │ 5. Build & Artifacts        │ │
│  └─────────────────────────────┘ └─────────────────────────────┘ │
│                                                                 │
│  Dependency Management                                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ • Weekly dependency updates (automated PRs)                 │ │
│  │ • Security audit (npm audit + OWASP)                       │ │
│  │ • License compliance check                                  │ │
│  │ • Vulnerability alerts → Auto issue creation               │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 🛡️ 보안 검사 도구

#### Frontend Security Stack
- **Snyk** (`snyk/actions/node@master`)
  - 의존성 취약점 스캔
  - 심각도 임계값: High
  - 실시간 보안 모니터링

- **npm audit**
  - 패키지 취약점 검사
  - 보안 수준: Moderate 이상
  - 자동 보안 업데이트

#### Backend Security Stack
- **OWASP Dependency Check** (`org.owasp.dependencycheck`)
  - 알려진 취약점 데이터베이스 대조
  - CVE 기반 취약점 검출
  - HTML 리포트 생성

- **Snyk** (`snyk/actions/gradle@master`)
  - Gradle 의존성 스캔
  - 실시간 보안 알림
  - 수정 제안 제공

### 📊 코드 품질 검사 도구

#### Frontend Quality Stack
- **ESLint** + **Prettier**
  - 코드 스타일 일관성
  - Best Practice 강제
  - 자동 포맷팅

- **TypeScript**
  - 타입 안전성 검증
  - 컴파일 시점 오류 검출

- **Lighthouse CI**
  - 성능 메트릭 측정
  - 접근성 검사
  - SEO 최적화 검증

- **Bundle Analyzer**
  - 번들 크기 모니터링
  - 코드 분할 최적화
  - 의존성 트리 분석

#### Backend Quality Stack
- **SonarCloud** (`sonarcloud.io`)
  - 코드 복잡도 분석
  - 코드 냄새 검출
  - 테스트 커버리지 측정 (최소 50%)
  - 보안 취약점 검사

- **JaCoCo**
  - 테스트 커버리지 리포트
  - XML/HTML 형식 리포트
  - 커버리지 임계값 설정

- **JUnit 5 + Testcontainers**
  - 단위/통합 테스트
  - 실제 데이터베이스 테스트
  - 격리된 테스트 환경

### 🔄 자동화된 품질 관리

#### 의존성 관리 자동화
- **주간 스케줄** (매주 월요일 9시 UTC)
- **Frontend**: npm update → 테스트 → 자동 PR
- **Backend**: Gradle 의존성 업데이트 → 자동 PR
- **보안 감사**: 취약점 발견 시 자동 이슈 생성

#### 라이선스 컴플라이언스
- **허용 라이선스**: MIT, Apache-2.0, BSD, ISC
- **Frontend**: license-checker 도구
- **Backend**: Gradle license 플러그인
- **자동 리포트**: 라이선스 호환성 검증

### 📈 품질 메트릭 & 모니터링

| 구분 | 도구 | 목표 지표 | 현재 설정 |
|------|------|-----------|-----------|
| **코드 커버리지** | JaCoCo | 80%+ | 50%+ (최소) |
| **성능 점수** | Lighthouse | 90+ | PR마다 측정 |
| **보안 등급** | SonarCloud | A+ | 연속 모니터링 |
| **취약점** | Snyk/OWASP | 0 High | 실시간 스캔 |
| **코드 품질** | SonarCloud | A+ | 매 빌드마다 |

### 🚨 알림 & 대응 체계

```
Security Alert → Auto Issue Creation → Slack Notification
     ↓                    ↓                    ↓
  OWASP/Snyk          GitHub Issue         Dev Team Alert
```

- **심각 보안 취약점**: 즉시 이슈 생성 + Slack 알림
- **의존성 업데이트**: 주간 자동 PR + 리뷰 요청
- **빌드 실패**: 실시간 Slack 알림
- **커버리지 하락**: SonarCloud 품질 게이트 실패

## 🚀 배포 전략

1. **Feature Branch** → GitHub Actions CI 검증 (모든 품질/보안 검사 통과)
2. **Main Branch** → 자동 통합 테스트 + SonarCloud 분석
3. **Production Deploy** → Blue-Green 배포 (무중단)
4. **Rollback** → 이전 버전 자동 복구

## 🔧 개발 환경 설정

```bash
# 전체 환경 시작
docker-compose -f docker-compose.dev.yml up -d

# 접속 정보
Frontend: http://localhost:3000
Backend:  http://localhost:8080
PgAdmin:  http://localhost:5050
```

---

## 📐 프론트엔드 & 백엔드 상세 구조도

### 🎨 Frontend Architecture (Next.js)

```
┌─────────────────────────────────────────────────────────────────┐
│                     Next.js Application                        │
├─────────────────────────────────────────────────────────────────┤
│  App Router (src/app/)                                         │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ Pages         │ API Routes      │ Layout & Components       │ │
│  │ ├── /         │ ├── auth/       │ ├── layout.tsx           │ │
│  │ ├── /auth     │ └── [...].ts    │ ├── globals.css          │ │
│  │ ├── /property │                 │ └── middleware.ts        │ │
│  │ ├── /search   │                 │                          │ │
│  │ └── /dashboard│                 │                          │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Components Layer (src/components/)                             │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ Auth          │ Property        │ Maps                      │ │
│  │ ├── login     │ ├── dashboard   │ ├── google-map           │ │
│  │ ├── session   │ ├── registration│ ├── address-search       │ │
│  │ └── profile   │ └── management  │ └── property-marker      │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Shared Layer (src/lib/ & src/hooks/)                          │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ API Client    │ Auth Utils      │ Custom Hooks              │ │
│  │ ├── client.ts │ ├── auth/       │ ├── use-auth.ts          │ │
│  │ └── types/    │ └── middleware  │ ├── use-geolocation.ts   │ │
│  │               │                 │ └── use-places.ts        │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**규모 수준**: **중간 규모** (50+ 컴포넌트, 20+ 페이지)
- **코드 라인 수**: ~15,000 lines
- **컴포넌트 수**: 50+ 개
- **페이지 수**: 20+ 개
- **API 엔드포인트**: 15+ 개

### ⚙️ Backend Architecture (Spring Boot)

```
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                      │
├─────────────────────────────────────────────────────────────────┤
│  API Layer (Controller)                                        │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ Auth API      │ Property API    │ User API                  │ │
│  │ ├── /auth/*   │ ├── /properties │ ├── /users                │ │
│  │ ├── /oauth2/* │ ├── /search     │ └── /profile              │ │
│  │ └── /jwt/*    │ └── /register   │                          │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Application Layer (Service)                                   │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ Auth Service  │ Property Service│ Notification Service      │ │
│  │ ├── JWT       │ ├── CRUD        │ ├── Email                │ │
│  │ ├── OAuth2    │ ├── Search      │ ├── SMS                  │ │
│  │ └── Session   │ └── Validation  │ └── Push                 │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Domain Layer (Entity & Repository)                            │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ User Domain   │ Property Domain │ Transaction Domain        │ │
│  │ ├── User      │ ├── Property    │ ├── Lease                │ │
│  │ ├── Role      │ ├── Address     │ ├── Payment              │ │
│  │ └── Profile   │ └── Image       │ └── Review               │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Infrastructure Layer                                          │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ Security      │ Database        │ External APIs             │ │
│  │ ├── JWT       │ ├── PostgreSQL  │ ├── Google Maps          │ │
│  │ ├── OAuth2    │ ├── Redis       │ ├── Firebase             │ │
│  │ └── CORS      │ └── Flyway      │ └── AWS S3               │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**규모 수준**: **중대형 규모** (엔터프라이즈급)
- **코드 라인 수**: ~25,000 lines
- **클래스 수**: 200+ 개
- **API 엔드포인트**: 40+ 개
- **데이터베이스 테이블**: 25+ 개
- **도메인 모델**: 15+ 개

### 🗄️ Database Schema Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                        │
├─────────────────────────────────────────────────────────────────┤
│  Core Schemas                                                  │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ auth          │ property        │ transaction               │ │
│  │ ├── users     │ ├── properties  │ ├── leases               │ │
│  │ ├── roles     │ ├── addresses   │ ├── payments             │ │
│  │ ├── sessions  │ ├── images      │ └── reviews              │ │
│  │ └── oauth2    │ └── amenities   │                          │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
│                                                                 │
│  Supporting Schemas                                             │
│  ┌───────────────┬─────────────────┬───────────────────────────┐ │
│  │ notification  │ audit           │ geospatial                │ │
│  │ ├── messages  │ ├── audit_logs  │ ├── locations            │ │
│  │ ├── templates │ └── changes     │ └── boundaries           │ │
│  │ └── channels  │                 │                          │ │
│  └───────────────┴─────────────────┴───────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 🔗 시스템 통합 아키텍처

```
External Services
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Google Maps API │    │ Firebase FCM    │    │ AWS Services    │
│ ├── Geocoding   │    │ ├── Push Notify │    │ ├── S3 Storage  │
│ ├── Places API  │    │ └── Analytics   │    │ ├── RDS         │
│ └── Static Maps │    └─────────────────┘    │ └── CloudWatch  │
└─────────────────┘                           └─────────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Application Layer                            │
│  Frontend (React/Next.js) ↔ Backend (Spring Boot) ↔ Database  │
└─────────────────────────────────────────────────────────────────┘
```

### 📈 성능 & 확장성 지표

| 구분 | Frontend | Backend | Database |
|------|----------|---------|----------|
| **동시 사용자** | 1,000+ | 500+ | 200+ |
| **응답 시간** | < 2초 | < 500ms | < 100ms |
| **처리량** | 10K req/min | 5K req/min | 2K queries/min |
| **가용성** | 99.5% | 99.9% | 99.95% |

---

*이 문서는 HaniHome AU 플랫폼의 전체 인프라 구조를 간략하게 정리한 것입니다.*