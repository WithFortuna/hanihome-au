#!/bin/bash
set -euo pipefail

perform_git_automation() {
  local branch_type="$1"

  if [[ -z "$branch_type" ]]; then
    echo "Usage: perform_git_automation <feature|documentation>"
    return 1
  fi

  local claude_prompt
  case "$branch_type" in
    feature)
      claude_prompt=" /git-automation-after-coding 이라는 custom command 실행해."
      ;;
    documentation)
      claude_prompt=" /git-autoation-after-documentation 이라는 custom command 실행해."
      ;;
    *)
      echo "Unknown branch type: $branch_type"
      return 1
      ;;
  esac

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
}

