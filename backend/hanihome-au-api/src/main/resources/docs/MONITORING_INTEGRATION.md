# CloudWatch 및 Sentry 모니터링 연동 구현

## 개요

HaniHome AU API의 인프라 모니터링 자동화 시스템이 완료되었습니다. 이 시스템은 CloudWatch 메트릭 수집, Sentry 에러 추적, SNS 알림 연동을 포함한 종합적인 모니터링 솔루션을 제공합니다.

## 구현된 컴포넌트

### 1. CloudWatch 메트릭 시스템

#### 설정 파일
- `MonitoringConfig.java` - CloudWatch 메트릭 레지스트리 설정
- `AWSConfig.java` - AWS 서비스 클라이언트 구성

#### 커스텀 메트릭
- **리뷰 작성률** (`hanihome.review.submission.rate`)
- **신고 처리율** (`hanihome.report.processing.rate`)
- **대시보드 성능** (`hanihome.dashboard.load.time`)
- **리뷰 품질 지표** (`hanihome.review.trust.score.average`)
- **시스템 활동 지표** (활성 사용자, 매물, 신고 수 등)

### 2. CloudWatch 알람 시스템

#### 자동 생성 알람
- **높은 에러율** - 5분간 10개 이상의 5xx 에러
- **느린 응답 시간** - 평균 5초 이상의 응답 시간
- **높은 메모리 사용률** - 85% 이상의 메모리 사용
- **대기 중인 신고 수** - 50개 이상의 미처리 신고
- **낮은 리뷰 작성률** - 시간당 5개 미만의 리뷰
- **데이터베이스 연결 문제** - 평균 10초 이상의 쿼리 시간

#### 기능
- SNS 연동을 통한 자동 알림
- 동적 커스텀 알람 생성
- 알람 상태 모니터링

### 3. Sentry 에러 추적

#### 기능
- 비즈니스 로직 에러 추적
- 보안 이벤트 모니터링
- 성능 이슈 자동 탐지
- 사용자 컨텍스트 자동 추가

#### 필터링
- 헬스체크 관련 에러 제외
- 사용자 정보 및 요청 컨텍스트 자동 추가
- 환경별 태그 설정

### 4. 스케줄링 시스템

#### 정기 작업
- **5분마다**: 커스텀 메트릭 업데이트
- **15분마다**: 시스템 헬스 체크
- **1시간마다**: 상세 리포팅 및 분석
- **매일 자정**: 일간 요약 리포트

## 설정 방법

### 1. 환경 변수 설정

```bash
# AWS 설정
AWS_REGION=ap-southeast-2
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# CloudWatch 설정
CLOUDWATCH_METRICS_ENABLED=true
CLOUDWATCH_NAMESPACE=HaniHome/AU/Production
AWS_CLOUDWATCH_SNS_TOPIC_ARN=arn:aws:sns:ap-southeast-2:account:hanihome-alerts

# Sentry 설정
SENTRY_DSN=https://your-dsn@sentry.io/project-id
SENTRY_ENVIRONMENT=production
SENTRY_SAMPLE_RATE=1.0
SENTRY_TRACES_SAMPLE_RATE=0.1
```

### 2. application.yml 설정

주요 설정이 프로파일별로 구성되어 있습니다:

- **Development**: 모든 액츄에이터 엔드포인트 노출
- **Staging**: CloudWatch 메트릭 활성화
- **Production**: 제한된 엔드포인트, 전체 모니터링 활성화

### 3. SNS 토픽 설정

AWS SNS에서 알림 토픽을 생성하고 구독자를 설정:

```bash
# SNS 토픽 생성
aws sns create-topic --name hanihome-alerts --region ap-southeast-2

# 이메일 구독 추가
aws sns subscribe \
  --topic-arn arn:aws:sns:ap-southeast-2:account:hanihome-alerts \
  --protocol email \
  --notification-endpoint admin@hanihome.com.au
```

## API 엔드포인트

### 모니터링 관리 (관리자 전용)

```bash
# 모니터링 상태 확인
GET /api/v1/monitoring/status

# CloudWatch 알람 상태 조회
GET /api/v1/monitoring/alarms

# 메트릭 테스트
POST /api/v1/monitoring/test/metrics?type=all&count=100

# Sentry 에러 테스트
POST /api/v1/monitoring/test/sentry?errorType=exception

# 성능 테스트
POST /api/v1/monitoring/test/performance?delayMs=2000

# 게이지 메트릭 업데이트
POST /api/v1/monitoring/test/gauges

# 커스텀 메트릭 업데이트
POST /api/v1/monitoring/test/custom-metrics

# 커스텀 알람 생성
POST /api/v1/monitoring/test/alarms?name=TestAlarm&metricName=test.metric&threshold=10.0
```

## 메트릭 가이드

### 주요 메트릭 목록

