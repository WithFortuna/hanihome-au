#!/bin/bash

# TaskMaster Progress Checker Utility
# Analyzes current task progress and subtask completion status

# Check task progress using TaskMaster
check_task_progress() {
    local project_root="$CLAUDE_PROJECT_DIR"
    local tasks_file="$project_root/.taskmaster/tasks/tasks.json"
    
    # Validate TaskMaster setup
    if ! validate_taskmaster_setup "$project_root"; then
        echo '{"error": "TaskMaster not properly configured"}' >&2
        return 1
    fi
    
    # Get current task information
    local current_task_info
    current_task_info=$(get_current_task_info "$tasks_file")
    
    if [[ $? -ne 0 ]]; then
        echo '{"error": "Failed to get current task information"}' >&2
        return 1
    fi
    
    # Calculate progress metrics
    local progress_data
    progress_data=$(calculate_progress_metrics "$current_task_info" "$tasks_file")
    
    echo "$progress_data"
}

# Validate TaskMaster setup
validate_taskmaster_setup() {
    local project_root="$1"
    
    # Check if TaskMaster directory exists
    if [[ ! -d "$project_root/.taskmaster" ]]; then
        log_debug "TaskMaster directory not found"
        return 1
    fi
    
    # Check if tasks.json exists
    if [[ ! -f "$project_root/.taskmaster/tasks/tasks.json" ]]; then
        log_debug "tasks.json not found"
        return 1
    fi
    
    # Check if jq is available
    if ! command -v jq >/dev/null 2>&1; then
        log_debug "jq command not found"
        return 1
    fi
    
    return 0
}

# Get current task information
get_current_task_info() {
    local tasks_file="$1"
    
    # Try to get current task using TaskMaster CLI if available
    if command -v task-master >/dev/null 2>&1; then
        task-master next --projectRoot="$CLAUDE_PROJECT_DIR" 2>/dev/null || echo "{}"
    else
        # Fallback: parse tasks.json directly
        get_current_task_from_json "$tasks_file"
    fi
}

