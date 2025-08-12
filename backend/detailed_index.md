# HaniHome AU Backend - Detailed Technical Index

This document provides a detailed technical index of Java classes, their purposes, methods, annotations, and architecture patterns in the HaniHome Australia property rental platform backend.

## Architecture Overview

The application follows **Domain-Driven Design (DDD)** principles with a layered architecture:
- **Presentation Layer** - REST controllers and DTOs
- **Application Layer** - Use cases and application services
- **Domain Layer** - Business logic, entities, value objects, and domain services
- **Infrastructure Layer** - Data persistence, external service integration

## Core Application

### Main Application Class

#### `HanihomeAuApiApplication.java`
- **Package**: `com.hanihome.hanihome_au_api`
- **Purpose**: Spring Boot main application entry point
- **Annotations**:
  - `@SpringBootApplication` - Auto-configuration and component scanning
  - `@EnableJpaAuditing` - JPA auditing for created/updated timestamps
  - `@EnableCaching` - Redis caching support
  - `@EnableScheduling` - Scheduled task execution
- **Methods**:
  - `main(String[] args)` - Application bootstrap method

## Domain Layer

### Aggregate Roots

#### `Property.java` (Property Aggregate)
- **Package**: `com.hanihome.hanihome_au_api.domain.property.entity`
- **Purpose**: Property aggregate root managing all property-related business logic
- **Extends**: `AggregateRoot<PropertyId>`
- **Key Fields**:
  - `PropertyId id` - Unique property identifier
  - `UserId ownerId` - Property owner reference
  - `PropertyType type` - Property type (APARTMENT, HOUSE, etc.)
  - `PropertyStatus status` - Current status (ACTIVE, PENDING, RENTED, etc.)
  - `Address address` - Property location value object
  - `Money rentPrice` - Rental price with currency
  - `PropertySpecs specs` - Property specifications (bedrooms, bathrooms, etc.)
- **Key Methods**:
  - `create(...)` - Static factory method for property creation
  - `updateDetails(String title, String description, PropertySpecs specs)` - Update property information
  - `updatePricing(Money rentPrice, Money depositAmount)` - Update pricing with validation
  - `changeStatus(PropertyStatus newStatus)` - Status transition with business rules
  - `activate()` - Activate property for rental
  - `approve(UserId approvedBy)` - Admin approval with validation
  - `isAvailableForRent()` - Business rule for rental availability
  - `getTotalUpfrontCost()` - Calculate total upfront payment
- **Domain Events**:
  - `PropertyCreatedEvent` - Raised on property creation
  - `PropertyStatusChangedEvent` - Raised on status changes
  - `PropertyPriceChangedEvent` - Raised on significant price changes
- **Business Rules**:
  - Property validation before activation
  - Status transition validation
  - Pricing validation (positive rent, same currency)
  - Title length limits (max 100 characters)

#### `User.java` (User Aggregate)
- **Package**: `com.hanihome.hanihome_au_api.domain.user.entity`
- **Purpose**: User aggregate root managing user lifecycle and permissions
- **Key Fields**:
  - `UserId id` - Unique user identifier
  - `Email email` - Email value object with validation
  - `UserRole role` - User role (TENANT, LANDLORD, AGENT, ADMIN)
  - `boolean emailVerified` - Email verification status
  - `boolean phoneVerified` - Phone verification status
- **Key Methods**:
  - `create(UserId id, Email email, String name, UserRole role)` - Static factory method
  - `updateProfile(String name, String phoneNumber)` - Update user profile
  - `changeRole(UserRole newRole)` - Change user role with event
  - `verifyEmail()` - Mark email as verified
  - `verifyPhone()` - Mark phone as verified
  - `isFullyVerified()` - Check if both email and phone verified
  - `canManageProperty()` - Business rule for property management permission
- **Domain Events**:
  - `UserRegisteredEvent` - Raised on user registration
  - `UserRoleChangedEvent` - Raised on role changes
- **Business Rules**:
  - Name validation (non-empty, max 100 characters)
  - Role-based permission checking

### Value Objects

#### `Money.java`
- **Package**: `com.hanihome.hanihome_au_api.domain.shared.valueobject`
- **Purpose**: Money value object with currency support
- **Key Fields**:
  - `BigDecimal amount` - Monetary amount
  - `String currency` - Currency code (e.g., "AUD")
