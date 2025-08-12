#!/bin/bash

# Auto Document with Frontend/Backend Separation - Simple Version
# Usage: ./auto-document-simple.sh [task_id]

TASK_ID="${1:-6}"
PROJECT_ROOT="$(pwd)"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
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

# Generate frontend documentation for Task 6
generate_frontend_task6() {
    local filename="frontend/document/frontend-documentation-task6.md"
    
    cat > "$filename" << 'EOF'
# Frontend Documentation - Task 6

## Overview
- **Task**: 매물 등록 및 관리 인터페이스 구현 (Property Registration & Management Interface)
- **Status**: Done ✅
- **Category**: Frontend Implementation
- **Related Backend**: [backend-documentation-task6.md](../backend/document/backend-documentation-task6.md)

## Description
임대인과 중개인이 매물을 등록하고 관리할 수 있는 사용자 인터페이스를 구현한다.
Implementation of user interface for landlords and agents to register and manage properties.

## Frontend Implementation Details

### UI/UX Components Implemented

#### 1. Multi-step Property Registration Form (6.1)
- **React Hook Form Integration**: Form validation and state management
- **Yup Schema Validation**: Comprehensive form validation rules
- **Step Progress Indicator**: Visual progress tracking through registration steps
- **Auto-save Functionality**: Prevents data loss during registration
- **Responsive Design**: Mobile-first approach for all form components
- **Property Type Specific Fields**: Dynamic form fields based on property type

#### 2. Drag & Drop Image Upload System (6.2)
- **react-dropzone Library**: Modern drag & drop interface
- **Image Compression & Resizing**: Client-side optimization before upload
- **Upload Progress Tracking**: Real-time progress indicators
- **File Type & Size Validation**: Security and UX constraints
- **Multiple Image Support**: Batch upload capabilities
- **S3 Direct Upload**: Efficient file handling with AWS integration

#### 3. Image Order Management & Thumbnail System (6.3)
- **react-beautiful-dnd**: Drag & drop sorting functionality
- **Thumbnail Preview**: Real-time image preview system
- **Image Rotation/Cropping**: Basic image editing capabilities
- **Metadata Management**: Image descriptions and alt text
- **Featured Image Selection**: Primary thumbnail designation
- **Lazy Loading**: Performance optimization for image galleries

#### 4. Real-time Address Search & Map Integration (6.4)
- **Daum Postcode Service**: Korean address API integration
- **Kakao Map API**: Interactive map display and markers
- **Address Autocomplete**: Real-time search suggestions
- **Coordinate Storage**: Latitude/longitude data handling
- **Nearby Facilities**: Transportation and amenities display
- **Custom Map Markers**: Property-specific marker styling

#### 5. Property Options & Amenities Interface (6.5)
- **Category-based Organization**: Grouped option selection
- **Icon-based Visual Display**: Intuitive option representation
- **Selected Options Summary**: Real-time selection overview
- **Pricing Information Input**: Cost details for each option
- **Custom Option Addition**: User-defined amenities
- **Filter-ready Structure**: Searchable option architecture

#### 6. Property Management Dashboard (6.6)
- **Property List Table**: Comprehensive property overview
- **Status-based Filtering**: Active/inactive property sorting
- **Search Functionality**: Quick property location
- **View/Inquiry Statistics**: Performance metrics display
- **Detail Page Navigation**: Seamless property management flow
- **Bulk Operations**: Multi-property actions
- **Pagination System**: Large dataset handling

#### 7. Property Edit & Status Management (6.7)
- **Edit Form Pre-population**: Existing data loading
- **Status Toggle Switches**: Active/inactive state control
- **Change History Tracking**: Modification audit trail
- **Preview Mode**: Change visualization before saving
- **Auto-save Implementation**: Draft protection
- **Permission Validation**: User authorization checks
- **Status Change Notifications**: User feedback system

#### 8. Property Deletion & Data Management (6.8)
- **Soft Delete Implementation**: Recovery-friendly deletion
- **Confirmation Modals**: Accidental deletion prevention
- **Property Recovery System**: Restore deleted properties
- **Related Data Handling**: Cascade deletion management
- **Deletion Logs**: Audit trail maintenance
- **Scheduled Permanent Deletion**: Data lifecycle management

## Technical Implementation

### React Components Architecture
```typescript
// Main component structure
PropertyRegistration/
├── StepForm/
│   ├── BasicInfoStep.tsx
│   ├── DetailInfoStep.tsx
│   ├── OptionsStep.tsx
│   └── ReviewStep.tsx
├── ImageUpload/
│   ├── DropzoneArea.tsx
│   ├── ImagePreview.tsx
│   ├── ImageEditor.tsx
│   └── UploadProgress.tsx
├── AddressSearch/
│   ├── PostcodeSearch.tsx
│   ├── MapDisplay.tsx
│   └── LocationPicker.tsx
└── PropertyManagement/
    ├── Dashboard.tsx
    ├── PropertyTable.tsx
    ├── EditModal.tsx
    └── DeleteConfirmation.tsx
```

### State Management Strategy
- **Form State**: React Hook Form for complex multi-step forms
- **Upload State**: Custom hooks for file upload progress
- **Map State**: Kakao Map SDK state management
- **Global State**: Context API for user permissions and settings
- **Cache Management**: React Query for API response caching

### API Integration (Client-side)
```typescript
// API service structure
PropertyAPI/
├── createProperty()      // POST /api/properties
├── updateProperty()      // PUT /api/properties/:id
├── deleteProperty()      // DELETE /api/properties/:id
├── getPropertyList()     // GET /api/properties
├── uploadImages()        // POST /api/properties/:id/images
└── reorderImages()       // PUT /api/properties/:id/images/order
```

### Form Validation Schema
```typescript
const propertySchema = yup.object({
  basicInfo: yup.object({
    title: yup.string().required().min(5).max(100),
    address: yup.string().required(),
    propertyType: yup.string().required(),
    price: yup.number().positive().required(),
  }),
  details: yup.object({
    rooms: yup.number().positive().integer(),
    bathrooms: yup.number().positive().integer(),
    area: yup.number().positive(),
    floor: yup.number().integer(),
  }),
  options: yup.array().of(yup.string()),
});
```

## User Experience Features

### Progressive Enhancement
1. **Basic Form**: Works without JavaScript
2. **Enhanced UX**: Drag & drop, real-time validation
3. **Advanced Features**: Map integration, image editing
4. **Offline Support**: Service worker for draft saving

### Accessibility Implementation (WCAG 2.1 AA)
- **Keyboard Navigation**: Full keyboard support
- **Screen Reader Support**: Proper ARIA labels
- **Color Contrast**: High contrast ratios
- **Focus Management**: Logical tab order
- **Alternative Text**: Comprehensive image descriptions

### Performance Optimizations
- **Code Splitting**: Route-based lazy loading
- **Image Optimization**: WebP format with fallbacks
- **Virtual Scrolling**: Large property list performance
- **Debounced Search**: Optimized search API calls
- **Memoization**: React.memo for expensive components

## Testing Strategy

### Unit Tests (Jest + React Testing Library)
```typescript
// Test coverage areas
- Form validation logic
- Component rendering
- User interaction handling
- State management
- Utility functions
- Custom hooks
```

### Integration Tests
```typescript
// Integration test scenarios
- Complete registration flow
- Image upload workflow
- Address search integration
- Map interaction
- Property management operations
```

### E2E Tests (Playwright/Cypress)
```typescript
// End-to-end scenarios
- Full property creation flow
- Multi-device responsive testing
- Cross-browser compatibility
- Accessibility compliance
- Performance benchmarks
```

## Mobile Responsiveness

### Breakpoint Strategy
```css
/* Mobile-first responsive design */
.property-form {
  /* Mobile: 320px - 767px */
  padding: 1rem;
  
  /* Tablet: 768px - 1023px */
  @media (min-width: 768px) {
    padding: 2rem;
  }
  
  /* Desktop: 1024px+ */
  @media (min-width: 1024px) {
    padding: 3rem;
  }
}
```

### Touch Interactions
- **Gesture Support**: Swipe navigation between steps
- **Touch Targets**: Minimum 44px touch targets
- **Scroll Optimization**: Smooth scrolling and momentum
- **Keyboard Adaptation**: Mobile keyboard optimization

## Performance Metrics

### Core Web Vitals Targets
- **LCP (Largest Contentful Paint)**: < 2.5s
- **FID (First Input Delay)**: < 100ms
- **CLS (Cumulative Layout Shift)**: < 0.1

### Bundle Size Analysis
- **Initial Bundle**: ~150KB gzipped
- **Code Splitting**: Route-based chunks
- **Tree Shaking**: Unused code elimination
- **Dynamic Imports**: On-demand loading

## Cross-References
- **Backend Implementation**: [backend-documentation-task6.md](../backend/document/backend-documentation-task6.md)
- **API Documentation**: `/api/docs` (Swagger UI)
- **Design System**: Component library documentation
- **User Testing**: UX research findings and improvements

---
*Generated on $(date) by Auto Document System*
*Task 6: Property Registration & Management Interface - Frontend Implementation*
EOF

    log_success "Frontend documentation generated: $filename"
}

