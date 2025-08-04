# Git Automation After Development

Automatically detect completed TaskMaster work, analyze change type, create appropriate Git branch, commit changes, and create PR following the project's Git workflow standards.

## Usage
```
/git-automation-after-coding
```

## Steps
1. Check git status for modified/added files
2. Analyze TaskMaster task list: `task-master list`
3. Identify recently completed tasks/subtasks and determine work type:
   - File changes analysis (new files = feat, modified = fix/refactor/etc)
   - Recently completed tasks in TaskMaster
   - Code patterns and implementation areas
4. Determine appropriate task ID, scope, and change type
5. Create appropriate branch following naming convention:
   - Feature: `feature/task-{id}-{description}`
   - Bug fix: `bugfix/task-{id}-{description}`
   - Performance: `perf/task-{id}-{description}`
   - Refactor: `refactor/task-{id}-{description}`
   - Documentation: `docs/task-{id}-{description}`
   - Style: `style/task-{id}-{description}`
   - Test: `test/task-{id}-{description}`
   - Chore: `chore/task-{id}-{description}`
6. Stage and commit changes with conventional commit format
7. Push branch to origin using GitHub MCP
8. Create PR using GitHub MCP with proper format
9. Update task status to review if appropriate

## Branch Naming Convention by Type
- **feat**: `feature/task-{id}-{description}` - New features
- **fix**: `bugfix/task-{id}-{description}` - Bug fixes
- **docs**: `docs/task-{id}-{description}` - Documentation changes
- **style**: `style/task-{id}-{description}` - Code style/formatting
- **refactor**: `refactor/task-{id}-{description}` - Code refactoring
- **perf**: `perf/task-{id}-{description}` - Performance improvements
- **test**: `test/task-{id}-{description}` - Adding/updating tests
- **chore**: `chore/task-{id}-{description}` - Maintenance tasks

## Commit Format by Type
```
<type>(scope): <description> (task <id>)

[optional body with implementation details]

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

## PR Title Format by Type
- `[Task {id}] Feat: {Description}` - New features
- `[Task {id}] Fix: {Description}` - Bug fixes
- `[Task {id}] Docs: {Description}` - Documentation
- `[Task {id}] Refactor: {Description}` - Code refactoring
- `[Task {id}] Perf: {Description}` - Performance improvements
- `[Task {id}] Style: {Description}` - Code formatting
- `[Task {id}] Test: {Description}` - Test updates
- `[Task {id}] Chore: {Description}` - Maintenance

## Auto-Detection Logic
- **New files created** → `feat` (new feature implementation)
- **Existing files modified + performance focus** → `perf` (optimization)
- **Test files modified/added** → `test` (testing)
- **Documentation files (.md, comments)** → `docs` (documentation)
- **Code structure changes without new features** → `refactor` (refactoring)
- **Bug fixes in existing functionality** → `fix` (bug fix)
- **Formatting, imports, code style** → `style` (styling)
- **Build, dependencies, tooling** → `chore` (maintenance)
