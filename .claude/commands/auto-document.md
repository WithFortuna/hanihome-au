# Auto Document - Frontend/Backend Separation

Automatically documents completed Task Master tasks, intelligently separating frontend and backend content.

Arguments: $ARGUMENTS (optional: task IDs, 'latest', 'all', or 'split')

1. **Detect project structure** - Find frontend/backend document directories
2. **Get completed tasks** - Query Task Master for done tasks or specified IDs
3. **Classify content** - Smart separation using keyword detection
4. **Generate documents** - Create separate frontend/backend documentation
5. **Add cross-references** - Link related frontend/backend components

## Steps to execute:

### 1. Project Structure Detection
```bash
# Find document directories
find . -name "document" -type d 2>/dev/null

# Check for frontend/backend structure
if [ -d "frontend/document" ] && [ -d "backend/document" ]; then
  echo "✅ Frontend/Backend structure detected"
fi
```

### 2. Task Data Collection
```bash
# Get tasks based on arguments
if [ "$ARGUMENTS" = "latest" ]; then
  # Get most recent completed task
  task-master get-tasks --status=done | head -1
elif [ "$ARGUMENTS" = "all" ]; then
  # Get all completed tasks
  task-master get-tasks --status=done
elif [[ "$ARGUMENTS" =~ ^[0-9,\.]+$ ]]; then
  # Get specific task IDs
  for id in $(echo $ARGUMENTS | tr ',' ' '); do
    task-master get-task --id=$id
  done
else
  # Default: Get recent completed tasks
  task-master get-tasks --status=done --limit=5
fi
```

### 3. Content Classification Logic

#### Frontend Keywords:
- **UI/UX**: React, Next.js, component, page, UI, UX, form, validation
- **Client**: frontend, client, browser, responsive, mobile, CSS, styling
- **React**: state, props, hook, context, router, useState, useEffect
- **Files**: .tsx, .jsx, /src/, /components/, /pages/, /styles/

#### Backend Keywords:
- **API**: API, endpoint, controller, service, repository, REST
- **Database**: database, schema, migration, query, SQL, JPA, entity
- **Framework**: Spring, Boot, Hibernate, Java, @Entity, @Service
- **Infrastructure**: AWS, S3, RDS, Redis, Docker, deployment
- **Files**: .java, /backend/, /api/, /src/main/, application.properties

#### Full-Stack Detection:
- Tasks containing both frontend and backend keywords
- Cross-cutting concerns (authentication, API integration)
- Features spanning both layers

### 4. Document Generation Strategy

#### Single Task Documentation (Task ID specified):
```bash
# File naming
frontend_file="frontend/document/frontend-documentation-task${task_id}.md"
backend_file="backend/document/backend-documentation-task${task_id}.md"

# Content separation
# Frontend document: UI components, client-side logic, styling
# Backend document: API endpoints, business logic, database changes
```

#### Batch Documentation (multiple tasks):
```bash
# Get next document numbers
frontend_num=$(ls frontend/document/ | grep -E "frontend-documentation-[0-9]+\.md" | sort -V | tail -1 | sed 's/.*-\([0-9]*\)\.md/\1/' || echo 0)
backend_num=$(ls backend/document/ | grep -E "backend-documentation-[0-9]+\.md" | sort -V | tail -1 | sed 's/.*-\([0-9]*\)\.md/\1/' || echo 0)

next_frontend=$((frontend_num + 1))
next_backend=$((backend_num + 1))
```

### 5. Document Template Structure

#### Frontend Document Template:
```markdown
# Frontend Documentation - Task ${task_id}

## Overview
- **Task**: ${task_title}
- **Status**: ${status}
- **Frontend Components**: ${component_count}
- **Related Backend APIs**: [${backend_file}](../backend/document/${backend_file})

## UI/UX Implementation
### Components Created/Modified
### Styling and Layout
### State Management
### API Integration (Client-side)

## Technical Details
### React Components
### Hooks and Context
### Form Handling
### Routing Changes

## Testing Approach
### Unit Tests
### Integration Tests
### E2E Tests

## Performance Considerations
### Code Splitting
### Image Optimization
### Bundle Size Impact

## Cross-References
- Backend API: [${backend_file}](../backend/document/${backend_file})
- Related Tasks: ${related_tasks}
```

#### Backend Document Template:
```markdown
# Backend Documentation - Task ${task_id}

## Overview
- **Task**: ${task_title}
- **Status**: ${status}
- **API Endpoints**: ${endpoint_count}
- **Related Frontend**: [${frontend_file}](../frontend/document/${frontend_file})

## API Implementation
### New Endpoints
### Modified Endpoints
### Request/Response Models

## Database Changes
### Schema Updates
### Migrations
### Query Optimizations

## Business Logic
### Services
### Repositories
### Validation Rules

## Security Implementation
### Authentication
### Authorization
### Data Protection

## Testing Strategy
### Unit Tests
### Integration Tests
### API Tests

## Infrastructure
### Configuration Changes
### Environment Variables
### Deployment Considerations

## Cross-References
- Frontend Implementation: [${frontend_file}](../frontend/document/${frontend_file})
- Related Tasks: ${related_tasks}
```

### 6. Implementation Script