# Get current task from JSON file directly
get_current_task_from_json() {
    local tasks_file="$1"
    
    if [[ ! -f "$tasks_file" ]]; then
        echo "{}"
        return 1
    fi
    
    # Find first in-progress task, or first pending task if none in progress
    local current_task
    current_task=$(jq -r '
        .tasks[] | 
        select(.status == "in-progress" or .status == "pending") | 
        . as $task |
        if .subtasks and (.subtasks | length > 0) then
            .subtasks | map(select(.status == "in-progress" or .status == "pending")) | 
            if length > 0 then 
                {
                    task_id: $task.id,
                    task_title: $task.title,
                    subtask_id: .[0].id,
                    subtask_title: .[0].title,
                    is_subtask: true
                }
            else
                {
                    task_id: $task.id,
                    task_title: $task.title,
                    is_subtask: false
                }
            end
        else
            {
                task_id: $task.id,
                task_title: $task.title,
                is_subtask: false
            }
        end
    ' "$tasks_file" | head -1)
    
    if [[ -z "$current_task" || "$current_task" == "null" ]]; then
        echo "{}"
    else
        echo "$current_task"
    fi
}

# Calculate progress metrics
calculate_progress_metrics() {
    local current_task_info="$1"
    local tasks_file="$2"
    
    local task_id
    task_id=$(echo "$current_task_info" | jq -r '.task_id // ""')
    
    if [[ -z "$task_id" || "$task_id" == "null" ]]; then
        echo '{"error": "No current task found"}' >&2
        return 1
    fi
    
    # Get task details
    local task_details
    task_details=$(jq --arg id "$task_id" '.tasks[] | select(.id == ($id | tonumber))' "$tasks_file")
    
    if [[ -z "$task_details" || "$task_details" == "null" ]]; then
        echo '{"error": "Task not found"}' >&2
        return 1
    fi
    
    # Calculate subtask progress
    local total_subtasks completed_subtasks progress_ratio
    
    total_subtasks=$(echo "$task_details" | jq '.subtasks | length')
    completed_subtasks=$(echo "$task_details" | jq '[.subtasks[] | select(.status == "done")] | length')
    
    if [[ $total_subtasks -gt 0 ]]; then
        progress_ratio=$(echo "scale=2; $completed_subtasks * 100 / $total_subtasks" | bc -l)
    else
        progress_ratio=0
    fi
    
    # Get next available subtask
    local next_subtask
    next_subtask=$(echo "$task_details" | jq -r '
        .subtasks[] | 
        select(.status == "pending" or .status == "in-progress") | 
        {id: .id, title: .title, status: .status} | 
        @json
    ' | head -1)
    
    # Build response
    local response
    response=$(jq -n \
        --arg task_id "$task_id" \
        --arg task_title "$(echo "$task_details" | jq -r '.title')" \
        --argjson total_subtasks "$total_subtasks" \
        --argjson completed_subtasks "$completed_subtasks" \
        --argjson completed_ratio "$progress_ratio" \
        --argjson next_subtask "${next_subtask:-null}" \
        '{
            current_task: $task_id,
            current_task_title: $task_title,
            total_subtasks: $total_subtasks,
            completed_subtasks: $completed_subtasks,
            completed_ratio: $completed_ratio,
            next_subtask: $next_subtask
        }')
    
    echo "$response"
}

# Get recommended next action based on progress
get_recommended_action() {
    local progress_info="$1"
    
    local completed_ratio
    completed_ratio=$(echo "$progress_info" | jq -r '.completed_ratio // 0')
    
    local next_subtask
    next_subtask=$(echo "$progress_info" | jq -r '.next_subtask // null')
    
    if (( $(echo "$completed_ratio < 50" | bc -l) )); then
        if [[ "$next_subtask" != "null" ]]; then
            local subtask_title
            subtask_title=$(echo "$next_subtask" | jq -r '.title')
            echo "continue_work:$subtask_title"
        else
            echo "continue_work:general"
        fi
    else
        echo "ready_for_commit"
    fi
}

# Check if specific task is ready for completion
is_task_ready_for_completion() {
    local task_id="$1"
    local tasks_file="$CLAUDE_PROJECT_DIR/.taskmaster/tasks/tasks.json"
    
    if [[ ! -f "$tasks_file" ]]; then
        return 1
    fi
    
    local task_progress
    task_progress=$(jq --arg id "$task_id" '
        .tasks[] | 
        select(.id == ($id | tonumber)) |
        {
            total: (.subtasks | length),
            completed: ([.subtasks[] | select(.status == "done")] | length)
        } |
        if .total > 0 then (.completed * 100 / .total) else 0 end
    ' "$tasks_file")
    
    if [[ -n "$task_progress" ]]; then
        return $(echo "$task_progress >= 50" | bc -l)
    fi
    
    return 1
}

# Get task summary for user display
get_task_summary() {
    local progress_info="$1"
    
    local task_title completed completed_ratio total
    task_title=$(echo "$progress_info" | jq -r '.current_task_title')
    completed=$(echo "$progress_info" | jq -r '.completed_subtasks')
    total=$(echo "$progress_info" | jq -r '.total_subtasks')
    completed_ratio=$(echo "$progress_info" | jq -r '.completed_ratio')
    
    cat << EOF
현재 작업: $task_title
진행률: $completed/$total subtasks (${completed_ratio}%)
EOF
}

# Update task status using TaskMaster CLI
update_task_status() {
    local task_id="$1"
    local status="$2"
    local project_root="$CLAUDE_PROJECT_DIR"
    
    if command -v task-master >/dev/null 2>&1; then
        task-master set-status --id="$task_id" --status="$status" --projectRoot="$project_root" >/dev/null 2>&1
        return $?
    else
        log_debug "TaskMaster CLI not available for status update"
        return 1
    fi
}

# Log debug messages
log_debug() {
    if [[ "${DEBUG_TASK_PROGRESS:-}" == "true" ]]; then
        echo "[DEBUG] $1" >&2
    fi
}

# Initialize if running directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
        export CLAUDE_PROJECT_DIR="$(cd "$(dirname "$(dirname "$(dirname "$0")")")" && pwd)"
    fi
    
    check_task_progress
fi