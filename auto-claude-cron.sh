#!/bin/bash
# 크론용 Claude 자동 실행 스크립트 (개선된 버전)

LOG_FILE="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/claude-cron.log"
PROJECT_DIR="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo"

echo "$(date): 🚀 크론용 Claude 자동 실행 시작..." >> "$LOG_FILE"

# 환경 설정
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:$PATH"
export HOME="/Users/gno_root"
cd "$PROJECT_DIR"

# 다음 태스크 확인
NEXT_TASK=$(task-master next 2>/dev/null | grep -o '[0-9]\+\.[0-9]\+' | head -1)
if [ -z "$NEXT_TASK" ]; then
    NEXT_TASK="4.1"
fi

echo "$(date): 처리할 태스크: $NEXT_TASK" >> "$LOG_FILE"

# 1. 기본 프로젝트 상태 분석 (non-interactive)
echo "$(date): 📊 프로젝트 상태 분석 중..." >> "$LOG_FILE"
ANALYSIS_PROMPT="HaniHome AU 프로젝트의 현재 상태를 분석하고 Task $NEXT_TASK 구현을 위한 체크리스트를 생성해줘. 간단명료하게 5줄 이내로 요약해줘."
ANALYSIS_RESULT=$(echo "$ANALYSIS_PROMPT" | timeout 30 claude --print 2>&1)

if [ $? -eq 0 ]; then
    echo "$(date): ✅ 분석 완료:" >> "$LOG_FILE"
    echo "$ANALYSIS_RESULT" | head -10 >> "$LOG_FILE"
else
    echo "$(date): ❌ 분석 실패 또는 타임아웃" >> "$LOG_FILE"
fi

# 2. 코드 구조 확인 (non-interactive)
echo "$(date): 🔍 코드 구조 확인 중..." >> "$LOG_FILE"
STRUCTURE_PROMPT="현재 프로젝트 구조를 보고 Task $NEXT_TASK를 위해 생성해야 할 파일들을 나열해줘. 파일 경로만 간단히 3개 이내로."
STRUCTURE_RESULT=$(echo "$STRUCTURE_PROMPT" | timeout 20 claude --print 2>&1)

if [ $? -eq 0 ]; then
    echo "$(date): ✅ 구조 분석 완료:" >> "$LOG_FILE"
    echo "$STRUCTURE_RESULT" | head -5 >> "$LOG_FILE"
else
    echo "$(date): ❌ 구조 분석 실패" >> "$LOG_FILE"
fi

# 3. 알림 생성
echo "$(date): 📢 개발자 알림 생성..." >> "$LOG_FILE"
cat << EOF >> "$LOG_FILE"

=== 📋 자동 분석 완료 알림 ===
시간: $(date)
대상 태스크: $NEXT_TASK
상태: PostgreSQL 컨테이너 실행 중
다음 단계: 대화형 Claude 세션 시작하여 구현 진행

권장 명령어:
1. cd $PROJECT_DIR
2. claude
3. "Task $NEXT_TASK 구현을 시작해줘"

==============================

EOF

echo "$(date): ✅ 크론용 Claude 자동 실행 완료" >> "$LOG_FILE"