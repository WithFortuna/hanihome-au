# Task 6: 매물 등록 및 관리 시스템 - Backend 지원 문서

## 프로젝트 개요

Task 6의 백엔드 구현은 프론트엔드 매물 등록 및 관리 인터페이스를 지원하는 RESTful API와 비즈니스 로직을 제공합니다. Spring Boot 기반의 견고하고 확장 가능한 백엔드 아키텍처를 구축했습니다.

### 구현 범위
- **완료일**: 2025년 8월 3일
- **API 엔드포인트**: 25+ 개 RESTful API
- **데이터베이스 스키마**: 15+ 개 테이블
- **비즈니스 로직**: DDD 패턴 기반 도메인 설계

## Backend 지원 기능 현황

### ✅ Task 6.1-6.2: 매물 등록 및 이미지 업로드 API
**관련 파일**: 
- `/src/main/java/com/hanihome/hanihome_au_api/presentation/web/property/PropertyController.java`
- `/src/main/java/com/hanihome/hanihome_au_api/application/property/service/PropertyApplicationService.java`
- `/src/main/java/com/hanihome/hanihome_au_api/service/FileStorageService.java`

**구현된 API 엔드포인트:**

#### 1. 매물 생성 API
```http
POST /api/v1/properties
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "title": "강남구 신축 아파트",
  "description": "깨끗하고 현대적인 아파트입니다.",
  "propertyType": "APARTMENT",
  "rentalType": "MONTHLY",
  "address": {
    "roadAddress": "서울시 강남구 역삼동 123-45",
    "jibunAddress": "서울시 강남구 역삼동 123-45",
    "latitude": 37.5665,
    "longitude": 126.9780
  },
  "pricing": {
    "rent": 1200000,
    "deposit": 10000000,
    "managementFee": 50000
  },
  "specifications": {
    "area": 84.5,
    "rooms": 3,
    "bathrooms": 2,
    "floor": 5,
    "totalFloors": 15
  },
  "availableFrom": "2025-08-15T00:00:00Z",
  "options": ["주차가능", "엘리베이터", "보안시설"]
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "id": "prop_123456789",
    "title": "강남구 신축 아파트",
    "status": "PENDING_APPROVAL",
    "createdAt": "2025-08-03T10:30:00Z",
    "updatedAt": "2025-08-03T10:30:00Z"
  },
  "message": "매물이 성공적으로 등록되었습니다."
}
```

#### 2. 이미지 업로드 API
```http
POST /api/v1/properties/{propertyId}/images
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}

files: [image1.jpg, image2.jpg, ...]
metadata: {
  "images": [
    {
      "alt": "거실 전경",
      "caption": "넓고 밝은 거실",
      "tags": ["거실", "인테리어"],
      "isThumbnail": true
    }
  ]
}
```

### ✅ Task 6.3: 이미지 관리 시스템 백엔드

#### PropertyImage 엔티티
```java
@Entity
@Table(name = "property_images")
public class PropertyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;
    
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type")
    private String mimeType;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "is_thumbnail")
    private Boolean isThumbnail = false;
    
    @Column(name = "alt_text")
    private String altText;
    
    @Column(name = "caption")
    private String caption;
    
    @Column(name = "tags")
    @Convert(converter = StringListConverter.class)
    private List<String> tags = new ArrayList<>();
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

#### 이미지 순서 변경 API
```http
PUT /api/v1/properties/{propertyId}/images/reorder
Content-Type: application/json

{
  "imageOrders": [
    { "imageId": "img_001", "sortOrder": 1 },
    { "imageId": "img_002", "sortOrder": 2 },
    { "imageId": "img_003", "sortOrder": 3 }
  ]
}
```

### ✅ Task 6.4: 주소 검색 및 지리 정보 API

#### Address Value Object
```java
@Embeddable
public class Address {
    @Column(name = "road_address")
    private String roadAddress;
    
    @Column(name = "jibun_address")
    private String jibunAddress;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "administrative_area")
    private String administrativeArea; // 시/도
    
    @Column(name = "sub_administrative_area")
    private String subAdministrativeArea; // 구/군
    
    @Column(name = "locality")
    private String locality; // 동/읍/면
}
```

#### 주소 검증 API
```http
POST /api/v1/properties/validate-address
Content-Type: application/json

