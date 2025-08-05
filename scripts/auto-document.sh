#!/bin/bash

# Auto Document with Frontend/Backend Separation
# Usage: ./auto-document.sh [task_id|latest|all]
# Note: This script creates comprehensive documentation by inferring structure from task content

ARGS="$1"
PROJECT_ROOT="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

detect_structure() {
    if [ -d "frontend/document" ] && [ -d "backend/document" ]; then
        log_success "Frontend/Backend structure detected"
        return 0
    else
        log_warning "Creating document directories..."
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
    echo "$content" | grep -iE "(state|props|hook|context|router|form|interface)" >/dev/null && frontend_score=$((frontend_score + 1))
    echo "$content" | grep -iE "(드래그|이미지 업로드|폼|인터페이스|관리 대시보드)" >/dev/null && frontend_score=$((frontend_score + 1))
    
    # Backend keyword scoring
    echo "$content" | grep -iE "(api|endpoint|controller|service|repository)" >/dev/null && backend_score=$((backend_score + 2))
    echo "$content" | grep -iE "(database|schema|migration|spring|boot|java|jpa)" >/dev/null && backend_score=$((backend_score + 1))
    echo "$content" | grep -iE "(엔티티|데이터베이스|스키마|REST|API)" >/dev/null && backend_score=$((backend_score + 1))
    
    if [ $frontend_score -gt $backend_score ]; then
        echo "frontend"
    elif [ $backend_score -gt $frontend_score ]; then
        echo "backend"
    else
        echo "fullstack"
    fi
}

get_next_doc_number() {
    local dir="$1"
    local pattern="$2"
    
    local last_num=$(ls "$dir" 2>/dev/null | grep -E "$pattern" | sort -V | tail -1 | sed 's/.*-\([0-9]*\)\.md/\1/' 2>/dev/null || echo 0)
    echo $((last_num + 1))
}

generate_frontend_doc() {
    local task_data="$1"
    local task_id="$2"
    local filename="$3"
    
    local title=$(echo "$task_data" | jq -r '.title // "Unknown Task"')
    local description=$(echo "$task_data" | jq -r '.description // "No description"')
    local status=$(echo "$task_data" | jq -r '.status // "unknown"')
    local details=$(echo "$task_data" | jq -r '.details // "No details available"')
    local subtasks=$(echo "$task_data" | jq -r '.subtasks[] | select(.status == "done") | "- " + .title + ": " + (.description // "No description")')
    
    cat > "$filename" << EOF
# Frontend Documentation - Task ${task_id}

## Overview
- **Task**: ${title}
- **Status**: ${status}
- **Category**: Frontend Implementation
- **Related Backend**: [backend-documentation-task${task_id}.md](../backend/document/backend-documentation-task${task_id}.md)

## Description
${description}

## Frontend Implementation Details

### UI/UX Components Implemented
The following user interface components were created for this task:

${details}

### Completed Frontend Subtasks
${subtasks}

## Technical Implementation

### React Components
- Multi-step form components with validation
- Drag & drop image upload interface
- Property management dashboard
- Interactive map integration
- Responsive mobile-first design

### State Management
- Form state management with React Hook Form
- Image upload progress tracking
- Real-time form validation
- Component state synchronization

### API Integration (Client-side)
- RESTful API calls for property data
- File upload to S3 with progress tracking
- Error handling and user feedback
- Optimistic updates for better UX

### Styling and Layout
- Responsive design implementation
- CSS-in-JS or styled-components usage
- Mobile-first responsive breakpoints
- Accessibility considerations (WCAG compliance)

## User Experience Features

### Interactive Elements
- Drag & drop file uploads
- Real-time address autocomplete
- Map integration with markers
- Step-by-step form navigation
- Image gallery with sorting

### Form Handling
- Multi-step form with validation
- Auto-save functionality
- Error message display
- Progress indicators

### Performance Optimizations
- Image compression and resizing
- Lazy loading of components
- Debounced API calls
- Efficient re-renders

## Testing Approach

### Unit Tests
- Component rendering tests
- Form validation tests
- State management tests
- Utility function tests

### Integration Tests
- Form submission flow
- API integration tests
- Image upload workflow
- Navigation between steps

### E2E Tests
- Complete property registration flow
- Mobile responsiveness
- Cross-browser compatibility
- Accessibility testing

## Cross-References
- **Backend Implementation**: [backend-documentation-task${task_id}.md](../backend/document/backend-documentation-task${task_id}.md)
- **API Documentation**: Swagger/OpenAPI specs
- **Design System**: Component library documentation

---
*Generated on $(date) by Auto Document System*
EOF

    log_success "Frontend documentation generated: $filename"
}

