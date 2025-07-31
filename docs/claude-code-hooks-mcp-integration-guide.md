# Claude Code 훅(Hooks)과 MCP 통합 가이드

## 목차
1. [개요](#개요)
2. [훅 이벤트 타입](#훅-이벤트-타입)
3. [훅 액션 종류](#훅-액션-종류)
4. [MCP 기본 기능](#mcp-기본-기능)
5. [통합 활용 시나리오](#통합-활용-시나리오)
6. [설정 예시](#설정-예시)
7. [고급 활용 패턴](#고급-활용-패턴)
8. [제한사항 및 주의사항](#제한사항-및-주의사항)
9. [실제 구현 예시](#실제-구현-예시)

---

## 개요

Claude Code는 **훅(Hooks)**과 **MCP(Model Context Protocol)**를 통해 강력한 확장성과 자동화 기능을 제공합니다.

- **훅(Hooks)**: 특정 이벤트 발생 시 자동으로 실행되는 명령어나 스크립트
- **MCP**: 외부 도구와 데이터 소스에 접근할 수 있는 클라이언트-서버 아키텍처

이 두 기능을 조합하면 완전 자동화된 개발 환경을 구축할 수 있습니다.

---

## 훅 이벤트 타입

### 1. PreToolUse
도구 사용 전에 실행되는 훅입니다.

```json
{
  "PreToolUse": [
    {
      "matcher": "Write",
      "hooks": [
        {
          "type": "command",
          "command": "validate-write.sh"
        }
      ]
    }
  ]
}
```

**활용 사례**:
- 파일 쓰기 전 보안 검증
- 코드 품질 검사
- 백업 생성

### 2. PostToolUse
도구 사용 후에 실행되는 훅입니다.

```json
{
  "PostToolUse": [
    {
      "matcher": "Bash",
      "hooks": [
        {
          "type": "command",
          "command": "log-bash-usage.sh"
        }
      ]
    }
  ]
}
```

**활용 사례**:
- 로깅 및 감사
- 자동 테스트 실행
- 배포 트리거

### 3. UserPromptSubmit
사용자 프롬프트 제출 시 실행되는 훅입니다.

```json
{
  "UserPromptSubmit": [
    {
      "hooks": [
        {
          "type": "command",
          "command": "add-context.sh"
        }
      ]
    }
  ]
}
```

**활용 사례**:
- 프로젝트별 컨텍스트 자동 추가
- 사용자 입력 전처리
- 세션 상태 관리

### 4. Notification
알림 이벤트 발생 시 실행되는 훅입니다.

**활용 사례**:
- 도구 권한 요청 시 알림
- 유휴 상태 감지
- 외부 시스템 알림

### 5. Stop/SubagentStop
응답 완료 시 실행되는 훅입니다.

**활용 사례**:
- 세션 정리
- 최종 상태 저장
- 보고서 생성

---

## 훅 액션 종류

### 기본 액션 타입

#### 1. Command 실행
```json
{
  "type": "command",
  "command": "your-script.sh",
  "timeout": 30000
}
```

- Bash 명령어 실행
- 타임아웃 설정 가능
- `$CLAUDE_PROJECT_DIR` 환경변수 사용 가능

#### 2. 제어 메커니즘

**Exit Code 기반**:
- `0`: 성공/허용
- `2`: 차단/거부
- 기타: 오류

**JSON 출력 기반**:
```json
{
  "continue": true,
  "decision": "allow",
  "reason": "검증 통과",
  "additionalContext": "프로젝트 정보: ..."
}
```

### 가능한 액션들

| 액션 | 설명 | 사용 예시 |
|------|------|-----------|
| **차단** | 특정 도구 사용 방지 | 민감한 파일 수정 방지 |
| **컨텍스트 주입** | 프롬프트에 추가 정보 삽입 | 프로젝트 가이드라인 추가 |
| **입력 수정** | 도구 파라미터 변경 | 파일 경로 정규화 |
| **로깅** | 모든 활동 기록 | 감사 로그 생성 |
| **알림** | 외부 시스템에 이벤트 전송 | Slack 알림, 이메일 발송 |
| **검증** | 보안 스캔, 코드 품질 체크 | 취약점 스캔, 린팅 |
| **자동화** | 백업, 배포, 테스트 실행 | CI/CD 파이프라인 트리거 |

---

## MCP 기본 기능

### 서버 타입

1. **Stdio 서버**: 표준 입출력 통신
2. **SSE 서버**: Server-Sent Events
3. **HTTP 서버**: REST API 통신

### 스코프 레벨

- **Local**: 개인용, 프로젝트별 설정
- **Project**: 팀 공유 설정
- **User**: 프로젝트 간 공유

### 주요 기능

- **리소스 접근**: "@" 멘션으로 외부 데이터 접근
- **인증**: OAuth 2.0 지원
- **다양한 데이터 타입**: text, JSON, 구조화된 데이터

### MCP 서버 설정 예시

```json
{
  "mcpServers": {
    "taskmaster-ai": {
      "command": "npx",
      "args": ["-y", "--package=task-master-ai", "task-master-ai"],
      "env": {
        "ANTHROPIC_API_KEY": "sk-...",
        "PERPLEXITY_API_KEY": "pplx-..."
      }
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "ghp_..."
      }
    }
  }
}
```

---

## 통합 활용 시나리오

### 1. 개발 워크플로우 자동화

```bash
# 파일 쓰기 전: 코드 품질 검사
./hooks/pre-write-check.sh
  ├─ MCP TaskMaster: 작업 상태 확인
  ├─ 린터 실행
  └─ 보안 스캔

# 파일 쓰기 후: 자동 업데이트
./hooks/post-write-update.sh
  ├─ MCP TaskMaster: 진행상황 업데이트
  ├─ Git 커밋 생성
  └─ MCP GitHub: PR 업데이트
```

### 2. 보안 강화 시나리오

```bash
# 민감한 작업 전 검증
./hooks/security-gate.sh
  ├─ 파일 내용 스캔
  ├─ API 키 노출 검사
  ├─ 권한 확인
  └─ 승인/차단 결정
```

### 3. 팀 협업 자동화

```bash
# 코드 변경 시 팀 알림
./hooks/team-notification.sh
  ├─ MCP Slack: 채널 알림
  ├─ MCP GitHub: 리뷰어 할당
  └─ MCP TaskMaster: 상태 공유
```

### 4. 품질 관리 자동화

```bash
# 코드 품질 파이프라인
./hooks/quality-pipeline.sh
  ├─ 테스트 실행
  ├─ 코버리지 체크
  ├─ 성능 벤치마크
  └─ 품질 리포트 생성
```

---

## 설정 예시

### 기본 훅 설정

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/pre-write-security-check.sh"
          }
        ]
      },
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command", 
            "command": "./hooks/pre-bash-validation.sh"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/post-write-taskmaster-update.sh"
          }
        ]
      }
    ],
    "UserPromptSubmit": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/add-project-context.sh"
          }
        ]
      }
    ]
  }
}
```

### 훅 스크립트 예시

**pre-write-security-check.sh**:
```bash
#!/bin/bash

