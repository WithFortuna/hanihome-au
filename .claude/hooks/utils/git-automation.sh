#!/bin/bash

# Git Automation Utility
# Handles automated git operations for different workflows

# Commit changes with appropriate message
commit_changes() {
    local commit_message="$1"
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    # Change to project directory
    cd "$project_dir" || {
        log_error "Failed to change to project directory: $project_dir"
        return 1
    }
    
    # Check if we're in a git repository
    if ! git rev-parse --git-dir >/dev/null 2>&1; then
        log_error "Not in a git repository"
        return 1
    fi
    
    # Check for staged changes
    if ! git diff --cached --quiet; then
        log_info "Staged changes found, committing..."
    else
        # Stage all changes if nothing is staged
        log_info "No staged changes, staging all modifications..."
        git add . || {
            log_error "Failed to stage changes"
            return 1
        }
        
        # Check if there are any changes to commit after staging
        if git diff --cached --quiet; then
            log_info "No changes to commit"
            return 0
        fi
    fi
    
    # Create commit with Claude signature
    git commit -m "$commit_message

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>" || {
        log_error "Failed to create commit"
        return 1
    }
    
    log_info "Successfully committed changes: $commit_message"
    return 0
}

# Switch to or create document branch
switch_to_document_branch() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    local doc_branch="document"
    
    cd "$project_dir" || return 1
    
    # Check if document branch exists
    if git show-ref --verify --quiet "refs/heads/$doc_branch"; then
        log_info "Switching to existing document branch"
        git checkout "$doc_branch" || {
            log_error "Failed to switch to document branch"
            return 1
        }
    else
        log_info "Creating new document branch"
        git checkout -b "$doc_branch" || {
            log_error "Failed to create document branch"
            return 1
        }
    fi
    
    return 0
}

# Switch to main/master branch
switch_to_main_branch() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    # Determine main branch name
    local main_branch
    main_branch=$(get_main_branch_name)
    
    if [[ -z "$main_branch" ]]; then
        log_error "Could not determine main branch name"
        return 1
    fi
    
    log_info "Switching to main branch: $main_branch"
    git checkout "$main_branch" || {
        log_error "Failed to switch to main branch"
        return 1
    }
    
    return 0
}

# Get the name of the main branch (main or master)
get_main_branch_name() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    # Check if 'main' branch exists
    if git show-ref --verify --quiet "refs/heads/main"; then
        echo "main"
    elif git show-ref --verify --quiet "refs/heads/master"; then
        echo "master"
    else
        # Try to get default branch from remote
        git symbolic-ref refs/remotes/origin/HEAD 2>/dev/null | sed 's@^refs/remotes/origin/@@' || echo ""
    fi
}

# Create a feature branch for current work
create_feature_branch() {
    local branch_name="$1"
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    # Generate branch name if not provided
    if [[ -z "$branch_name" ]]; then
        branch_name=$(generate_feature_branch_name)
    fi
    
    log_info "Creating feature branch: $branch_name"
    
    # Ensure we're on main branch first
    local main_branch
    main_branch=$(get_main_branch_name)
    git checkout "$main_branch" >/dev/null 2>&1
    
    # Create and switch to feature branch
    git checkout -b "$branch_name" || {
        log_error "Failed to create feature branch: $branch_name"
        return 1
    }
    
    echo "$branch_name"
    return 0
}

# Generate feature branch name based on current TaskMaster task
generate_feature_branch_name() {
    local tasks_file="$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json"
    local branch_name="feature/auto-$(date +%Y%m%d-%H%M%S)"
    
    # Try to get current task for better branch name
    if [[ -f "$tasks_file" ]] && command -v jq >/dev/null 2>&1; then
        local task_title
        task_title=$(jq -r '
            .tasks[] | 
            select(.status == "in-progress" or .status == "pending") | 
            .title | 
            ascii_downcase | 
            gsub("[^a-z0-9가-힣]"; "-") | 
            gsub("-+"; "-") | 
            gsub("^-|-$"; "")
        ' "$tasks_file" | head -1)
        
        if [[ -n "$task_title" && "$task_title" != "null" ]]; then
            # Truncate if too long
            if [[ ${#task_title} -gt 30 ]]; then
                task_title="${task_title:0:30}"
            fi
            branch_name="feature/$task_title-$(date +%m%d)"
        fi
    fi
    
    echo "$branch_name"
}

# Push current branch to remote
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
    
    # Push with upstream tracking
    git push -u origin "$current_branch" || {
        log_error "Failed to push branch to remote"
        return 1
    }
    
    return 0
}

# Check if there are uncommitted changes
has_uncommitted_changes() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    # Check for staged or unstaged changes
    if ! git diff --quiet || ! git diff --cached --quiet; then
        return 0  # Has changes
    fi
    
    # Check for untracked files
    if [[ -n "$(git ls-files --others --exclude-standard)" ]]; then
        return 0  # Has untracked files
    fi
    
    return 1  # No changes
}

# Get git status summary
get_git_status_summary() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    cd "$project_dir" || return 1
    
    local status_info
    status_info=$(git status --porcelain)
    
    local modified_count=0
    local added_count=0
    local deleted_count=0
    local untracked_count=0
    
    while IFS= read -r line; do
        case "${line:0:2}" in
            " M"|"MM"|"AM") ((modified_count++)) ;;
            "A "|"AM") ((added_count++)) ;;
            " D"|"AD") ((deleted_count++)) ;;
            "??") ((untracked_count++)) ;;
        esac
    done <<< "$status_info"
    
    jq -n \
        --argjson modified "$modified_count" \
        --argjson added "$added_count" \
        --argjson deleted "$deleted_count" \
        --argjson untracked "$untracked_count" \
        '{
            modified: $modified,
            added: $added,
            deleted: $deleted,
            untracked: $untracked,
            total: ($modified + $added + $deleted + $untracked)
        }'
}

# Validate git environment
validate_git_environment() {
    local project_dir="$CLAUDE_PROJECT_DIR"
    
    # Check if git is available
    if ! command -v git >/dev/null 2>&1; then
        log_error "Git command not found"
        return 1
    fi
    
    # Check if we're in project directory
    if [[ ! -d "$project_dir" ]]; then
        log_error "Project directory not found: $project_dir"
        return 1
    fi
    
    cd "$project_dir" || return 1
    
    # Check if it's a git repository
    if ! git rev-parse --git-dir >/dev/null 2>&1; then
        log_error "Not a git repository: $project_dir"
        return 1
    fi
    
    # Check git configuration
    if [[ -z "$(git config user.name)" ]] || [[ -z "$(git config user.email)" ]]; then
        log_warning "Git user configuration incomplete"
    fi
    
    return 0
}

# Logging functions
log_info() {
    echo "[INFO] Git: $1" | tee -a "${HOOKS_DIR:-}/git-automation.log"
}

log_error() {
    echo "[ERROR] Git: $1" | tee -a "${HOOKS_DIR:-}/git-automation.log" >&2
}

log_warning() {
    echo "[WARNING] Git: $1" | tee -a "${HOOKS_DIR:-}/git-automation.log" >&2
}

# Initialize if running directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    validate_git_environment
    
    # Handle command line arguments
    case "${1:-}" in
        "status")
            get_git_status_summary
            ;;
        "commit")
            commit_changes "${2:-Auto commit by Claude Code}"
            ;;
        "switch-doc")
            switch_to_document_branch
            ;;
        "switch-main")
            switch_to_main_branch
            ;;
        *)
            echo "Usage: $0 {status|commit [message]|switch-doc|switch-main}"
            exit 1
            ;;
    esac
fi