- **Key Methods**:
  - `add(Money other)` - Add two money amounts
  - `subtract(Money other)` - Subtract money amounts
  - `isLessThan(Money other)` - Comparison method
- **Validation**: Same currency validation for operations

#### `Address.java`
- **Package**: `com.hanihome.hanihome_au_api.domain.shared.valueobject`
- **Purpose**: Address value object for location data
- **Key Fields**:
  - `String street` - Street address
  - `String city` - City name
  - `String state` - State/province
  - `String country` - Country
  - `String postalCode` - Postal/ZIP code
  - `BigDecimal latitude` - Geographic latitude
  - `BigDecimal longitude` - Geographic longitude

#### `PropertySpecs.java`
- **Package**: `com.hanihome.hanihome_au_api.domain.property.valueobject`
- **Purpose**: Property specifications value object
- **Key Fields**:
  - `Integer bedrooms` - Number of bedrooms
  - `Integer bathrooms` - Number of bathrooms
  - `BigDecimal floorArea` - Floor area in square meters
  - `Integer floor` - Floor number
  - `Integer totalFloors` - Total floors in building
- **Validation**: Non-negative values, logical constraints

### Domain Events

#### `PropertyCreatedEvent.java`
- **Purpose**: Domain event for property creation
- **Fields**: `PropertyId propertyId`, `UserId ownerId`, `String title`, `PropertyType type`

#### `PropertyStatusChangedEvent.java`
- **Purpose**: Domain event for property status changes
- **Fields**: `PropertyId propertyId`, `PropertyStatus oldStatus`, `PropertyStatus newStatus`

#### `UserRegisteredEvent.java`
- **Purpose**: Domain event for user registration
- **Fields**: `UserId userId`, `Email email`, `String name`, `UserRole role`

### Enums

#### `PropertyStatus.java`
- **Values**: `PENDING_APPROVAL`, `ACTIVE`, `INACTIVE`, `RENTED`, `SUSPENDED`
- **Methods**:
  - `canTransitionTo(PropertyStatus newStatus)` - Status transition validation
  - `isAvailableForRent()` - Rental availability check
  - `canBeModified()` - Modification permission check

#### `UserRole.java`
- **Values**: `TENANT`, `LANDLORD`, `AGENT`, `ADMIN`
- **Methods**:
  - `hasPermission(String permission)` - Permission checking

#### `PropertyType.java`
- **Values**: `APARTMENT`, `HOUSE`, `STUDIO`, `TOWNHOUSE`, `VILLA`

#### `RentalType.java`
- **Values**: `LONG_TERM`, `SHORT_TERM`

## Application Layer

### Application Services

#### `PropertyApplicationService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.property.service`
- **Purpose**: Property use case orchestration and transaction management
- **Dependencies**: `PropertyRepository`, `UserRepository`, `DomainEventPublisher`
- **Key Methods**:
  - `createProperty(CreatePropertyCommand command)` - Create new property
  - `getProperty(Long propertyId)` - Retrieve property by ID
  - `getAvailableProperties()` - Get all available properties
  - `activateProperty(Long propertyId, Long ownerId)` - Activate property
  - `approveProperty(Long propertyId, Long agentId)` - Admin approval
  - `getPropertiesByOwner(Long ownerId)` - Get owner's properties
- **Annotations**: `@Service`, `@Transactional`

#### `UserApplicationService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.user.service`
- **Purpose**: User management and authentication support
- **Key Methods**:
  - `createUser(CreateUserCommand command)` - User registration
  - `getUserById(Long userId)` - User retrieval
  - `updateUserProfile(Long userId, String name, String phoneNumber)` - Profile updates

#### `PropertySearchService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.property.service`
- **Purpose**: Advanced property search with filtering and pagination
- **Key Methods**:
  - `searchProperties(PropertySearchRequest request)` - Execute property search
- **Features**: Geographic search, price filtering, amenity filtering, cursor-based pagination

#### `PropertyAutocompleteService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.property.service`
- **Purpose**: Real-time search suggestions
- **Key Methods**:
  - `getAutocompleteSuggestions(AutocompleteRequest request)` - Get search suggestions

