# HaniHome AU Backend - General File Index

This document provides a comprehensive index of all files in the HaniHome Australia property rental platform backend.

## Project Overview
HaniHome AU is a Spring Boot-based REST API for a Korean-Australian property rental platform. The application uses Domain Driven Design (DDD) architecture with PostgreSQL database, Redis caching, JWT authentication, OAuth2 integration, and Firebase Cloud Messaging for notifications.

## Documentation Files

### `/backend/document/`
- `backend-documentation.md` - Core backend documentation
- `backend-documentation-2.md` - Additional documentation for release 2
- `backend-documentation-3.md` - Additional documentation for release 3
- `backend-documentation-4.md` - Additional documentation for release 4
- `backend-documentation-5.md` - Additional documentation for release 5
- `backend-documentation-task3.md` - Task 3 specific documentation
- `backend-documentation-task6.md` - Task 6 specific documentation

## Main Application Structure

### `/backend/hanihome-au-api/`

#### Root Configuration Files
- `Dockerfile` - Docker containerization configuration
- `HELP.md` - Spring Boot generated help documentation
- `README.md` - Project readme and setup instructions
- `build.gradle` - Gradle build configuration with dependencies and plugins
- `settings.gradle` - Gradle settings file
- `gradlew` - Gradle wrapper script (Unix)
- `gradlew.bat` - Gradle wrapper script (Windows)
- `dependency-check-suppressions.xml` - OWASP dependency check suppressions

#### Gradle Configuration
- `gradle/wrapper/gradle-wrapper.jar` - Gradle wrapper JAR
- `gradle/wrapper/gradle-wrapper.properties` - Gradle wrapper properties

#### Build Directory
- `build/` - Generated build artifacts (compiled classes, JARs, reports)

## Source Code Structure

### Main Application (`src/main/java/com/hanihome/hanihome_au_api/`)

#### Core Application
- `HanihomeAuApiApplication.java` - Spring Boot main application class with @EnableJpaAuditing, @EnableCaching, @EnableScheduling

#### Application Layer (`application/`)

**Admin Module (`admin/`)**
- `dto/DashboardStatsDto.java` - Dashboard statistics data transfer object
- `dto/PropertyManagementDto.java` - Property management DTO for admin interface
- `dto/UserManagementDto.java` - User management DTO for admin operations
- `service/AdminDashboardService.java` - Admin dashboard business logic
- `service/AdminStatsBatchService.java` - Batch processing for admin statistics

**Moderation Module (`moderation/`)**
- `service/AutoModerationService.java` - Automated content moderation service
- `service/ModerationActionService.java` - Manual moderation actions service
- `service/ReportService.java` - User report handling service

**Notification Module (`notification/`)**
- `service/EmailNotificationService.java` - Email notification handling
- `service/FCMNotificationService.java` - Firebase Cloud Messaging service
- `service/FCMTokenService.java` - FCM token management
- `service/SSENotificationService.java` - Server-Sent Events for real-time notifications

**Property Module (`property/`)**
- `dto/AddToFavoritesCommand.java` - Command for adding properties to favorites
- `dto/CreatePropertyCommand.java` - Command for creating new properties
- `dto/PropertyFavoriteResponseDto.java` - Response DTO for property favorites
- `dto/PropertyResponseDto.java` - Main property response DTO
- `dto/UpdateFavoriteCommand.java` - Command for updating favorites
- `dto/UpdatePropertyCommand.java` - Command for updating property details
- `service/PropertyApplicationService.java` - Main property application service
- `service/PropertyAutocompleteService.java` - Property search autocomplete
- `service/PropertyFavoriteService.java` - Property favorites management
- `service/PropertySearchService.java` - Advanced property search functionality
- `service/SearchCacheEvictionService.java` - Cache management for search
- `service/SearchPerformanceService.java` - Search performance monitoring
- `usecase/CreatePropertyUseCase.java` - Use case for property creation

**Search Module (`search/`)**
- `dto/CreateSearchHistoryCommand.java` - Command for recording search history
- `dto/SaveSearchCommand.java` - Command for saving user searches
- `dto/SearchHistoryResponseDto.java` - Search history response DTO
- `service/SearchHistoryCleanupService.java` - Cleanup old search history
- `service/SearchHistoryService.java` - Search history management

**Transaction Module (`transaction/`)**
- `service/TransactionFinancialService.java` - Financial transaction processing
- `service/TransactionHistoryService.java` - Transaction history management

**User Module (`user/`)**
- `dto/CreateUserCommand.java` - Command for user registration
- `dto/UserResponseDto.java` - User response DTO
- `service/UserApplicationService.java` - User management service
- `usecase/CreateUserUseCase.java` - Use case for user creation

