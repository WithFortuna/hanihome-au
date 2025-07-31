# Manage Documentation

프로젝트의 문서 구조를 관리하고 번호 체계를 유지하는 유틸리티 명령어입니다.

Arguments: $ARGUMENTS (action: 'list', 'next-number', 'reorganize', 'validate')

## 주요 기능:

### 1. 문서 목록 조회 (`list`)
현재 프로젝트의 모든 문서를 구조화하여 보여줍니다.

```bash
/manage-docs list
```

#### 출력 예시:
```
📚 HaniHome AU - Documentation Structure

Frontend Documents (frontend/document/):
├── frontend-documentation.md (base documentation)
├── frontend-documentation-2.md (환경 설정 시스템)
├── frontend-documentation-3.md (사용자 인증 시스템)
├── frontend-documentation-4.md (매물 관리 시스템)
├── frontend-documentation-5.md (Google Maps 연동)
└── frontend-documentation-task3.md (사용자 프로필 관리)

Backend Documents (backend/document/):
├── backend-documentation.md (base documentation)
├── backend-documentation-2.md (AWS 인프라 구축)
├── backend-documentation-3.md (Spring Boot API 구현)
├── backend-documentation-4.md (PostgreSQL 최적화)
├── backend-documentation-5.md (보안 시스템 구현)
└── backend-documentation-task3.md (사용자 프로필 API)

Archive Documents (docs/archive/):
├── old-documentation-v1.md
└── task-3-user-profile-management-system.md.bak

📊 Summary:
- Frontend: 6 documents (total ~180KB)
- Backend: 6 documents (total ~320KB)
- Next available numbers: Frontend #6, Backend #6
- Task-specific docs: 2 (task3)
```

### 2. 다음 번호 확인 (`next-number`)
새 문서를 생성할 때 사용할 번호를 확인합니다.

```bash
/manage-docs next-number
```

#### 출력 예시:
```
📋 Next Available Documentation Numbers:

Frontend: #6
- Last document: frontend-documentation-5.md
- Suggested naming: frontend-documentation-6.md

Backend: #6  
- Last document: backend-documentation-5.md
- Suggested naming: backend-documentation-6.md

Task-specific: task4, task5, task6...
- Existing: task3
- Next available: task4, task5, task6, etc.

💡 Usage Examples:
- For general documentation: frontend-documentation-6.md
- For specific task: frontend-documentation-task4.md
- For feature-based: frontend-documentation-maps-integration.md
```

### 3. 문서 재정리 (`reorganize`)
기존 문서들을 번호 체계에 맞게 재정리합니다.

```bash
/manage-docs reorganize
```

#### 수행 작업:
1. **번호 간격 정리**: 빠진 번호가 있으면 순서대로 재정렬
2. **파일명 표준화**: 일관된 명명 규칙 적용
3. **중복 제거**: 같은 내용의 문서가 있으면 통합 제안
4. **아카이브 정리**: 오래된 문서를 archive 폴더로 이동

### 4. 문서 유효성 검사 (`validate`)
문서들의 구조와 링크를 검사합니다.

```bash
/manage-docs validate
```

#### 검사 항목:
1. **파일명 규칙**: 올바른 명명 규칙을 따르는지 확인
2. **내부 링크**: 다른 문서로의 링크가 유효한지 검사
3. **교차 참조**: Frontend ↔ Backend 문서 간 참조 링크 검증
4. **마크다운 문법**: 문법 오류 및 구조 문제 확인
5. **중복 내용**: 여러 문서에 중복된 내용이 있는지 확인

## 고급 사용 예시:

### 전체 문서 관리 워크플로:
```bash
# 1. 현재 상태 확인
/manage-docs list

# 2. 문서 유효성 검사
/manage-docs validate

# 3. 필요시 재정리
/manage-docs reorganize

# 4. 새 문서 번호 확인
/manage-docs next-number

# 5. 새 문서 생성 후 다시 확인
/manage-docs list
```

### 문서 생성 가이드라인:

#### 일반 문서 (연속 번호):
- **Frontend**: `frontend-documentation-N.md`
- **Backend**: `backend-documentation-N.md`
- **용도**: 여러 작업을 종합한 주제별 문서

#### Task 기반 문서:
- **Frontend**: `frontend-documentation-taskN.md`
- **Backend**: `backend-documentation-taskN.md`
- **용도**: 특정 TaskMaster 작업의 상세 구현 문서

#### 기능 기반 문서:
- **Frontend**: `frontend-documentation-feature-name.md`
- **Backend**: `backend-documentation-feature-name.md`
- **용도**: 특정 기능/모듈의 전문 문서

## 출력 결과 예시:

### validate 실행시:
```
🔍 Documentation Validation Report

✅ File Naming:
- All frontend documents follow naming convention
- All backend documents follow naming convention
- No duplicate filenames found

⚠️ Internal Links:
- frontend-documentation-3.md: 2 broken links to backend APIs
- backend-documentation-4.md: 1 broken link to frontend component

✅ Cross-References:
- Frontend → Backend: 25 valid references
- Backend → Frontend: 18 valid references

⚠️ Content Issues:
- Duplicate section in frontend-documentation-2.md and 4.md
- Missing code examples in backend-documentation-3.md

📋 Recommendations:
1. Fix broken links in frontend-documentation-3.md
2. Update API endpoints in backend-documentation-4.md
3. Consider merging duplicate sections
4. Add missing code examples

Overall Score: 8.5/10 (Good)
```

### reorganize 실행시:
```
🗂️ Documentation Reorganization Complete

📝 Changes Made:
- Renamed 'frontend-docs-old.md' → 'frontend-documentation-1.md'
- Moved 'old-backend-stuff.md' → 'docs/archive/'
- Updated 15 internal references
- Standardized 8 filenames

📊 Before → After:
- Frontend documents: 7 → 6 (1 archived)
- Backend documents: 8 → 6 (2 archived) 
- Numbering gaps: 3 → 0
- Broken links: 8 → 0

✨ Directory Structure:
Frontend: 1, 2, 3, 4, 5, task3 ✓
Backend: 1, 2, 3, 4, 5, task3 ✓
Archive: 4 old documents moved
```

이 명령어는 문서 관리의 복잡성을 줄이고 일관된 문서 구조를 유지하는 데 도움을 줍니다.