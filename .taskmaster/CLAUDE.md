# Task Master AI - Agent Integration Guide

## Essential Commands

### Core Workflow Commands

```bash
# Project Setup
task-master init                                    # Initialize Task Master in current project
task-master parse-prd .taskmaster/docs/prd.txt      # Generate tasks from PRD document
task-master models --setup                        # Configure AI models interactively

# Daily Development Workflow
task-master list                                   # Show all tasks with status
task-master next                                   # Get next available task to work on
task-master show <id>                             # View detailed task information (e.g., task-master show 1.2)
task-master set-status --id=<id> --status=done    # Mark task complete

# Task Management
task-master add-task --prompt="description" --research        # Add new task with AI assistance
task-master expand --id=<id> --research --force              # Break task into subtasks
task-master update-task --id=<id> --prompt="changes"         # Update specific task
task-master update --from=<id> --prompt="changes"            # Update multiple tasks from ID onwards
task-master update-subtask --id=<id> --prompt="notes"        # Add implementation notes to subtask

# Analysis & Planning
task-master analyze-complexity --research          # Analyze task complexity
task-master complexity-report                      # View complexity analysis
task-master expand --all --research               # Expand all eligible tasks

# Dependencies & Organization
task-master add-dependency --id=<id> --depends-on=<id>       # Add task dependency
task-master move --from=<id> --to=<id>                       # Reorganize task hierarchy
task-master validate-dependencies                            # Check for dependency issues
task-master generate                                         # Update task markdown files (usually auto-called)
```

## Key Files & Project Structure

### Core Files

- `.taskmaster/tasks/tasks.json` - Main task data file (auto-managed)
- `.taskmaster/config.json` - AI model configuration (use `task-master models` to modify)
- `.taskmaster/docs/prd.txt` - Product Requirements Document for parsing
- `.taskmaster/tasks/*.txt` - Individual task files (auto-generated from tasks.json)
- `.env` - API keys for CLI usage

### Claude Code Integration Files

- `CLAUDE.md` - Auto-loaded context for Claude Code (this file)
- `.claude/settings.json` - Claude Code tool allowlist and preferences
- `.claude/commands/` - Custom slash commands for repeated workflows
- `.mcp.json` - MCP server configuration (project-specific)

### Directory Structure

```
project/
├── .taskmaster/
│   ├── tasks/              # Task files directory
│   │   ├── tasks.json      # Main task database
│   │   ├── task-1.md      # Individual task files
│   │   └── task-2.md
│   ├── docs/              # Documentation directory
│   │   ├── prd.txt        # Product requirements
│   ├── reports/           # Analysis reports directory
│   │   └── task-complexity-report.json
│   ├── templates/         # Template files
│   │   └── example_prd.txt  # Example PRD template
│   └── config.json        # AI models & settings
├── .claude/
│   ├── settings.json      # Claude Code configuration
│   └── commands/         # Custom slash commands
├── .env                  # API keys
├── .mcp.json            # MCP configuration
└── CLAUDE.md            # This file - auto-loaded by Claude Code
```

## MCP Integration

Task Master provides an MCP server that Claude Code can connect to. Configure in `.mcp.json`:

```json
{
  "mcpServers": {
    "task-master-ai": {
      "command": "npx",
      "args": ["-y", "--package=task-master-ai", "task-master-ai"],
      "env": {
        "ANTHROPIC_API_KEY": "your_key_here",
        "PERPLEXITY_API_KEY": "your_key_here",
        "OPENAI_API_KEY": "OPENAI_API_KEY_HERE",
        "GOOGLE_API_KEY": "GOOGLE_API_KEY_HERE",
        "XAI_API_KEY": "XAI_API_KEY_HERE",
        "OPENROUTER_API_KEY": "OPENROUTER_API_KEY_HERE",
        "MISTRAL_API_KEY": "MISTRAL_API_KEY_HERE",
        "AZURE_OPENAI_API_KEY": "AZURE_OPENAI_API_KEY_HERE",
        "OLLAMA_API_KEY": "OLLAMA_API_KEY_HERE"
      }
    }
  }
}
```

### Essential MCP Tools

