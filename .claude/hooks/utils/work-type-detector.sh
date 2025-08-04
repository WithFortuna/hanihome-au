#!/bin/bash

# Work Type Detection Utility
# Analyzes session transcript and recent changes to determine work type
#
# Environment Variables:
# - DEBUG_WORK_DETECTOR: Set to "true" to enable debug logging
# - WORK_DETECTOR_LOG_LEVEL: Set log level (DEBUG, INFO, WARN, ERROR)
# - WORK_DETECTOR_LOG_TO_STDERR: Set to "true" to also output logs to stderr

# Load work patterns configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="$(dirname "$SCRIPT_DIR")/config"
PATTERNS_FILE="$CONFIG_DIR/work-patterns.json"

# Global variables for loaded patterns
PATTERNS_LOADED=false

# Logging configuration
LOG_FILE="$SCRIPT_DIR/work-type-detector.log"

# Setup logging environment
setup_logging() {
    # Create log file if it doesn't exist
    if [[ ! -f "$LOG_FILE" ]]; then
        touch "$LOG_FILE" 2>/dev/null || {
            echo "WARNING: Cannot create log file at $LOG_FILE" >&2
            LOG_FILE="/dev/null"
            return 1
        }
        chmod 644 "$LOG_FILE" 2>/dev/null
    fi

    # Check if log file is writable
    if [[ ! -w "$LOG_FILE" ]]; then
        echo "WARNING: Log file $LOG_FILE is not writable" >&2
        LOG_FILE="/dev/null"
        return 1
    fi

    # Rotate log if it's too large (>10MB)
    if [[ -f "$LOG_FILE" ]] && [[ $(stat -f%z "$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_FILE" 2>/dev/null || echo 0) -gt 10485760 ]]; then
        mv "$LOG_FILE" "${LOG_FILE}.old" 2>/dev/null
        touch "$LOG_FILE" 2>/dev/null
        chmod 644 "$LOG_FILE" 2>/dev/null
    fi

    return 0
}

# Internal logging function
log_message() {
    local level="$1"
    local message="$2"
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    # Write to log file only
    echo "[$timestamp] [$level] $message" >> "$LOG_FILE" 2>/dev/null
}

# Logging functions for different levels
wd_log_debug() {
    local log_level="${WORK_DETECTOR_LOG_LEVEL:-INFO}"
    if [[ "${DEBUG_WORK_DETECTOR:-}" == "true" ]] || [[ "$log_level" == "DEBUG" ]]; then
        log_message "DEBUG" "$1"
    fi
}

wd_log_info() {
    local log_level="${WORK_DETECTOR_LOG_LEVEL:-INFO}"
    if [[ "$log_level" =~ ^(DEBUG|INFO)$ ]]; then
        log_message "INFO" "$1"
    fi
}

wd_log_warn() {
    local log_level="${WORK_DETECTOR_LOG_LEVEL:-INFO}"
    if [[ "$log_level" =~ ^(DEBUG|INFO|WARN)$ ]]; then
        log_message "WARN" "$1"
    fi
}

wd_log_error() {
    log_message "ERROR" "$1"
}

# Log management functions
cleanup_old_logs() {
    # Remove log files older than 7 days
    if [[ -f "$LOG_FILE" ]]; then
        find "$(dirname "$LOG_FILE")" -name "$(basename "$LOG_FILE").old*" -mtime +7 -delete 2>/dev/null || true
    fi
}

show_log_usage() {
    cat << 'EOF'
Work Type Detector - Logging Configuration

Environment Variables:
  DEBUG_WORK_DETECTOR=true         Enable debug logging
  WORK_DETECTOR_LOG_LEVEL=DEBUG    Set log level (DEBUG|INFO|WARN|ERROR)
  WORK_DETECTOR_LOG_TO_STDERR=true Also output to stderr

Log File Location:
  Log file: work-type-detector.log (in script directory)
  Rotated when > 10MB, old logs cleaned up after 7 days

Examples:
  # Enable debug logging
  DEBUG_WORK_DETECTOR=true ./work-type-detector.sh transcript.txt

  # Set log level to WARNING only
  WORK_DETECTOR_LOG_LEVEL=WARN ./work-type-detector.sh transcript.txt

  # Enable stderr output
  WORK_DETECTOR_LOG_TO_STDERR=true ./work-type-detector.sh transcript.txt
EOF
}

# Initialize logging
setup_logging
cleanup_old_logs

