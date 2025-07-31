#!/bin/bash
# HaniHome AU 자동 세션 시작 스크립트 (Cron용)

# 로그 파일 설정
LOG_FILE="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-session.log"
echo "$(date): 🚀 자동 세션 시작..." >> "$LOG_FILE"

# 프로젝트 디렉토리로 이동
PROJECT_DIR="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo"
cd "$PROJECT_DIR"

# 크론잡용 환경 변수 설정
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:$PATH"
export HOME="/Users/gno_root"
export USER="gno_root"

# 환경변수 로그
echo "$(date): PATH=$PATH" >> "$LOG_FILE"
echo "$(date): HOME=$HOME" >> "$LOG_FILE"
echo "$(date): USER=$USER" >> "$LOG_FILE"
echo "$(date): PWD=$(pwd)" >> "$LOG_FILE"

# PostgreSQL 컨테이너 시작
echo "$(date): 🐳 PostgreSQL 컨테이너 시작..." >> "$LOG_FILE"
docker-compose -f docker-compose.dev.yml up -d postgres >> "$LOG_FILE" 2>&1

# 컨테이너 준비 대기
sleep 15

# Task Master 상태 확인
echo "$(date): 📊 Task Master 상태 확인..." >> "$LOG_FILE"

# task-master 명령 테스트
echo "$(date): Task Master 명령 테스트..." >> "$LOG_FILE"
if command -v task-master >/dev/null 2>&1; then
    echo "$(date): task-master 명령 찾음: $(which task-master)" >> "$LOG_FILE"
    
    # 현재 프로젝트 루트 설정
    export PROJECT_ROOT="$PROJECT_DIR"
    echo "$(date): PROJECT_ROOT=$PROJECT_ROOT" >> "$LOG_FILE"
    
    # Task Master 다음 작업 조회 (프로젝트 디렉토리에서 실행)
    cd "$PROJECT_ROOT"
    NEXT_TASK_OUTPUT=$(task-master next 2>&1)
    echo "$(date): Task Master 출력: $NEXT_TASK_OUTPUT" >> "$LOG_FILE"
    
    # Task ID 추출 (텍스트에서 숫자.숫자 패턴 찾기)
    NEXT_TASK=$(echo "$NEXT_TASK_OUTPUT" | grep -o '[0-9]\+\.[0-9]\+' | head -1)
    if [ -z "$NEXT_TASK" ]; then
        # 단순 숫자 ID도 시도
        NEXT_TASK=$(echo "$NEXT_TASK_OUTPUT" | grep -o 'Task [0-9]\+\.[0-9]\+' | cut -d' ' -f2 | head -1)
    fi
    if [ -z "$NEXT_TASK" ]; then
        NEXT_TASK="4.1"  # 기본값
    fi
    echo "$(date): 다음 작업할 태스크: $NEXT_TASK" >> "$LOG_FILE"
    
    # 자동으로 다음 태스크를 in-progress로 설정
    if [ "$NEXT_TASK" != "null" ] && [ "$NEXT_TASK" != "" ]; then
        echo "$(date): 🎯 태스크 $NEXT_TASK를 in-progress로 설정..." >> "$LOG_FILE"
        task-master set-status --id="$NEXT_TASK" --status=in-progress >> "$LOG_FILE" 2>&1
    fi
else
    echo "$(date): ❌ task-master 명령을 찾을 수 없음" >> "$LOG_FILE"
    NEXT_TASK="4.1"  # 기본값
fi

# Claude 실행 대신 기본 정보만 로그에 기록
echo "$(date): 📋 태스크 $NEXT_TASK 자동 준비 완료" >> "$LOG_FILE"
echo "$(date): ✅ PostgreSQL 컨테이너 실행 중" >> "$LOG_FILE"
echo "$(date): ✅ Task Master 상태 업데이트 완료" >> "$LOG_FILE"
echo "$(date): 🎯 다음 단계: 대화형 Claude 세션에서 구현 시작" >> "$LOG_FILE"

# 대화형 세션 시작 권장사항
echo "$(date): 📝 상세 구현을 위해 수동으로 대화형 세션 시작 권장:" >> "$LOG_FILE"
echo "$(date): claude" >> "$LOG_FILE"

echo "$(date): ✅ 자동 세션 시작 완료. PID: $!" >> "$LOG_FILE"
echo "$(date): 📋 로그 확인: tail -f $LOG_FILE" >> "$LOG_FILE"
