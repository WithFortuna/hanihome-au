#!/bin/bash
# 진행상황 체크 및 필요시 재시작

LOG_FILE="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/progress-check.log"
cd /Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo

echo "$(date): 🔍 진행상황 체크 시작..." >> "$LOG_FILE"

# Claude Code 프로세스 확인
CLAUDE_PID=$(pgrep -f "claude.*headless" | head -1)

if [ -z "$CLAUDE_PID" ]; then
    echo "$(date): ❌ Claude Code 프로세스가 실행되지 않음. 재시작 필요." >> "$LOG_FILE"
    
    # 다음 태스크 확인
    NEXT_TASK=$(task-master list --status=in-progress,pending | head -1 | grep -o "^[0-9]\+\(\.[0-9]\+\)\?")
    
    if [ -n "$NEXT_TASK" ]; then
        echo "$(date): 🚀 태스크 $NEXT_TASK 자동 재시작..." >> "$LOG_FILE"
        /Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-start-session.sh
    else
        echo "$(date): ✅ 모든 태스크 완료됨. 재시작 불필요." >> "$LOG_FILE"
    fi
else
    echo "$(date): ✅ Claude Code 실행 중 (PID: $CLAUDE_PID)" >> "$LOG_FILE"
    
    # 현재 진행 상황 로그
    CURRENT_STATUS=$(task-master list --status=in-progress | head -1)
    echo "$(date): 📊 현재 진행: $CURRENT_STATUS" >> "$LOG_FILE"
fi