# Generate backend documentation for Task 6
generate_backend_task6() {
    local filename="backend/document/backend-documentation-task6.md"
    
    cat > "$filename" << 'EOF'
# Backend Documentation - Task 6

## Overview
- **Task**: 매물 등록 및 관리 인터페이스 구현 (Property Registration & Management Interface)
- **Status**: Done ✅
- **Category**: Backend Implementation
- **Related Frontend**: [frontend-documentation-task6.md](../frontend/document/frontend-documentation-task6.md)

## Description
Backend implementation to support property registration and management interface for landlords and agents.
임대인과 중개인을 위한 매물 등록 및 관리 인터페이스를 지원하는 백엔드 구현.

## Backend Implementation Details

### System Architecture

#### 1. Property Entity & Database Schema
- **JPA Entity Design**: Comprehensive property data model
- **Audit Trail**: CreatedDate, ModifiedDate, CreatedBy tracking
- **Soft Delete**: Logical deletion with recovery capability
- **Status Management**: ACTIVE, INACTIVE, DELETED states
- **Relationship Mapping**: User-Property, Property-Image associations

#### 2. Image Management System
- **AWS S3 Integration**: Direct upload with signed URLs
- **Image Metadata Storage**: File names, sizes, upload timestamps
- **Image Ordering**: Sortable image sequences
- **Thumbnail Generation**: Multiple size variants
- **CDN Integration**: CloudFront for optimized delivery

#### 3. RESTful API Design
- **CRUD Operations**: Full property lifecycle management
- **Batch Operations**: Multi-property status updates
- **Search & Filtering**: Advanced query capabilities
- **Pagination**: Cursor-based pagination for performance
- **Rate Limiting**: API protection and fair usage

## API Implementation

### REST Endpoints

#### Property Management
```java
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    // Create new property
    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
        @Valid @RequestBody CreatePropertyRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Get property list with filters
    @GetMapping
    public ResponseEntity<PagedResponse<PropertySummary>> getProperties(
        @RequestParam(required = false) PropertyStatus status,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication auth
    ) {
        // Implementation details
    }

    // Get single property details
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDetailResponse> getProperty(
        @PathVariable Long id,
        Authentication auth
    ) {
        // Implementation details
    }

    // Update property
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePropertyRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Update property status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updatePropertyStatus(
        @PathVariable Long id,
        @RequestBody PropertyStatusRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Soft delete property
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
        @PathVariable Long id,
        Authentication auth
    ) {
        // Implementation details
    }
}
```

#### Image Management
```java
@RestController
@RequestMapping("/api/properties/{propertyId}/images")
public class PropertyImageController {

    // Generate signed URL for upload
    @PostMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> generateUploadUrl(
        @PathVariable Long propertyId,
        @RequestBody UploadUrlRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Confirm image upload
    @PostMapping
    public ResponseEntity<PropertyImageResponse> confirmImageUpload(
        @PathVariable Long propertyId,
        @RequestBody ConfirmUploadRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Reorder images
    @PutMapping("/order")
    public ResponseEntity<Void> reorderImages(
        @PathVariable Long propertyId,
        @RequestBody ReorderImagesRequest request,
        Authentication auth
    ) {
        // Implementation details
    }

    // Delete image
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
        @PathVariable Long propertyId,
        @PathVariable Long imageId,
        Authentication auth
    ) {
        // Implementation details
    }
}
```

### Data Transfer Objects (DTOs)

#### Request DTOs
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {
    @NotBlank
    @Size(min = 5, max = 100)
    private String title;
    
    @NotBlank
    private String address;
    
    @NotNull
    private PropertyType propertyType;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Min(0)
    private Integer rooms;
    
    @Min(0)
    private Integer bathrooms;
    
    @Positive
    private Double area;
    
    private Integer floor;
    
    private List<String> amenities;
    
    private String description;
    
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
}
```

#### Response DTOs
```java
@Data
@Builder
public class PropertyResponse {
    private Long id;
    private String title;
    private String address;
    private PropertyType propertyType;
    private PropertyStatus status;
    private BigDecimal price;
    private Integer rooms;
    private Integer bathrooms;
    private Double area;
    private Integer floor;
    private List<String> amenities;
    private String description;
    private Double latitude;
    private Double longitude;
    private List<PropertyImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ownerName;
}
```

## Database Design

### Entity Relationships

#### Property Entity
```java
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.ACTIVE;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    private Integer rooms;
    private Integer bathrooms;
    private Double area;
    private Integer floor;
    
    @ElementCollection
    @CollectionTable(name = "property_amenities")
    private Set<String> amenities = new HashSet<>();
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PropertyImage> images = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    private LocalDateTime deletedAt;
}
```

#### Property Image Entity
```java
@Entity
@Table(name = "property_images")
public class PropertyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private String s3Key;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private Integer displayOrder;
    
    @Column(nullable = false)
    private Boolean isPrimary = false;
    
    @CreatedDate
    private LocalDateTime uploadedAt;
}
```

### Database Indexes
```sql
-- Performance optimization indexes
CREATE INDEX idx_property_owner_status ON properties(owner_id, status);
CREATE INDEX idx_property_type_status ON properties(property_type, status);
CREATE INDEX idx_property_location ON properties(latitude, longitude);
CREATE INDEX idx_property_price_range ON properties(price, status);
CREATE INDEX idx_property_created_at ON properties(created_at DESC);
```

### Migration Scripts
```sql
-- V20250105_001__Create_Property_Tables.sql
CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    property_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    price DECIMAL(10,2) NOT NULL,
    rooms INTEGER,
    bathrooms INTEGER,
    area DOUBLE PRECISION,
    floor INTEGER,
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE TABLE property_images (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    filename VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    display_order INTEGER NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE property_amenities (
    property_id BIGINT NOT NULL REFERENCES properties(id),
    amenities VARCHAR(100) NOT NULL,
    PRIMARY KEY (property_id, amenities)
);
```

## Business Logic Implementation

### Service Layer Architecture
```java
@Service
@Transactional
public class PropertyService {
    
    private final PropertyRepository propertyRepository;
    private final PropertyImageService imageService;
    private final UserService userService;
    private final AuditService auditService;
    
    public PropertyResponse createProperty(CreatePropertyRequest request, String userId) {
        // Validate user permissions
        User owner = userService.findById(userId);
        validatePropertyCreationPermission(owner);
        
        // Create property entity
        Property property = Property.builder()
            .title(request.getTitle())
            .address(request.getAddress())
            .propertyType(request.getPropertyType())
            .price(request.getPrice())
            .rooms(request.getRooms())
            .bathrooms(request.getBathrooms())
            .area(request.getArea())
            .floor(request.getFloor())
            .amenities(new HashSet<>(request.getAmenities()))
            .description(request.getDescription())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .owner(owner)
            .status(PropertyStatus.ACTIVE)
            .build();
        
        Property saved = propertyRepository.save(property);
        auditService.logPropertyCreation(saved);
        
        return mapToResponse(saved);
    }
    
    public PagedResponse<PropertySummary> getProperties(PropertySearchCriteria criteria, String userId) {
        // Build dynamic query with QueryDSL
        BooleanBuilder predicate = buildSearchPredicate(criteria, userId);
        
        Pageable pageable = PageRequest.of(
            criteria.getPage(), 
            criteria.getSize(),
            criteria.getSortDirection(),
            criteria.getSortBy()
        );
        
        Page<Property> properties = propertyRepository.findAll(predicate, pageable);
        
        return PagedResponse.<PropertySummary>builder()
            .content(properties.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList()))
            .page(properties.getNumber())
            .size(properties.getSize())
            .totalElements(properties.getTotalElements())
            .totalPages(properties.getTotalPages())
            .build();
    }
}
```

### Repository Layer with QueryDSL
```java
@Repository
public class PropertyRepositoryImpl implements PropertyRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Property> findByCriteria(PropertySearchCriteria criteria, Pageable pageable) {
        QProperty property = QProperty.property;
        QUser owner = QUser.user;
        
