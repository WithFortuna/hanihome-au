#!/bin/bash
# Cron Job 설정 스크립트 (시간 입력 버전)

set -e

SCRIPT_PATH="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-start-session.sh"
LOG_PATH="/Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo/auto-session.log"

echo "⏰ HaniHome AU 자동 실행 Cron Job 설정 중..."

# 1) 실행 시간 입력 받기
read -p "자동 실행 시간을 입력하세요 (HH:MM 형식, 24시간제): " TIME_INPUT

# 2) 입력 포맷 검증 (정규식)
if [[ ! $TIME_INPUT =~ ^([01][0-9]|2[0-3]):([0-5][0-9])$ ]]; then
  echo "🚫 시간 형식이 잘못되었습니다. HH:MM (00:00~23:59) 형태로 다시 시도해주세요."
  exit 1
fi

HOUR="${BASH_REMATCH[1]}"
MINUTE="${BASH_REMATCH[2]}"

# 3) 기존 크론탭 삭제 및 백업
# HaniHome AU 관련 라인만 제거한 뒤 나머지는 그대로 등록
CRON_LINE="${MINUTE} ${HOUR} * * * ${SCRIPT_PATH} >> ${LOG_PATH} 2>&1"

# 1) 기존 크론탭 백업
crontab -l > /tmp/cron_bk_$(date +%Y%m%d_%H%M%S) 2>/dev/null || true

# 2) HaniHome AU 관련 모든 라인 제거 후 새로운 라인 추가
{
  # 기존 crontab 읽기 및 HaniHome AU 관련 라인 필터링
  CURRENT_CRON=$(crontab -l 2>/dev/null || true)
  
  # HaniHome AU 관련 라인들을 모두 제거 (주석, 스크립트 경로 포함)
  echo "$CURRENT_CRON" | grep -v -E "(HaniHome AU|auto-start-session\.sh)" || true
  
  # 새로운 HaniHome AU 크론 라인 추가
  echo "# HaniHome AU 자동 세션 시작"
  echo "${CRON_LINE}"
} | crontab -

# 5) 완료 메시지 및 확인
echo "✅ Cron Job 설정 완료!"
echo ""
echo "📋 현재 설정된 HaniHome AU 크론탭:"
crontab -l | sed -n '1,/^$/p'
echo ""
echo "🔍 로그 확인:"
echo "   tail -f ${LOG_PATH}"
echo ""
echo "🛑 Cron 제거:"
echo "   또는 crontab -e 로 편집하여 HaniHome AU 관련 라인만 삭제"