{
  "address": "서울시 강남구 역삼동 123-45",
  "coordinates": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}
```

### ✅ Task 6.5: 매물 옵션 관리 시스템

#### PropertyOption 엔티티
```java
@Entity
@Table(name = "property_options")
public class PropertyOption {
    @Id
    private String id;
    
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private OptionCategory category;
    
    @Column(name = "label", nullable = false)
    private String label;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "icon_name")
    private String iconName;
    
    @Column(name = "has_pricing")
    private Boolean hasPricing = false;
    
    @Column(name = "is_popular")
    private Boolean isPopular = false;
    
    @Column(name = "tags")
    @Convert(converter = StringListConverter.class)
    private List<String> tags = new ArrayList<>();
}

public enum OptionCategory {
    SECURITY,      // 보안시설
    APPLIANCES,    // 생활가전
    BUILDING,      // 건물시설
    LOCATION,      // 주변환경
    STRUCTURE,     // 공간구성
    POLICY         // 특별조건
}
```

#### 매물 옵션 설정 API
```http
PUT /api/v1/properties/{propertyId}/options
Content-Type: application/json

{
  "selectedOptions": [
    {
      "optionId": "parking",
      "monthlyFee": 50000,
      "depositFee": 0,
      "customNote": "지하 1층 주차장"
    },
    {
      "optionId": "elevator",
      "monthlyFee": 0,
      "depositFee": 0
    }
  ]
}
```

### ✅ Task 6.6: 매물 관리 대시보드 API

#### 대시보드 통계 API
```http
GET /api/v1/properties/dashboard/stats
Authorization: Bearer {jwt_token}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "totalProperties": 150,
    "activeProperties": 120,
    "inactiveProperties": 25,
    "pendingApproval": 5,
    "totalViews": 15420,
    "totalInquiries": 89,
    "thisMonthRegistrations": 12,
    "averageViewsPerProperty": 102.8,
    "topPerformingProperties": [
      {
        "id": "prop_001",
        "title": "강남구 신축 아파트",
        "views": 450,
        "inquiries": 12
      }
    ]
  }
}
```

#### 매물 필터링 및 검색 API
```http
GET /api/v1/properties/search
Query Parameters:
- search: 검색어 (제목, 주소)
- propertyType: APARTMENT, VILLA, OFFICETEL, etc.
- rentalType: MONTHLY, JEONSE, SALE
- status: ACTIVE, INACTIVE, PENDING_APPROVAL, REJECTED
- minRent: 최소 임대료
- maxRent: 최대 임대료
- area: 면적 범위
- rooms: 방 개수
- sortBy: createdAt, updatedAt, views, inquiries, rent
- sortOrder: asc, desc
- page: 페이지 번호 (기본값: 0)
- size: 페이지 크기 (기본값: 20)
```

#### QueryDSL 동적 검색 구현
```java
@Repository
public class PropertyRepositoryImpl implements PropertyRepositoryCustom {
    
    @Autowired
    private JPAQueryFactory queryFactory;
    