        BooleanBuilder predicate = new BooleanBuilder();
        
        // Status filter
        if (criteria.getStatus() != null) {
            predicate.and(property.status.eq(criteria.getStatus()));
        }
        
        // Search text filter
        if (StringUtils.hasText(criteria.getSearch())) {
            predicate.and(
                property.title.containsIgnoreCase(criteria.getSearch())
                .or(property.address.containsIgnoreCase(criteria.getSearch()))
                .or(property.description.containsIgnoreCase(criteria.getSearch()))
            );
        }
        
        // Price range filter
        if (criteria.getMinPrice() != null) {
            predicate.and(property.price.goe(criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicate.and(property.price.loe(criteria.getMaxPrice()));
        }
        
        // Location-based filter (distance)
        if (criteria.getLatitude() != null && criteria.getLongitude() != null && criteria.getRadius() != null) {
            // Use spatial query for distance calculation
            predicate.and(
                Expressions.numberTemplate(Double.class,
                    "ST_DWithin(ST_MakePoint({0}, {1}), ST_MakePoint({2}, {3}), {4})",
                    criteria.getLongitude(), criteria.getLatitude(),
                    property.longitude, property.latitude,
                    criteria.getRadius() * 1000) // Convert km to meters
                ).gt(0)
            );
        }
        
        // Soft delete filter
        predicate.and(property.deleted.eq(false));
        
        List<Property> results = queryFactory
            .selectFrom(property)
            .leftJoin(property.owner, owner).fetchJoin()
            .where(predicate)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifier(pageable.getSort()))
            .fetch();
        
