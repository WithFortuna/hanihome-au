# Git Automation After Documentation

기존 문서화 전략을 참고하여 새롭게 작성된 문서를 파악하고, 해당 문서에 대해 적절한 git branch, commit, PR을 자동으로 생성하고 업로드합니다.

## Usage
```
/git-automation-after-documentation
```

## Steps

### 1. 문서 파일 감지 및 분석
1. Git status로 추적되지 않은 문서 파일 확인:
   - `backend/document/*.md`
   - `frontend/document/*.md` 
   - `frontend/hanihome-au/document/*.md`
2. 문서 파일명에서 태스크 정보 추출 (예: `backend-documentation-task6.md`)
3. TaskMaster에서 관련 태스크 정보 확인

### 2. 브랜치 전략 결정
- **태스크별 문서**: `docs/task-{id}-documentation` (예: `docs/task-6-documentation`)
- **일반 문서 업데이트**: `docs/documentation-update-{timestamp}`
- **다중 태스크 문서**: `docs/multi-task-documentation-{timestamp}`

### 3. Git 작업 자동화
1. 적절한 문서화 브랜치 생성
2. 문서 파일만 선별적으로 스테이징 (코드 파일 제외)
3. 컨벤셔널 커밋 메시지 생성
4. GitHub MCP를 사용하여 브랜치 푸시
5. 문서화 템플릿을 사용한 PR 생성
6. TaskMaster 태스크 상태 업데이트

### 4. 문서 유형별 처리

#### Task-specific Documentation
```bash
# 브랜치명: docs/task-{id}-documentation
# 커밋: docs(task-{id}): add comprehensive documentation for task {id}
# PR 제목: [Task {id}] Docs: Add comprehensive documentation
```

#### General Documentation Updates
```bash
# 브랜치명: docs/documentation-update-{YYYYMMDD-HHMMSS}
# 커밋: docs: update project documentation
# PR 제목: [Docs] Update project documentation
```

#### Multi-task Documentation
```bash
# 브랜치명: docs/multi-task-documentation-{YYYYMMDD-HHMMSS}
# 커밋: docs: add documentation for multiple completed tasks
# PR 제목: [Docs] Add documentation for completed development work
```

## Workflow Implementation

### Documentation Detection Logic
```bash
# 1. 추적되지 않은 문서 파일 찾기
git status --porcelain | grep "^??" | grep -E "\.(md)$" | grep -E "(document/|docs/)"

# 2. 파일명에서 태스크 정보 추출
# backend-documentation-task6.md -> task 6
# frontend-documentation-task3.md -> task 3
# general-documentation.md -> general

# 3. TaskMaster에서 해당 태스크 상태 확인
task-master show {task-id}
```

### Branch Creation Strategy
```bash
# 태스크별 문서화 브랜치
git checkout -b docs/task-{id}-documentation

# 일반 문서화 브랜치  
git checkout -b docs/documentation-update-$(date +%Y%m%d-%H%M%S)

# 다중 태스크 문서화 브랜치
git checkout -b docs/multi-task-documentation-$(date +%Y%m%d-%H%M%S)
```

### Commit Message Format
```
docs[({scope})]: <description> [(task {id})]

[optional body with documentation summary]

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### PR Template for Documentation
```markdown
## Documentation Summary
- Added comprehensive documentation for [task/feature]
- Documented implementation details and configurations
- Updated project documentation structure

## Documentation Type
- [ ] Task-specific documentation
- [ ] General project documentation  
- [ ] API documentation
- [ ] Configuration documentation
- [ ] Deployment documentation

## Changes Made
- Created/Updated: [list of documentation files]
- Covers: [summary of documented features/changes]

## Related Tasks
- Task ID: {task-id} (if applicable)
- Related PRs: [links if applicable]

🤖 Generated with [Claude Code](https://claude.ai/code)
```

## Safety Features

### File Validation
- Only stage `.md` files in documentation directories
- Prevent accidental staging of code files
- Validate documentation files exist and are readable
- Check for empty or malformed documentation files

### Error Handling
- Handle cases where documentation folders don't exist
- Graceful handling of git operation failures
- Provide clear feedback on staged files
- Rollback capability if operations fail

### TaskMaster Integration
- Update task status to "review" for documented tasks
- Add documentation completion notes to tasks
- Link PR information to TaskMaster tasks

## Auto-Detection Logic by File Pattern

### Task-Specific Documentation
- Pattern: `*-documentation-task*.md`
- Extract task ID from filename
- Create task-specific branch and commit
- Update TaskMaster task status

### General Documentation
- Pattern: `*-documentation.md` (without task suffix)
- Create general documentation branch
- Generic commit message
- No TaskMaster integration

### API Documentation
- Pattern: `*-api-*.md`
- Focus on API documentation branch naming
- Include API version information if available

## Example Execution Flow

1. **Detection Phase**:
   ```
   Found documentation files:
   - backend/document/backend-documentation-task6.md (Task 6)
   - frontend/hanihome-au/document/frontend-documentation-task6.md (Task 6)
   ```

2. **Branch Creation**:
   ```
   Creating branch: docs/task-6-documentation
   ```

3. **Staging & Commit**:
   ```
   Staging documentation files only...
   Creating commit: docs(task-6): add comprehensive documentation for task 6 implementation
   ```

4. **PR Creation**:
   ```
   Creating PR: [Task 6] Docs: Add comprehensive documentation for property management implementation
   ```

5. **TaskMaster Update**:
   ```
   Updating task 6 status with documentation completion note
   ```

이 커맨드는 문서화 워크플로우를 자동화하여 개발 완료 후 문서 작성 과정을 효율적으로 관리합니다.