```bash
#!/bin/bash

# Auto Document with Frontend/Backend Separation
ARGS="$1"
PROJECT_ROOT="$(pwd)"

# Helper functions
detect_structure() {
    if [ -d "frontend/document" ] && [ -d "backend/document" ]; then
        echo "✅ Frontend/Backend structure detected"
        return 0
    else
        echo "⚠️  Creating document directories..."
        mkdir -p frontend/document backend/document
        return 1
    fi
}

classify_content() {
    local content="$1"
    local frontend_score=0
    local backend_score=0
    
    # Frontend keyword scoring
    echo "$content" | grep -iE "(react|component|jsx|tsx|frontend|client|ui|ux)" >/dev/null && frontend_score=$((frontend_score + 2))
    echo "$content" | grep -iE "(state|props|hook|context|router)" >/dev/null && frontend_score=$((frontend_score + 1))
    
    # Backend keyword scoring
    echo "$content" | grep -iE "(api|endpoint|controller|service|repository)" >/dev/null && backend_score=$((backend_score + 2))
    echo "$content" | grep -iE "(database|schema|migration|spring|boot|java)" >/dev/null && backend_score=$((backend_score + 1))
    
    if [ $frontend_score -gt $backend_score ]; then
        echo "frontend"
    elif [ $backend_score -gt $frontend_score ]; then
        echo "backend"
    else
        echo "fullstack"
    fi
}

generate_documentation() {
    local task_data="$1"
    local task_id="$2"
    local classification="$3"
    
    case "$classification" in
        "frontend")
            generate_frontend_doc "$task_data" "$task_id"
            ;;
        "backend")
            generate_backend_doc "$task_data" "$task_id"
            ;;
        "fullstack")
            generate_frontend_doc "$task_data" "$task_id"
            generate_backend_doc "$task_data" "$task_id"
            ;;
    esac
}

# Main execution
main() {
    echo "🔄 Starting Auto Documentation with Frontend/Backend Separation..."
    
    detect_structure
    
    # Get tasks based on arguments
    if [ "$ARGS" = "latest" ]; then
        echo "📋 Getting latest completed task..."
        tasks=$(task-master get-tasks --status=done | head -1)
    elif [ "$ARGS" = "all" ]; then
        echo "📋 Getting all completed tasks..."
        tasks=$(task-master get-tasks --status=done)
    elif [[ "$ARGS" =~ ^[0-9,\.]+$ ]]; then
        echo "📋 Getting specified tasks: $ARGS..."
        tasks=""
        for id in $(echo $ARGS | tr ',' ' '); do
            task_data=$(task-master get-task --id=$id 2>/dev/null)
            if [ $? -eq 0 ]; then
                tasks="$tasks$task_data\n"
            fi
        done
    else
        echo "📋 Getting recent completed tasks..."
        tasks=$(task-master get-tasks --status=done | head -5)
    fi
    
    if [ -z "$tasks" ]; then
        echo "❌ No tasks found to document"
        return 1
    fi
    
    # Process each task
    echo "$tasks" | while IFS= read -r task_line; do
        if [ -n "$task_line" ]; then
            task_id=$(echo "$task_line" | grep -o '"id":\s*"[^"]*"' | cut -d'"' -f4)
            classification=$(classify_content "$task_line")
            
            echo "📝 Processing Task $task_id ($classification)..."
            generate_documentation "$task_line" "$task_id" "$classification"
        fi
    done
    
    echo "✅ Auto Documentation Complete!"
    echo ""
    echo "📄 Generated Documents:"
    [ -n "$(find frontend/document -name "*.md" -newer .taskmaster/tasks/tasks.json 2>/dev/null)" ] && echo "- Frontend: $(find frontend/document -name "*.md" -newer .taskmaster/tasks/tasks.json | wc -l) files"
    [ -n "$(find backend/document -name "*.md" -newer .taskmaster/tasks/tasks.json 2>/dev/null)" ] && echo "- Backend: $(find backend/document -name "*.md" -newer .taskmaster/tasks/tasks.json | wc -l) files"
}

main
```

## Usage Examples:

```bash
# Document latest completed task
/auto-document latest

# Document specific task with separation
/auto-document 6

# Document multiple tasks
/auto-document 4,5,6

# Document all completed tasks
/auto-document all

# Split existing unified documentation
/auto-document split
```

This enhanced command provides intelligent frontend/backend separation while maintaining comprehensive documentation quality.

## Actual Implementation

The working auto-document script is available at:
- **Script Location**: `scripts/auto-document-simple.sh`
- **Usage**: `./scripts/auto-document-simple.sh [task_id]`
- **Default**: Processes Task 6 if no ID specified

### Features Implemented:
✅ **Project Structure Detection**: Automatically detects frontend/backend document directories
✅ **Comprehensive Documentation**: Generates detailed frontend and backend documentation
✅ **Cross-Reference Linking**: Automatic linking between related documents
✅ **Professional Formatting**: Clean, structured markdown with code examples
✅ **Multi-Language Support**: Korean and English descriptions
✅ **Technical Details**: Architecture diagrams, API specifications, database schemas
✅ **Testing Strategies**: Unit, integration, and E2E test documentation
✅ **Performance Metrics**: Code splitting, bundle analysis, optimization strategies

### Generated Documentation Includes:

#### Frontend Documentation:
- UI/UX component implementation details
- React architecture and component structure
- State management strategies
- API integration patterns
- Form validation and handling
- Image upload and management
- Map integration and geolocation
- Mobile responsiveness and accessibility
- Performance optimization techniques
- Testing approaches and strategies

#### Backend Documentation:
- REST API endpoint specifications
- Database schema and entity relationships
- Business logic implementation
- Security and authorization
- AWS integration (S3, CloudFront)
- Performance optimization and caching
- Monitoring and logging strategies
- Testing approaches (unit, integration)
- Deployment configuration
- Error handling and validation

### Current Status:
- ✅ Task 6 documentation generated successfully
- ✅ Frontend and backend documents created with cross-references
- ✅ Professional formatting with code examples and architecture details
- ✅ Comprehensive coverage of all implementation aspects

### Next Development:
- Add support for multiple task IDs simultaneously
- Implement dynamic task data extraction from Task Master
- Add automatic code example extraction from actual implementation files
- Create visual diagrams and flowcharts generation