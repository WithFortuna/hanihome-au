#!/bin/bash

# Pull Request Automation Utility
# Handles automated PR creation for different workflows

# Create feature PR to main branch
create_feature_pr() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    local current_branch
    local main_branch
    
    cd "$project_dir" || return 1
    
    current_branch=$(git branch --show-current)
    main_branch=$(get_main_branch_name)
    
    if [[ -z "$current_branch" ]]; then
        log_error "Could not determine current branch"
        return 1
    fi
    
    if [[ -z "$main_branch" ]]; then
        log_error "Could not determine main branch"
        return 1
    fi
    
    # Push current branch if needed
    if ! git ls-remote --exit-code origin "$current_branch" >/dev/null 2>&1; then
        log_info "Pushing branch to remote..."
        if ! push_to_remote; then
            return 1
        fi
    fi
    
    # Generate PR title and body
    local pr_title pr_body
    pr_title=$(generate_feature_pr_title)
    pr_body=$(generate_feature_pr_body)
    
    # Create PR using gh CLI if available
    if command -v gh >/dev/null 2>&1; then
        log_info "Creating PR using GitHub CLI..."
        create_pr_with_gh "$pr_title" "$pr_body" "$main_branch"
    else
        log_info "GitHub CLI not available, creating PR URL..."
        create_pr_url "$pr_title" "$pr_body" "$main_branch"
    fi
}

# Create documentation PR to document branch or main
create_documentation_pr() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    local current_branch
    local target_branch="main"  # Default target for documentation
    
    cd "$project_dir" || return 1
    
    current_branch=$(git branch --show-current)
    
    # If we're on document branch, target main branch
    if [[ "$current_branch" == "document" ]]; then
        target_branch=$(get_main_branch_name)
    fi
    
    # Push current branch if needed
    if ! git ls-remote --exit-code origin "$current_branch" >/dev/null 2>&1; then
        log_info "Pushing documentation branch to remote..."
        if ! push_to_remote; then
            return 1
        fi
    fi
    
    # Generate PR title and body for documentation
    local pr_title pr_body
    pr_title=$(generate_doc_pr_title)
    pr_body=$(generate_doc_pr_body)
    
    # Create PR
    if command -v gh >/dev/null 2>&1; then
        log_info "Creating documentation PR using GitHub CLI..."
        create_pr_with_gh "$pr_title" "$pr_body" "$target_branch"
    else
        log_info "GitHub CLI not available, creating PR URL..."
        create_pr_url "$pr_title" "$pr_body" "$target_branch"
    fi
}

# Create PR using GitHub CLI
create_pr_with_gh() {
    local title="$1"
    local body="$2"
    local base_branch="$3"
    
    # Create PR with gh command
    local pr_result
    pr_result=$(gh pr create \
        --title "$title" \
        --body "$body" \
        --base "$base_branch" \
        --head "$(git branch --show-current)" 2>&1)
    
    if [[ $? -eq 0 ]]; then
        log_info "PR created successfully"
        echo "$pr_result" | grep -o 'https://github.com[^[:space:]]*'
        return 0
    else
        log_error "Failed to create PR: $pr_result"
        return 1
    fi
}

# Generate PR URL for manual creation
create_pr_url() {
    local title="$1"
    local body="$2"
    local base_branch="$3"
    local current_branch
    
    current_branch=$(git branch --show-current)
    
    # Get repository info
    local remote_url repo_info
    remote_url=$(git remote get-url origin 2>/dev/null)
    
    if [[ -z "$remote_url" ]]; then
        log_error "No remote origin found"
        return 1
    fi
    
    # Extract owner/repo from remote URL
    repo_info=$(extract_repo_info "$remote_url")
    
    if [[ -z "$repo_info" ]]; then
        log_error "Could not extract repository information"
        return 1
    fi
    
    # Generate GitHub PR URL
    local encoded_title encoded_body pr_url
    encoded_title=$(url_encode "$title")
    encoded_body=$(url_encode "$body")
    
    pr_url="https://github.com/$repo_info/compare/$base_branch...$current_branch?expand=1&title=$encoded_title&body=$encoded_body"
    
    log_info "PR URL generated: $pr_url"
    echo "$pr_url"
    
    return 0
}

