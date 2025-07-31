#!/bin/bash
# 크론잡 상태 체크 스크립트

echo "🔍 HaniHome AU 크론잡 상태 확인"
echo "=================================="

# 1. 크론 데몬 상태 확인
echo "1. 크론 데몬 상태:"
if pgrep -x "cron" > /dev/null; then
    echo "   ✅ 크론 데몬 실행 중"
else
    echo "   ❌ 크론 데몬 실행되지 않음"
fi

# 2. 크론탭 설정 확인
echo -e "\n2. 크론탭 설정:"
CRON_OUTPUT=$(crontab -l 2>/dev/null)
if [ -n "$CRON_OUTPUT" ]; then
    echo "$CRON_OUTPUT" | grep -A 1 -B 1 "HaniHome AU"
    if [ $? -eq 0 ]; then
        echo "   ✅ HaniHome AU 크론잡 설정됨"
    else
        echo "   ❌ HaniHome AU 크론잡 설정되지 않음"
    fi
else
    echo "   ❌ 크론탭 설정이 없음"
fi

# 3. 스크립트 파일 확인
SCRIPT_PATH="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-start-session.sh"
echo -e "\n3. 스크립트 파일 상태:"
if [ -f "$SCRIPT_PATH" ]; then
    echo "   ✅ 스크립트 파일 존재: $SCRIPT_PATH"
    if [ -x "$SCRIPT_PATH" ]; then
        echo "   ✅ 실행 권한 있음"
    else
        echo "   ❌ 실행 권한 없음"
    fi
else
    echo "   ❌ 스크립트 파일 없음: $SCRIPT_PATH"
fi

# 4. 로그 파일 확인
LOG_PATH="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-session.log"
echo -e "\n4. 로그 파일 상태:"
if [ -f "$LOG_PATH" ]; then
    echo "   ✅ 로그 파일 존재: $LOG_PATH"
    echo "   📊 파일 크기: $(ls -lh "$LOG_PATH" | awk '{print $5}')"
    echo "   🕐 마지막 수정: $(ls -l "$LOG_PATH" | awk '{print $6, $7, $8}')"
    
    echo -e "\n   📋 최근 로그 (마지막 5줄):"
    tail -5 "$LOG_PATH" 2>/dev/null | sed 's/^/      /'
else
    echo "   ❌ 로그 파일 없음: $LOG_PATH"
fi

# 5. 현재 시간과 다음 실행 시간 확인
echo -e "\n5. 실행 시간 정보:"
echo "   현재 시간: $(date)"
CRON_TIME=$(crontab -l 2>/dev/null | grep "HaniHome AU" -A 1 | tail -1 | awk '{print $1, $2}')
if [ -n "$CRON_TIME" ]; then
    echo "   크론 설정 시간: $CRON_TIME (분 시간)"
else  
    echo "   크론 설정 시간을 찾을 수 없음"
fi

# 6. 수동 실행 테스트 제안
echo -e "\n6. 문제 해결 방법:"
echo "   🔧 수동 실행 테스트:"
echo "      $SCRIPT_PATH"
echo ""
echo "   🔧 크론 로그 확인:"
echo "      tail -f $LOG_PATH"
echo ""
echo "   🔧 크론탭 편집:"  
echo "      crontab -e"
echo ""
echo "   🔧 크론 재시작 (필요시):"
echo "      sudo launchctl unload /System/Library/LaunchDaemons/com.vix.cron.plist"
echo "      sudo launchctl load /System/Library/LaunchDaemons/com.vix.cron.plist"

echo -e "\n=================================="
echo "크론잡 상태 확인 완료"