# Load patterns from JSON file
load_patterns() {
    if [[ "$PATTERNS_LOADED" == "true" ]]; then
        return 0
    fi

    if [[ ! -f "$PATTERNS_FILE" ]]; then
        wd_log_error "Patterns file not found: $PATTERNS_FILE"
        return 1
    fi

    if ! command -v jq >/dev/null 2>&1; then
        wd_log_error "jq command not found - cannot load patterns from JSON"
        return 1
    fi

    wd_log_debug "Loading patterns from: $PATTERNS_FILE"
    PATTERNS_LOADED=true
    return 0
}

# Check if pattern matches in text using jq
check_pattern_matches() {
    local work_type="$1"
    local pattern_type="$2"
    local text="$3"
    local score=0

    if [[ ! -f "$PATTERNS_FILE" ]] || ! command -v jq >/dev/null 2>&1; then
        echo "0"
        return
    fi

    # Get patterns for the specific work type and pattern type
    local patterns
    patterns=$(jq -r ".patterns.${work_type}.${pattern_type}[]? // empty" "$PATTERNS_FILE" 2>/dev/null)

    if [[ -z "$patterns" ]]; then
        echo "0"
        return
    fi

    # Count matches for each pattern
    local total_matches=0
    while IFS= read -r pattern; do
        if [[ -n "$pattern" && -n "$text" ]]; then
            local matches
            matches=$(echo "$text" | grep -ic "$pattern" 2>/dev/null)
            if [[ $? -eq 0 && -n "$matches" && "$matches" =~ ^[0-9]+$ ]]; then
                total_matches=$((total_matches + matches))
            fi
        fi
    done <<< "$patterns"

    echo "$total_matches"
}

# Get scoring weight from patterns file
get_scoring_weight() {
    local score_type="$1"

    if [[ ! -f "$PATTERNS_FILE" ]] || ! command -v jq >/dev/null 2>&1; then
        # Fallback to default weights
        case "$score_type" in
            "transcript_match") echo 2 ;;
            "file_extension_match") echo 3 ;;
            "directory_match") echo 2 ;;
            "git_commit_match") echo 1 ;;
            "mcp_indicator_match") echo 4 ;;
            "cli_indicator_match") echo 3 ;;
            "file_action_match") echo 2 ;;
            "content_indicator_match") echo 1 ;;
            "taskmaster_activity") echo 5 ;;
            *) echo 1 ;;
        esac
        return
    fi

    local weight
    weight=$(jq -r ".scoring.${score_type} // 1" "$PATTERNS_FILE" 2>/dev/null)
    echo "${weight}"
}

