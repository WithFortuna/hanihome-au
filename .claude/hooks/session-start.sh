#!/bin/bash
# Claude Code Session Start Hook
# 새 세션 시작 시 자동으로 실행됩니다

echo "🚀 HaniHome AU 프로젝트 세션 시작..."

# 1. 현재 위치 확인
cd /Users/gno_root/Desktop/open_the_door/lets_developing/project/olaf_universe/demo

# 2. PostgreSQL 컨테이너 상태 확인 및 시작
echo "📊 PostgreSQL 컨테이너 상태 확인..."
if ! docker-compose -f docker-compose.dev.yml ps postgres | grep -q "Up"; then
    echo "🐳 PostgreSQL 컨테이너 시작 중..."
    docker-compose -f docker-compose.dev.yml up -d postgres
fi

# 3. 다음 작업할 태스크 표시
echo "📋 현재 진행 상황:"
task-master list --status=in-progress,pending | head -20

# 4. 다음 태스크 자동 제안
echo "🎯 추천 다음 작업:"
task-master next

echo ""
echo "✨ 준비완료! 다음 명령어로 작업을 계속하세요:"
echo "   • /tm:workflows:command-pipeline  지금까지 진행한 작업 상황을 파악해서 다음 task의 subtask를 절반개수 만큼 수행해. 그 후에는 /clear해. 그 후에는 /auto-document "
echo ""