### Command Objects (CQRS Pattern)

#### `CreatePropertyCommand.java`
- **Purpose**: Command for property creation
- **Fields**: All required property creation parameters
- **Validation**: Bean validation annotations

#### `CreateUserCommand.java`
- **Purpose**: Command for user registration
- **Fields**: User registration parameters

### Response DTOs

#### `PropertyResponseDto.java`
- **Purpose**: Property data transfer object for API responses
- **Fields**: All property information formatted for client consumption

#### `UserResponseDto.java`
- **Purpose**: User data transfer object for API responses
- **Fields**: User information (excluding sensitive data)

## Presentation Layer

### REST Controllers

#### `PropertyController.java`
- **Package**: `com.hanihome.hanihome_au_api.presentation.web.property`
- **Purpose**: Property management REST endpoints
- **Base Path**: `/api/v1/properties`
- **Annotations**: `@RestController`, `@RequestMapping`, `@Tag`, `@SecurityRequirement`
- **Dependencies**: `PropertyApplicationService`, `PropertySearchService`, `PropertyAutocompleteService`

**Endpoints**:
- `POST /` - Create property
  - **Security**: `@PreAuthorize("@securityExpressionHandler.hasPermission('property:create')")`
  - **Request**: `CreatePropertyRequest`
  - **Response**: `PropertyResponseDto`
  - **Status**: 201 Created

- `GET /{propertyId}` - Get property by ID
  - **Security**: `@PreAuthorize("@securityExpressionHandler.canViewProperty(#propertyId)")`
  - **Response**: `PropertyResponseDto`

- `GET /` - Get available properties
  - **Response**: `List<PropertyResponseDto>`

- `GET /my-properties` - Get user's properties
  - **Security**: `@PreAuthorize("@securityExpressionHandler.canAccessLandlordFeatures()")`
  - **Response**: `List<PropertyResponseDto>`

- `PUT /{id}/activate` - Activate property
  - **Security**: `@PreAuthorize("@securityExpressionHandler.canManageProperty(#id) and @securityExpressionHandler.hasPermission('property:update')")`

- `POST /{propertyId}/approve` - Approve property (Admin only)
  - **Security**: `@PreAuthorize("@securityExpressionHandler.hasPermission('property:approve')")`

- `POST /search` - Advanced property search
  - **Request**: `PropertySearchRequest`
  - **Response**: `PropertySearchResponse`

- `GET /autocomplete` - Get autocomplete suggestions
  - **Parameters**: `query`, `type`, `limit`
  - **Response**: `AutocompleteResponse`

#### `UserController.java`
- **Package**: `com.hanihome.hanihome_au_api.presentation.web.user`
- **Purpose**: User management REST endpoints
- **Base Path**: `/api/v1/users`

#### `AuthController.java`
- **Package**: `com.hanihome.hanihome_au_api.controller`
- **Purpose**: Authentication and OAuth2 endpoints
- **Features**: JWT token generation, OAuth2 callback handling

### Request/Response DTOs

#### `PropertySearchRequest.java`
- **Purpose**: Property search criteria
- **Fields**:
  - `String location` - Location search term
  - `BigDecimal minPrice` - Minimum price filter
  - `BigDecimal maxPrice` - Maximum price filter
  - `PropertyType propertyType` - Property type filter
  - `Integer minBedrooms` - Minimum bedrooms
  - `List<String> amenities` - Required amenities
  - `PropertySearchCursor cursor` - Pagination cursor

#### `PropertySearchResponse.java`
- **Purpose**: Property search results
- **Fields**:
  - `List<PropertyResponseDto> properties` - Search results
  - `PropertySearchCursor nextCursor` - Next page cursor
  - `Long totalCount` - Total matching properties
  - `Map<String, Object> aggregations` - Search aggregations

## Infrastructure Layer

### Persistence

#### `PropertyJpaEntity.java`
- **Package**: `com.hanihome.hanihome_au_api.infrastructure.persistence.property`
- **Purpose**: JPA entity for property persistence
- **Annotations**: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- **Relationships**: Various `@OneToMany`, `@ManyToOne` relationships

