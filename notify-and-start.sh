#!/bin/bash
# macOS 알림과 함께 개발 세션 시작하는 스크립트

PROJECT_DIR="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo"
cd "$PROJECT_DIR"

# 다음 태스크 확인
NEXT_TASK=$(task-master next 2>/dev/null | grep -o '[0-9]\+\.[0-9]\+' | head -1)
if [ -z "$NEXT_TASK" ]; then
    NEXT_TASK="4.1"
fi

# macOS 알림 발송
osascript -e "display notification \"Task $NEXT_TASK 개발 시작 준비 완료\" with title \"HaniHome AU\" subtitle \"PostgreSQL 컨테이너 실행 중\" sound name \"Ping\""

# Terminal에서 새 창으로 Claude 세션 시작
osascript << EOF
tell application "Terminal"
    activate
    do script "cd $PROJECT_DIR && echo '🚀 HaniHome AU Task $NEXT_TASK 개발 세션 시작' && claude"
end tell
EOF

echo "$(date): macOS 알림 발송 및 Claude 세션 시작 완료 - Task $NEXT_TASK"