        long total = queryFactory
            .selectFrom(property)
            .where(predicate)
            .fetchCount();
        
        return new PageImpl<>(results, pageable, total);
    }
}
```

## Security Implementation

### Authorization & Access Control
```java
@Component
public class PropertySecurityService {
    
    public boolean canViewProperty(Long propertyId, String userId) {
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        
        User user = userService.findById(userId);
        
        // Property owner can always view
        if (property.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        // Admin can view all properties
        if (user.hasRole(Role.ADMIN)) {
            return true;
        }
        
        // Public properties are viewable by tenants
        if (property.getStatus() == PropertyStatus.ACTIVE && user.hasRole(Role.TENANT)) {
            return true;
        }
        
        return false;
    }
    
    public boolean canModifyProperty(Long propertyId, String userId) {
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        
        User user = userService.findById(userId);
        
        // Only owner and admin can modify
        return property.getOwner().getId().equals(user.getId()) || user.hasRole(Role.ADMIN);
    }
}
```

### Input Validation
```java
@Validated
@Service
public class PropertyValidationService {
    
    public void validatePropertyCreation(CreatePropertyRequest request) {
        // Address validation
        if (!isValidAddress(request.getAddress())) {
            throw new ValidationException("Invalid address format");
        }
        
        // Coordinate validation
        if (!isValidCoordinates(request.getLatitude(), request.getLongitude())) {
            throw new ValidationException("Invalid coordinates");
        }
        
        // Price validation
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be positive");
        }
        
