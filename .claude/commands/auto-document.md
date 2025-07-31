# Auto Document Progress

최근 완료된 Task Master 작업들을 자동으로 문서화하고 프론트엔드/백엔드별로 분리하여 저장하는 고급 문서화 명령어입니다.

Arguments: $ARGUMENTS (선택사항: task IDs 또는 'latest' 또는 'all' 또는 'split')

## 실행 과정:

### 1. 프로젝트 구조 자동 인식
```bash
# 프로젝트 루트에서 문서 디렉토리 구조 확인
find . -name "document" -type d
# 결과: ./frontend/document 및 ./backend/document 감지
```

### 2. Task Master 작업 조회 및 분석
- **완료된 작업 조회**: `task-master get-tasks --status=done`
- **지정된 ID 처리**: $ARGUMENTS에 task ID가 있으면 해당 작업만 대상
- **작업 카테고리 자동 분류**:
  - Frontend: UI/UX, React, Next.js, 클라이언트 관련
  - Backend: API, 데이터베이스, 서버, 인프라 관련
  - Full-stack: 양쪽 모두 포함하는 작업

### 3. 기존 문서 번호 체계 파악
```bash
# 프론트엔드 문서 번호 확인
ls frontend/document/ | grep -E "frontend-documentation-[0-9]+\.md" | sort -V
# 백엔드 문서 번호 확인  
ls backend/document/ | grep -E "backend-documentation-[0-9]+\.md" | sort -V
# 다음 번호 자동 계산 (현재 최고번호 + 1)
```

### 4. 문서 생성 전략

#### A. 단일 Task 문서화 (task ID 지정시)
- **파일명**: `frontend-documentation-task{ID}.md`, `backend-documentation-task{ID}.md`
- **내용 분리**: Task의 frontend/backend 구현 내용을 각각 해당 디렉토리에 저장

#### B. 일반 번호 문서화 (번호 지정시)
- **파일명**: `frontend-documentation-{N}.md`, `backend-documentation-{N}.md`
- **내용**: 여러 완료된 작업들을 종합한 문서

### 5. 스마트 문서 분리 로직

#### Frontend 문서 포함 내용:
- **UI/UX 구현**: React 컴포넌트, 페이지 구조, 스타일링
- **상태 관리**: Redux, Context API, React Query 등
- **API 연동**: axios, fetch, 데이터 페칭 로직
- **폼 처리**: React Hook Form, 유효성 검사
- **라우팅**: Next.js 라우팅, 페이지 전환
- **성능 최적화**: 코드 스플리팅, 이미지 최적화
- **테스트**: Jest, React Testing Library
- **빌드/배포**: Webpack, Next.js 빌드 설정

#### Backend 문서 포함 내용:
- **API 엔드포인트**: REST API, GraphQL 스키마
- **데이터베이스**: 스키마, 마이그레이션, 쿼리 최적화
- **비즈니스 로직**: 서비스 레이어, 비즈니스 규칙
- **인증/보안**: JWT, OAuth, 권한 관리
- **인프라**: AWS 설정, Docker, CI/CD
- **모니터링**: 로깅, 메트릭, 헬스체크
- **테스트**: 단위 테스트, 통합 테스트
- **성능**: 캐싱, 쿼리 최적화, 스케일링

### 6. 고급 기능

#### 문서 연결 및 참조
- 프론트엔드 문서에서 관련 백엔드 API 참조 링크 자동 생성
- 백엔드 문서에서 관련 프론트엔드 구현 참조 링크 자동 생성
- 이전/다음 문서와의 연결 관계 명시

#### 코드 블록 자동 추출
- 실제 구현된 코드 파일에서 관련 코드 블록 자동 추출
- 설정 파일, 환경 변수, 스크립트 내용 포함
- 코드 예제와 설명 자동 매칭

## 사용 예시:

```bash
# 최근 완료된 모든 작업을 프론트엔드/백엔드별로 분리 문서화
/auto-document

# Task 3 작업을 프론트엔드/백엔드별로 분리 문서화 (기존처럼)
/auto-document 3

# 여러 작업들을 종합하여 문서화
/auto-document 1,2,3

# 기존 통합 문서를 프론트엔드/백엔드로 분리
/auto-document split

# 가장 최근 완료된 작업만 문서화
/auto-document latest

# 모든 완료된 작업 종합 문서화 (주의: 대용량)
/auto-document all
```

## 출력 결과:

### 성공적인 문서 생성시:
```
✅ Auto Documentation Complete!

📄 Generated Documents:
- Frontend: frontend/document/frontend-documentation-task3.md
- Backend:  backend/document/backend-documentation-task3.md

📊 Documentation Summary:
- Tasks processed: 1 (Task 3: 사용자 프로필 관리 기능 구현)
- Frontend sections: 8 (Components, API Integration, State Management, etc.)
- Backend sections: 9 (Database, REST API, Security, AWS Integration, etc.)
- Total content: ~60KB (Frontend: ~30KB, Backend: ~45KB)

🔗 Cross-References:
- Frontend document references 12 backend API endpoints
- Backend document references 8 frontend components
- 5 shared configuration files documented in both

📋 Next Steps:
- Review generated documentation for accuracy
- Update any specific implementation details
- Consider creating additional task-specific documentation
```

## 스마트 분류 규칙:

### Frontend 키워드 감지:
- `React`, `Next.js`, `component`, `page`, `UI`, `UX`, `form`, `validation`
- `frontend`, `client`, `browser`, `responsive`, `mobile`
- `state`, `props`, `hook`, `context`, `router`
- `.tsx`, `.jsx`, `/src/`, `/components/`, `/pages/`

### Backend 키워드 감지:
- `API`, `endpoint`, `controller`, `service`, `repository`
- `database`, `schema`, `migration`, `query`, `SQL`
- `Spring`, `Boot`, `JPA`, `Hibernate`
- `AWS`, `S3`, `RDS`, `Redis`, `Docker`
- `.java`, `/backend/`, `/api/`, `/src/main/`

### 양방향 작업 처리:
- Full-stack 작업은 양쪽 문서에 모두 포함하되, 각각의 관점에서 작성
- API 설계: Backend에서는 구현 세부사항, Frontend에서는 사용법 중심
- 데이터 모델: Backend에서는 엔티티/스키마, Frontend에서는 타입/인터페이스

이 개선된 명령어는 프로젝트의 실제 구조를 정확히 인식하고, 각 파트별로 적절한 내용을 분리하여 고품질의 기술 문서를 자동 생성합니다.