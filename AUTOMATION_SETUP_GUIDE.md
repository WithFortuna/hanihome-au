# Automation Setup and Configuration Guide

## Quick Start

This guide helps you configure and use the enhanced automation features in the project. Follow these steps to enable intelligent workflow automation with TaskMaster AI integration.

## Prerequisites

### Required Software
- **macOS**: iTerm2 terminal application
- **Node.js**: Version 18+ for TaskMaster AI
- **Git**: Configured with user name and email
- **Claude Code**: Latest version with MCP support

### Required API Keys
At least one of these API keys must be configured:
- `ANTHROPIC_API_KEY` (Claude models) - **Recommended**
- `PERPLEXITY_API_KEY` (Research features) - **Highly recommended**

## Installation Steps

### 1. Install Required Dependencies

```bash
# Install iTerm2 (macOS)
brew install --cask iterm2

# Install TaskMaster AI globally
npm install -g task-master-ai

# Verify installation
task-master-ai --version
```

### 2. Configure Environment Variables

Create or update your `.env` file in the project root:

```bash
# Required for TaskMaster AI
ANTHROPIC_API_KEY=your_claude_api_key_here
PERPLEXITY_API_KEY=your_perplexity_key_here

# Project configuration
CLAUDE_PROJECT_DIR=/path/to/your/project
```

**For zsh users**, add to your `~/.zshrc`:
```bash
export CLAUDE_PROJECT_DIR="/path/to/your/project"
```

### 3. Set Script Permissions

```bash
# Navigate to project directory
cd /path/to/your/project

# Make all automation scripts executable
chmod +x .claude/hooks/stop-automation.sh
chmod +x .claude/hooks/utils/git-automation.sh
chmod +x auto-start-claude.sh

# Verify permissions
ls -la .claude/hooks/*.sh
ls -la auto-start-claude.sh
```

### 4. Configure TaskMaster AI

```bash
# Initialize TaskMaster in your project
task-master-ai init

# Configure AI models interactively
task-master-ai models --setup

# Verify configuration
task-master-ai models
```

### 5. Configure iTerm2 Permissions

1. Open **System Preferences** → **Security & Privacy** → **Privacy**
2. Select **Automation** from the left sidebar
3. Find your terminal application and ensure **iTerm2** is checked
4. If prompted, allow automation permissions

## Configuration Options

### TaskMaster AI Models

Configure different models for different purposes:

```bash
# Set primary model for task operations
task-master-ai models --set-main claude-3-5-sonnet-20241022

# Set research model for enhanced task generation
task-master-ai models --set-research perplexity-llama-3.1-sonar-large-128k-online

# Set fallback model
task-master-ai models --set-fallback gpt-4o-mini
```

### CI/CD Configuration (Optional)

Add these secrets to your GitHub repository for enhanced CI/CD features:

1. Go to **Repository Settings** → **Secrets and Variables** → **Actions**
2. Add the following secrets (optional):
   - `CODECOV_TOKEN`: For test coverage reporting
   - `SONAR_TOKEN`: For code quality analysis
   - `SNYK_TOKEN`: For security vulnerability scanning

## Usage Guide

### Starting Your Development Session

#### Option 1: Auto-Start Script
```bash
# Automatically starts Claude with TaskMaster workflow
./auto-start-claude.sh
```

#### Option 2: Manual Start
```bash
# Start Claude with specific workflow
claude "/tm:workflows:command-pipeline"
```

#### Option 3: TaskMaster Commands
```bash
# Get next available task
task-master-ai next

# Show specific task details
task-master-ai show 5.5

# Start working on a task
task-master-ai set-status --id=5.5 --status=in-progress
```

### During Development

1. **Work on TaskMaster Subtasks**: Progress is automatically tracked
2. **Session Management**: Use `/clear` between different tasks
3. **Progress Updates**: Use `task-master-ai update-subtask` to log progress

### Session Completion Behaviors

The automation system will automatically:

#### When Task Progress < 50%
- Open new iTerm2 tab
- Launch Claude with continuation prompt
- Display progress message in Korean
- Suggest completing remaining subtasks

#### When Task Progress ≥ 50%
- Trigger Claude for git operations
- Create commits with proper formatting
- Generate pull requests
- Update task status

#### For Documentation Work
- Switch to document branch
- Commit documentation changes
- Create documentation pull request

## Troubleshooting

### Common Issues and Solutions

#### 1. iTerm2 Not Opening
**Symptoms**: AppleScript errors, no new terminal tabs
**Solutions**:
```bash
# Check iTerm2 installation
which iterm2
brew install --cask iterm2

# Verify permissions in System Preferences
# Security & Privacy → Privacy → Automation
```

#### 2. TaskMaster Commands Failing
**Symptoms**: "API key not found" or connection errors
**Solutions**:
```bash
# Check environment variables
echo $ANTHROPIC_API_KEY
cat .env

# Reconfigure models
task-master-ai models --setup

# Test connection
task-master-ai list
```