# stdin으로 도구 파라미터 받기
input=$(cat)

# API 키 패턴 검사
if echo "$input" | grep -q "sk-\|pk-\|api_key"; then
    echo '{"continue": false, "reason": "API 키가 포함되어 있습니다"}'
    exit 2
fi

# 통과
echo '{"continue": true}'
exit 0
```

**post-write-taskmaster-update.sh**:
```bash
#!/bin/bash

# TaskMaster MCP를 통해 진행상황 업데이트
claude mcp call taskmaster update_subtask \
  --id="$CURRENT_TASK_ID" \
  --prompt="파일 수정 완료: $1"
```

---

## 고급 활용 패턴

### 1. 멀티 MCP 서버 조합

```bash
#!/bin/bash
# 복합 워크플로우 훅

# 1. TaskMaster에서 현재 작업 확인
task_info=$(claude mcp call taskmaster get_task --id="$TASK_ID")

# 2. GitHub에서 관련 이슈 확인
github_status=$(claude mcp call github get_issue --number="$ISSUE_NUMBER")

# 3. 조건부 실행
if [[ "$task_info" == *"pending"* ]]; then
    # Slack 알림
    claude mcp call slack notify \
      --channel="dev" \
      --message="작업 $TASK_ID 시작됨"
fi
```

### 2. 조건부 워크플로우

```bash
#!/bin/bash
# 프로젝트 타입별 다른 처리