```javascript
help; // = shows available taskmaster commands
// Project setup
initialize_project; // = task-master init
parse_prd; // = task-master parse-prd

// Daily workflow
get_tasks; // = task-master list
next_task; // = task-master next
get_task; // = task-master show <id>
set_task_status; // = task-master set-status

// Task management
add_task; // = task-master add-task
expand_task; // = task-master expand
update_task; // = task-master update-task
update_subtask; // = task-master update-subtask
update; // = task-master update

// Analysis
analyze_project_complexity; // = task-master analyze-complexity
complexity_report; // = task-master complexity-report
```

## Claude Code Workflow Integration

### Standard Development Workflow

#### 1. Project Initialization

```bash
# Initialize Task Master
task-master init

# Create or obtain PRD, then parse it
task-master parse-prd .taskmaster/docs/prd.txt

# Analyze complexity and expand tasks
task-master analyze-complexity --research
task-master expand --all --research
```

If tasks already exist, another PRD can be parsed (with new information only!) using parse-prd with --append flag. This will add the generated tasks to the existing list of tasks..

#### 2. Daily Development Loop

```bash
# Start each session
task-master next                           # Find next available task
task-master show <id>                     # Review task details

# During implementation, check in code context into the tasks and subtasks
task-master update-subtask --id=<id> --prompt="implementation notes..."

# Complete tasks
task-master set-status --id=<id> --status=done
```

#### 3. Multi-Claude Workflows

For complex projects, use multiple Claude Code sessions:

```bash
# Terminal 1: Main implementation
cd project && claude

# Terminal 2: Testing and validation
cd project-test-worktree && claude

# Terminal 3: Documentation updates
cd project-docs-worktree && claude
```

### Custom Slash Commands

Create `.claude/commands/taskmaster-next.md`:

```markdown
Find the next available Task Master task and show its details.

Steps:

1. Run `task-master next` to get the next task
2. If a task is available, run `task-master show <id>` for full details
3. Provide a summary of what needs to be implemented
4. Suggest the first implementation step
```

Create `.claude/commands/taskmaster-complete.md`:

```markdown
Complete a Task Master task: $ARGUMENTS

Steps:

1. Review the current task with `task-master show $ARGUMENTS`
2. Verify all implementation is complete
3. Run any tests related to this task
4. Mark as complete: `task-master set-status --id=$ARGUMENTS --status=done`
5. Show the next available task with `task-master next`
```

## Tool Allowlist Recommendations

Add to `.claude/settings.json`:

```json
{
  "allowedTools": [
    "Edit",
    "Bash(task-master *)",
    "Bash(git commit:*)",
    "Bash(git add:*)",
    "Bash(npm run *)",
    "mcp__task_master_ai__*"
  ]
}
```

## Configuration & Setup

### API Keys Required

At least **one** of these API keys must be configured:

- `ANTHROPIC_API_KEY` (Claude models) - **Recommended**
- `PERPLEXITY_API_KEY` (Research features) - **Highly recommended**
- `OPENAI_API_KEY` (GPT models)
- `GOOGLE_API_KEY` (Gemini models)
- `MISTRAL_API_KEY` (Mistral models)
- `OPENROUTER_API_KEY` (Multiple models)
- `XAI_API_KEY` (Grok models)

An API key is required for any provider used across any of the 3 roles defined in the `models` command.

### Model Configuration

```bash
# Interactive setup (recommended)
task-master models --setup

# Set specific models
task-master models --set-main claude-3-5-sonnet-20241022
task-master models --set-research perplexity-llama-3.1-sonar-large-128k-online
task-master models --set-fallback gpt-4o-mini
```

## Task Structure & IDs

### Task ID Format

- Main tasks: `1`, `2`, `3`, etc.
- Subtasks: `1.1`, `1.2`, `2.1`, etc.
- Sub-subtasks: `1.1.1`, `1.1.2`, etc.

### Task Status Values

- `pending` - Ready to work on
- `in-progress` - Currently being worked on
- `done` - Completed and verified
- `deferred` - Postponed
- `cancelled` - No longer needed
- `blocked` - Waiting on external factors

### Task Fields

```json
{
  "id": "1.2",
  "title": "Implement user authentication",
  "description": "Set up JWT-based auth system",
  "status": "pending",
  "priority": "high",
  "dependencies": ["1.1"],
  "details": "Use bcrypt for hashing, JWT for tokens...",
  "testStrategy": "Unit tests for auth functions, integration tests for login flow",
  "subtasks": []
}
```

## Claude Code Best Practices with Task Master