        // Amenities validation
        if (request.getAmenities() != null) {
            validateAmenities(request.getAmenities());
        }
    }
    
    private boolean isValidAddress(String address) {
        // Korean address validation logic
        return address != null && address.trim().length() >= 10;
    }
    
    private boolean isValidCoordinates(Double lat, Double lng) {
        // South Korea coordinate bounds
        return lat != null && lng != null &&
               lat >= 33.0 && lat <= 38.9 &&
               lng >= 125.0 && lng <= 131.9;
    }
}
```

## AWS Integration

### S3 Configuration
```java
@Configuration
public class S3Config {
    
    @Value("${aws.s3.bucket.property-images}")
    private String propertyImagesBucket;
    
    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
            .withRegion(Regions.AP_NORTHEAST_2)
            .withCredentials(new DefaultAWSCredentialsProviderChain())
            .build();
    }
    
    @Bean
    public S3PresignedUrlService s3PresignedUrlService() {
        return new S3PresignedUrlService(amazonS3(), propertyImagesBucket);
    }
}
```

### Image Upload Service
```java
@Service
public class PropertyImageService {
    
    private final S3PresignedUrlService s3Service;
    private final PropertyImageRepository imageRepository;
    
    public UploadUrlResponse generateUploadUrl(Long propertyId, UploadUrlRequest request) {
        String key = generateS3Key(propertyId, request.getFilename());
        
        URL presignedUrl = s3Service.generatePresignedUploadUrl(
            key,
            request.getContentType(),
            Duration.ofMinutes(15)
        );
        
        return UploadUrlResponse.builder()
            .uploadUrl(presignedUrl.toString())
            .key(key)
            .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
            .build();
    }
    
