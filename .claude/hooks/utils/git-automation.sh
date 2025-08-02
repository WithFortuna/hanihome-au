#!/usr/bin/env bash
set -euo pipefail

# perform_git_automation.sh
# Usage: perform_git_automation <feature|documentation>

branch_type="$1"
if [[ -z "$branch_type" ]]; then
  echo "Usage: $0 <feature|documentation>"
  exit 1
fi

# Determine Claude prompt based on branch_type
case "$branch_type" in
  feature)
    claude_prompt="기능 개발 완료된 후에 git commit, git pr 작성과 관련해서 정의해놓은 정책/메모리를 참고하여 자동으로 깃허브 commit 후 pr 작성해서 레포지토리에 올려"
    ;;
  documentation)
    claude_prompt="문서 작업 완료된 후에 git commit, git pr 작성과 관련해서 정의해놓은 정책/메모리를 참고하여 자동으로 깃허브 commit 후 pr 작성해서 레포지토리에 올려"
    ;;
  *)
    echo "Unknown branch type: $branch_type"
    exit 1
    ;;
esac

# Open iTerm and trigger Claude automation
osascript <<EOF
 tell application "iTerm"
   activate
   tell current window
     create tab with default profile
     tell current session
       write text "cd ${CLAUDE_PROJECT_DIR}"
       write text "claude \" '$claude_prompt' \" "
     end tell
   end tell
 end tell
EOF