#### 3. Automation Scripts Not Executing
**Symptoms**: No automation after session end
**Solutions**:
```bash
# Check script permissions
ls -la .claude/hooks/*.sh

# Fix permissions
chmod +x .claude/hooks/stop-automation.sh

# Check logs
tail -f .claude/hooks/stop-automation.log
```

#### 4. Claude Sessions Not Starting
**Symptoms**: iTerm2 opens but Claude doesn't start
**Solutions**:
```bash
# Check Claude Code installation
claude --version

# Verify project directory
echo $CLAUDE_PROJECT_DIR

# Test manual start
cd $CLAUDE_PROJECT_DIR && claude
```

### Debugging Commands

```bash
# Check automation logs
tail -f .claude/hooks/stop-automation.log

# Monitor TaskMaster status
watch -n 5 'task-master-ai next'

# Verify git status
git status

# Check environment
env | grep -E "(CLAUDE|ANTHROPIC|PERPLEXITY)"
```

## Advanced Configuration

### Custom Work Type Detection

Edit `.claude/hooks/config/work-patterns.json` to customize work type detection patterns:

```json
{
  "feature_development": [
    "implement",
    "create",
    "add feature",
    "build",
    "develop"
  ],
  "documentation": [
    "document",
    "readme",
    "docs",
    "comment",
    "explain"
  ],
  "github_work": [
    "pull request",
    "pr",
    "merge",
    "review",
    "github"
  ]
}
```

### Custom Automation Prompts

Modify the prompts in `.claude/hooks/utils/git-automation.sh`:

```bash
# For feature development
claude_prompt="Your custom feature completion prompt here"

# For documentation
claude_prompt="Your custom documentation prompt here"
```

### Slack/Discord Integration (Optional)

Add webhook notifications by modifying the automation scripts:

```bash
# Add to stop-automation.sh
notify_completion() {
    curl -X POST -H 'Content-type: application/json' \
        --data '{"text":"Task completed: '"$1"'"}' \
        $SLACK_WEBHOOK_URL
}
```

## Performance Optimization

### Script Performance
- **Caching**: TaskMaster state and git status are cached
- **Parallel Processing**: Multiple automation tracks run concurrently
- **Resource Management**: Automatic cleanup of temporary files

### Memory Usage
- **Optimized Logging**: Log rotation and cleanup
- **Efficient Parsing**: Minimal JSON processing overhead
- **Session Management**: Proper cleanup of iTerm2 sessions

## Security Considerations

### Credential Management
- **Environment Variables**: Store API keys securely
- **No Hardcoding**: Never commit credentials to git
- **Access Control**: Proper file permissions on scripts

### Script Security
- **Input Validation**: All user inputs are validated
- **Error Handling**: Graceful failure handling
- **Logging**: Secure logging without sensitive data

## Backup and Recovery

### Configuration Backup
```bash
# Backup your configuration
cp .env .env.backup
cp -r .claude/hooks .claude/hooks.backup
cp .taskmaster/config.json .taskmaster/config.json.backup
```

### Recovery Steps
```bash
# Restore from backup
cp .env.backup .env
cp -r .claude/hooks.backup .claude/hooks
cp .taskmaster/config.json.backup .taskmaster/config.json

# Restore permissions
chmod +x .claude/hooks/*.sh
```

## Best Practices

### Development Workflow
1. **Start Each Session**: Use auto-start script or TaskMaster commands
2. **Stay Focused**: Use `/clear` between different tasks
3. **Track Progress**: Update subtasks with implementation notes
4. **Let Automation Work**: Trust the progress-based decision making

### Task Management
1. **Break Down Complex Tasks**: Use `task-master-ai expand` for large tasks
2. **Update Progress**: Use `task-master-ai update-subtask` regularly
3. **Complete Tasks**: Mark subtasks as done when finished
4. **Review Dependencies**: Check task dependencies before starting

### Git Workflow
1. **Trust Automation**: Let the system handle commits and PRs when ready
2. **Review Generated PRs**: Always review automatically created PRs
3. **Follow Conventions**: Use standard commit message formats
4. **Branch Management**: Let automation handle branch switching

## Support and Updates

### Getting Help
- Check logs first: `.claude/hooks/*.log`
- Review this guide for common solutions
- Test with simple tasks before complex workflows
- Verify all prerequisites are met

### Staying Updated
```bash
# Update TaskMaster AI
npm update -g task-master-ai

# Update Claude Code
# Follow Claude Code update instructions

# Pull latest project changes
git pull origin main
```

### Contributing Improvements
- Log issues and suggestions
- Test changes thoroughly
- Follow existing code patterns
- Update documentation accordingly

---

**Need Help?** Check the troubleshooting section above or review the automation logs for specific error messages.