# Generate feature PR title
generate_feature_pr_title() {
    local tasks_file="$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json"
    local title="Feature: Auto-generated implementation"
    
    # Try to get current task title
    if [[ -f "$tasks_file" ]] && command -v jq >/dev/null 2>&1; then
        local task_title
        task_title=$(jq -r '
            .tasks[] | 
            select(.status == "in-progress" or .status == "done") | 
            .title
        ' "$tasks_file" | head -1)
        
        if [[ -n "$task_title" && "$task_title" != "null" ]]; then
            title="Feature: $task_title"
        fi
    fi
    
    echo "$title"
}

# Generate feature PR body
generate_feature_pr_body() {
    local tasks_file="$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json"
    local body="## Summary\n자동 생성된 기능 구현\n\n"
    
    # Add TaskMaster task information if available
    if [[ -f "$tasks_file" ]] && command -v jq >/dev/null 2>&1; then
        local task_info
        task_info=$(jq -r '
            .tasks[] | 
            select(.status == "in-progress" or .status == "done") | 
            "- **Task " + (.id | tostring) + "**: " + .title + "\n" +
            "  - Status: " + .status + "\n" +
            "  - Subtasks: " + (
                if .subtasks then 
                    (.subtasks | map(select(.status == "done")) | length | tostring) + 
                    "/" + 
                    (.subtasks | length | tostring) + " completed"
                else 
                    "No subtasks"
                end
            )
        ' "$tasks_file" | head -5)
        
        if [[ -n "$task_info" ]]; then
            body+="## TaskMaster Tasks\n$task_info\n\n"
        fi
    fi
    
    # Add git changes summary
    local git_summary
    git_summary=$(get_git_changes_summary)
    if [[ -n "$git_summary" ]]; then
        body+="## Changes\n$git_summary\n\n"
    fi
    
    body+="## Test Plan\n- [ ] 기능 테스트 완료\n- [ ] 코드 리뷰 완료\n- [ ] 문서 업데이트 확인\n\n"
    body+="🤖 Generated with [Claude Code](https://claude.ai/code)"
    
    echo -e "$body"
}

# Generate documentation PR title
generate_doc_pr_title() {
    echo "docs: Update project documentation"
}

# Generate documentation PR body
generate_doc_pr_body() {
    local body="## Summary\n프로젝트 문서 업데이트\n\n"
    
    # Add changed documentation files
    local doc_changes
    doc_changes=$(git diff --name-only HEAD~1 2>/dev/null | grep -E '\.(md|txt|rst)$' | head -10)
    
    if [[ -n "$doc_changes" ]]; then
        body+="## Updated Files\n"
        while IFS= read -r file; do
            body+="- $file\n"
        done <<< "$doc_changes"
        body+="\n"
    fi
    
    body+="## Review Checklist\n"
    body+="- [ ] 문서 내용 정확성 확인\n"
    body+="- [ ] 링크 동작 확인\n"
    body+="- [ ] 형식 및 스타일 확인\n\n"
    body+="🤖 Generated with [Claude Code](https://claude.ai/code)"
    
    echo -e "$body"
}

# Get git changes summary
get_git_changes_summary() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    # Get file changes from last few commits
    local changes
    changes=$(git diff --name-status HEAD~3..HEAD 2>/dev/null | head -20)
    
    if [[ -z "$changes" ]]; then
        echo "No recent changes detected"
        return 0
    fi
    
    local summary=""
    while IFS=$'\t' read -r status file; do
        case "$status" in
            "A") summary+="- ✅ Added: $file\n" ;;
            "M") summary+="- 📝 Modified: $file\n" ;;
            "D") summary+="- ❌ Deleted: $file\n" ;;
            "R"*) summary+="- 🔄 Renamed: $file\n" ;;
            *) summary+="- 📄 Changed: $file\n" ;;
        esac
    done <<< "$changes"
    
    echo -e "$summary"
}

# Extract repository info from git remote URL
extract_repo_info() {
    local remote_url="$1"
    
    # Handle different Git URL formats
    case "$remote_url" in
        https://github.com/*)
            echo "$remote_url" | sed 's|https://github.com/||' | sed 's|\.git$||'
            ;;
        git@github.com:*)
            echo "$remote_url" | sed 's|git@github.com:||' | sed 's|\.git$||'
            ;;
        *)
            echo ""
            ;;
    esac
}

# URL encoding function
url_encode() {
    local string="$1"
    echo "$string" | python3 -c "import sys, urllib.parse; print(urllib.parse.quote(sys.stdin.read().strip()))" 2>/dev/null || \
    echo "$string" | sed 's/ /%20/g; s/!/%21/g; s/"/%22/g; s/#/%23/g; s/\$/%24/g; s/&/%26/g; s/'\''/%27/g'
}

# Get main branch name (reuse from git-automation.sh)
get_main_branch_name() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    if git show-ref --verify --quiet "refs/heads/main"; then
        echo "main"
    elif git show-ref --verify --quiet "refs/heads/master"; then
        echo "master"
    else
        git symbolic-ref refs/remotes/origin/HEAD 2>/dev/null | sed 's@^refs/remotes/origin/@@' || echo "main"
    fi
}

# Push to remote (reuse from git-automation.sh)
push_to_remote() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    local current_branch
    
    cd "$project_dir" || return 1
    
    current_branch=$(git branch --show-current)
    
    if [[ -z "$current_branch" ]]; then
        log_error "Could not determine current branch"
        return 1
    fi
    
    log_info "Pushing branch to remote: $current_branch"
    
    git push -u origin "$current_branch" || {
        log_error "Failed to push branch to remote"
        return 1
    }
    
    return 0
}

# Logging functions
log_info() {
    echo "[INFO] PR: $1" | tee -a "${HOOKS_DIR:-}/pr-automation.log"
}

log_error() {
    echo "[ERROR] PR: $1" | tee -a "${HOOKS_DIR:-}/pr-automation.log" >&2
}

# Initialize if running directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    case "${1:-}" in
        "feature")
            create_feature_pr
            ;;
        "docs")
            create_documentation_pr
            ;;
        *)
            echo "Usage: $0 {feature|docs}"
            exit 1
            ;;
    esac
fi