#### `PropertyRepositoryImpl.java`
- **Package**: `com.hanihome.hanihome_au_api.infrastructure.persistence.property`
- **Purpose**: Implementation of domain PropertyRepository
- **Implements**: `PropertyRepository`
- **Dependencies**: `PropertyJpaRepository`
- **Methods**: Domain-to-JPA entity mapping and vice versa

#### `UserJpaEntity.java`
- **Package**: `com.hanihome.hanihome_au_api.infrastructure.persistence.user`
- **Purpose**: JPA entity for user persistence
- **Features**: OAuth2 provider mapping, role management

### JPA Repositories

#### `PropertyJpaRepository.java`
- **Extends**: `JpaRepository<PropertyJpaEntity, Long>`, `QuerydslPredicateExecutor`
- **Purpose**: Spring Data JPA repository for property data access
- **Custom Queries**: Geographic search, status filtering, advanced search

#### `UserJpaRepository.java`
- **Extends**: `JpaRepository<UserJpaEntity, Long>`
- **Purpose**: User data access with OAuth2 support
- **Methods**: `findByEmail`, `findByOauthProviderAndOauthId`

## Security Layer

### Authentication & Authorization

#### `SecurityConfig.java`
- **Package**: `com.hanihome.hanihome_au_api.config`
- **Purpose**: Spring Security configuration
- **Features**:
  - JWT authentication
  - OAuth2 client configuration
  - CORS configuration
  - Method-level security
- **Annotations**: `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`

#### `JwtTokenProvider.java`
- **Package**: `com.hanihome.hanihome_au_api.security.jwt`
- **Purpose**: JWT token generation and validation
- **Methods**:
  - `generateToken(Authentication authentication)` - Create JWT token
  - `validateToken(String token)` - Validate JWT token
  - `getUserIdFromToken(String token)` - Extract user ID from token

#### `JwtAuthenticationFilter.java`
- **Package**: `com.hanihome.hanihome_au_api.security.jwt`
- **Purpose**: JWT authentication filter
- **Extends**: `OncePerRequestFilter`
- **Process**: Extract and validate JWT from request headers

#### `CustomPermissionEvaluator.java`
- **Package**: `com.hanihome.hanihome_au_api.security`
- **Purpose**: Custom permission evaluation for @PreAuthorize
- **Methods**:
  - `hasPermission(Authentication auth, Object targetId, Object permission)` - Permission checking
  - `canViewProperty(Long propertyId)` - Property view permission
  - `canManageProperty(Long propertyId)` - Property management permission

### OAuth2 Integration

#### `CustomOAuth2UserService.java`
- **Package**: `com.hanihome.hanihome_au_api.security.oauth2`
- **Purpose**: Custom OAuth2 user loading and registration
- **Methods**:
  - `loadUser(OAuth2UserRequest userRequest)` - Load OAuth2 user

#### `OAuth2AuthenticationSuccessHandler.java`
- **Purpose**: Handle successful OAuth2 authentication
- **Process**: Generate JWT token and redirect to frontend

#### OAuth2 User Info Classes

#### `GoogleOAuth2UserInfo.java`
- **Purpose**: Google OAuth2 user information extraction
- **Methods**: Extract email, name, and profile image from Google response

#### `KakaoOAuth2UserInfo.java`
- **Purpose**: Kakao OAuth2 user information extraction
- **Methods**: Extract user data from Kakao response format

## Configuration Classes

### `CacheConfig.java`
- **Package**: `com.hanihome.hanihome_au_api.config`
- **Purpose**: Redis cache configuration
- **Features**: Cache managers, serialization, TTL settings

### `EmailConfig.java`
- **Purpose**: Email service configuration
- **Features**: SMTP settings, template configuration

### `FirebaseConfig.java`
- **Purpose**: Firebase Cloud Messaging configuration
- **Features**: FCM credentials, notification settings

### `OpenApiConfig.java`
- **Purpose**: Swagger/OpenAPI documentation configuration
- **Features**: API documentation metadata, security schemes

## Service Classes

### Business Services

#### `SearchHistoryService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.search.service`
- **Purpose**: User search history management
- **Methods**:
  - `recordSearch(CreateSearchHistoryCommand command)` - Record user search
  - `getUserSearchHistory(Long userId)` - Get user's search history

