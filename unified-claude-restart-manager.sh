#!/bin/bash
# unified-claude-manager.sh
# launchd가 5시간마다 호출하는 메인 스크립트
# 1) auto-restart 호출
# 2) 알림 전송 및 로그 기록

LOGFILE="/Users/gno_root/Library/Logs/claude-manager.log"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Starting unified manager" >> "$LOGFILE"

# 자동 재시작 호출
bash /Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-start-claude.sh >> "$LOGFILE" 2>&1

# 알림 전송
osascript -e 'display notification "Claude 세션이 재시작되었습니다." with title "Claude Manager"'

echo "[$(date '+%Y-%m-%d %H:%M:%S')] unified manager is done" >> "$LOGFILE"
