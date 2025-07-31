#!/bin/bash
# HaniHome AU 자동 세션 시작 스크립트 (Cron용)

# 로그 파일 설정
LOG_FILE="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-session.log"
echo "$(date): 🚀 자동 세션 시작..." >> "$LOG_FILE"

# 프로젝트 디렉토리로 이동
cd /Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo

# 환경 변수 설정 (필요시)
export PATH="/usr/local/bin:/opt/homebrew/bin:$PATH"

# PostgreSQL 컨테이너 시작
echo "$(date): 🐳 PostgreSQL 컨테이너 시작..." >> "$LOG_FILE"
docker-compose -f docker-compose.dev.yml up -d postgres >> "$LOG_FILE" 2>&1

# 컨테이너 준비 대기
sleep 15

# Task Master 상태 확인
echo "$(date): 📊 Task Master 상태 확인..." >> "$LOG_FILE"
NEXT_TASK=$(task-master next --format=json 2>/dev/null | jq -r '.id // "1.4"')
echo "$(date): 다음 작업할 태스크: $NEXT_TASK" >> "$LOG_FILE"

# 자동으로 다음 태스크를 in-progress로 설정
if [ "$NEXT_TASK" != "null" ] && [ "$NEXT_TASK" != "" ]; then
    echo "$(date): 🎯 태스크 $NEXT_TASK를 in-progress로 설정..." >> "$LOG_FILE"
    task-master set-status --id="$NEXT_TASK" --status=in-progress >> "$LOG_FILE" 2>&1
fi

# Claude 실행 한 후에 명령어 삽입:
claude "tm:workflows:auto-implement-tasks Task $NEXT_TASK 구현을 시작해줘. 모든 의존성이 준비되었고, PostgreSQL 컨테이너가 실행 중입니다."

echo "$(date): ✅ 자동 세션 시작 완료. PID: $!" >> "$LOG_FILE"
echo "$(date): 📋 로그 확인: tail -f $LOG_FILE" >> "$LOG_FILE"