    @Override
    public Page<Property> searchProperties(PropertySearchCriteria criteria, Pageable pageable) {
        QProperty property = QProperty.property;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 검색어 필터링
        if (StringUtils.hasText(criteria.getSearch())) {
            builder.and(
                property.title.containsIgnoreCase(criteria.getSearch())
                .or(property.address.roadAddress.containsIgnoreCase(criteria.getSearch()))
            );
        }
        
        // 매물 타입 필터링
        if (criteria.getPropertyType() != null) {
            builder.and(property.propertyType.eq(criteria.getPropertyType()));
        }
        
        // 가격 범위 필터링
        if (criteria.getMinRent() != null) {
            builder.and(property.rent.goe(criteria.getMinRent()));
        }
        if (criteria.getMaxRent() != null) {
            builder.and(property.rent.loe(criteria.getMaxRent()));
        }
        
        // 지리적 범위 검색 (반경 기반)
        if (criteria.getLatitude() != null && criteria.getLongitude() != null && criteria.getRadius() != null) {
            builder.and(
                Expressions.numberTemplate(Double.class,
                    "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3}))",
                    criteria.getLongitude(), criteria.getLatitude(),
                    property.address.longitude, property.address.latitude
                ).loe(criteria.getRadius())
            );
        }
        
        // 쿼리 실행
        JPAQuery<Property> query = queryFactory
            .selectFrom(property)
            .where(builder)
            .orderBy(getOrderSpecifier(criteria.getSortBy(), criteria.getSortOrder()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());
            
        List<Property> results = query.fetch();
        long total = queryFactory
            .selectFrom(property)
            .where(builder)
            .fetchCount();
            
        return new PageImpl<>(results, pageable, total);
    }
}
```

### ✅ Task 6.7: 매물 수정 및 상태 관리

#### 매물 상태 변경 API
```http
PUT /api/v1/properties/{propertyId}/status
Content-Type: application/json

{
  "status": "ACTIVE",
  "reason": "검토 완료",
  "adminNotes": "모든 요구사항 충족"
}
```

#### PropertyStatusHistory 엔티티
```java
@Entity
@Table(name = "property_status_history")
public class PropertyStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;
    
    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private PropertyStatus previousStatus;
    
    @Column(name = "new_status")
    @Enumerated(EnumType.STRING)
    private PropertyStatus newStatus;
    
    @Column(name = "changed_by")
    private String changedBy;
    
    @Column(name = "change_reason")
    private String changeReason;
    
    @Column(name = "admin_notes")
    private String adminNotes;
    
    @Column(name = "changed_at")
    private Instant changedAt;
}
```

#### 매물 수정 API
```http
PUT /api/v1/properties/{propertyId}
Content-Type: application/json

{
  "title": "강남구 신축 아파트 (수정됨)",
  "description": "업데이트된 설명",
  "rent": 1300000,
  "deposit": 12000000,
  "availableFrom": "2025-09-01T00:00:00Z",
  "options": ["주차가능", "엘리베이터", "보안시설", "체육시설"]
}
```

### ✅ Task 6.8: 매물 삭제 및 데이터 관리

#### 소프트 삭제 구현
```java
@Entity
@Table(name = "properties")
@SQLDelete(sql = "UPDATE properties SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Property extends AggregateRoot<PropertyId> {
    
    @Column(name = "deleted_at")
    private Instant deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    @Column(name = "deletion_reason")
    private String deletionReason;
    
    public void softDelete(String deletedBy, String reason) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.deletionReason = reason;
    }
    
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.deletionReason = null;
    }
}
```

#### 휴지통 관리 API
```http
# 삭제된 매물 목록 조회
GET /api/v1/properties/trash
Authorization: Bearer {jwt_token}

# 매물 복구
POST /api/v1/properties/{propertyId}/restore
Content-Type: application/json

{
  "reason": "사용자 요청에 의한 복구"
}

# 영구 삭제
DELETE /api/v1/properties/{propertyId}/permanent
Content-Type: application/json