### Context Management

- Use `/clear` between different tasks to maintain focus
- This CLAUDE.md file is automatically loaded for context
- Use `task-master show <id>` to pull specific task context when needed

### Iterative Implementation

1. `task-master show <subtask-id>` - Understand requirements
2. Explore codebase and plan implementation
3. `task-master update-subtask --id=<id> --prompt="detailed plan"` - Log plan
4. `task-master set-status --id=<id> --status=in-progress` - Start work
5. Implement code following logged plan
6. `task-master update-subtask --id=<id> --prompt="what worked/didn't work"` - Log progress
7. `task-master set-status --id=<id> --status=done` - Complete task

### Complex Workflows with Checklists

For large migrations or multi-step processes:

1. Create a markdown PRD file describing the new changes: `touch task-migration-checklist.md` (prds can be .txt or .md)
2. Use Taskmaster to parse the new prd with `task-master parse-prd --append` (also available in MCP)
3. Use Taskmaster to expand the newly generated tasks into subtasks. Consdier using `analyze-complexity` with the correct --to and --from IDs (the new ids) to identify the ideal subtask amounts for each task. Then expand them.
4. Work through items systematically, checking them off as completed
5. Use `task-master update-subtask` to log progress on each task/subtask and/or updating/researching them before/during implementation if getting stuck

### Git Integration

Task Master works well with `gh` CLI:

```bash
# Create PR for completed task
gh pr create --title "Complete task 1.2: User authentication" --body "Implements JWT auth system as specified in task 1.2"

# Reference task in commits
git commit -m "feat: implement JWT auth (task 1.2)"
```

### Parallel Development with Git Worktrees

```bash
# Create worktrees for parallel task development
git worktree add ../project-auth feature/auth-system
git worktree add ../project-api feature/api-refactor

# Run Claude Code in each worktree
cd ../project-auth && claude    # Terminal 1: Auth work
cd ../project-api && claude     # Terminal 2: API work
```

## Troubleshooting

### AI Commands Failing

```bash
# Check API keys are configured
cat .env                           # For CLI usage

# Verify model configuration
task-master models

# Test with different model
task-master models --set-fallback gpt-4o-mini
```

### MCP Connection Issues

- Check `.mcp.json` configuration
- Verify Node.js installation
- Use `--mcp-debug` flag when starting Claude Code
- Use CLI as fallback if MCP unavailable

### Task File Sync Issues

```bash
# Regenerate task files from tasks.json
task-master generate

# Fix dependency issues
task-master fix-dependencies
```

DO NOT RE-INITIALIZE. That will not do anything beyond re-adding the same Taskmaster core files.

## Important Notes

### AI-Powered Operations

These commands make AI calls and may take up to a minute:

- `parse_prd` / `task-master parse-prd`
- `analyze_project_complexity` / `task-master analyze-complexity`
- `expand_task` / `task-master expand`
- `expand_all` / `task-master expand --all`
- `add_task` / `task-master add-task`
- `update` / `task-master update`
- `update_task` / `task-master update-task`
- `update_subtask` / `task-master update-subtask`

### File Management

- Never manually edit `tasks.json` - use commands instead
- Never manually edit `.taskmaster/config.json` - use `task-master models`
- Task markdown files in `tasks/` are auto-generated
- Run `task-master generate` after manual changes to tasks.json

### Claude Code Session Management

- Use `/clear` frequently to maintain focused context
- Create custom slash commands for repeated Task Master workflows
- Configure tool allowlist to streamline permissions
- Use headless mode for automation: `claude -p "task-master next"`

### Multi-Task Updates

- Use `update --from=<id>` to update multiple future tasks
- Use `update-task --id=<id>` for single task updates
- Use `update-subtask --id=<id>` for implementation logging

### Research Mode

- Add `--research` flag for research-based AI enhancement
- Requires a research model API key like Perplexity (`PERPLEXITY_API_KEY`) in environment
- Provides more informed task creation and updates
- Recommended for complex technical tasks

---

## GitHub Workflow Integration

### Branch Strategy & Naming Conventions

#### Git Flow Integration with TaskMaster

