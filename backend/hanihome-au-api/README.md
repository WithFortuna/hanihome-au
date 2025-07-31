# HaniHome Australia - Backend API ⚙️

> Spring Boot-based RESTful API for Australian real estate management platform

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-green)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue)](https://www.postgresql.org/)

## 📖 Overview

The backend API of HaniHome Australia is a robust, scalable Spring Boot application that provides RESTful endpoints for property management, user authentication, and geographic search capabilities.

### ✨ Key Features

- **🏠 Property Management**: CRUD operations for property listings
- **🗺️ Geographic Search**: PostGIS-powered location-based queries
- **🔐 JWT Authentication**: Secure token-based authentication
- **🔑 OAuth2 Integration**: Google and Kakao OAuth support
- **📊 Performance Monitoring**: Actuator-based health checks and metrics
- **🗄️ Database Management**: Flyway migrations and connection pooling
- **🛡️ Security**: Comprehensive security headers and input validation

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21 (LTS)
- **Database**: PostgreSQL 14+ with PostGIS
- **Cache**: Redis 6+
- **Security**: Spring Security 6 + JWT
- **Data Access**: JPA/Hibernate + QueryDSL
- **Migration**: Flyway
- **Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Gradle 8.5
- **Testing**: JUnit 5 + Testcontainers

## 🏗️ Project Structure

```
src/main/java/com/hanihome/
├── api/                       # Common API components
│   ├── config/               # Configuration classes
│   │   ├── CacheConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── controller/           # REST controllers
│   ├── service/              # Business logic services
│   └── security/             # Security implementations
└── hanihome_au_api/          # Main application
    ├── config/               # App-specific configuration
    ├── controller/           # REST controllers
    │   ├── AuthController.java
    │   ├── PropertyController.java
    │   └── UserManagementController.java
    ├── domain/               # Domain entities and enums
    │   ├── entity/
    │   └── enums/
    ├── dto/                  # Data Transfer Objects
    │   ├── request/
    │   └── response/
    ├── repository/           # Data access layer
    ├── service/              # Business services
    ├── security/             # Security implementations
    └── validation/           # Custom validators

src/main/resources/
├── application.yml           # Main configuration
├── db/migration/            # Flyway migration scripts
└── static/                  # Static resources
```

## 🚀 Getting Started

### Prerequisites

- **Java** 21 (JDK)
- **PostgreSQL** 14+ with PostGIS extension
- **Redis** 6+ (for caching and sessions)
- **Gradle** 8.5+ (or use wrapper)

### Installation

1. **Navigate to backend directory**:
   ```bash
   cd backend/hanihome-au-api
   ```

2. **Set up PostgreSQL database**:
   ```sql
   CREATE DATABASE hanihome_au;
   CREATE USER hanihome_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE hanihome_au TO hanihome_user;
   
   -- Enable PostGIS extension
   \c hanihome_au
   CREATE EXTENSION postgis;
   ```

3. **Configure application properties**:
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

4. **Update configuration** in `application-local.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/hanihome_au
       username: hanihome_user
       password: your_password
     
     redis:
       host: localhost
       port: 6379
   
   jwt:
     secret: your-jwt-secret-key
     expiration: 86400000  # 24 hours
   
   google:
     client-id: your-google-client-id
     client-secret: your-google-client-secret
   
   kakao:
     client-id: your-kakao-client-id
     client-secret: your-kakao-client-secret
   ```

### Development

1. **Grant execute permission** (Unix/Linux/Mac):
   ```bash
   chmod +x ./gradlew
   ```

2. **Run the application**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

3. **Access the application**:
   - **API Base URL**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **Health Check**: http://localhost:8080/actuator/health

## 📜 Available Gradle Tasks

```bash
# Development
./gradlew bootRun                    # Run the application
./gradlew bootRun --debug-jvm        # Run with debug mode

# Build
./gradlew build                      # Build the application
./gradlew build -x test             # Build without running tests
./gradlew clean build               # Clean and build

# Testing
./gradlew test                      # Run unit tests
./gradlew integrationTest           # Run integration tests
./gradlew jacocoTestReport         # Generate test coverage report

# Code Quality
./gradlew spotlessCheck            # Check code formatting
./gradlew spotlessApply            # Apply code formatting
./gradlew dependencyCheckAnalyze   # Security dependency check

# QueryDSL
./gradlew compileQuerydsl          # Generate QueryDSL Q-classes
./gradlew clean compileQuerydsl    # Clean and regenerate Q-classes

# Docker
./gradlew bootBuildImage           # Build Docker image with buildpacks
```

## 🌐 API Endpoints

### Authentication Endpoints
```http
POST   /api/auth/login              # User login
POST   /api/auth/refresh            # Refresh JWT token
POST   /api/auth/logout             # User logout
GET    /api/auth/oauth2/google      # Google OAuth2 login
GET    /api/auth/oauth2/kakao       # Kakao OAuth2 login
```

### Property Endpoints
```http
GET    /api/properties              # List properties with pagination
POST   /api/properties              # Create new property
GET    /api/properties/{id}         # Get property by ID
PUT    /api/properties/{id}         # Update property
DELETE /api/properties/{id}         # Delete property
GET    /api/properties/search       # Search properties
POST   /api/properties/search/geo   # Geographic search
```

### User Management Endpoints
```http
GET    /api/users/profile           # Get user profile
PUT    /api/users/profile           # Update user profile
GET    /api/users/preferences       # Get user preferences
PUT    /api/users/preferences       # Update user preferences
PUT    /api/users/password          # Change password
```

### Administrative Endpoints
```http
GET    /actuator/health             # Application health
GET    /actuator/metrics            # Application metrics
GET    /actuator/info               # Application info
```

## 🗄️ Database Schema

### Core Entities

#### Users Table
```sql
users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

#### Properties Table
```sql
properties (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    property_type VARCHAR(50),
    rental_type VARCHAR(50),
    price DECIMAL(15,2),
    bedrooms INTEGER,
    bathrooms INTEGER,
    area_sqm DECIMAL(10,2),
    address TEXT,
    location GEOGRAPHY(POINT, 4326),
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Indexes
- **Spatial indexes** on location columns for geographic queries
- **Composite indexes** on frequently queried columns
- **Unique indexes** on email and other unique constraints

## 🔐 Security Configuration

### JWT Authentication
- **Token Expiration**: 24 hours (configurable)
- **Refresh Token**: 7 days (configurable)
- **Secret Key**: Configured via environment variables
- **Algorithm**: HS512 (HMAC SHA-512)

### OAuth2 Providers
- **Google OAuth2**: Full profile access
- **Kakao OAuth2**: Basic profile information
- **Custom User Details**: Mapped to internal user entities

### Security Headers
```java
// Configured security headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

## 🔍 QueryDSL Integration

### Type-safe Queries
```java
@Repository
public class PropertyRepositoryCustomImpl implements PropertyRepositoryCustom {
    
    @Override
    public Page<Property> findPropertiesWithFilters(
            PropertySearchCriteria criteria, Pageable pageable) {
        
        QProperty property = QProperty.property;
        BooleanBuilder builder = new BooleanBuilder();
        
        // Type-safe query building
        if (criteria.getPropertyType() != null) {
            builder.and(property.propertyType.eq(criteria.getPropertyType()));
        }
        
        if (criteria.getMinPrice() != null) {
            builder.and(property.price.goe(criteria.getMinPrice()));
        }
        
        return queryFactory
            .selectFrom(property)
            .where(builder)
            .orderBy(property.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();
    }
}
```

## 🗺️ Geographic Features

### PostGIS Integration
```java
@Entity
public class Property {
    @Column(columnDefinition = "GEOGRAPHY(POINT, 4326)")
    private Point location;
    
    // Geographic search methods
    public static native Predicate withinDistance(
        Path<Point> location, Point center, double distanceKm);
}
```

### Geographic Search Service
```java
@Service
public class GeographicSearchService {
    
    public List<PropertyWithDistanceResponse> findPropertiesWithinRadius(
            GeographicSearchRequest request) {
        
        Point searchCenter = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );
        
        return propertyRepository.findWithinDistanceOrderByDistance(
            searchCenter, request.getRadiusKm()
        );
    }
}
```

## 🔄 Caching Strategy

### Redis Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(connectionFactory)
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

### Cached Operations
- **Property searches** with TTL of 1 hour
- **User profiles** with TTL of 30 minutes
- **Geographic queries** with TTL of 2 hours
- **Metadata** (property types, etc.) with TTL of 24 hours

## 🧪 Testing

### Test Structure
```bash
src/test/java/
├── unit/                    # Unit tests
├── integration/             # Integration tests
└── resources/
    ├── application-test.yml # Test configuration
    └── test-data/          # Test data files
```

### Testing with Testcontainers
```java
@SpringBootTest
@Testcontainers
class PropertyIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:14-3.2")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(6379);
    
    @Test
    void shouldCreateAndRetrieveProperty() {
        // Test implementation
    }
}
```

## 📊 Performance Monitoring

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics format
- `/actuator/info` - Application information

### Custom Metrics
```java
@Component
public class PropertyMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter propertyCreationCounter;
    private final Timer searchTimer;
    
    public PropertyMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.propertyCreationCounter = Counter.builder("properties.created")
            .description("Number of properties created")
            .register(meterRegistry);
        this.searchTimer = Timer.builder("properties.search.time")
            .description("Property search execution time")
            .register(meterRegistry);
    }
}
```

## 🔧 Configuration

### Profile-based Configuration
- **`local`**: Local development with H2/PostgreSQL
- **`test`**: Testing with Testcontainers
- **`staging`**: Staging environment
- **`production`**: Production environment

### Environment Variables
| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | ✅ |
| `DB_URL` | Database connection URL | ✅ |
| `DB_USERNAME` | Database username | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `REDIS_HOST` | Redis host | ✅ |
| `REDIS_PORT` | Redis port | ✅ |
| `JWT_SECRET` | JWT signing secret | ✅ |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | ⚠️ |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret | ⚠️ |
| `KAKAO_CLIENT_ID` | Kakao OAuth client ID | ⚠️ |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth client secret | ⚠️ |

## 🚀 Deployment

### Docker Build
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Copy source code
COPY src src

# Build application
RUN ./gradlew build -x test

# Run application
EXPOSE 8080
CMD ["java", "-jar", "build/libs/hanihome-au-api-0.0.1-SNAPSHOT.jar"]
```

