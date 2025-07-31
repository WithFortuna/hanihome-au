#!/bin/bash
# HaniHome AU 자동 재개 스크립트

echo "🔄 HaniHome AU 프로젝트 자동 재개 중..."

# 현재 디렉토리로 이동
cd "$(dirname "$0")"

# PostgreSQL 컨테이너 시작
echo "🐳 PostgreSQL 컨테이너 확인 및 시작..."
docker-compose -f docker-compose.dev.yml up -d postgres

# 컨테이너 준비 대기
echo "⏳ PostgreSQL 준비 대기 중..."
sleep 10

# Task Master 상태 확인
echo "📊 현재 태스크 상태:"
task-master list | head -10

# 다음 태스크 확인
echo "🎯 다음 작업할 태스크:"
NEXT_TASK=$(task-master next --format=json 2>/dev/null | jq -r '.id // "1.4"')
echo "추천 태스크: $NEXT_TASK"

echo ""
echo "✅ 자동 재개 완료!"
echo "📋 다음 단계:"
echo "   1. Claude에게 다음과 같이 요청하세요:"
echo "   2. 'tm:workflows:auto-implement-tasks로 Task $NEXT_TASK 구현해줘'"
echo ""

# 선택적: 자동으로 태스크 시작 (주석 해제하여 사용)
# echo "🚀 자동으로 태스크 $NEXT_TASK 시작..."
# task-master set-status --id="$NEXT_TASK" --status=in-progress