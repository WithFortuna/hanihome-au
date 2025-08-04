#!/bin/bash
# auto-restart-claude.sh
# 새로운 터미널을 열어 Claude 세션을 재시작합니다.

# 작업 디렉토리 설정
WORK_DIR="$HOME/Desktop/open_the_door/lets_developing/project/olaf_universe/demo"

# AppleScript로 iTerm2 열기 및 Claude 실행
osascript <<EOF
 tell application "iTerm"
   activate
   # 새 탭 생성
   tell current window
     create tab with default profile
     tell current session
       # 디렉토리 이동 명령 실행
       write text "cd ${WORK_DIR}"
       # Claude 실행 명령 실행
       write text "claude \" /finish-subtasks-half-amount-of 커스텀 명령어를 실행해 \" "
     end tell
   end tell
 end tell
EOF

exit 0
