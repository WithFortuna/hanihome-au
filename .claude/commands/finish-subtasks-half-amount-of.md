# Finish Half Subtasks of Next Task

Analyze current task progress and complete half of the pending subtasks from the next available task.

## Usage
```
/finish-subtasks-half-amount-of
```

## Steps
1. Get current project status: `task-master list`
2. Find next available task: `task-master next`
3. Get detailed view of next task: `task-master show <next-task-id>`
4. Identify all pending subtasks
5. Calculate half of pending subtasks (rounded up)
6. Complete each subtask in order:
   - Set subtask status to in-progress
   - Implement required functionality
   - Set subtask status to done
7. Show remaining subtasks and overall progress

## Example
If next task has 6 pending subtasks, this will complete the first 3 subtasks.