#### 카운터 메트릭
- `hanihome.property.views` - 매물 조회 수
- `hanihome.property.created` - 매물 등록 수
- `hanihome.user.registrations` - 사용자 가입 수
- `hanihome.review.submitted` - 리뷰 작성 수
- `hanihome.report.processed` - 신고 처리 수
- `hanihome.search.requests` - 검색 요청 수

#### 게이지 메트릭
- `hanihome.users.active` - 활성 사용자 수
- `hanihome.properties.active` - 활성 매물 수
- `hanihome.reports.pending` - 대기 중인 신고 수
- `hanihome.review.submission.rate` - 리뷰 작성률
- `hanihome.report.processing.rate` - 신고 처리율

#### 타이머 메트릭
- `hanihome.search.duration` - 검색 수행 시간
- `hanihome.database.query.duration` - 데이터베이스 쿼리 시간
- `hanihome.external.api.duration` - 외부 API 호출 시간

### 커스텀 메트릭 사용법

```java
@Autowired
private MetricsService metricsService;

// 카운터 증가
metricsService.incrementPropertyView("APARTMENT", "Melbourne");

// 타이머 측정
Timer.Sample sample = metricsService.startPropertySearchTimer();
// ... 작업 수행 ...
metricsService.recordPropertySearchDuration(sample, "location", true);

// 게이지 업데이트
metricsService.updateActiveUsers(1250);

// 커스텀 메트릭
metricsService.incrementCustomCounter("my.metric", "Description", "tag", "value");
```

## 알람 설정 가이드

### 알람 임계값 권장사항

| 메트릭 | 임계값 | 설명 |
|--------|--------|------|
| 에러율 | 10 errors/5min | 5분간 10개 이상의 5xx 에러 |
| 응답시간 | 5 seconds | 평균 응답시간 5초 초과 |
| 메모리 사용률 | 85% | JVM 메모리 사용률 85% 초과 |
| 대기 신고 | 50 reports | 미처리 신고 50개 초과 |
| 리뷰 작성률 | < 5 reviews/hour | 시간당 리뷰 5개 미만 |

### 커스텀 알람 생성

```java
@Autowired
private CloudWatchAlarmService alarmService;

// 커스텀 알람 생성
alarmService.createCustomAlarm(
    "HighPropertyViews",           // 알람명
    "hanihome.property.views",     // 메트릭명
    1000.0,                        // 임계값
    ComparisonOperator.GREATER_THAN_THRESHOLD,
    2                              // 평가 기간
);
```

## 문제 해결

### 일반적인 문제들

1. **CloudWatch 메트릭이 표시되지 않음**
   - AWS 자격증명 확인
   - 리전 설정 확인
   - IAM 권한 확인 (CloudWatch:PutMetricData)

2. **Sentry 에러가 전송되지 않음**
   - SENTRY_DSN 설정 확인
   - 네트워크 연결 확인
   - 샘플링 비율 확인

3. **알람이 작동하지 않음**
   - SNS 토픽 ARN 확인
   - 구독 상태 확인
   - 메트릭 데이터 생성 확인

### 디버깅 방법

```bash
# 로그 레벨 조정
logging.level.com.hanihome.hanihome_au_api.application.monitoring=DEBUG

# 액츄에이터 엔드포인트 확인
curl http://localhost:8080/api/v1/actuator/metrics
curl http://localhost:8080/api/v1/actuator/health

# 모니터링 상태 확인
curl http://localhost:8080/api/v1/monitoring/status
```

## 보안 고려사항

1. **API 엔드포인트 보안**
   - 모든 테스트 엔드포인트는 ADMIN 권한 필요
   - 프로덕션에서는 모니터링 컨트롤러 비활성화

2. **민감 정보 보호**
   - 에러 메시지에서 민감 정보 필터링
   - 사용자 컨텍스트 최소화

3. **리소스 제한**
   - 메트릭 수집 빈도 제한
   - 배치 크기 제한

## 성능 최적화

1. **메트릭 수집 최적화**
   - 비동기 전송 사용
   - 배치 처리 활용
   - 캐싱 적용

2. **알람 최적화**
   - 적절한 평가 기간 설정
   - 불필요한 알람 제거
   - 알람 피로도 방지

## 모니터링 대시보드

### CloudWatch 대시보드 구성

1. **애플리케이션 메트릭**
   - 요청 수, 응답 시간, 에러율
   - 사용자 활동 지표
   - 비즈니스 메트릭

2. **인프라 메트릭**
   - CPU, 메모리, 네트워크
   - 데이터베이스 성능
   - JVM 메트릭

3. **알람 상태**
   - 활성 알람 목록
   - 알람 히스토리
   - 알림 상태

이 문서는 CloudWatch 및 Sentry 모니터링 연동 구현의 완전한 가이드입니다. 추가 질문이나 구체적인 설정 도움이 필요하면 개발팀에 문의하시기 바랍니다.