# Determine work type based on transcript and git changes
determine_work_type() {
    local transcript_path="$1"

    # Load patterns if not already loaded
    load_patterns || {
        wd_log_error "Failed to load patterns, falling back to unknown"
        echo "unknown"
        return
    }

    # Initialize scores for each work type
    local feature_development_score=0
    local documentation_score=0
    local github_work_score=0

    # Read transcript content if available
    local transcript_content=""
    if [[ -f "$transcript_path" ]]; then
        transcript_content=$(cat "$transcript_path" 2>/dev/null || echo "")
    fi

    # Analyze each work type using patterns
    for work_type in feature_development documentation github_work; do
        local total_score=0

        # Custom command matches (for feature_development)
        if [[ "$work_type" == "feature_development" && -n "$transcript_content" ]]; then
            local custom_matches
            custom_matches=$(check_pattern_matches "$work_type" "custom_command_for_feature" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "custom_command_for_feature_match")
            total_score=$((total_score + custom_matches * weight))
            wd_log_debug "$work_type custom command matches: $custom_matches (weighted: $((custom_matches * weight)))"
        fi

        # Custom command matches (for documentation)
        if [[ "$work_type" == "documentation" && -n "$transcript_content" ]]; then
            local custom_matches
            custom_matches=$(check_pattern_matches "$work_type" "custom_command_for_documentation" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "custom_command_for_documentation_match")
            total_score=$((total_score + custom_matches * weight))
            wd_log_debug "$work_type custom command matches: $custom_matches (weighted: $((custom_matches * weight)))"
        fi


        # Transcript keyword matches
        if [[ -n "$transcript_content" ]]; then
            local transcript_matches
            transcript_matches=$(check_pattern_matches "$work_type" "transcript_keywords" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "transcript_match")
            total_score=$((total_score + transcript_matches * weight))
            wd_log_debug "$work_type transcript matches: $transcript_matches (weighted: $((transcript_matches * weight)))"
        fi

        # MCP indicator matches
        if [[ -n "$transcript_content" ]]; then
            local mcp_matches
            mcp_matches=$(check_pattern_matches "$work_type" "mcp_indicators" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "mcp_indicator_match")
            total_score=$((total_score + mcp_matches * weight))
            wd_log_debug "$work_type MCP matches: $mcp_matches (weighted: $((mcp_matches * weight)))"
        fi

        # CLI indicator matches (for github_work)
        if [[ "$work_type" == "github_work" && -n "$transcript_content" ]]; then
            local cli_matches
            cli_matches=$(check_pattern_matches "$work_type" "cli_indicators" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "cli_indicator_match")
            total_score=$((total_score + cli_matches * weight))
            wd_log_debug "$work_type CLI matches: $cli_matches (weighted: $((cli_matches * weight)))"
        fi

        # File action matches (for documentation)
        if [[ "$work_type" == "documentation" && -n "$transcript_content" ]]; then
            local action_matches
            action_matches=$(check_pattern_matches "$work_type" "file_actions" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "file_action_match")
            total_score=$((total_score + action_matches * weight))
            wd_log_debug "$work_type file action matches: $action_matches (weighted: $((action_matches * weight)))"
        fi

        # Content indicator matches (for documentation)
        if [[ "$work_type" == "documentation" && -n "$transcript_content" ]]; then
            local content_matches
            content_matches=$(check_pattern_matches "$work_type" "content_indicators" "$transcript_content")
            local weight
            weight=$(get_scoring_weight "content_indicator_match")
            total_score=$((total_score + content_matches * weight))
            wd_log_debug "$work_type content matches: $content_matches (weighted: $((content_matches * weight)))"
        fi

        # Analyze recent git changes
        total_score=$((total_score + $(analyze_git_changes_for_work_type "$work_type")))

        # Analyze TaskMaster activity for feature development
        if [[ "$work_type" == "feature_development" ]]; then
            local taskmaster_activity
            taskmaster_activity=$(analyze_taskmaster_activity 2>/dev/null || echo 0)
            local weight
            weight=$(get_scoring_weight "taskmaster_activity")
            total_score=$((total_score + taskmaster_activity * weight))
            wd_log_debug "$work_type TaskMaster activity: $taskmaster_activity (weighted: $((taskmaster_activity * weight)))"
        fi

        # Assign score to appropriate variable
        case "$work_type" in
            "feature_development") feature_development_score=$total_score ;;
            "documentation") documentation_score=$total_score ;;
            "github_work") github_work_score=$total_score ;;
        esac
    done

    # Determine highest scoring type
    local max_score=0
    local best_work_type="unknown"

    if [[ $feature_development_score -gt $max_score ]]; then
        max_score=$feature_development_score
        best_work_type="feature_development"
    fi

    if [[ $documentation_score -gt $max_score ]]; then
        max_score=$documentation_score
        best_work_type="documentation"
    fi

    if [[ $github_work_score -gt $max_score ]]; then
        max_score=$github_work_score
        best_work_type="github_work"
    fi

    # Apply minimum threshold
    local min_threshold
    min_threshold=$(jq -r ".thresholds.minimum_score // 3" "$PATTERNS_FILE" 2>/dev/null || echo 3)
    if [[ $max_score -lt $min_threshold ]]; then
        best_work_type="unknown"
    fi

    # Log scoring details
    wd_log_info "Work type scores - Feature: $feature_development_score, Docs: $documentation_score, GitHub: $github_work_score"
    wd_log_info "Selected work type: $best_work_type (score: $max_score, threshold: $min_threshold)"

    # Output only work_type to stdout
    echo "$best_work_type"
}

