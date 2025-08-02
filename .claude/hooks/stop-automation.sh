#!/bin/bash
set -euo pipefail

# Stop Hook - Automated Work Flow Management
# Automatically determines work type and performs appropriate follow-up actions

# Configuration
HOOKS_DIR="$(dirname "$0")"
UTILS_DIR="$HOOKS_DIR/utils"
CONFIG_DIR="$HOOKS_DIR/config"
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(cd "$HOOKS_DIR/../.." && pwd)}"

# Export PROJECT_DIR for utility scripts
export CLAUDE_PROJECT_DIR="$PROJECT_DIR"

# Source utility functions
source "$UTILS_DIR/work-type-detector.sh"
source "$UTILS_DIR/task-progress-checker.sh"
source "$UTILS_DIR/git-automation.sh"
source "$UTILS_DIR/pr-automation.sh"

# Logging
LOG_FILE="$HOOKS_DIR/stop-automation.log"
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Main execution
main() {
    log "=== Stop Hook Automation Started ==="

    # Parse hook input
    local input=$(cat)
    local session_id=$(echo "$input" | jq -r '.session_id // ""')
    local transcript_path=$(echo "$input" | jq -r '.transcript_path // ""')
    local stop_hook_active=$(echo "$input" | jq -r '.stop_hook_active // false')

    log "Session ID: $session_id"
    log "Transcript: $transcript_path"
    log "Stop hook active: $stop_hook_active"

    # Prevent infinite loops
    if [[ "$stop_hook_active" == "true" ]]; then
        log "Stop hook already active, exiting to prevent loop"
        exit 0
    fi

    # Determine work type from recent session activity
    local work_type
    work_type=$(determine_work_type "$transcript_path")
    log "Detected work type: $work_type"

    # Execute appropriate workflow based on work type
    case "$work_type" in
        "feature_development")
            handle_feature_development
            ;;
        "documentation")
            handle_documentation_work
            ;;
        "github_work")
            handle_github_work
            ;;
        "unknown")
            log "Unknown work type, no automated action taken"
            ;;
        *)
            log "Unhandled work type: $work_type"
            ;;
    esac

    log "=== Stop Hook Automation Completed ==="
}

# Handle feature development workflow
handle_feature_development() {
    log "Processing feature development workflow..."

    # Check TaskMaster progress
    local progress_info
    progress_info=$(check_task_progress)

    if [[ $? -ne 0 ]]; then
        log "Failed to check task progress, skipping automation"
        return 1
    fi

    local completed_ratio=$(echo "$progress_info" | jq -r '.completed_ratio // 0')
    local current_task=$(echo "$progress_info" | jq -r '.current_task // ""')
    local total_subtasks=$(echo "$progress_info" | jq -r '.total_subtasks // 0')
    local completed_subtasks=$(echo "$progress_info" | jq -r '.completed_subtasks // 0')

    log "Task progress: $completed_subtasks/$total_subtasks ($completed_ratio%)"

    # Decision logic based on progress
    if (( $(echo "$completed_ratio < 50" | bc -l) )); then
        log "Progress is less than 50%, encouraging to continue work"
        suggest_continue_work "$current_task" "$completed_subtasks" "$total_subtasks"
    else
        log "Progress is 50% or more, proceeding with git automation"
        perform_git_automation "feature"
    fi
}

# Handle documentation workflow
handle_documentation_work() {
    log "Processing git workflow after documentation workflow... "

    log "Delegating commit and PR to perform_git_automation.sh"

    if ! perform_git_automation "documentation"; then
        log "Documentation 작업 후 git  automation via Claude failed"
        return 1
    fi

    log "Documentation automation triggered successfully"

}

# Handle GitHub work (wait/no action)
handle_github_work() {
    log "GitHub work detected - no automated action taken (waiting)"
    echo "GitHub-related work completed. Manual review recommended."
}

# Suggest continuing work
suggest_continue_work() {
    local current_task="$1"
    local completed="$2"
    local total="$3"

    log "Suggesting to continue work on task: $current_task"

     # AppleScript로 iTerm2 열기 및 Claude 실행
 # AppleScript로 iTerm2 열기 및 Claude 실행
osascript <<EOF
  tell application "iTerm"
    activate
    # 새 탭 생성
    tell current window
      create tab with default profile
      tell current session
        # 디렉토리 이동 명령 실행
        write text "cd ${PROJECT_DIR}"
        # Claude 실행 명령 실행
        write text "claude \"작업 진행 중 ($completed/$total subtasks 완료) 현재 작업: $current_task
다음 작업을 계속 진행하세요:
- 남은 subtask들을 완료해주세요
- 50% 이상 완료해.
 그 후에는 /clear해. /auto-document\" "
      end tell
    end tell
  end tell
EOF

    # Output message for user
    cat << EOF

🚧 작업 진행 중 ($completed/$total subtasks 완료)
현재 작업: $current_task

다음 작업을 계속 진행하세요:
- 남은 subtask들을 완료해주세요
- 50% 이상 완료 시 자동으로 커밋 및 PR이 생성됩니다

EOF
}

# Error handling
trap 'log "Error occurred in stop automation script"' ERR

# Execute main function
main "$@"