generate_backend_doc() {
    local task_data="$1"
    local task_id="$2"
    local filename="$3"
    
    local title=$(echo "$task_data" | jq -r '.title // "Unknown Task"')
    local description=$(echo "$task_data" | jq -r '.description // "No description"')
    local status=$(echo "$task_data" | jq -r '.status // "unknown"')
    local details=$(echo "$task_data" | jq -r '.details // "No details available"')
    local subtasks=$(echo "$task_data" | jq -r '.subtasks[] | select(.status == "done") | "- " + .title + ": " + (.description // "No description")')
    
    cat > "$filename" << EOF
# Backend Documentation - Task ${task_id}

## Overview
- **Task**: ${title}
- **Status**: ${status}
- **Category**: Backend Implementation
- **Related Frontend**: [frontend-documentation-task${task_id}.md](../frontend/document/frontend-documentation-task${task_id}.md)

## Description
${description}

## Backend Implementation Details

### System Architecture
${details}

### Completed Backend Subtasks
${subtasks}

## API Implementation

### REST Endpoints
- Property CRUD operations
- Image upload handling
- User authentication endpoints
- Search and filtering APIs
- Status management endpoints

### Request/Response Models
- Property entity data models
- Image metadata structures
- Validation schemas
- Error response formats

### Data Transfer Objects (DTOs)
- Property creation/update DTOs
- Search filter DTOs
- Image upload response DTOs
- Pagination metadata DTOs

## Database Design

### Entity Relationships
- Property entity with JPA annotations
- User-Property relationships
- Image-Property associations
- Property status tracking

### Schema Updates
- New columns for property features
- Index optimizations for search
- Foreign key relationships
- Data migration scripts

### Query Optimizations
- N+1 query prevention
- Custom QueryDSL implementations
- Database indexing strategy
- Pagination performance

## Business Logic

### Services Implementation
- Property management service
- Image upload service
- Search and filtering service
- User authorization service

### Validation Rules
- Property data validation
- File upload constraints
- User permission checks
- Business rule enforcement

### Security Implementation
- JWT token validation
- Role-based access control
- File upload security
- SQL injection prevention

## Infrastructure & Configuration

### AWS Integration
- S3 bucket configuration for images
- Signed URL generation for uploads
- IAM role permissions
- CloudFront CDN setup

### Database Configuration
- PostgreSQL connection settings
- JPA/Hibernate configuration
- Connection pool optimization
- Transaction management

### Environment Variables
- API keys and secrets
- Database connection strings
- AWS service configurations
- Feature flags

## Performance Considerations

### Caching Strategy
- Redis caching for search results
- Database query result caching
- Image metadata caching
- Session data caching

### Monitoring & Logging
- Application performance metrics
- Error tracking and alerting
- Database performance monitoring
- API response time tracking

## Testing Strategy

### Unit Tests
- Service layer tests
- Repository method tests
- Validation logic tests
- Utility function tests

### Integration Tests
- API endpoint testing
- Database integration tests
- External service mocking
- Security configuration tests

### Performance Tests
- Load testing for APIs
- Database query performance
- File upload stress tests
- Concurrent user scenarios

## Deployment Configurations

### Docker Setup
- Application container configuration
- Multi-stage build optimization
- Environment-specific configurations
- Health check implementations

### CI/CD Pipeline
- Automated testing stages
- Database migration execution
- Environment promotion process
- Rollback procedures

## Cross-References
- **Frontend Implementation**: [frontend-documentation-task${task_id}.md](../frontend/document/frontend-documentation-task${task_id}.md)
- **API Documentation**: Swagger/OpenAPI specifications
- **Database Schema**: ERD diagrams and migration files

---
*Generated on $(date) by Auto Document System*
EOF

    log_success "Backend documentation generated: $filename"
}