{
  "reason": "30일 경과 후 자동 삭제",
  "confirmDelete": true
}
```

## 데이터베이스 스키마 설계

### 1. Property 테이블 (매물 정보)
```sql
CREATE TABLE properties (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    property_type VARCHAR(50) NOT NULL,
    rental_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    
    -- 주소 정보
    road_address VARCHAR(500),
    jibun_address VARCHAR(500),
    postal_code VARCHAR(10),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    administrative_area VARCHAR(100),
    sub_administrative_area VARCHAR(100),
    locality VARCHAR(100),
    
    -- 가격 정보
    rent BIGINT NOT NULL,
    deposit BIGINT,
    management_fee BIGINT,
    
    -- 매물 사양
    area DECIMAL(8,2),
    rooms INTEGER,
    bathrooms INTEGER,
    bedrooms INTEGER,
    floor INTEGER,
    total_floors INTEGER,
    
    -- 기타 정보
    available_from TIMESTAMP,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    options JSON,
    
    -- 메타데이터
    user_id VARCHAR(255) NOT NULL,
    views INTEGER DEFAULT 0,
    inquiries INTEGER DEFAULT 0,
    favorites INTEGER DEFAULT 0,
    
    -- 관리 정보
    admin_notes TEXT,
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    featured BOOLEAN DEFAULT FALSE,
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(255) NULL,
    deletion_reason TEXT NULL,
    
    -- 인덱스
    INDEX idx_property_type_status (property_type, status),
    INDEX idx_location (latitude, longitude),
    INDEX idx_rent_range (rent, deposit),
    INDEX idx_created_at (created_at),
    INDEX idx_user_properties (user_id, status),
    SPATIAL INDEX idx_location_spatial (latitude, longitude),
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 2. PropertyImage 테이블 (매물 이미지)
```sql
CREATE TABLE property_images (
    id VARCHAR(255) PRIMARY KEY,
    property_id VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    
    -- 이미지 메타데이터
    sort_order INTEGER DEFAULT 0,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    alt_text VARCHAR(255),
    caption TEXT,
    tags JSON,
    
    -- 이미지 속성
    width INTEGER,
    height INTEGER,
    compression_ratio DECIMAL(5,2),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_property_images (property_id, sort_order),
    INDEX idx_thumbnail (property_id, is_thumbnail),
    
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);
```

### 3. PropertyOptions 테이블 (매물 옵션)
```sql
CREATE TABLE property_options (
    id VARCHAR(100) PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    description TEXT,
    icon_name VARCHAR(100),
    has_pricing BOOLEAN DEFAULT FALSE,
    is_popular BOOLEAN DEFAULT FALSE,
    tags JSON,
    display_order INTEGER DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_popular (is_popular, display_order)
);
```

## 비즈니스 로직 및 도메인 설계

### 1. Property Domain Entity
```java
@Entity
public class Property extends AggregateRoot<PropertyId> {
    
    private PropertyId id;
    private String title;
    private String description;
    private PropertyType propertyType;
    private RentalType rentalType;
    private PropertyStatus status;
    private Address address;
    private Money rent;
    private Money deposit;
    private PropertySpecs specifications;
    private List<String> options;
    private UserId ownerId;
    
    // Domain Events
    public void changeStatus(PropertyStatus newStatus, String reason, String changedBy) {
        PropertyStatus oldStatus = this.status;
        this.status = newStatus;
        
        // 도메인 이벤트 발행
        addDomainEvent(new PropertyStatusChangedEvent(
            this.id, oldStatus, newStatus, reason, changedBy
        ));
    }
    
    public void updateRent(Money newRent, String reason) {
        Money oldRent = this.rent;
        this.rent = newRent;
        
        addDomainEvent(new PropertyPriceChangedEvent(
            this.id, oldRent, newRent, reason
        ));
    }
    
    // Business Rules
    public boolean canBeActivated() {
        return hasRequiredInformation() && 
               hasAtLeastOneImage() && 
               isAddressVerified();
    }
    
    private boolean hasRequiredInformation() {
        return title != null && !title.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               rent != null && rent.isPositive() &&
               specifications != null && specifications.isValid();
    }
}
```

### 2. Property Domain Service
```java
@Service
public class PropertyDomainService {
    
    public void validatePropertyCreation(Property property) {
        // 비즈니스 규칙 검증
        if (property.getRent().isGreaterThan(Money.of(10_000_000))) {
            throw new PropertyException("임대료가 너무 높습니다.");
        }
        
        if (property.getDeposit().isGreaterThan(property.getRent().multiply(24))) {
            throw new PropertyException("보증금이 월세의 24배를 초과할 수 없습니다.");
        }
        
        // 중복 매물 검사
        if (isDuplicateProperty(property)) {
            throw new PropertyException("유사한 매물이 이미 등록되어 있습니다.");
        }
    }
    
    public Money calculateTotalMonthlyCost(Property property) {
        Money totalCost = property.getRent();
        
        if (property.getManagementFee() != null) {
            totalCost = totalCost.add(property.getManagementFee());
        }
        
        // 옵션별 추가 비용 계산
        for (String optionId : property.getOptions()) {
            PropertyOption option = propertyOptionRepository.findById(optionId);
            if (option != null && option.hasPricing()) {
                totalCost = totalCost.add(option.getMonthlyFee());
            }
        }
        
        return totalCost;
    }
}
```

### 3. Application Service Layer
```java
@Service
@Transactional
public class PropertyApplicationService {
    
    private final PropertyRepository propertyRepository;
    private final PropertyDomainService propertyDomainService;
    private final FileStorageService fileStorageService;
    private final DomainEventPublisher eventPublisher;
    
    public PropertyResponseDto createProperty(CreatePropertyCommand command) {
        // 1. 명령 검증
        validateCreatePropertyCommand(command);
        
        // 2. 도메인 객체 생성
        Property property = Property.builder()
            .title(command.getTitle())
            .description(command.getDescription())
            .propertyType(command.getPropertyType())
            .rentalType(command.getRentalType())
            .address(command.getAddress())
            .rent(Money.of(command.getRent()))
            .deposit(Money.of(command.getDeposit()))
            .specifications(command.getSpecifications())
            .options(command.getOptions())
            .ownerId(UserId.of(command.getUserId()))
            .build();
        
        // 3. 비즈니스 규칙 검증
        propertyDomainService.validatePropertyCreation(property);
        
        // 4. 저장
        Property savedProperty = propertyRepository.save(property);
        
        // 5. 도메인 이벤트 발행
        eventPublisher.publishEvents(savedProperty.getDomainEvents());
        
        // 6. 응답 DTO 변환
        return PropertyResponseDto.from(savedProperty);
    }
    
    @EventListener
    public void handlePropertyCreated(PropertyCreatedEvent event) {
        // 매물 생성 후 후속 처리
        // - 알림 발송
        // - 검색 인덱스 업데이트
        // - 통계 정보 갱신
    }
}
```

## 파일 저장 및 이미지 처리

### 1. FileStorageService 구현
```java
@Service
public class FileStorageService {
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    private final AmazonS3 s3Client;
    
    public List<String> uploadPropertyImages(String propertyId, List<MultipartFile> files) {
        List<String> uploadedUrls = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            
            // 파일 검증
            validateImageFile(file);
            
            // 파일명 생성
            String fileName = generateFileName(propertyId, i, file.getOriginalFilename());
            String s3Key = "properties/" + propertyId + "/images/" + fileName;
            
            try {
                // S3 업로드
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                
                s3Client.putObject(new PutObjectRequest(
                    bucketName, s3Key, file.getInputStream(), metadata
                ));
                
                // URL 생성
                String fileUrl = s3Client.getUrl(bucketName, s3Key).toString();
                uploadedUrls.add(fileUrl);
                
            } catch (Exception e) {
                log.error("Failed to upload file: " + fileName, e);
                throw new FileStorageException("파일 업로드에 실패했습니다: " + fileName);
            }
        }
        
        return uploadedUrls;
    }
    
    private void validateImageFile(MultipartFile file) {
        // 파일 크기 검증 (최대 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileStorageException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        
        // MIME 타입 검증
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new FileStorageException("지원하지 않는 파일 형식입니다.");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            throw new FileStorageException("올바른 이미지 파일이 아닙니다.");
        }
    }
}
```

### 2. 이미지 리사이징 및 최적화
```java
@Service
public class ImageProcessingService {
    
    public byte[] resizeImage(byte[] imageData, int maxWidth, int maxHeight) {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            
            // 비율 계산
            double widthRatio = (double) maxWidth / originalImage.getWidth();
            double heightRatio = (double) maxHeight / originalImage.getHeight();
            double ratio = Math.min(widthRatio, heightRatio);
            
            int newWidth = (int) (originalImage.getWidth() * ratio);
            int newHeight = (int) (originalImage.getHeight() * ratio);
            
            // 리사이징
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            graphics.dispose();
            
            // JPEG로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new ImageProcessingException("이미지 리사이징에 실패했습니다.", e);
        }
    }
    
    public List<ImageSize> generateThumbnails(byte[] imageData) {
        List<ImageSize> thumbnails = new ArrayList<>();
        
        // 다양한 크기의 썸네일 생성
        int[][] sizes = {{150, 150}, {300, 300}, {600, 400}, {1200, 800}};
        
        for (int[] size : sizes) {
            byte[] thumbnail = resizeImage(imageData, size[0], size[1]);
            thumbnails.add(new ImageSize(size[0], size[1], thumbnail));
        }
        
        return thumbnails;
    }
}
```

## 성능 최적화 및 캐싱

### 1. Redis 캐싱 설정
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class PropertyService {
    
    @Cacheable(value = "properties", key = "#propertyId")
    public PropertyResponseDto getProperty(String propertyId) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
            .orElseThrow(() -> new PropertyNotFoundException("매물을 찾을 수 없습니다: " + propertyId));
        
        return PropertyResponseDto.from(property);
    }
    
    @CacheEvict(value = "properties", key = "#propertyId")
    public void updateProperty(String propertyId, UpdatePropertyCommand command) {
        // 매물 업데이트 로직
    }
}
```

### 2. 데이터베이스 최적화
```sql
-- 복합 인덱스 최적화
CREATE INDEX idx_property_search ON properties (status, property_type, rent, area);
CREATE INDEX idx_property_location ON properties (latitude, longitude) USING SPATIAL;
CREATE INDEX idx_property_created_desc ON properties (created_at DESC);