**Viewing Module (`viewing/`)**
- `dto/CreateViewingCommand.java` - Command for scheduling property viewings
- `dto/UpdateViewingCommand.java` - Command for updating viewing appointments
- `dto/ViewingResponseDto.java` - Viewing appointment response DTO
- `service/ViewingConflictService.java` - Viewing conflict resolution
- `service/ViewingMaintenanceService.java` - Viewing maintenance operations
- `service/ViewingService.java` - Main viewing service

#### Configuration (`config/`)
- `CacheConfig.java` - Redis cache configuration
- `CacheInterceptorAspect.java` - AOP interceptor for caching
- `EmailConfig.java` - Email service configuration
- `FirebaseConfig.java` - Firebase/FCM configuration
- `MenuConfiguration.java` - Application menu configuration
- `OpenApiConfig.java` - Swagger/OpenAPI documentation config
- `RedisConfig.java` - Redis connection and serialization config
- `SecurityConfig.java` - Spring Security configuration

#### Controllers (`controller/`)
- `AuthController.java` - Authentication and OAuth2 endpoints
- `MenuController.java` - Dynamic menu endpoints
- `UserManagementController.java` - User management endpoints

#### Domain Layer (`domain/`)

**Entities (`entity/`)**
- `FCMToken.java` - Firebase Cloud Messaging token entity
- `PropertyFavorite.java` - Property favorites entity
- `PropertyImage.java` - Property image entity
- `PropertyStatusHistory.java` - Property status change history
- `Report.java` - User report entity
- `ReportAction.java` - Moderation action entity
- `SearchHistory.java` - User search history entity
- `Transaction.java` - Financial transaction entity
- `TransactionActivity.java` - Transaction activity log
- `TransactionFinancialInfo.java` - Detailed financial information
- `Viewing.java` - Property viewing appointment entity

**Enums (`enums/`)**
- `OAuthProvider.java` - OAuth provider enumeration (Google, Kakao)
- `PaymentFrequency.java` - Payment frequency options
- `PaymentStatus.java` - Payment status enumeration
- `Permission.java` - User permission enumeration
- `PrivacyLevel.java` - Privacy level settings
- `PropertyStatus.java` - Property status enumeration
- `PropertyType.java` - Property type enumeration (APARTMENT, HOUSE, etc.)
- `RentalType.java` - Rental type enumeration (LONG_TERM, SHORT_TERM)
- `ReportStatus.java` - Report status enumeration
- `ReportType.java` - Report type enumeration
- `TransactionActivityType.java` - Transaction activity types
- `TransactionStatus.java` - Transaction status enumeration
- `UserRole.java` - User role enumeration (TENANT, LANDLORD, AGENT, ADMIN)
- `ViewingStatus.java` - Viewing appointment status

**Property Domain (`property/`)**
- `entity/Property.java` - Property aggregate root with business logic
- `event/PropertyCreatedEvent.java` - Domain event for property creation
- `event/PropertyPriceChangedEvent.java` - Domain event for price changes
- `event/PropertyStatusChangedEvent.java` - Domain event for status changes
- `exception/PropertyException.java` - Property-specific exceptions
- `repository/PropertyRepository.java` - Property repository interface
- `service/PropertyDomainService.java` - Property domain service
- `valueobject/PropertyId.java` - Property identifier value object
- `valueobject/PropertySpecs.java` - Property specifications value object

**Shared Domain (`shared/`)**
- `entity/AggregateRoot.java` - Base aggregate root for DDD
- `event/DomainEventPublisher.java` - Domain event publisher interface
- `exception/DomainException.java` - Base domain exception
- `valueobject/Address.java` - Address value object
- `valueobject/BaseId.java` - Base identifier value object
- `valueobject/Money.java` - Money value object with currency

**User Domain (`user/`)**
- `entity/User.java` - User aggregate root
- `event/UserRegisteredEvent.java` - User registration domain event
- `event/UserRoleChangedEvent.java` - User role change domain event
- `exception/UserException.java` - User-specific exceptions
- `repository/UserRepository.java` - User repository interface
- `valueobject/Email.java` - Email value object
- `valueobject/OAuthProvider.java` - OAuth provider value object
- `valueobject/UserId.java` - User identifier value object
- `valueobject/UserRole.java` - User role value object

#### DTOs (`dto/`)

**Request DTOs (`request/`)**
- `GeographicSearchRequest.java` - Geographic search parameters
- `RefreshTokenRequest.java` - JWT refresh token request