```bash
# Main branches
main                    # Production-ready code
develop                 # Integration branch for features

# Feature branches based on TaskMaster tasks
feature/task-{id}-{short-description}     # e.g., feature/task-4-property-management
feature/task-{id}.{subtask}-{description} # e.g., feature/task-4.1-property-entity

# Release branches
release/{version}       # e.g., release/1.2.0

# Hotfix branches
hotfix/{version}        # e.g., hotfix/1.1.1

# Support branches
support/{version}       # e.g., support/1.0.x
```

#### Branch Naming Examples

```bash
# Main task implementation
feature/task-5-user-authentication
feature/task-6-notification-system

# Subtask implementation
feature/task-5.1-jwt-setup
feature/task-5.2-password-hashing
feature/task-6.3-email-templates

# Bug fixes with task reference
bugfix/task-7-fix-login-validation
hotfix/task-8-security-patch
```

### Commit Message Standards

#### Conventional Commits with TaskMaster Integration

```bash
# Format
<type>[optional scope]: <description> (task <id>)

[optional body]

[optional footer(s)]
```

#### Commit Types

- `feat`: New feature (task implementation)
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `ci`: CI/CD pipeline changes

#### Examples

```bash
# Task implementation commits
feat(auth): implement JWT authentication system (task 5.1)
feat(property): add CRUD operations for properties (task 4.4)
fix(validation): resolve property validation errors (task 4.5)

# Subtask completion commits
feat(entity): create Property entity with JPA annotations (task 4.1)
feat(api): implement property search endpoints (task 4.3)

# Multi-task commits
feat(backend): complete property management system (tasks 4.1-4.7)
```

### Pull Request Management

#### PR Title Format

```bash
[Task {id}] {Type}: {Description}

# Examples
[Task 4] Feat: Complete property management system
[Task 5.2] Fix: Resolve password hashing issues
[Task 6] Docs: Add API documentation for notifications
```

#### PR Template Integration

```markdown
## Task Reference
- **Task ID**: {task-id}
- **TaskMaster Link**: `.taskmaster/tasks/task-{id}.md`

## Summary
<!-- Brief description of changes -->

## TaskMaster Integration
- [ ] Task status updated to 'done'
- [ ] Subtasks completed and documented
- [ ] Implementation notes added to task

## Implementation Details
<!-- Technical details of the implementation -->

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests passing
- [ ] Manual testing completed

## Code Quality
- [ ] Code review completed
- [ ] SonarCloud analysis passed
- [ ] Security scan completed

## Documentation
- [ ] API documentation updated
- [ ] README updated if needed
- [ ] TaskMaster task documentation updated
```

### GitHub Issues Integration

#### Issue Templates with TaskMaster

```yaml
# .github/ISSUE_TEMPLATE/task-issue.yml
name: TaskMaster Task Issue
description: Create an issue based on TaskMaster task
title: "[Task {task-id}] {Short description}"
labels: ["task", "needs-triage"]
body:
  - type: input
    id: task-id
    attributes:
      label: TaskMaster Task ID
      description: The ID from TaskMaster (e.g., 4.2)
      placeholder: "4.2"
    validations:
      required: true
      
  - type: textarea
    id: task-details
    attributes:
      label: Task Details
      description: Copy task description from TaskMaster
      placeholder: "Paste task description here..."
    validations:
      required: true
```

### GitHub Actions Integration

#### TaskMaster Status Sync Workflow

```yaml
# .github/workflows/taskmaster-sync.yml
name: TaskMaster Status Sync

on:
  pull_request:
    types: [opened, closed, merged]
  push:
    branches: [main, develop]

jobs:
  sync-task-status:
    runs-on: ubuntu-latest
    if: contains(github.event.head_commit.message, 'task ') || contains(github.event.pull_request.title, 'Task ')
    
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        
      - name: Extract Task ID
        id: extract-task
        run: |
          if [[ "${{ github.event_name }}" == "pull_request" ]]; then
            TITLE="${{ github.event.pull_request.title }}"
          else
            TITLE="${{ github.event.head_commit.message }}"
          fi
          
          TASK_ID=$(echo "$TITLE" | grep -oP '(?i)task\s+\K[\d.]+' | head -1)
          echo "task-id=$TASK_ID" >> $GITHUB_OUTPUT
          
      - name: Update TaskMaster Status
        if: steps.extract-task.outputs.task-id != ''
        run: |
          if [[ "${{ github.event.action }}" == "closed" && "${{ github.event.pull_request.merged }}" == "true" ]]; then
            npx task-master-ai set-status --id="${{ steps.extract-task.outputs.task-id }}" --status=done
          fi
```