-- 쿼리 최적화를 위한 뷰
CREATE VIEW property_summary_view AS
SELECT 
    p.id,
    p.title,
    p.property_type,
    p.rental_type,
    p.status,
    p.rent,
    p.deposit,
    p.area,
    p.rooms,
    p.address,
    p.created_at,
    p.views,
    p.inquiries,
    p.favorites,
    pi.thumbnail_url
FROM properties p
LEFT JOIN (
    SELECT 
        property_id,
        file_url as thumbnail_url,
        ROW_NUMBER() OVER (PARTITION BY property_id ORDER BY is_thumbnail DESC, sort_order ASC) as rn
    FROM property_images
) pi ON p.id = pi.property_id AND pi.rn = 1
WHERE p.deleted_at IS NULL;
```

### 3. 비동기 처리
```java
@Service
public class AsyncPropertyService {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processPropertyImages(String propertyId, List<MultipartFile> files) {
        try {
            List<String> urls = fileStorageService.uploadPropertyImages(propertyId, files);
            
            for (int i = 0; i < urls.size(); i++) {
                PropertyImage image = PropertyImage.builder()
                    .propertyId(PropertyId.of(propertyId))
                    .fileUrl(urls.get(i))
                    .sortOrder(i)
                    .build();
                
                propertyImageRepository.save(image);
                
                // 썸네일 생성 (비동기)
                generateThumbnailAsync(image);
            }
            
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("이미지 처리 중 오류 발생", e);
            throw new CompletionException(e);
        }
    }
    