**Response DTOs (`response/`)**
- `ApiResponse.java` - Generic API response wrapper
- `ErrorResponse.java` - Error response structure
- `JwtAuthenticationResponse.java` - JWT authentication response
- `MenuItemDto.java` - Menu item data transfer object

#### Exception Handling (`exception/`)
- `GlobalExceptionHandler.java` - Global exception handler with @ControllerAdvice

#### Infrastructure Layer (`infrastructure/`)

**Configuration (`config/`)**
- `DddConfiguration.java` - Domain-driven design configuration

**Event Handling (`event/`)**
- `SpringDomainEventPublisher.java` - Spring-based domain event publisher

**Persistence (`persistence/`)**

*Property Persistence (`property/`)*
- `PropertyJpaEntity.java` - Property JPA entity for database mapping
- `PropertyJpaRepository.java` - Property JPA repository interface
- `PropertyRepositoryImpl.java` - Property repository implementation

*User Persistence (`user/`)*
- `UserJpaEntity.java` - User JPA entity for database mapping
- `UserJpaRepository.java` - User JPA repository interface
- `UserRepositoryImpl.java` - User repository implementation

#### Presentation Layer (`presentation/`)

**DTOs (`dto/`)**
- `AddToFavoritesRequest.java` - Add to favorites request
- `AutocompleteRequest.java` - Autocomplete search request
- `AutocompleteResponse.java` - Autocomplete search response
- `CreatePropertyRequest.java` - Property creation request
- `CreateUserRequest.java` - User creation request
- `PropertySearchCursor.java` - Cursor-based pagination for property search
- `PropertySearchRequest.java` - Property search request with filters
- `PropertySearchResponse.java` - Property search response with results
- `UpdateFavoriteRequest.java` - Update favorite request

**Web Controllers (`web/`)**

*Admin Controllers (`admin/`)*
- `AdminDashboardController.java` - Admin dashboard endpoints

*Moderation Controllers (`moderation/`)*
- `ReportController.java` - Content moderation and reporting endpoints

*Notification Controllers (`notification/`)*
- `FCMController.java` - Firebase Cloud Messaging endpoints
- `NotificationController.java` - General notification endpoints

*Property Controllers (`property/`)*
- `PropertyController.java` - Main property CRUD and search endpoints
- `PropertyFavoriteController.java` - Property favorites endpoints
- `SearchPerformanceController.java` - Search performance monitoring endpoints

*Search Controllers (`search/`)*
- `SearchHistoryController.java` - Search history management endpoints

*User Controllers (`user/`)*
- `UserController.java` - User management endpoints

*Viewing Controllers (`viewing/`)*
- `ViewingController.java` - Property viewing appointment endpoints

#### Repository Layer (`repository/`)
- `FCMTokenRepository.java` - FCM token data access
- `PropertyFavoriteRepository.java` - Property favorites data access
- `PropertyImageRepository.java` - Property image data access
- `PropertyStatusHistoryRepository.java` - Property status history data access
- `ReportActionRepository.java` - Report action data access
- `ReportRepository.java` - Report data access
- `SearchHistoryRepository.java` - Search history data access
- `TransactionActivityRepository.java` - Transaction activity data access
- `TransactionFinancialInfoRepository.java` - Transaction financial info data access
- `TransactionRepository.java` - Transaction data access
- `ViewingRepository.java` - Viewing appointment data access

#### Security (`security/`)
- `CustomPermissionEvaluator.java` - Custom permission evaluation logic
- `SecurityExpressionHandler.java` - Security expression handler
- `UserPrincipal.java` - Custom user principal for authentication

**JWT Security (`jwt/`)**
- `JwtAuthenticationEntryPoint.java` - JWT authentication entry point
- `JwtAuthenticationFilter.java` - JWT authentication filter
- `JwtTokenProvider.java` - JWT token generation and validation

**OAuth2 Security (`oauth2/`)**
- `CustomOAuth2UserService.java` - Custom OAuth2 user service
- `OAuth2AuthenticationFailureHandler.java` - OAuth2 failure handler
- `OAuth2AuthenticationSuccessHandler.java` - OAuth2 success handler

*OAuth2 User Info (`oauth2/user/`)*
- `GoogleOAuth2UserInfo.java` - Google OAuth2 user information
- `KakaoOAuth2UserInfo.java` - Kakao OAuth2 user information
- `OAuth2UserInfo.java` - OAuth2 user info interface
- `OAuth2UserInfoFactory.java` - OAuth2 user info factory

#### Services (`service/`)
- `FileStorageService.java` - File upload and storage service
- `PropertyImageService.java` - Property image management service
- `UserService.java` - User business logic service

