# Pull Request 🚀

## 📋 Task Reference
- **Task ID**: <!-- e.g., 4.2 -->
- **TaskMaster Link**: `.taskmaster/tasks/task-[ID].md`
- **Related Issue**: <!-- Link to GitHub issue if exists -->

## 📝 Summary
<!-- Brief description of what this PR accomplishes -->

## 🎯 Type of Change
- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📚 Documentation update
- [ ] 🔧 Configuration change
- [ ] ♻️ Code refactoring (no functional changes)
- [ ] ⚡ Performance improvement
- [ ] 🧪 Test update

## 🔗 TaskMaster Integration
- [ ] Task status updated to 'done'
- [ ] Subtasks completed and documented
- [ ] Implementation notes added to TaskMaster subtask
- [ ] All dependencies resolved

## 🛠 Changes Made

### Frontend Changes (`frontend/hanihome-au/`)
- [ ] Component updates
- [ ] New features added
- [ ] Bug fixes
- [ ] Styling/UI changes
- [ ] API integration
- [ ] N/A

### Backend Changes (`backend/hanihome-au-api/`)
- [ ] New API endpoints
- [ ] Business logic updates
- [ ] Security enhancements
- [ ] Performance optimizations
- [ ] Bug fixes
- [ ] N/A

### Database Changes
- [ ] New migrations added
- [ ] Schema changes
- [ ] Index optimizations
- [ ] Data seeding
- [ ] N/A

### Infrastructure Changes
- [ ] CI/CD pipeline updates
- [ ] Docker configuration
- [ ] Terraform changes
- [ ] Deployment scripts
- [ ] N/A

## 🧪 Testing

### Automated Testing
- [ ] Unit tests added/updated
- [ ] Integration tests passing
- [ ] E2E tests (if applicable)
- [ ] All existing tests pass

### Manual Testing
- [ ] Functionality tested locally
- [ ] Cross-browser testing (if frontend)
- [ ] Mobile responsiveness (if frontend)
- [ ] API endpoints tested (if backend)

### Test Coverage
<!-- Include test coverage information -->
- **Frontend**: <!-- Coverage percentage -->
- **Backend**: <!-- Coverage percentage -->

## 📖 Documentation
- [ ] Code comments added where necessary
- [ ] API documentation updated (Swagger/OpenAPI)
- [ ] README updated (if needed)
- [ ] TaskMaster task documentation updated
- [ ] Inline documentation for complex logic

## 🔍 Code Quality

### Pre-submission Checks
- [ ] Self code review completed
- [ ] **Frontend**: `npm run lint` passed
- [ ] **Frontend**: `npm run type-check` passed  
- [ ] **Frontend**: `npm run build` successful
- [ ] **Backend**: `./gradlew spotlessCheck` passed
- [ ] **Backend**: `./gradlew compileJava` passed
- [ ] **Backend**: `./gradlew build` successful

### Code Standards
- [ ] Follows project conventions
- [ ] Meaningful variable/function names
- [ ] DRY principle followed
- [ ] SOLID principles applied
- [ ] Proper error handling

## 🔐 Security Considerations
- [ ] No sensitive data exposed
- [ ] Input validation implemented
- [ ] Authentication/authorization handled properly
- [ ] SQL injection protection (if database changes)
- [ ] XSS prevention (if frontend changes)
- [ ] No new security vulnerabilities introduced

## ⚡ Performance Impact
- [ ] Performance improved
- [ ] No performance impact
- [ ] Performance tested
- [ ] Database queries optimized (if applicable)
- [ ] Bundle size impact assessed (if frontend)

**Performance Notes:**
<!-- Describe any performance implications -->

## 🚀 Deployment

### Deployment Safety
- [ ] Safe to deploy to staging
- [ ] Safe to deploy to production
- [ ] Backward compatible
- [ ] Rollback plan available

### Deployment Requirements
- [ ] Database migrations included
- [ ] Environment variables updated
- [ ] External service dependencies updated
- [ ] No special deployment requirements

## 📱 Screenshots/Demo
<!-- Include screenshots or demo links if UI changes are involved -->

### Before
<!-- Current state -->

### After  
<!-- New state -->

## ⚠️ Breaking Changes
<!-- List any breaking changes and migration steps required -->

**Migration Steps:**
1. <!-- Step 1 -->
2. <!-- Step 2 -->

## 🔄 Dependencies
- [ ] No new dependencies
- [ ] New dependencies added (justified below)
- [ ] Dependencies updated
- [ ] Dependencies removed

**New Dependencies:**
<!-- List and justify any new dependencies -->

## 🌐 Browser/Platform Support
- [ ] Chrome/Chromium
- [ ] Firefox  
- [ ] Safari
- [ ] Edge
- [ ] Mobile browsers
- [ ] N/A

## 📝 Additional Notes
<!-- Any additional context, concerns, or notes for reviewers -->

---

## ✅ Reviewer Checklist
- [ ] Task reference is valid and matches implementation
- [ ] Code follows HaniHome Australia conventions
- [ ] Tests are comprehensive and passing
- [ ] Documentation is accurate and complete
- [ ] No security concerns identified
- [ ] Performance impact assessed
- [ ] Ready for merge

### Focus Areas for Review
<!-- What should reviewers pay special attention to? -->

---

## 📏 PR Title Format
**Format**: `[Task {id}] {type}: {description}`

**Examples**:
- `[Task 4.2] feat: implement property search API with geographic filtering`
- `[Task 5.1] fix: resolve JWT authentication token refresh issues`
- `[Task 6] docs: add comprehensive API documentation for notifications`
- `[Task 3.4] refactor: optimize property query performance with caching`

---

## 🔄 CI/CD Status
<!-- Will be automatically populated by GitHub Actions -->
- **Frontend CI**: <!-- Status will appear here -->
- **Backend CI**: <!-- Status will appear here -->
- **Security Scan**: <!-- Status will appear here -->

---

*🤖 This PR template integrates with TaskMaster AI for automated task tracking and follows HaniHome Australia development standards*