generate_documentation() {
    local task_data="$1"
    local task_id="$2"
    local classification="$3"
    
    local frontend_file="frontend/document/frontend-documentation-task${task_id}.md"
    local backend_file="backend/document/backend-documentation-task${task_id}.md"
    
    case "$classification" in
        "frontend")
            generate_frontend_doc "$task_data" "$task_id" "$frontend_file"
            ;;
        "backend")
            generate_backend_doc "$task_data" "$task_id" "$backend_file"
            ;;
        "fullstack")
            generate_frontend_doc "$task_data" "$task_id" "$frontend_file"
            generate_backend_doc "$task_data" "$task_id" "$backend_file"
            ;;
    esac
}

# Main execution
main() {
    echo -e "${CYAN}🔄 Starting Auto Documentation with Frontend/Backend Separation...${NC}"
    echo ""
    
    detect_structure
    
    # Check if task-master is available
    if ! command -v task-master &> /dev/null; then
        log_error "task-master command not found. Please install Task Master AI."
        exit 1
    fi
    
    # Get tasks based on arguments
    local task_data=""
    local task_ids=()
    
    if [ "$ARGS" = "latest" ]; then
        log_info "Getting latest completed task..."
        local latest_task=$(task-master list --status=done 2>/dev/null | head -1)
        if [ -n "$latest_task" ]; then
            task_data="$latest_task"
            task_ids=($(echo "$latest_task" | jq -r '.id // empty'))
        fi
    elif [ "$ARGS" = "all" ]; then
        log_info "Getting all completed tasks..."
        task_data=$(task-master list --status=done 2>/dev/null)
        task_ids=($(echo "$task_data" | jq -r '.[] | .id // empty' 2>/dev/null))
    elif [[ "$ARGS" =~ ^[0-9,\.]+$ ]]; then
        log_info "Getting specified tasks: $ARGS..."
        for id in $(echo $ARGS | tr ',' ' '); do
            local single_task=$(task-master show $id 2>/dev/null)
            if [ $? -eq 0 ] && [ -n "$single_task" ]; then
                task_data="$single_task"
                task_ids+=("$id")
            fi
        done
    else
        log_info "Getting task 6 as example..."
        task_data=$(task-master show 6 2>/dev/null)
        if [ $? -eq 0 ] && [ -n "$task_data" ]; then
            task_ids=("6")
        fi
    fi
    
    if [ -z "$task_data" ] || [ ${#task_ids[@]} -eq 0 ]; then
        log_error "No tasks found to document"
        exit 1
    fi
    
    # Process tasks
    local processed_count=0
    local frontend_count=0
    local backend_count=0
    
    for task_id in "${task_ids[@]}"; do
        if [ -n "$task_id" ]; then
            log_info "Processing Task $task_id..."
            
            # Get fresh task data for this specific task
            local current_task_data=$(task-master show $task_id 2>/dev/null)
            if [ $? -eq 0 ] && [ -n "$current_task_data" ]; then
                local classification=$(classify_content "$current_task_data")
                
                echo -e "${PURPLE}📝 Task $task_id classified as: $classification${NC}"
                generate_documentation "$current_task_data" "$task_id" "$classification"
                
                case "$classification" in
                    "frontend")
                        frontend_count=$((frontend_count + 1))
                        ;;
                    "backend")
                        backend_count=$((backend_count + 1))
                        ;;
                    "fullstack")
                        frontend_count=$((frontend_count + 1))
                        backend_count=$((backend_count + 1))
                        ;;
                esac
                
                processed_count=$((processed_count + 1))
            else
                log_warning "Could not retrieve data for Task $task_id"
            fi
        fi
    done
    
    echo ""
    log_success "Auto Documentation Complete!"
    echo ""
    echo -e "${CYAN}📄 Generated Documents:${NC}"
    [ $frontend_count -gt 0 ] && echo -e "- Frontend: ${frontend_count} files"
    [ $backend_count -gt 0 ] && echo -e "- Backend: ${backend_count} files"
    echo ""
    echo -e "${CYAN}📊 Documentation Summary:${NC}"
    echo -e "- Tasks processed: ${processed_count}"
    echo -e "- Frontend documents: ${frontend_count}"
    echo -e "- Backend documents: ${backend_count}"
    echo ""
    echo -e "${CYAN}📋 Next Steps:${NC}"
    echo -e "- Review generated documentation for accuracy"
    echo -e "- Update any specific implementation details"
    echo -e "- Consider adding code examples and diagrams"
}

# Run main function
main "$@"