# Analyze git changes for specific work type
analyze_git_changes_for_work_type() {
    local work_type="$1"
    local score=0

    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo "$score"
        return
    fi

    # Check file extensions in recent changes
    if [[ -n "$(git diff --name-only HEAD~1 2>/dev/null)" ]]; then
        local changed_files
        changed_files=$(git diff --name-only HEAD~1 2>/dev/null | tr '\n' ' ')

        local ext_matches
        ext_matches=$(check_pattern_matches "$work_type" "file_extensions" "$changed_files")
        local weight
        weight=$(get_scoring_weight "file_extension_match")
        score=$((score + ext_matches * weight))
        wd_log_debug "$work_type file extension matches: $ext_matches (weighted: $((ext_matches * weight)))"
    fi

    # Check staged files too
    if [[ -n "$(git diff --cached --name-only 2>/dev/null)" ]]; then
        local staged_files
        staged_files=$(git diff --cached --name-only 2>/dev/null | tr '\n' ' ')

        local ext_matches
        ext_matches=$(check_pattern_matches "$work_type" "file_extensions" "$staged_files")
        local weight
        weight=$(get_scoring_weight "file_extension_match")
        score=$((score + ext_matches * weight))
        wd_log_debug "$work_type staged file extension matches: $ext_matches (weighted: $((ext_matches * weight)))"
    fi

    # Check directory patterns
    local all_files=""
    all_files+=$(git diff --name-only HEAD~1 2>/dev/null | tr '\n' ' ')
    all_files+=$(git diff --cached --name-only 2>/dev/null | tr '\n' ' ')

    if [[ -n "$all_files" ]]; then
        local dir_matches
        dir_matches=$(check_pattern_matches "$work_type" "directory_patterns" "$all_files")
        local weight
        weight=$(get_scoring_weight "directory_match")
        score=$((score + dir_matches * weight))
        wd_log_debug "$work_type directory matches: $dir_matches (weighted: $((dir_matches * weight)))"
    fi

    # Check git commit patterns in recent commits
    if [[ -n "$(git log --oneline -5 --format='%s' 2>/dev/null)" ]]; then
        local recent_commits
        recent_commits=$(git log --oneline -5 --format='%s' 2>/dev/null | tr '\n' ' ')

        local commit_matches
        commit_matches=$(check_pattern_matches "$work_type" "git_commit_patterns" "$recent_commits")
        local weight
        weight=$(get_scoring_weight "git_commit_match")
        score=$((score + commit_matches * weight))
        wd_log_debug "$work_type git commit matches: $commit_matches (weighted: $((commit_matches * weight)))"
    fi

    echo "$score"
}

# Analyze transcript for feature development indicators
analyze_transcript_for_features() {
    local transcript="$1"
    local score=0

    if [[ -f "$transcript" ]]; then
        # Check for feature development keywords
        local feature_keywords
        feature_keywords=$(safe_numeric "grep -ic 'implement\|develop\|create.*function\|add.*feature\|build.*component' '$transcript'")
        score=$((score + feature_keywords))

        local code_files
        code_files=$(safe_numeric "grep -ic '\\.java\|\\.js\|\\.ts\|\\.py\|\\.go\|\\.rs\|\\.cpp' '$transcript'")
        score=$((score + code_files))

        local task_keywords
        task_keywords=$(safe_numeric "grep -ic 'task.*\\(pending\\|in-progress\\|completed\\)' '$transcript'")
        score=$((score + task_keywords))

        local api_keywords
        api_keywords=$(safe_numeric "grep -ic 'api\|endpoint\|service\|controller\|repository' '$transcript'")
        score=$((score + api_keywords))
    fi

    echo "$score"
}

# Analyze transcript for documentation indicators
analyze_transcript_for_docs() {
    local transcript="$1"
    local score=0

    if [[ -f "$transcript" ]]; then
        # Check for documentation keywords
        local doc_keywords
        doc_keywords=$(safe_numeric "grep -ic 'document\|readme\|guide\|\\.md\|\\.txt' '$transcript'")
        score=$((score + doc_keywords))

        local doc_actions
        doc_actions=$(safe_numeric "grep -ic 'write.*doc\|create.*guide\|update.*readme' '$transcript'")
        score=$((score + doc_actions))

        local explain_keywords
        explain_keywords=$(safe_numeric "grep -ic 'explain\|describe\|how to\|tutorial' '$transcript'")
        score=$((score + explain_keywords))
    fi

    echo "$score"
}

# Analyze transcript for GitHub work indicators
analyze_transcript_for_github() {
    local transcript="$1"
    local score=0

    if [[ -f "$transcript" ]]; then
        # Check for GitHub-specific keywords
        local github_keywords
        github_keywords=$(safe_numeric "grep -ic 'pull request\|pr\|merge\|github\|issue' '$transcript'")
        score=$((score + github_keywords))

        local review_keywords
        review_keywords=$(safe_numeric "grep -ic 'review\|approve\|comment.*pr\|close.*issue' '$transcript'")
        score=$((score + review_keywords))

        local mcp_github
        mcp_github=$(safe_numeric "grep -ic 'mcp__github\|gh.*pr\|gh.*issue' '$transcript'")
        score=$((score + mcp_github))
    fi

    echo "$score"
}

