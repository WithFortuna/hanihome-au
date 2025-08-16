Finish half the amount of pending subtasks by marking them as done.

Steps:
1. Get all pending subtasks using TaskMaster
2. Calculate half the amount (rounded down)
3. Mark that many subtasks as completed, respecting dependencies
4. Show the updated task status

This command helps with bulk task completion during development cycles.