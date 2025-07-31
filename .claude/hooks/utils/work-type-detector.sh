#!/bin/bash

# Work Type Detection Utility
# Analyzes session transcript and recent changes to determine work type

# Load work patterns configuration
CONFIG_DIR="$(dirname "$(dirname "$0")")/config"
PATTERNS_FILE="$CONFIG_DIR/work-patterns.json"

# Determine work type based on transcript and git changes
determine_work_type() {
    local transcript_path="$1"
    
    # Initialize scores for each work type
    local feature_score=0
    local doc_score=0
    local github_score=0
    
    # Analyze transcript content if available
    if [[ -f "$transcript_path" ]]; then
        local transcript_features
        transcript_features=$(analyze_transcript_for_features "$transcript_path" 2>/dev/null || echo 0)
        feature_score=$((feature_score + ${transcript_features:-0}))
        
        local transcript_docs
        transcript_docs=$(analyze_transcript_for_docs "$transcript_path" 2>/dev/null || echo 0)
        doc_score=$((doc_score + ${transcript_docs:-0}))
        
        local transcript_github
        transcript_github=$(analyze_transcript_for_github "$transcript_path" 2>/dev/null || echo 0)
        github_score=$((github_score + ${transcript_github:-0}))
    fi
    
    # Analyze recent git changes
    local git_features
    git_features=$(analyze_git_changes_for_features 2>/dev/null || echo 0)
    feature_score=$((feature_score + ${git_features:-0}))
    
    local git_docs
    git_docs=$(analyze_git_changes_for_docs 2>/dev/null || echo 0)
    doc_score=$((doc_score + ${git_docs:-0}))
    
    local git_github
    git_github=$(analyze_git_changes_for_github 2>/dev/null || echo 0)
    github_score=$((github_score + ${git_github:-0}))
    
    # Analyze TaskMaster activity
    local taskmaster_activity
    taskmaster_activity=$(analyze_taskmaster_activity 2>/dev/null || echo 0)
    feature_score=$((feature_score + ${taskmaster_activity:-0}))
    
    # Determine highest scoring type
    local max_score=0
    local work_type="unknown"
    
    if [[ $feature_score -gt $max_score ]]; then
        max_score=$feature_score
        work_type="feature_development"
    fi
    
    if [[ $doc_score -gt $max_score ]]; then
        max_score=$doc_score
        work_type="documentation"
    fi
    
    if [[ $github_score -gt $max_score ]]; then
        max_score=$github_score  
        work_type="github_work"
    fi
    
    # Log scoring details
    log_debug "Work type scores - Feature: $feature_score, Docs: $doc_score, GitHub: $github_score"
    log_debug "Selected work type: $work_type (score: $max_score)"
    
    echo "$work_type"
}

# Analyze transcript for feature development indicators
analyze_transcript_for_features() {
    local transcript="$1"
    local score=0
    
    if [[ -f "$transcript" ]]; then
        # Check for feature development keywords
        local feature_keywords
        feature_keywords=$(grep -ic "implement\|develop\|create.*function\|add.*feature\|build.*component" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${feature_keywords:-0}))
        local code_files
        code_files=$(grep -ic "\.java\|\.js\|\.ts\|\.py\|\.go\|\.rs\|\.cpp" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${code_files:-0}))
        local task_keywords
        task_keywords=$(grep -ic "task.*\(pending\|in-progress\|completed\)" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${task_keywords:-0}))
        local api_keywords
        api_keywords=$(grep -ic "api\|endpoint\|service\|controller\|repository" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${api_keywords:-0}))
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
        doc_keywords=$(grep -ic "document\|readme\|guide\|\.md\|\.txt" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${doc_keywords:-0}))
        local doc_actions
        doc_actions=$(grep -ic "write.*doc\|create.*guide\|update.*readme" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${doc_actions:-0}))
        local explain_keywords
        explain_keywords=$(grep -ic "explain\|describe\|how to\|tutorial" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${explain_keywords:-0}))
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
        github_keywords=$(grep -ic "pull request\|pr\|merge\|github\|issue" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${github_keywords:-0}))
        local review_keywords
        review_keywords=$(grep -ic "review\|approve\|comment.*pr\|close.*issue" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${review_keywords:-0}))
        local mcp_github
        mcp_github=$(grep -ic "mcp__github\|gh.*pr\|gh.*issue" "$transcript" 2>/dev/null || echo 0)
        score=$((score + ${mcp_github:-0}))
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
        recent_code=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '\.(java|js|ts|py|go|rs|cpp|c|h)$' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${recent_code:-0}))
        local staged_code
        staged_code=$(git diff --cached --name-only 2>/dev/null | grep -E '\.(java|js|ts|py|go|rs|cpp|c|h)$' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${staged_code:-0}))
        
        # Check for feature-related directories
        local feature_dirs
        feature_dirs=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '(src/|lib/|app/|components/)' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${feature_dirs:-0}))
    fi
    
    echo "$score"
}

# Analyze recent git changes for documentation
analyze_git_changes_for_docs() {
    local score=0
    
    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Count documentation files in recent changes
        local recent_docs
        recent_docs=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '\.(md|txt|rst|adoc)$' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${recent_docs:-0}))
        local staged_docs
        staged_docs=$(git diff --cached --name-only 2>/dev/null | grep -E '\.(md|txt|rst|adoc)$' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${staged_docs:-0}))
        
        # Check for docs directories
        local docs_dirs
        docs_dirs=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '(docs/|documentation/|README)' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${docs_dirs:-0}))
    fi
    
    echo "$score"
}

# Analyze recent git changes for GitHub work
analyze_git_changes_for_github() {
    local score=0
    
    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Check for GitHub-specific files
        local github_files
        github_files=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '(\.github/|workflows/|PULL_REQUEST_TEMPLATE)' | wc -l 2>/dev/null || echo 0)
        score=$((score + ${github_files:-0}))
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
            in_progress_count=$(jq '[.tasks[] | select(.status == "in-progress")] | length' "$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json" 2>/dev/null || echo 0)
            score=$((score + ${in_progress_count:-0} * 2))
        fi
    fi
    
    echo "$score"
}

# Debug logging function
log_debug() {
    if [[ "${DEBUG_WORK_DETECTOR:-}" == "true" ]]; then
        echo "[DEBUG] $1" >&2
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
        echo "WARNING: git command not found" >&2
    fi
    
    if ! command_exists jq; then
        echo "WARNING: jq command not found, TaskMaster analysis will be limited" >&2
    fi
    
    return $errors
}

# Initialize if running directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    validate_environment
    if [[ $# -gt 0 ]]; then
        determine_work_type "$1"
    else
        echo "Usage: $0 <transcript_path>"
        exit 1
    fi
fi