# Analyze recent git changes for feature development
analyze_git_changes_for_features() {
    local score=0

    # Check recent commits and staged changes
    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Count code files in recent changes
        local recent_code
        recent_code=$(safe_numeric "git diff --name-only HEAD~1 | grep -E '\\.(java|js|ts|py|go|rs|cpp|c|h)$' | wc -l")
        score=$((score + recent_code))

        local staged_code
        staged_code=$(safe_numeric "git diff --cached --name-only | grep -E '\\.(java|js|ts|py|go|rs|cpp|c|h)$' | wc -l")
        score=$((score + staged_code))

        # Check for feature-related directories
        local feature_dirs
        feature_dirs=$(safe_numeric "git diff --name-only HEAD~1 | grep -E '(src/|lib/|app/|components/)' | wc -l")
        score=$((score + feature_dirs))
    fi

    echo "$score"
}

# Analyze recent git changes for documentation
analyze_git_changes_for_docs() {
    local score=0

    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Count documentation files in recent changes
        local recent_docs
        recent_docs=$(safe_numeric "git diff --name-only HEAD~1 | grep -E '\\.(md|txt|rst|adoc)$' | wc -l")
        score=$((score + recent_docs))

        local staged_docs
        staged_docs=$(safe_numeric "git diff --cached --name-only | grep -E '\\.(md|txt|rst|adoc)$' | wc -l")
        score=$((score + staged_docs))

        # Check for docs directories
        local docs_dirs
        docs_dirs=$(safe_numeric "git diff --name-only HEAD~1 | grep -E '(docs/|documentation/|README)' | wc -l")
        score=$((score + docs_dirs))
    fi

    echo "$score"
}

# Analyze recent git changes for GitHub work
analyze_git_changes_for_github() {
    local score=0

    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Check for GitHub-specific files
        local github_files
        github_files=$(safe_numeric "git diff --name-only HEAD~1 | grep -E '(\\.github/|workflows/|PULL_REQUEST_TEMPLATE)' | wc -l")
        score=$((score + github_files))
    fi

    echo "$score"
}

# Analyze TaskMaster activity for feature development
analyze_taskmaster_activity() {
    local score=0

    # Check if TaskMaster files exist and have recent activity
    if [[ -f "$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json" ]]; then
        # Check for recent TaskMaster activity (file modified in last hour)
        if [[ $(find "$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json" -mmin -60 2>/dev/null) ]]; then
            score=$((score + 5))
        fi

        # Check for in-progress tasks
        if command -v jq >/dev/null 2>&1; then
            local in_progress_count
            in_progress_count=$(safe_numeric "jq '[.tasks[] | select(.status == \"in-progress\")] | length' '$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json'")
            score=$((score + in_progress_count * 2))
        fi
    fi

    echo "$score"
}


# Helper function to safely get numeric value from command output
safe_numeric() {
    local value
    value=$(eval "$1" 2>/dev/null)
    value=${value:-0}
    # Clean the value - take first line and remove spaces
    value=$(echo "$value" | head -1 | tr -d '[:space:]')
    # Validate it's a number
    if [[ "$value" =~ ^[0-9]+$ ]]; then
        echo "$value"
    else
        echo "0"
    fi
}

# Helper function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Validate environment
validate_environment() {
    local errors=0

    # Set default CLAUDE_PROJECT_DIR if not set
    if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
        export CLAUDE_PROJECT_DIR="$(cd "$(dirname "$(dirname "$(dirname "$0")")")" && pwd)"
    fi

    if ! command_exists git; then
        wd_log_warn "git command not found - git-based analysis will be limited"
        errors=$((errors + 1))
    fi

    if ! command_exists jq; then
        wd_log_warn "jq command not found - TaskMaster analysis will be limited"
        errors=$((errors + 1))
    fi

    return $errors
}

# Initialize if running directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    wd_log_info "Starting work type detection utility"
    wd_log_debug "Environment validation starting"
    validate_environment

    if [[ $# -gt 0 ]]; then
        case "$1" in
            --help|-h)
                echo "Usage: $0 <transcript_path>"
                echo "       $0 --help"
                echo "       $0 --log-help"
                echo ""
                echo "Analyzes session transcript and git changes to determine work type."
                echo ""
                echo "Options:"
                echo "  --help, -h     Show this help message"
                echo "  --log-help     Show logging configuration help"
                echo ""
                echo "Output: One of 'feature_development', 'documentation', 'github_work', or 'unknown'"
                exit 0
                ;;
            --log-help)
                show_log_usage
                exit 0
                ;;
            *)
                wd_log_info "Analyzing transcript: $1"
                work_type=$(determine_work_type "$1")
                wd_log_info "Detected work type: $work_type"
                echo "$work_type"
                ;;
        esac
    else
        wd_log_error "No transcript path provided"
        echo "Usage: $0 <transcript_path>"
        echo "Use '$0 --help' for more information."
        exit 1
    fi

    wd_log_info "Work type detection completed"
fi
