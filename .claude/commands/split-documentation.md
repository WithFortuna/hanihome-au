# Split Documentation

기존 통합 문서를 프론트엔드와 백엔드로 자동 분리하는 전용 명령어입니다.

Arguments: $ARGUMENTS (파일 경로 또는 'all' 또는 'latest')

## 실행 과정:

### 1. 분리 대상 문서 식별
```bash
# 지정된 파일이 있으면 해당 파일만 처리
# 'all' 인경우 docs/ 디렉토리의 모든 통합 문서 처리
# 'latest' 인경우 가장 최근 생성된 통합 문서 처리
find . -name "*.md" -path "*/docs/*" | sort -t
```

### 2. 프로젝트 구조 확인
```bash
# frontend/document 및 backend/document 디렉토리 존재 확인
mkdir -p frontend/document backend/document
```

### 3. 스마트 콘텐츠 분석 및 분리

#### Frontend 섹션 식별 키워드:
- **UI/UX**: `component`, `page`, `interface`, `responsive`, `mobile`
- **React 관련**: `React`, `Next.js`, `useState`, `useEffect`, `props`, `state`
- **Frontend 도구**: `webpack`, `vite`, `tailwind`, `css`, `scss`, `typescript`
- **클라이언트 로직**: `browser`, `client`, `frontend`, `validation`, `form`

#### Backend 섹션 식별 키워드:
- **서버 관련**: `API`, `server`, `backend`, `endpoint`, `controller`, `service`
- **데이터베이스**: `database`, `schema`, `migration`, `query`, `SQL`, `NoSQL`
- **Java/Spring**: `Spring`, `Boot`, `JPA`, `Hibernate`, `Maven`, `Gradle`
- **인프라**: `AWS`, `Docker`, `kubernetes`, `nginx`, `redis`, `kafka`

### 4. 문서 생성 규칙

#### 파일명 규칙:
- **원본이 task 기반**: `task-N-*` → `frontend-documentation-taskN.md`, `backend-documentation-taskN.md`
- **원본이 번호 기반**: `documentation-N.md` → `frontend-documentation-N.md`, `backend-documentation-N.md`
- **원본이 일반**: `filename.md` → `frontend-filename.md`, `backend-filename.md`

#### 헤더 처리:
```markdown
# 원본 제목 - Frontend Implementation
# 원본 제목 - Backend Implementation
```

#### 교차 참조 링크 자동 생성:
```markdown
**Related Documentation:**
- Backend Implementation: [backend-documentation-task3.md](../backend/document/backend-documentation-task3.md)
- Frontend Implementation: [frontend-documentation-task3.md](../frontend/document/frontend-documentation-task3.md)
```

### 5. 고급 분리 로직

#### 공통 섹션 처리:
- **개요/요약**: 양쪽 문서에 모두 포함 (각각의 관점에서 수정)
- **아키텍처**: 전체 아키텍처는 양쪽에, 각 파트별 세부사항은 해당 문서에만
- **보안**: 클라이언트 보안은 Frontend에, 서버 보안은 Backend에
- **테스트**: 단위/통합 테스트는 각각 해당 문서에

#### 애매한 섹션 분류 규칙:
1. **API 문서**: 
   - Backend: API 구현, 스키마, 보안, 성능
   - Frontend: API 사용법, 에러 처리, 상태 관리

2. **데이터 모델**:
   - Backend: 엔티티, 스키마, 관계, 제약조건
   - Frontend: TypeScript 인터페이스, 상태 모델

3. **인증/권한**:
   - Backend: JWT 생성, 검증, 미들웨어, DB 저장
   - Frontend: 토큰 저장, 자동 갱신, 라우팅 가드

## 사용 예시:

```bash
# docs/task-3-user-profile-management-system.md 파일 분리
/split-documentation docs/task-3-user-profile-management-system.md

# docs 디렉토리의 모든 통합 문서 분리
/split-documentation all

# 가장 최근 생성된 통합 문서 분리
/split-documentation latest

# 특정 패턴의 문서들 분리
/split-documentation docs/*-system.md
```

## 출력 결과:

### 성공적인 분리시:
```
✅ Documentation Split Complete!

📄 Original Document: docs/task-3-user-profile-management-system.md (150KB)

📄 Generated Split Documents:
- Frontend: frontend/document/frontend-documentation-task3.md (75KB)
  └─ 8 sections: Components, API Integration, State Management, Forms, etc.
- Backend: backend/document/backend-documentation-task3.md (95KB)
  └─ 10 sections: Database, REST API, Security, AWS Integration, etc.

🔗 Cross-References Added:
- Frontend document references 15 backend endpoints
- Backend document references 12 frontend components
- 8 shared configuration sections properly distributed

🗑️ Original File Management:
- Original file moved to: docs/archive/task-3-user-profile-management-system.md.bak
- Timestamp: 2025-07-31T12:45:00Z

📋 Quality Metrics:
- Content distribution: 52% Backend, 48% Frontend
- Cross-references: 100% accurate
- Code blocks: 45 properly categorized
- Images/diagrams: 8 duplicated appropriately
```

## 분리 품질 검증:

### 자동 검증 항목:
1. **내용 완성도**: 원본의 모든 섹션이 적절히 분류되었는지 확인
2. **교차 참조**: 다른 파트를 참조하는 내용에 올바른 링크 생성
3. **코드 블록**: 코드가 올바른 파트에 배치되었는지 검증
4. **이미지/다이어그램**: 필요한 이미지가 양쪽에 적절히 복사되었는지 확인

### 수동 검토 권장사항:
- 분리된 문서의 논리적 흐름 확인
- 각 파트별 독립성 검증 (다른 문서 없이도 이해 가능한지)
- 중복 내용 최소화 확인
- 전문 용어 일관성 검증

이 명령어는 기존 통합 문서를 지능적으로 분석하여 프론트엔드와 백엔드 관점에서 최적화된 별도 문서로 변환합니다.