project_type=$(cat package.json | jq -r '.type // "unknown"')

case "$project_type" in
    "frontend")
        ./hooks/frontend-quality-check.sh
        ;;
    "backend")
        ./hooks/backend-security-scan.sh
        ;;
    *)
        ./hooks/general-validation.sh
        ;;
esac
```

### 3. 실시간 협업 시스템

```json
{
  "PostToolUse": [
    {
      "matcher": "Write",
      "hooks": [
        {
          "type": "command",
          "command": "./hooks/realtime-collaboration.sh"
        }
      ]
    }
  ]
}
```

```bash
#!/bin/bash
# realtime-collaboration.sh

file_path="$1"
change_type="write"

# 1. 변경사항을 팀에게 실시간 알림
claude mcp call slack notify \
  --channel="dev-changes" \
  --message="📝 $USER가 $file_path 수정"

# 2. GitHub 이슈에 진행상황 업데이트
claude mcp call github add_issue_comment \
  --issue="$RELATED_ISSUE" \
  --comment="파일 수정: $file_path"

# 3. TaskMaster 진행률 업데이트
claude mcp call taskmaster update_subtask \
  --id="$CURRENT_SUBTASK" \
  --prompt="$file_path 구현 완료"
```

---

## 제한사항 및 주의사항

### 🔒 보안 위험

**제3자 MCP 서버 사용 시 주의사항**:
- Prompt injection 공격 위험
- 인터넷 연결 서버는 특히 위험
- 신뢰할 수 있는 서버만 사용

**보안 모범 사례**:
```bash
# 훅에서 입력 검증
validate_input() {
    local input="$1"
    # 위험한 패턴 검사
    if echo "$input" | grep -qE "(rm -rf|sudo|eval)"; then
        return 1
    fi
    return 0
}
```

### ⚡ 성능 고려사항

**훅 실행 시간 최적화**:
- 비동기 처리 사용
- 캐싱 활용
- 타임아웃 설정

```bash
# 비동기 처리 예시
{
    long_running_task &
    echo '{"continue": true}'
} < /dev/null
```

**MCP 서버 응답 최적화**:
- 연결 풀링
- 결과 캐싱
- 재시도 로직

### 🔧 복잡성 관리

**디버깅 전략**:
```bash
# 디버그 로깅
DEBUG_HOOKS=true claude

# 훅 실행 로그
echo "[$(date)] Hook: $0, Input: $input" >> hooks.log
```

**의존성 관리**:
- MCP 서버 상태 확인
- 순환 의존성 방지
- 오류 복구 전략

---

## 실제 구현 예시

### 완전한 설정 파일

**.claude/settings.json**:
```json
{
  "mcpServers": {
    "taskmaster-ai": {
      "command": "npx",
      "args": ["-y", "--package=task-master-ai", "task-master-ai"],
      "env": {
        "ANTHROPIC_API_KEY": "sk-ant-...",
        "PERPLEXITY_API_KEY": "pplx-..."
      }
    },
    "github": {
      "command": "npx", 
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "ghp_..."
      }
    },
    "slack": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "xoxb-..."
      }
    }
  },
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/pre-write-security-check.sh",
            "timeout": 10000
          }
        ]
      },
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/pre-bash-validation.sh",
            "timeout": 5000
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/post-write-integration.sh"
          }
        ]
      },
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/post-bash-logging.sh"
          }
        ]
      }
    ],
    "UserPromptSubmit": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/add-session-context.sh"
          }
        ]
      }
    ]
  },
  "allowedTools": [
    "Edit",
    "Read", 
    "Write",
    "Bash",
    "mcp__taskmaster_ai__*",
    "mcp__github__*"
  ]
}
```

### 훅 스크립트 디렉토리 구조

```
hooks/
├── pre-write-security-check.sh    # 쓰기 전 보안 검사
├── post-write-integration.sh      # 쓰기 후 통합 처리
├── pre-bash-validation.sh         # Bash 전 검증
├── post-bash-logging.sh           # Bash 후 로깅
├── add-session-context.sh         # 세션 컨텍스트 추가
├── utils/
│   ├── security-scanner.sh        # 보안 스캔 유틸
│   ├── mcp-helper.sh              # MCP 호출 헬퍼
│   └── notification.sh            # 알림 헬퍼
└── config/
    ├── security-rules.json        # 보안 규칙
    └── project-context.json       # 프로젝트 컨텍스트
