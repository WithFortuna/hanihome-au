# HaniHome AU Backend Documentation - Part 2
## Task 1.5-1.7 Implementation Details

### Document History
- **Part 1**: Initial Spring Boot setup, basic configuration, database setup
- **Part 2**: AWS infrastructure, CI/CD pipelines, environment configuration, production deployment

---

## Table of Contents

1. [AWS Infrastructure Integration](#aws-infrastructure-integration)
2. [Environment Configuration System](#environment-configuration-system)
3. [CI/CD Pipeline Implementation](#cicd-pipeline-implementation)
4. [Docker Production Configuration](#docker-production-configuration)
5. [Security and Secrets Management](#security-and-secrets-management)
6. [Performance Optimization](#performance-optimization)

---

## AWS Infrastructure Integration

### Infrastructure Overview

The backend is designed to run on AWS with the following architecture:

```
Internet Gateway
    ↓
Application Load Balancer (ALB)
    ↓
EC2 Instances (Auto Scaling Group)
    ↓
RDS PostgreSQL (Multi-AZ)
    ↓
ElastiCache Redis
    ↓
S3 Buckets (Assets, Backups, Logs)
```

### Terraform Infrastructure Configuration

#### VPC and Networking
```hcl
# VPC Configuration
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.project_name}-vpc"
    Environment = var.environment
  }
}

# Public Subnets for ALB
resource "aws_subnet" "public" {
  count = 2
  
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 1}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
  
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-${count.index + 1}"
    Type = "Public"
  }
}

# Private Subnets for Application
resource "aws_subnet" "private" {
  count = 2
  
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "${var.project_name}-private-${count.index + 1}"
    Type = "Private"
  }
}

# Database Subnets
resource "aws_subnet" "database" {
  count = 2
  
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 20}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "${var.project_name}-database-${count.index + 1}"
    Type = "Database"
  }
}
```

#### RDS PostgreSQL Configuration
```hcl
resource "aws_db_instance" "main" {
  identifier     = "${var.project_name}-${var.environment}"
  engine         = "postgres"
  engine_version = "16.9"
  instance_class = var.environment == "production" ? "db.r6g.large" : "db.t3.micro"
  
  allocated_storage     = var.environment == "production" ? 100 : 20
  max_allocated_storage = var.environment == "production" ? 1000 : 100
  storage_type          = "gp3"
  storage_encrypted     = true
  
  db_name  = "hanihome_au_${var.environment}"
  username = "hanihome_user"
  password = random_password.db_password.result
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  
  backup_retention_period = var.environment == "production" ? 30 : 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  multi_az               = var.environment == "production"
  publicly_accessible    = false
  
  skip_final_snapshot = var.environment != "production"
  deletion_protection = var.environment == "production"

  tags = {
    Name        = "${var.project_name}-db-${var.environment}"
    Environment = var.environment
  }
}
```

#### S3 Buckets Configuration
```hcl
# Assets Bucket
resource "aws_s3_bucket" "assets" {
  bucket = "${var.project_name}-assets-${var.environment}"

  tags = {
    Name        = "${var.project_name}-assets-${var.environment}"
    Environment = var.environment
    Purpose     = "Application Assets"
  }
}

resource "aws_s3_bucket_public_access_block" "assets" {
  bucket = aws_s3_bucket.assets.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "assets" {
  bucket = aws_s3_bucket.assets.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Backups Bucket
resource "aws_s3_bucket" "backups" {
  bucket = "${var.project_name}-backups-${var.environment}"

  tags = {
    Name        = "${var.project_name}-backups-${var.environment}"
    Environment = var.environment
    Purpose     = "Database Backups"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "backups" {
  bucket = aws_s3_bucket.backups.id

  rule {
    id     = "backup_lifecycle"
    status = "Enabled"

    expiration {
      days = var.environment == "production" ? 2555 : 90 # 7 years for prod, 90 days for others
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}
```

### Application Load Balancer Configuration

```hcl
resource "aws_lb" "main" {
  name               = "${var.project_name}-alb-${var.environment}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets           = aws_subnet.public[*].id

  enable_deletion_protection = var.environment == "production"

  tags = {
    Environment = var.environment
  }
}

resource "aws_lb_target_group" "backend" {
  name     = "${var.project_name}-backend-${var.environment}"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/api/v1/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 2
  }

  tags = {
    Name = "${var.project_name}-backend-tg-${var.environment}"
  }
}
```

---

## Environment Configuration System

### Spring Boot Application Configuration

#### Enhanced application.yml Structure
```yaml
# Base Configuration
spring:
  application:
    name: hanihome-au-api
  
  # Database Configuration with Connection Pooling
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/hanihome_au_dev}
    username: ${DATABASE_USERNAME:hanihome_user}
    password: ${DATABASE_PASSWORD:hanihome_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${DB_POOL_MAX_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      idle-timeout: ${DB_POOL_IDLE_TIMEOUT:300000}
      connection-timeout: ${DB_POOL_CONNECTION_TIMEOUT:20000}
      leak-detection-threshold: ${DB_POOL_LEAK_DETECTION:60000}
      
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.SnakeCasePhysicalNamingStrategy
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        generate_statistics: false
        
  # Redis Configuration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: ${REDIS_DATABASE:0}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          
  # Security Configuration
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
            redirect-uri: ${GOOGLE_REDIRECT_URI:http://localhost:3000/auth/callback/google}
          kakao:
            client-id: ${KAKAO_CLIENT_ID:}
            client-secret: ${KAKAO_CLIENT_SECRET:}
            authorization-grant-type: authorization_code
            redirect-uri: ${KAKAO_REDIRECT_URI:http://localhost:3000/auth/callback/kakao}
            client-authentication-method: client_secret_post
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

# Server Configuration
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${SERVER_SERVLET_CONTEXT_PATH:/api/v1}
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

# Custom Application Configuration
jwt:
  secret: ${JWT_SECRET:dev-jwt-secret-key-for-development-only}
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

# AWS Configuration
aws:
  region: ${AWS_REGION:ap-southeast-2}
  access-key-id: ${AWS_ACCESS_KEY_ID:}
  secret-access-key: ${AWS_SECRET_ACCESS_KEY:}
  s3:
    bucket-assets: ${AWS_S3_BUCKET_ASSETS:hanihome-au-assets-dev}
    bucket-backups: ${AWS_S3_BUCKET_BACKUPS:hanihome-au-backups-dev}
    bucket-logs: ${AWS_S3_BUCKET_LOGS:hanihome-au-logs-dev}

# CORS Configuration
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  allowed-methods: ${CORS_ALLOWED_METHODS:GET,POST,PUT,DELETE,OPTIONS}
  allowed-headers: ${CORS_ALLOWED_HEADERS:Content-Type,Authorization,X-Requested-With}
  allow-credentials: true

# Rate Limiting
rate-limit:
  requests-per-minute: ${RATE_LIMIT_REQUESTS_PER_MINUTE:100}
  burst-size: ${RATE_LIMIT_BURST_SIZE:20}
```

#### Environment-Specific Profiles

##### Development Profile
```yaml
---
spring:
  config:
    activate:
      on-profile: development
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
  h2:
    console:
      enabled: ${ENABLE_H2_CONSOLE:false}

logging:
  level:
    com.hanihome: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.web: DEBUG
    org.springframework.transaction: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

##### Production Profile
```yaml
---
spring:
  config:
    activate:
      on-profile: production
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        generate_statistics: false
        
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10

logging:
  level:
    com.hanihome: WARN
    org.springframework.security: WARN
    org.hibernate.SQL: ERROR
    org.springframework.web: WARN
    root: WARN
  file:
    name: /var/log/hanihome-au/application-production.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: never
  metrics:
    export:
      cloudwatch:
        enabled: true
        namespace: HaniHomeAU/Production

server:
  error:
    include-stacktrace: never
    include-message: never
```

---

## CI/CD Pipeline Implementation

### Backend CI Pipeline

The backend CI pipeline (`ci-backend.yml`) includes comprehensive testing and quality checks:

#### 1. Unit Testing with PostgreSQL
```yaml
services:
  postgres:
    image: postgres:16-alpine
    env:
      POSTGRES_DB: hanihome_au_test
      POSTGRES_USER: test_user
      POSTGRES_PASSWORD: test_password
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5

steps:
  - name: Run tests
    run: ./gradlew test
    env:
      SPRING_PROFILES_ACTIVE: test
      SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/hanihome_au_test
      SPRING_DATASOURCE_USERNAME: test_user
      SPRING_DATASOURCE_PASSWORD: test_password
```

#### 2. Integration Testing
```yaml
integration-test:
  name: Integration Tests
  runs-on: ubuntu-latest
  
  services:
    postgres:
      image: postgres:16-alpine
      env:
        POSTGRES_DB: hanihome_au_integration
        POSTGRES_USER: integration_user
        POSTGRES_PASSWORD: integration_password
    
    redis:
      image: redis:7-alpine
      
  steps:
    - name: Run integration tests
      run: ./gradlew integrationTest
      env:
        SPRING_PROFILES_ACTIVE: integration
        SPRING_REDIS_HOST: localhost
        SPRING_REDIS_PORT: 6379
```

#### 3. Code Quality Analysis
```yaml
code-quality:
  name: Code Quality Analysis
  steps:
    - name: Build and analyze with SonarCloud
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: ./gradlew build sonar --info
```

#### 4. Security Scanning
```yaml
security-scan:
  name: Security Scan
  steps:
    - name: Run OWASP Dependency Check
      run: ./gradlew dependencyCheckAnalyze

    - name: Run Snyk Security Check
      uses: snyk/actions/gradle@master
      env:
        SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      with:
        args: --severity-threshold=high
```

### Production Deployment Pipeline

#### Blue-Green Deployment Strategy
```yaml
deploy-to-production:
  name: Deploy to Production
  steps:
    - name: Blue-Green Deployment Setup
      run: |
        # Create new target group for green environment
        GREEN_TG_ARN=$(aws elbv2 create-target-group \
          --name hanihome-au-green-$(date +%s) \
          --protocol HTTP \
          --port 8080 \
          --vpc-id ${{ secrets.VPC_ID }} \
          --health-check-path /api/v1/actuator/health \
          --query 'TargetGroups[0].TargetGroupArn' \
          --output text)

    - name: Deploy to Green Environment
      run: |
        # Update ECS services with new task definitions
        aws ecs update-service \
          --cluster hanihome-au-prod \
          --service hanihome-au-backend-prod \
          --task-definition hanihome-au-backend-prod \
          --force-new-deployment

    - name: Health Check Green Environment
      run: |
        # Verify health endpoint
        curl -f http://$BACKEND_IP:8080/api/v1/actuator/health || exit 1

    - name: Switch Traffic to Green
      run: |
        # Update ALB listener to point to green target group
        aws elbv2 modify-listener \
          --listener-arn ${{ secrets.ALB_LISTENER_ARN }} \
          --default-actions Type=forward,TargetGroupArn=$GREEN_TG_ARN
```

---

## Docker Production Configuration

### Optimized Multi-Stage Dockerfile

#### Production Dockerfile
```dockerfile
# Build stage
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Copy dependency files
COPY build.gradle settings.gradle ./
COPY gradle gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src

# Clean generated files and build
RUN rm -rf src/main/generated
RUN gradle build --no-daemon -x test

# Runtime stage
FROM openjdk:21-jre-slim AS production

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR file
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose Production Configuration

```yaml
version: '3.8'

services:
  backend:
    image: ${ECR_REGISTRY}/hanihome-au-backend:${IMAGE_TAG:-latest}
    container_name: hanihome-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: production
      DATABASE_URL: ${DATABASE_URL}
      DATABASE_USERNAME: ${DATABASE_USERNAME}
      DATABASE_PASSWORD: ${DATABASE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      AWS_REGION: ${AWS_REGION}
      AWS_S3_BUCKET_ASSETS: ${AWS_S3_BUCKET_ASSETS}
      REDIS_HOST: ${REDIS_HOST}
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      LOG_LEVEL: warn
      ENABLE_SWAGGER: false
      SECURITY_HEADERS_ENABLED: true
    ports:
      - "8080:8080"
    networks:
      - hanihome-network
    restart: unless-stopped
    logging:
      driver: awslogs
      options:
        awslogs-group: /ecs/hanihome-au-production
        awslogs-region: ap-southeast-2
        awslogs-stream-prefix: backend
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

---

## Security and Secrets Management

### AWS Secrets Manager Integration

#### Secrets Setup Script
The `setup-secrets.sh` script automatically configures secrets for all environments:

```bash
# Application secrets structure
{
  "JWT_SECRET": "REPLACE-WITH-PRODUCTION-JWT-SECRET-256-BITS-MINIMUM",
  "GOOGLE_CLIENT_ID": "REPLACE-WITH-PRODUCTION-GOOGLE-CLIENT-ID",
  "GOOGLE_CLIENT_SECRET": "REPLACE-WITH-PRODUCTION-GOOGLE-CLIENT-SECRET",
  "KAKAO_CLIENT_ID": "REPLACE-WITH-PRODUCTION-KAKAO-CLIENT-ID",
  "KAKAO_CLIENT_SECRET": "REPLACE-WITH-PRODUCTION-KAKAO-CLIENT-SECRET",
  "DATABASE_PASSWORD": "REPLACE-WITH-PRODUCTION-DATABASE-PASSWORD",
  "MAIL_PASSWORD": "REPLACE-WITH-PRODUCTION-MAIL-PASSWORD",
  "SENTRY_DSN": "REPLACE-WITH-PRODUCTION-SENTRY-DSN"
}
```

#### Database Credentials Management
```bash
# Database secrets structure
{
  "username": "hanihome_user_production",
  "password": "auto-generated-secure-password",
  "engine": "postgres",
  "host": "REPLACE-WITH-RDS-ENDPOINT",
  "port": 5432,
  "dbname": "hanihome_au_production"
}
```

### Spring Security Configuration

#### Security Configuration Class
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/**", "/api/v1/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/properties/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/agent/**").hasAnyRole("AGENT", "ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
            Arrays.asList(corsAllowedOrigins.split(",")));
        configuration.setAllowedMethods(
            Arrays.asList(corsAllowedMethods.split(",")));
        configuration.setAllowedHeaders(
            Arrays.asList(corsAllowedHeaders.split(",")));
        configuration.setAllowCredentials(corsAllowCredentials);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Rate Limiting Implementation

```java
@Component
public class RateLimitingFilter implements Filter {

    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;
    
    @Value("${rate-limit.burst-size:20}")
    private int burstSize;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientIdentifier(httpRequest);
        
        if (isRateLimited(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientId) {
        String key = "rate_limit:" + clientId;
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return false;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= requestsPerMinute) {
            return true;
        }
        
        redisTemplate.opsForValue().increment(key);
        return false;
    }
}
```

---

## Performance Optimization

### Database Optimization

#### Connection Pool Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000
      max-lifetime: 1800000
```

#### JPA Performance Settings
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        connection:
          provider_disables_autocommit: true
        query:
          in_clause_parameter_padding: true
```

### Caching Strategy

#### Redis Cache Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### Service-Level Caching
```java
@Service
@Transactional(readOnly = true)
public class PropertyService {

    @Cacheable(value = "properties", key = "#id")
    public PropertyDTO getProperty(Long id) {
        // Implementation
    }

    @CacheEvict(value = "properties", key = "#result.id")
    @Transactional
    public PropertyDTO updateProperty(PropertyUpdateRequest request) {
        // Implementation
    }
}
```

### JVM Optimization

#### Production JVM Settings
```bash
# Memory settings
-Xms1g -Xmx2g

# Garbage Collector
-XX:+UseG1GC
-XX:G1HeapRegionSize=16m
-XX:+UseStringDeduplication

# Performance monitoring
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:/var/log/hanihome-au/gc.log

# Security
-Djava.security.egd=file:/dev/./urandom
```

### Monitoring and Metrics

#### Actuator Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
      cloudwatch:
        enabled: true
        namespace: HaniHomeAU/Production
        step: PT1M
```

#### Custom Metrics
```java
@Component
public class ApplicationMetrics {

    private final Counter authenticationCounter;
    private final Timer databaseQueryTimer;
    private final Gauge activeUserGauge;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.authenticationCounter = Counter.builder("auth.attempts")
            .description("Number of authentication attempts")
            .register(meterRegistry);
            
        this.databaseQueryTimer = Timer.builder("db.query.duration")
            .description("Database query execution time")
            .register(meterRegistry);
            
        this.activeUserGauge = Gauge.builder("users.active")
            .description("Number of active users")
            .register(meterRegistry, this, ApplicationMetrics::getActiveUserCount);
    }

    public void recordAuthenticationAttempt(String result) {
        authenticationCounter.increment(Tags.of("result", result));
    }

    public Timer.Sample startDatabaseTimer() {
        return Timer.start(databaseQueryTimer);
    }

    private double getActiveUserCount() {
        // Implementation to count active users
        return 0.0;
    }
}
```

---

## Configuration Validation

### Automated Configuration Validation Script

The `validate-config.sh` script performs comprehensive validation:

#### Key Validation Checks
1. **Required Environment Variables**: Ensures all critical variables are set
2. **JWT Secret Strength**: Validates minimum length and complexity
3. **Database Configuration**: Verifies connection string format
4. **AWS Configuration**: Checks region and S3 bucket settings
5. **OAuth Configuration**: Validates client ID formats
6. **Security Settings**: Ensures production security standards

#### Usage Examples
```bash
# Validate development environment
./scripts/validate-config.sh development

# Validate production environment
./scripts/validate-config.sh production

# Test database connectivity
./scripts/validate-config.sh production --test-db
```

---

## Deployment Automation

### Infrastructure Deployment

#### Terraform Deployment Script
```bash
# Deploy infrastructure
./scripts/deploy-infrastructure.sh production

# This script performs:
# 1. Prerequisites check (AWS CLI, Terraform)
# 2. Terraform backend setup (S3 + DynamoDB)
# 3. Infrastructure deployment with confirmation
# 4. Output saving for application configuration
# 5. Environment file generation
```

### Application Deployment

#### ECS Task Definition
```json
{
  "family": "hanihome-au-backend-prod",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::account:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "account.dkr.ecr.region.amazonaws.com/hanihome-au-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "production"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:hanihome-au-db-production"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/hanihome-au-production",
          "awslogs-region": "ap-southeast-2",
          "awslogs-stream-prefix": "backend"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/api/v1/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

---

## Conclusion

This documentation covers the major backend improvements implemented during Tasks 1.5-1.7:

### Key Achievements
1. **AWS Infrastructure**: Complete cloud infrastructure setup with Terraform
2. **Environment Management**: Comprehensive configuration system for all environments
3. **CI/CD Pipeline**: Automated testing, security scanning, and deployment
4. **Production Optimization**: Performance tuning and security hardening
5. **Secrets Management**: AWS Secrets Manager integration
6. **Monitoring**: Comprehensive metrics and health checks

### Security Highlights
- JWT-based authentication with configurable expiration
- Rate limiting to prevent abuse
- CORS configuration for cross-origin security
- SQL injection and XSS prevention
- Encrypted secrets management
- Security headers and HTTPS enforcement

### Performance Features
- Connection pooling with HikariCP
- Redis caching for frequently accessed data
- JVM optimization for production workloads
- Database query optimization
- Comprehensive monitoring and metrics

### Next Steps
The backend is now ready for:
- User authentication system implementation (Task 2)
- Property management features
- Real-time notifications
- Payment processing integration
- Advanced search capabilities

### Maintenance Guidelines
- Regular security updates and dependency management
- Performance monitoring and optimization
- Infrastructure cost optimization
- Backup and disaster recovery procedures
- Documentation updates as features evolve