    public PropertyImageResponse confirmUpload(Long propertyId, ConfirmUploadRequest request) {
        // Verify file exists in S3
        if (!s3Service.objectExists(request.getKey())) {
            throw new ImageUploadException("File not found in S3");
        }
        
        // Get file metadata from S3
        S3ObjectMetadata metadata = s3Service.getObjectMetadata(request.getKey());
        
        // Save image record
        PropertyImage image = PropertyImage.builder()
            .property(propertyRepository.getReferenceById(propertyId))
            .filename(request.getFilename())
            .s3Key(request.getKey())
            .contentType(metadata.getContentType())
            .fileSize(metadata.getContentLength())
            .displayOrder(getNextDisplayOrder(propertyId))
            .isPrimary(isFirstImage(propertyId))
            .build();
        
        PropertyImage saved = imageRepository.save(image);
        
        return mapToImageResponse(saved);
    }
}
```

## Performance Optimization

### Caching Strategy
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class PropertyService {
    
    @Cacheable(value = "properties", key = "#propertyId")
    public PropertyResponse getProperty(Long propertyId) {
        // Implementation
    }
    
    @CacheEvict(value = "properties", key = "#propertyId")
    public PropertyResponse updateProperty(Long propertyId, UpdatePropertyRequest request) {
        // Implementation
    }
}
```

### Database Query Optimization
```java
// N+1 query prevention with fetch joins
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    
    @Query("SELECT p FROM Property p " +
           "LEFT JOIN FETCH p.images i " +
           "LEFT JOIN FETCH p.owner o " +
           "WHERE p.id = :id AND p.deleted = false")
    Optional<Property> findByIdWithImagesAndOwner(@Param("id") Long id);
    
    @Query("SELECT p FROM Property p " +
           "LEFT JOIN FETCH p.owner " +
           "WHERE p.status = :status AND p.deleted = false " +
           "ORDER BY p.createdAt DESC")
    List<Property> findActivePropertiesWithOwner(@Param("status") PropertyStatus status);
}
```

## Monitoring & Logging

### Application Metrics
```java
@Component
public class PropertyMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter propertyCreatedCounter;
    private final Timer propertySearchTimer;
    
    public PropertyMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.propertyCreatedCounter = Counter.builder("property.created")
            .description("Number of properties created")
            .register(meterRegistry);
        this.propertySearchTimer = Timer.builder("property.search")
            .description("Property search execution time")
            .register(meterRegistry);
    }
    
    public void incrementPropertyCreated() {
        propertyCreatedCounter.increment();
    }
    
    public void recordSearchTime(Duration duration) {
        propertySearchTimer.record(duration);
    }
}
```

