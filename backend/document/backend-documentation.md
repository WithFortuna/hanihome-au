# HaniHome AU Backend Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Setup](#project-setup)
4. [Database Configuration](#database-configuration)
5. [Spring Boot Configuration](#spring-boot-configuration)
6. [Security Configuration](#security-configuration)
7. [JPA/Hibernate & QueryDSL](#jphibernate--querydsl)
8. [Docker Configuration](#docker-configuration)
9. [API Endpoints](#api-endpoints)
10. [Project Structure](#project-structure)
11. [Development Workflow](#development-workflow)
12. [Build Process](#build-process)
13. [Troubleshooting](#troubleshooting)

## Project Overview

HaniHome AU is a property management and rental platform backend built with Spring Boot 3.x. The application provides REST APIs for property management, user authentication, transaction processing, and review systems.

### Key Features
- RESTful API architecture
- OAuth2 integration (Google, Kakao)
- Multi-schema PostgreSQL database
- Docker containerization
- Flyway database migrations
- QueryDSL for type-safe queries
- Spring Security integration
- Health monitoring with Spring Actuator

## Technology Stack

### Core Technologies
- **Java 21** - Programming language
- **Spring Boot 3.4.2** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework
- **PostgreSQL 16** - Primary database
- **Flyway** - Database migration tool
- **QueryDSL 5.1.0** - Type-safe query framework
- **Lombok** - Code generation library
- **Gradle** - Build tool
- **Docker** - Containerization

### Supporting Tools
- **Spring Actuator** - Application monitoring
- **pgAdmin 4** - Database administration
- **JUnit 5** - Testing framework

## Project Setup

### Prerequisites
- Java 21 JDK
- Docker and Docker Compose
- PostgreSQL 16 (optional for local development)
- Gradle 8.x (optional, wrapper included)

### Environment Setup

1. **Clone and navigate to backend directory:**
```bash
cd backend/hanihome-au-api
```

2. **Set up environment variables:**
```bash
# Database credentials
DB_PASSWORD=hanihome_password

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=your_kakao_redirect_uri

# Production database (for prod profile)
DATABASE_URL=jdbc:postgresql://prod-host:5432/hanihome_au
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=prod_password
```

3. **Build the application:**
```bash
./gradlew build
```

4. **Run with Docker Compose (recommended):**
```bash
cd ../..
docker-compose -f docker-compose.dev.yml up -d
```

## Database Configuration

### PostgreSQL Setup

The application uses PostgreSQL 16 with multiple schemas for different functional domains:

#### Database Schemas
- **public** - System-wide tables (database_info)
- **auth** - User authentication and authorization
- **property** - Property listings and management
- **transaction** - Property transactions and bookings
- **review** - User reviews and ratings

#### Connection Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hanihome_au
    username: hanihome_user
    password: ${DB_PASSWORD:hanihome_password}
    driver-class-name: org.postgresql.Driver
```

### Flyway Migrations

Database migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

#### Migration Files
- **V1__Initial_Schema.sql** - Creates initial database structure including:
  - Database schemas (auth, property, transaction, review)
  - UUID extension setup
  - Database info table for version tracking
  - User permissions and privileges

#### Flyway Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public,auth,property,transaction,review
```

### Database Initialization
The initial migration creates a `database_info` table to track database version and initialization status:

```sql
CREATE TABLE IF NOT EXISTS public.database_info (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(50) NOT NULL,
    initialized_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);
```

## Spring Boot Configuration

### Application Properties Structure

The application uses YAML configuration with profile-specific overrides:

#### Main Configuration (`application.yml`)
```yaml
spring:
  application:
    name: hanihome-au-api
  
  # Database configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/hanihome_au
    username: hanihome_user
    password: ${DB_PASSWORD:hanihome_password}
    
  # JPA/Hibernate settings
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: 8080
  servlet:
    context-path: /api/v1
```

#### Profile-Specific Configurations

**Development Profile (`dev`):**
- Uses `create-drop` DDL mode for rapid development
- Enables SQL logging
- Uses local database

**Production Profile (`prod`):**
- Uses `validate` DDL mode for safety
- Minimal logging
- Uses environment-based database configuration

### Build Configuration

#### Gradle Build Script (`build.gradle`)
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.2'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Spring Boot starters
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
    implementation 'com.querydsl:querydsl-core:5.1.0'
    annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
    
    // Database
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
    runtimeOnly 'org.postgresql:postgresql'
    
    // Development tools
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

## Security Configuration

### OAuth2 Integration

The application supports OAuth2 authentication with multiple providers:

#### Supported Providers
1. **Google OAuth2**
2. **Kakao OAuth2** (Korean social platform)

#### OAuth2 Configuration
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
            scope:
              - email
              - profile
          kakao:
            client-id: ${KAKAO_CLIENT_ID:}
            client-secret: ${KAKAO_CLIENT_SECRET:}
            authorization-grant-type: authorization_code
            redirect-uri: ${KAKAO_REDIRECT_URI:}
            client-authentication-method: client_secret_post
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

### Security Filter Chain

#### Current Implementation (`SecurityConfig.java`)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/health/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
```

#### Security Features
- CSRF protection disabled for API usage
- Health endpoints publicly accessible
- Actuator endpoints publicly accessible
- All other endpoints require authentication

## JPA/Hibernate & QueryDSL

### JPA Configuration

#### Hibernate Settings
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # validate in prod, create-drop in dev
    show-sql: false       # true in dev for debugging
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

#### Entity Configuration
The application uses JPA entities with Lombok annotations for reduced boilerplate:

```java
@Entity
@Table(name = "database_info", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "initialized_at", nullable = false)
    private LocalDateTime initializedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### QueryDSL Integration

#### Configuration (`QueryDslConfig.java`)
```java
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {
    private final EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

#### Generated Query Classes
QueryDSL automatically generates Q-classes for type-safe queries:
- Located in `src/main/generated/`
- Generated during compilation
- Example: `QDatabaseInfo.java` for `DatabaseInfo` entity

#### Gradle Configuration for QueryDSL
```gradle
// QueryDSL configuration
def querydslDir = "src/main/generated"

sourceSets {
    main.java.srcDirs += [querydslDir]
}

tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(querydslDir))
}

clean.doLast {
    file(querydslDir).deleteDir()
}
```

## Docker Configuration

### Multi-Stage Dockerfile

The application uses a multi-stage Docker build for optimization:

#### Build Stage
```dockerfile
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Copy gradle wrapper and configuration
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source and build
COPY src src
RUN rm -rf src/main/generated
RUN ./gradlew build --no-daemon -x test
```

#### Runtime Stage
```dockerfile
FROM openjdk:21-jdk-slim AS runtime

# Create application user for security
RUN groupadd -r hanihome && useradd -r -g hanihome hanihome

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R hanihome:hanihome /app

USER hanihome
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose Development Setup

#### Services Configuration (`docker-compose.dev.yml`)
```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: hanihome-postgres-dev
    environment:
      POSTGRES_DB: hanihome_au
      POSTGRES_USER: hanihome_user
      POSTGRES_PASSWORD: hanihome_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hanihome_user -d hanihome_au"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend/hanihome-au-api
    container_name: hanihome-backend-dev
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/hanihome_au
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: hanihome-pgadmin-dev
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@hanihome.com
      PGLADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      postgres:
        condition: service_healthy
```

## API Endpoints

### Health Check Endpoints

#### Application Health
```http
GET /api/v1/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "service": "HaniHome AU API",
  "version": "0.0.1-SNAPSHOT",
  "database": {
    "status": "UP",
    "info": {
      "name": "hanihome_au",
      "version": "1.0.0",
      "initialized_at": "2024-01-15T08:00:00"
    }
  }
}
```

#### Database Health
```http
GET /api/v1/health/database
```

**Response:**
```json
{
  "status": "UP",
  "info": {
    "name": "hanihome_au",
    "version": "1.0.0",
    "initialized_at": "2024-01-15T08:00:00"
  }
}
```

### Spring Actuator Endpoints

Available at `/api/v1/actuator/`:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Project Structure

```
backend/hanihome-au-api/
├── src/
│   ├── main/
│   │   ├── java/com/hanihome/api/
│   │   │   ├── HanihomeAuApiApplication.java  # Main application class
│   │   │   ├── config/                        # Configuration classes
│   │   │   │   ├── QueryDslConfig.java       # QueryDSL configuration
│   │   │   │   └── SecurityConfig.java       # Security configuration
│   │   │   ├── controller/                   # REST controllers
│   │   │   │   └── HealthController.java     # Health check endpoints
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   ├── entity/                       # JPA entities
│   │   │   │   └── DatabaseInfo.java         # Database info entity
│   │   │   ├── exception/                    # Exception handlers
│   │   │   ├── repository/                   # Data repositories
│   │   │   │   └── DatabaseInfoRepository.java
│   │   │   ├── security/                     # Security implementations
│   │   │   └── service/                      # Business logic services
│   │   │       └── DatabaseService.java     # Database operations
│   │   ├── generated/                        # QueryDSL generated classes
│   │   │   └── com/hanihome/api/entity/
│   │   │       └── QDatabaseInfo.java        # Generated Q-class
│   │   └── resources/
│   │       ├── application.yml               # Application configuration
│   │       ├── db/migration/                 # Flyway migrations
│   │       │   └── V1__Initial_Schema.sql    # Initial database schema
│   │       ├── static/                       # Static resources
│   │       └── templates/                    # Template files
│   └── test/
│       └── java/com/hanihome/api/
│           └── HanihomeAuApiApplicationTests.java  # Integration tests
├── build.gradle                             # Gradle build configuration
├── settings.gradle                          # Gradle settings
├── Dockerfile                              # Docker build configuration
├── gradlew                                 # Gradle wrapper script (Unix)
├── gradlew.bat                             # Gradle wrapper script (Windows)
└── gradle/wrapper/                         # Gradle wrapper files
```

### Key Directories and Files

#### Configuration Layer (`config/`)
- **QueryDslConfig.java** - Configures QueryDSL JPAQueryFactory bean
- **SecurityConfig.java** - Spring Security configuration

#### Controller Layer (`controller/`)
- **HealthController.java** - Health check and monitoring endpoints

#### Data Layer (`entity/`, `repository/`)
- **DatabaseInfo.java** - JPA entity for database versioning
- **DatabaseInfoRepository.java** - Spring Data repository

#### Service Layer (`service/`)
- **DatabaseService.java** - Business logic for database operations

#### Resources (`resources/`)
- **application.yml** - Main configuration file with profiles
- **db/migration/** - Flyway database migrations

## Development Workflow

### Local Development Setup

1. **Start PostgreSQL:**
```bash
# Using Docker Compose
docker-compose -f docker-compose.dev.yml up postgres -d

# Or using local PostgreSQL
createdb hanihome_au
psql hanihome_au -f scripts/init-db.sql
```

2. **Run the application:**
```bash
# Using Gradle
./gradlew bootRun

# Or using Spring Boot profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Or using Docker Compose (full stack)
docker-compose -f docker-compose.dev.yml up -d
```

3. **Verify application startup:**
```bash
curl http://localhost:8080/api/v1/health
```

### Database Management

#### Flyway Migrations
```bash
# Check migration status
./gradlew flywayInfo

# Apply migrations
./gradlew flywayMigrate

# Clean database (development only)
./gradlew flywayClean
```

#### QueryDSL Code Generation
```bash
# Generate Q-classes
./gradlew compileJava

# Clean generated files
./gradlew clean
```

### Testing

#### Run Tests
```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests HanihomeAuApiApplicationTests

# Integration tests only
./gradlew integrationTest
```

#### Test Coverage
```bash
# Generate coverage report
./gradlew jacocoTestReport
```

### Code Quality

#### Static Analysis
```bash
# CheckStyle
./gradlew checkstyleMain

# SpotBugs
./gradlew spotbugsMain

# PMD
./gradlew pmdMain
```

## Build Process

### Gradle Build Lifecycle

1. **Clean:** Remove previous build artifacts
2. **Compile:** Compile Java source code and generate QueryDSL classes
3. **Process Resources:** Copy and process resource files
4. **Test:** Execute unit and integration tests
5. **Package:** Create JAR file
6. **Assemble:** Collect all artifacts

### Build Commands

#### Development Build
```bash
# Clean and build
./gradlew clean build

# Skip tests for faster builds
./gradlew clean build -x test

# Build with specific profile
./gradlew build -Pprofile=dev
```

#### Production Build
```bash
# Production-ready build
./gradlew clean build -Pprofile=prod --no-daemon

# Docker image build
docker build -t hanihome-au-api:latest .
```

#### Continuous Integration
```bash
# CI-friendly build
./gradlew clean test build --continue --no-daemon
```

### Build Artifacts

#### Generated Files
- **JAR File:** `build/libs/hanihome-au-api-0.0.1-SNAPSHOT.jar`
- **QueryDSL Classes:** `src/main/generated/`
- **Test Reports:** `build/reports/tests/`
- **Coverage Reports:** `build/reports/jacoco/`

## Troubleshooting

### Common Issues

#### Database Connection Issues
**Problem:** Cannot connect to PostgreSQL
**Solutions:**
1. Check if PostgreSQL is running: `docker ps | grep postgres`
2. Verify database credentials in `application.yml`
3. Check network connectivity: `pg_isready -h localhost -p 5432`
4. Review Docker Compose logs: `docker-compose logs postgres`

#### QueryDSL Generation Issues
**Problem:** Q-classes not generated or outdated
**Solutions:**
1. Clean and rebuild: `./gradlew clean compileJava`
2. Check entity annotations and package structure
3. Verify QueryDSL dependencies in `build.gradle`
4. Delete `src/main/generated` and rebuild

#### OAuth2 Configuration Issues
**Problem:** OAuth2 authentication failing
**Solutions:**
1. Verify client IDs and secrets are set
2. Check redirect URIs match provider configuration
3. Review Spring Security logs for detailed error messages
4. Ensure OAuth2 providers are properly configured

#### Docker Build Issues
**Problem:** Docker build failing
**Solutions:**
1. Check Dockerfile syntax and layer caching
2. Verify all required files are copied
3. Ensure Gradle wrapper has execute permissions
4. Review Docker build logs for specific errors

#### Application Startup Issues
**Problem:** Spring Boot application fails to start
**Solutions:**
1. Check application logs for detailed error messages
2. Verify database schema and migrations
3. Review configuration properties
4. Check for port conflicts (8080)
5. Ensure Java version compatibility (Java 21)

### Debugging Tips

#### Enable Debug Logging
```yaml
logging:
  level:
    com.hanihome: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Health Check Verification
```bash
# Application health
curl -X GET http://localhost:8080/api/v1/health | jq

# Database health
curl -X GET http://localhost:8080/api/v1/health/database | jq

# Actuator endpoints
curl -X GET http://localhost:8080/api/v1/actuator/health | jq
```

#### Database Inspection
```bash
# Connect to PostgreSQL container
docker exec -it hanihome-postgres-dev psql -U hanihome_user -d hanihome_au

# Check database schema
\dt public.*
\dt auth.*
\dt property.*

# Check migrations
SELECT * FROM flyway_schema_history;
```

### Performance Optimization

#### JVM Tuning
```bash
# Production JVM options
java -Xms512m -Xmx2g -XX:+UseG1GC -jar app.jar
```

#### Database Optimization
- Create proper indexes for frequently queried columns
- Use connection pooling for database connections
- Monitor query performance with EXPLAIN ANALYZE
- Consider read replicas for heavy read workloads

#### Caching Strategy
- Implement Redis for session storage
- Use Spring Cache for frequently accessed data
- Configure Hibernate second-level cache

### Monitoring and Observability

#### Metrics Collection
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Log Aggregation
- Configure structured logging with JSON format
- Use centralized logging with ELK stack or similar
- Implement correlation IDs for request tracing

#### Health Checks
- Configure application health indicators
- Implement custom health checks for external dependencies
- Set up alerting based on health check status

---

## Conclusion

This documentation provides a comprehensive overview of the HaniHome AU backend implementation. The application is built with modern Spring Boot practices, proper security configurations, and containerized deployment strategies.

For additional support or questions, refer to the Spring Boot documentation or contact the development team.

**Last Updated:** January 2024  
**Version:** 0.0.1-SNAPSHOT  
**Author:** HaniHome Development Team