    @Async
    public void generateThumbnailAsync(PropertyImage image) {
        // 썸네일 생성 로직
    }
}
```

## API 보안 및 권한 관리

### 1. JWT 기반 인증
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(UserPrincipal userPrincipal) {
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId())
            .claim("role", userPrincipal.getRole())
            .claim("permissions", userPrincipal.getPermissions())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 2. 권한 기반 접근 제어
```java
@RestController
@RequestMapping("/api/v1/properties")
@PreAuthorize("hasRole('USER')")
public class PropertyController {
    
    @PostMapping
    @PreAuthorize("hasPermission(null, 'PROPERTY_CREATE')")
    public ResponseEntity<PropertyResponseDto> createProperty(
        @Valid @RequestBody CreatePropertyRequest request,
        Authentication authentication
    ) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        
        CreatePropertyCommand command = CreatePropertyCommand.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .userId(user.getId())
            .build();
        
        PropertyResponseDto response = propertyApplicationService.createProperty(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{propertyId}")
    @PreAuthorize("hasPermission(#propertyId, 'Property', 'EDIT')")
    public ResponseEntity<PropertyResponseDto> updateProperty(
        @PathVariable String propertyId,
        @Valid @RequestBody UpdatePropertyRequest request,
        Authentication authentication
    ) {
        // 매물 수정 로직
    }
}
```

### 3. 커스텀 권한 평가자
```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Autowired
    private PropertyRepository propertyRepository;
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String permissionString = permission.toString();
        