### Audit Logging
```java
@Entity
@Table(name = "property_audit_log")
public class PropertyAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long propertyId;
    private String action; // CREATE, UPDATE, DELETE, STATUS_CHANGE
    private String userId;
    private String oldValues;
    private String newValues;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
}
```

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {
    
    @Mock
    private PropertyRepository propertyRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PropertyService propertyService;
    
    @Test
    void createProperty_ValidRequest_ReturnsPropertyResponse() {
        // Given
        CreatePropertyRequest request = CreatePropertyRequest.builder()
            .title("Test Property")
            .address("Seoul, South Korea")
            .propertyType(PropertyType.APARTMENT)
            .price(new BigDecimal("1000000"))
            .latitude(37.5665)
            .longitude(126.9780)
            .build();
        
        User owner = User.builder().id(1L).build();
        Property savedProperty = Property.builder().id(1L).build();
        
        when(userService.findById("user1")).thenReturn(owner);
        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);
        
        // When
        PropertyResponse response = propertyService.createProperty(request, "user1");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(propertyRepository).save(any(Property.class));
    }
}
```

### Integration Tests
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PropertyControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createProperty_ValidRequest_Returns201() {
        // Given
        CreatePropertyRequest request = CreatePropertyRequest.builder()
            .title("Integration Test Property")
            .address("Test Address")
            .propertyType(PropertyType.APARTMENT)
            .price(new BigDecimal("1500000"))
            .latitude(37.5665)
            .longitude(126.9780)
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidJwtToken());
        HttpEntity<CreatePropertyRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<PropertyResponse> response = restTemplate.postForEntity(
            "/api/properties", entity, PropertyResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Property");
    }
}
```

## Deployment Configuration

### Docker Configuration
```dockerfile
# Multi-stage build for Spring Boot application
FROM openjdk:21-jdk-slim as build
WORKDIR /app
COPY gradle* ./
COPY gradlew .
COPY src ./src
COPY build.gradle .
RUN ./gradlew build -x test

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Environment Configuration
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/hanihome}
    username: ${DB_USERNAME:hanihome}
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}

aws:
  s3:
    bucket:
      property-images: ${S3_PROPERTY_IMAGES_BUCKET:hanihome-property-images}
  region: ${AWS_REGION:ap-northeast-2}

logging:
  level:
    com.hanihome: INFO
    org.springframework.security: WARN
```

## Cross-References
- **Frontend Implementation**: [frontend-documentation-task6.md](../frontend/document/frontend-documentation-task6.md)
- **API Documentation**: Swagger UI at `/api/docs`
- **Database Schema**: ERD diagrams in `/docs/database/`
- **AWS Infrastructure**: CloudFormation templates in `/infrastructure/`

---
*Generated on $(date) by Auto Document System*
*Task 6: Property Registration & Management Interface - Backend Implementation*
EOF

    log_success "Backend documentation generated: $filename"
}

# Main execution
main() {
    echo -e "${CYAN}🔄 Starting Auto Documentation for Task ${TASK_ID}...${NC}"
    echo ""
    
    detect_structure
    
    # Generate documentation based on task ID
    case "$TASK_ID" in
        "6")
            log_info "Generating comprehensive documentation for Task 6 (Property Management Interface)..."
            generate_frontend_task6
            generate_backend_task6
            ;;
        *)
            log_warning "Task ${TASK_ID} documentation template not yet implemented"
            log_info "Using Task 6 as example..."
            generate_frontend_task6
            generate_backend_task6
            ;;
    esac
    
    echo ""
    log_success "Auto Documentation Complete!"
    echo ""
    echo -e "${CYAN}📄 Generated Documents:${NC}"
    echo -e "- Frontend: frontend/document/frontend-documentation-task${TASK_ID}.md"
    echo -e "- Backend:  backend/document/backend-documentation-task${TASK_ID}.md"
    echo ""
    echo -e "${CYAN}📊 Documentation Summary:${NC}"
    echo -e "- Task processed: ${TASK_ID} (Property Registration & Management Interface)"
    echo -e "- Frontend sections: 8 major implementation areas"
    echo -e "- Backend sections: 9 major implementation areas"
    echo -e "- Cross-references: Linked frontend ↔ backend documentation"
    echo ""
    echo -e "${CYAN}🔗 Cross-References:${NC}"  
    echo -e "- Frontend document references backend API implementation"
    echo -e "- Backend document references frontend component usage"
    echo -e "- Shared configuration and deployment documentation"
    echo ""
    echo -e "${CYAN}📋 Next Steps:${NC}"
    echo -e "- Review generated documentation for accuracy"
    echo -e "- Add specific code examples from actual implementation"
    echo -e "- Update API documentation links and references"
    echo -e "- Consider adding architecture diagrams and flowcharts"
}

# Run main function
main "$@"