```

### 마스터 통합 스크립트

**hooks/post-write-integration.sh**:
```bash
#!/bin/bash
set -euo pipefail

# 설정 로드
source "./hooks/utils/mcp-helper.sh"
source "./hooks/utils/notification.sh"

# 입력 파싱
input=$(cat)
file_path=$(echo "$input" | jq -r '.file_path // ""')
content_preview=$(echo "$input" | jq -r '.content' | head -3)

# 1. TaskMaster 업데이트
if [[ -n "$CURRENT_TASK_ID" ]]; then
    update_taskmaster_progress "$CURRENT_TASK_ID" "파일 수정: $file_path"
fi

# 2. GitHub 통합
if [[ -n "$GITHUB_ISSUE" ]]; then
    update_github_issue "$GITHUB_ISSUE" "구현 진행: $file_path"
fi

# 3. 팀 알림
if [[ "$file_path" =~ \.(js|ts|py|java)$ ]]; then
    notify_slack "dev" "📝 코드 변경: $file_path"
fi

# 4. 자동 테스트 (비동기)
{
    run_quality_checks "$file_path" &
} < /dev/null

echo '{"continue": true, "reason": "통합 처리 완료"}'
```

### 유틸리티 함수

**hooks/utils/mcp-helper.sh**:
```bash
#!/bin/bash

# TaskMaster 진행상황 업데이트
update_taskmaster_progress() {
    local task_id="$1"
    local message="$2"
    
    claude mcp call taskmaster-ai update_subtask \
        --id="$task_id" \
        --prompt="$message" \
        --projectRoot="$CLAUDE_PROJECT_DIR"
}

# GitHub 이슈 댓글 추가
update_github_issue() {
    local issue_number="$1"
    local comment="$2"
    
    claude mcp call github add_issue_comment \
        --issue_number="$issue_number" \
        --body="$comment" \
        --owner="$GITHUB_OWNER" \
        --repo="$GITHUB_REPO"
}

# Slack 알림
notify_slack() {
    local channel="$1"
    local message="$2"
    
    claude mcp call slack send_message \
        --channel="$channel" \
        --text="$message"
}
```

---

## 결론

Claude Code의 훅과 MCP 통합을 통해 다음과 같은 이점을 얻을 수 있습니다:

### ✅ 주요 이점

1. **완전 자동화**: 반복적인 작업 자동화
2. **품질 향상**: 자동 검증 및 테스트
3. **팀 협업**: 실시간 알림 및 상태 공유
4. **보안 강화**: 자동 보안 검사 및 차단
5. **효율성 증대**: 워크플로우 최적화

### 🚀 권장 시작 단계

1. **기본 훅 설정**: 파일 쓰기 전후 간단한 로깅
2. **MCP 서버 추가**: TaskMaster, GitHub 등 필수 도구
3. **점진적 확장**: 보안, 알림, 자동화 기능 추가
4. **팀 적용**: 팀 전체 워크플로우로 확장

이 가이드를 참고하여 프로젝트에 맞는 훅과 MCP 통합 환경을 구축해보세요.

---

*작성일: 2024년 1월*  
*버전: 1.0*  
*업데이트: Claude Code 최신 버전 기준*