#### `ViewingService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.viewing.service`
- **Purpose**: Property viewing appointment management
- **Methods**:
  - `scheduleViewing(CreateViewingCommand command)` - Schedule viewing
  - `cancelViewing(Long viewingId)` - Cancel viewing appointment

#### `FCMNotificationService.java`
- **Package**: `com.hanihome.hanihome_au_api.application.notification.service`
- **Purpose**: Firebase Cloud Messaging notifications
- **Methods**:
  - `sendNotification(String token, String title, String body)` - Send push notification

### Infrastructure Services

#### `FileStorageService.java`
- **Package**: `com.hanihome.hanihome_au_api.service`
- **Purpose**: File upload and storage management
- **Methods**:
  - `store(MultipartFile file)` - Store uploaded file
  - `loadAsResource(String filename)` - Load stored file

## Validation

### Custom Validators

#### `PropertyTypeValidator.java`
- **Package**: `com.hanihome.hanihome_au_api.validation`
- **Purpose**: Custom property type validation
- **Annotation**: `@ValidPropertyType`

#### `DateRangeValidator.java`
- **Purpose**: Date range validation
- **Annotation**: `@ValidDateRange`

## Database Schema

### Migration Files (Flyway)

#### `V1__Initial_Schema.sql`
- **Purpose**: Initial database schema setup
- **Features**: Schema creation, permissions, extensions

#### `V2__Create_users_table.sql`
- **Purpose**: User table creation
- **Fields**: User profile, authentication, OAuth2 data

#### `V4__Create_properties_tables.sql`
- **Purpose**: Property-related tables
- **Tables**: Properties, property images, property features

#### `V20250104_003__Create_Viewings_Table.sql`
- **Purpose**: Property viewing appointments
- **Features**: Scheduling, conflict detection

#### `V20250105_001__Create_Transaction_Tables.sql`
- **Purpose**: Financial transaction management
- **Features**: Payment processing, transaction history

## Exception Handling

### `GlobalExceptionHandler.java`
- **Package**: `com.hanihome.hanihome_au_api.exception`
- **Purpose**: Global exception handling
- **Annotations**: `@ControllerAdvice`, `@ExceptionHandler`
- **Methods**:
  - `handlePropertyException(PropertyException ex)` - Property-specific exceptions
  - `handleUserException(UserException ex)` - User-specific exceptions
  - `handleValidationExceptions(MethodArgumentNotValidException ex)` - Validation errors
  - `handleGenericException(Exception ex)` - Generic error handling

## Key Design Patterns

### 1. Domain-Driven Design (DDD)
- **Aggregate Roots**: Property, User
- **Value Objects**: Money, Address, PropertySpecs
- **Domain Events**: PropertyCreatedEvent, UserRegisteredEvent
- **Domain Services**: PropertyDomainService

### 2. Command Query Responsibility Segregation (CQRS)
- **Commands**: CreatePropertyCommand, CreateUserCommand
- **Queries**: Separate read models for search and display

### 3. Repository Pattern
- **Domain Repositories**: PropertyRepository, UserRepository
- **JPA Implementations**: PropertyRepositoryImpl, UserRepositoryImpl

### 4. Factory Pattern
- **OAuth2UserInfoFactory**: Creates appropriate OAuth2 user info objects
- **Static Factory Methods**: Property.create(), User.create()

### 5. Strategy Pattern
- **OAuth2 User Info**: Different strategies for Google and Kakao

### 6. Observer Pattern
- **Domain Events**: Event-driven architecture for cross-cutting concerns

## Security Implementation

### Authentication Flow
1. **JWT Authentication**: Token-based stateless authentication
2. **OAuth2 Integration**: Google and Kakao social login
3. **Permission-based Authorization**: Method-level security with custom expressions

### Authorization Levels
- **TENANT**: Basic user operations
- **LANDLORD**: Property management
- **AGENT**: Property approval and management
- **ADMIN**: Full system access

### Security Annotations Usage
- `@PreAuthorize`: Method-level authorization
- `@SecurityRequirement`: OpenAPI security documentation
- Custom expressions: `@securityExpressionHandler.hasPermission()`

This architecture provides a robust, scalable foundation for the property rental platform with clear separation of concerns, comprehensive security, and maintainable code structure following established patterns and best practices.