#### Auto Branch Creation from Tasks

```yaml
# .github/workflows/auto-branch.yml
name: Auto Branch Creation

on:
  workflow_dispatch:
    inputs:
      task-id:
        description: 'TaskMaster Task ID (e.g., 4.2)'
        required: true
        type: string

jobs:
  create-branch:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        
      - name: Get Task Details
        id: task-details
        run: |
          TASK_TITLE=$(npx task-master-ai show ${{ github.event.inputs.task-id }} --format=json | jq -r '.title')
          BRANCH_NAME="feature/task-${{ github.event.inputs.task-id }}-$(echo "$TASK_TITLE" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-\|-$//g')"
          echo "branch-name=$BRANCH_NAME" >> $GITHUB_OUTPUT
          
      - name: Create Branch
        run: |
          git checkout -b "${{ steps.task-details.outputs.branch-name }}"
          git push -u origin "${{ steps.task-details.outputs.branch-name }}"
```

### Development Workflow Integration

#### Standard Development Process

```bash
# 1. Start new task
task-master next                           # Get next task
task-master show <id>                     # Review task details

# 2. Create feature branch
git checkout develop
git pull origin develop
git checkout -b feature/task-<id>-<description>

# 3. Implement with TaskMaster tracking
task-master set-status --id=<id> --status=in-progress
# ... implement code ...
task-master update-subtask --id=<id> --prompt="implementation progress..."

# 4. Commit with proper format
git add .
git commit -m "feat(scope): implement feature (task <id>)"

# 5. Create pull request
gh pr create --title "[Task <id>] Feat: <description>" --body "Task: <id>"

# 6. Mark task complete after merge
task-master set-status --id=<id> --status=done
```

#### Multi-Task Development with Worktrees

```bash
# Setup parallel development
git worktree add ../project-task-5 feature/task-5-auth
git worktree add ../project-task-6 feature/task-6-notifications

# Work in parallel
cd ../project-task-5 && claude    # Terminal 1: Auth implementation
cd ../project-task-6 && claude    # Terminal 2: Notifications implementation
```

### GitHub MCP Tools Integration

#### Essential GitHub Operations

```bash
# Using GitHub MCP tools with TaskMaster
mcp__github__create_pull_request --title="[Task 4] Complete property system" --head="feature/task-4-property-management" --base="develop"

# Auto-update task status on PR merge
mcp__github__get_pull_request --pullNumber=123 --owner=owner --repo=repo
task-master set-status --id=4 --status=done
```

#### Automated Task Tracking

```bash
# Create GitHub issue from task
mcp__github__create_issue --title="[Task 5] User Authentication System" --body="$(task-master show 5 --format=markdown)"

# Link PR to task
mcp__github__create_pull_request --title="[Task 5.1] JWT Implementation" --body="Implements task 5.1 from TaskMaster\n\nSee: .taskmaster/tasks/task-5.md"
```

### Security & Compliance

#### GitHub Security Integration

```bash
# Security scanning with task context
mcp__github__list_secret_scanning_alerts --owner=owner --repo=repo
mcp__github__list_dependabot_alerts --owner=owner --repo=repo

# Update tasks with security findings
task-master update-task --id=<id> --prompt="Security scan results: ..."
```

#### Compliance Automation

```yaml
# .github/workflows/compliance.yml
name: Compliance Check

on:
  pull_request:
    branches: [main, develop]

jobs:
  compliance:
    runs-on: ubuntu-latest
    steps:
      - name: Check Task Reference
        run: |
          if ! echo "${{ github.event.pull_request.title }}" | grep -q "\[Task [0-9]"; then
            echo "PR title must include TaskMaster task reference: [Task X]"
            exit 1
          fi
```

### Troubleshooting

#### Common Integration Issues

```bash
# GitHub MCP connection issues
echo $GITHUB_TOKEN                        # Verify token
mcp__github__get_me                       # Test connection

# Task-branch sync issues
git branch --show-current                 # Check current branch
task-master show <id>                     # Verify task exists

# Commit message validation
git log --oneline -5                      # Review recent commits
git commit --amend -m "fix: correct commit format (task 4.2)"
```

---

_This GitHub integration guide ensures seamless coordination between TaskMaster AI, Claude Code, and GitHub workflows for efficient development processes._