### Build and Run
```bash
# Build Docker image
docker build -t hanihome-au-backend .

# Run with environment variables
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_URL=jdbc:postgresql://db:5432/hanihome_au \
  -e REDIS_HOST=redis \
  hanihome-au-backend
```

## 🐛 Troubleshooting

### Common Issues

**Database Connection Issues**:
```bash
# Check PostgreSQL connection
psql -h localhost -U hanihome_user -d hanihome_au

# Verify PostGIS extension
SELECT name, default_version,installed_version 
FROM pg_available_extensions WHERE name LIKE 'postgis%';
```

**QueryDSL Q-classes Not Generated**:
```bash
# Clean and regenerate
./gradlew clean compileQuerydsl

# Check generated files
ls -la src/main/generated/
```

**Redis Connection Issues**:
```bash
# Check Redis connection
redis-cli ping

# Check Redis configuration
redis-cli config get "*"
```

## 🤝 Contributing

### Development Guidelines
1. Follow Spring Boot best practices
2. Use QueryDSL for type-safe queries
3. Write comprehensive tests
4. Document API changes in OpenAPI spec
5. Follow conventional commit messages

### Code Style
- Use **Google Java Style** with Spotless
- **4-space indentation**
- **CamelCase** for methods and variables
- **PascalCase** for classes and interfaces
- **UPPER_SNAKE_CASE** for constants

### Pull Request Process
1. Create feature branch from `develop`
2. Write tests for new functionality
3. Ensure all tests pass
4. Update documentation
5. Submit PR with clear description

---

**Part of the [HaniHome Australia](../../README.md) monorepo**