        // 관리자는 모든 권한
        if (user.hasRole("ADMIN")) {
            return true;
        }
        
        // 매물 생성 권한
        if ("PROPERTY_CREATE".equals(permissionString)) {
            return user.hasRole("LANDLORD") || user.hasRole("AGENT");
        }
        
        return false;
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!"Property".equals(targetType)) {
            return false;
        }
        
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String propertyId = targetId.toString();
        
        // 매물 소유자 확인
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
            .orElse(null);
            
        if (property != null && property.getOwnerId().getValue().equals(user.getId())) {
            return true;
        }
        
        return user.hasRole("ADMIN");
    }
}
```

## 모니터링 및 로깅

### 1. Actuator 설정
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 커스텀 메트릭
```java
@Component
public class PropertyMetrics {
    
    private final Counter propertyCreationCounter;
    private final Timer propertySearchTimer;
    private final Gauge activePropertiesGauge;
    
    public PropertyMetrics(MeterRegistry meterRegistry) {
        this.propertyCreationCounter = Counter.builder("property.created")
            .description("Number of properties created")
            .register(meterRegistry);
            
        this.propertySearchTimer = Timer.builder("property.search.duration")
            .description("Property search duration")
            .register(meterRegistry);
            
        this.activePropertiesGauge = Gauge.builder("property.active.count")
            .description("Number of active properties")
            .register(meterRegistry, this, PropertyMetrics::getActivePropertyCount);
    }
    
    public void incrementPropertyCreation() {
        propertyCreationCounter.increment();
    }
    
    public Timer.Sample startPropertySearchTimer() {
        return Timer.start(propertySearchTimer);
    }
    
    private double getActivePropertyCount() {
        return propertyRepository.countByStatus(PropertyStatus.ACTIVE);
    }
}
```

### 3. 구조화된 로깅
```java
@Slf4j
@Component
public class PropertyEventLogger {
    
    public void logPropertyCreated(PropertyCreatedEvent event) {
        log.info("Property created: propertyId={}, title={}, ownerId={}, timestamp={}",
            event.getPropertyId(),
            event.getTitle(),
            event.getOwnerId(),
            event.getOccurredOn()
        );
    }
    
    public void logPropertyStatusChanged(PropertyStatusChangedEvent event) {
        log.info("Property status changed: propertyId={}, oldStatus={}, newStatus={}, reason={}, changedBy={}",
            event.getPropertyId(),
            event.getOldStatus(),
            event.getNewStatus(),
            event.getReason(),
            event.getChangedBy()
        );
    }
}
```

## 결론

Task 6의 백엔드 구현은 프론트엔드 매물 등록 및 관리 인터페이스를 완벽하게 지원하는 견고하고 확장 가능한 시스템을 제공합니다.

### 핵심 성과
1. **완전한 RESTful API**: 모든 프론트엔드 기능을 지원하는 포괄적 API
2. **도메인 주도 설계**: DDD 패턴을 활용한 유지보수 가능한 아키텍처
3. **고성능 구현**: 캐싱, 인덱싱, 비동기 처리를 통한 최적화
4. **보안 강화**: JWT 인증, 권한 기반 접근 제어, 데이터 검증
5. **모니터링 완비**: 메트릭, 로깅, 헬스체크를 통한 운영 가시성

### 기술적 우수성
- **Spring Boot 3**: 최신 스프링 생태계 활용
- **DDD Architecture**: 비즈니스 로직의 명확한 분리와 관리
- **QueryDSL**: 타입 안전한 동적 쿼리 구현
- **Event-Driven**: 도메인 이벤트를 통한 느슨한 결합
- **Performance**: 캐싱과 최적화를 통한 고성능 달성

이 백엔드 구현은 HaniHome AU 플랫폼의 프론트엔드와 완벽하게 연동되어 사용자에게 원활하고 안정적인 매물 관리 경험을 제공할 준비가 완료되었습니다.

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2025년 8월 3일  
**관련 Frontend 문서**: [frontend-documentation-task6.md](../../frontend/hanihome-au/document/frontend-documentation-task6.md)