#### Validation (`validation/`)
- `DateRangeValidator.java` - Date range validation logic
- `PropertyTypeValidator.java` - Property type validation logic
- `ValidDateRange.java` - Date range validation annotation
- `ValidPropertyType.java` - Property type validation annotation

## Resources (`src/main/resources/`)

### Configuration Files
- `application.yml` - Main application configuration with profiles (dev, staging, prod)

### Database Migrations (`db/migration/`)
- `V1__Initial_Schema.sql` - Initial database schema with schemas and permissions
- `V2__Create_users_table.sql` - User table creation
- `V3__Add_user_profile_fields.sql` - Additional user profile fields
- `V4__Create_properties_tables.sql` - Property-related tables
- `V5__Create_property_status_history_table.sql` - Property status history tracking
- `V6__Create_user_privacy_and_regions_tables.sql` - User privacy and region tables
- `V7__Add_geo_spatial_indexes.sql` - Geospatial indexes for location search
- `V8_Add_adminNotes_to_properties.sql` - Admin notes field for properties
- `V9_Modify_userPrefferedRegion_latitude_longitude.sql` - User preferred region coordinates
- `V10_Add_bedrooms_clumn.sql` - Bedrooms column addition
- `V20250104_001__Create_Search_Performance_Indexes.sql` - Search performance optimization indexes
- `V20250104_002__Create_Search_History_Indexes.sql` - Search history indexes
- `V20250104_003__Create_Viewings_Table.sql` - Property viewing appointments table
- `V20250104_004__Create_FCM_Tokens_Table.sql` - Firebase Cloud Messaging tokens table
- `V20250105_001__Create_Transaction_Tables.sql` - Transaction management tables
- `V20250105_002__Create_Transaction_Financial_Info_Table.sql` - Detailed financial information table
- `V20250105_003__Create_Report_Tables.sql` - User reporting and moderation tables

### Email Templates (`templates/email/`)
- `base.html` - Base email template with common styling
- `new-viewing-request.html` - New viewing request notification email
- `viewing-cancellation.html` - Viewing cancellation notification email
- `viewing-confirmation.html` - Viewing confirmation email
- `viewing-reminder.html` - Viewing reminder email

### Static Resources
- `static/` - Static web assets directory

## Generated Code (`src/main/generated/`)

### QueryDSL Generated Classes
- `com/hanihome/hanihome_au_api/domain/entity/QPropertyImage.java` - QueryDSL Q-class for PropertyImage
- `com/hanihome/hanihome_au_api/domain/entity/QPropertyStatusHistory.java` - QueryDSL Q-class for PropertyStatusHistory
- `com/hanihome/hanihome_au_api/infrastructure/persistence/property/QPropertyJpaEntity.java` - QueryDSL Q-class for PropertyJpaEntity
- `com/hanihome/hanihome_au_api/infrastructure/persistence/user/QUserJpaEntity.java` - QueryDSL Q-class for UserJpaEntity

## Test Resources (`src/test/`)

### Integration Tests (`src/test/java/com/hanihome/hanihome_au_api/integration/`)
- Integration test directory structure

### Test Configuration
- `src/test/resources/application-test.yml` - Test-specific application configuration

## Key Technologies and Dependencies

### Core Framework
- **Spring Boot 3.4.2** - Main framework with Java 21
- **Spring Data JPA** - Data persistence layer
- **Spring Security** - Authentication and authorization
- **Spring Boot Actuator** - Application monitoring

### Database and Caching
- **PostgreSQL** - Primary database
- **Flyway** - Database migration management
- **Redis** - Caching and session management
- **QueryDSL** - Type-safe SQL query generation

### Authentication and Security
- **JWT (JSON Web Tokens)** - Token-based authentication
- **OAuth2** - Third-party authentication (Google, Kakao)
- **Spring Security OAuth2** - OAuth2 client and resource server

### External Services
- **Firebase Cloud Messaging** - Push notifications
- **Email Service** - SMTP-based email notifications
- **Thymeleaf** - Email template engine

### Development and Testing
- **JaCoCo** - Code coverage analysis
- **SonarQube** - Code quality analysis
- **OWASP Dependency Check** - Security vulnerability scanning
- **Swagger/OpenAPI 3** - API documentation
- **TestContainers** - Integration testing with Docker

### Build and Deployment
- **Gradle** - Build automation
- **Docker** - Containerization
- **Lombok** - Java boilerplate code reduction

This backend implements a comprehensive property rental platform with advanced features including geospatial search, real-time notifications, transaction management, and robust security mechanisms following Domain-Driven Design principles.