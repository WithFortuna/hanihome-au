# Backend Documentation #3 - 프로젝트 초기 설정 및 개발 환경 구축

## 문서 히스토리 및 개요

- **문서 번호**: backend-documentation-3.md
- **작성일**: 2025-01-30
- **Task Master 연계**: Task 1 및 관련 Subtasks (1.2, 1.3, 1.4, 1.5, 1.6, 1.7)
- **이전 문서**: [backend-documentation-2.md](./backend-documentation-2.md)

이 문서는 HaniHome AU 프로젝트의 백엔드 초기 설정 및 개발 환경 구축 과정을 상세히 기록합니다. Task Master에서 완료된 작업들을 바탕으로 Spring Boot 3.x 환경 구축, PostgreSQL 데이터베이스 연동, Docker 컨테이너화, AWS 인프라 설정, CI/CD 파이프라인 구현을 다룹니다.

---

## 1. Spring Boot 3.x 백엔드 프로젝트 초기 설정

### 1.1 프로젝트 생성 및 기본 구조

#### 프로젝트 초기화 (Spring Initializr)
```bash
# 기본 정보
- Project: Gradle - Groovy
- Language: Java
- Spring Boot: 3.2.x
- Packaging: Jar
- Java: 17

# Dependencies:
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Security
- OAuth2 Client
- Redis
- Flyway Migration
- Validation
- Spring Boot Actuator
```

#### build.gradle 주요 의존성
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10'
}

group = 'com.hanihome'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Database
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    
    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

// QueryDSL Configuration
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main.java.srcDir querydslDir
}

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
    querydsl.extendsFrom compileClasspath
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 1.2 프로젝트 구조 설계

#### 디렉토리 구조
```
hanihome-au-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── hanihome/
│   │   │           ├── api/                           # 메인 패키지 (헬스체크 등)
│   │   │           │   ├── HanihomeAuApiApplication.java
│   │   │           │   ├── config/
│   │   │           │   │   ├── QueryDslConfig.java
│   │   │           │   │   └── SecurityConfig.java
│   │   │           │   ├── controller/
│   │   │           │   │   └── HealthController.java
│   │   │           │   ├── entity/
│   │   │           │   │   └── DatabaseInfo.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── DatabaseInfoRepository.java
│   │   │           │   └── service/
│   │   │           │       └── DatabaseService.java
│   │   │           └── hanihome_au_api/              # 비즈니스 로직 패키지
│   │   │               ├── config/
│   │   │               │   ├── JpaConfig.java
│   │   │               │   ├── MenuConfiguration.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── MenuController.java
│   │   │               │   ├── PropertyController.java
│   │   │               │   └── UserManagementController.java
│   │   │               ├── domain/
│   │   │               │   ├── entity/
│   │   │               │   │   └── User.java
│   │   │               │   └── enums/
│   │   │               │       ├── OAuthProvider.java
│   │   │               │       ├── Permission.java
│   │   │               │       └── UserRole.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   └── RefreshTokenRequest.java
│   │   │               │   └── response/
│   │   │               │       ├── ApiResponse.java
│   │   │               │       ├── JwtAuthenticationResponse.java
│   │   │               │       └── MenuItemDto.java
│   │   │               ├── repository/
│   │   │               │   └── UserRepository.java
│   │   │               ├── security/
│   │   │               │   ├── CustomPermissionEvaluator.java
│   │   │               │   ├── SecurityExpressionHandler.java
│   │   │               │   ├── UserPrincipal.java
│   │   │               │   ├── jwt/
│   │   │               │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │               │   │   ├── JwtAuthenticationFilter.java
│   │   │               │   │   └── JwtTokenProvider.java
│   │   │               │   └── oauth2/
│   │   │               │       ├── CustomOAuth2UserService.java
│   │   │               │       ├── OAuth2AuthenticationFailureHandler.java
│   │   │               │       ├── OAuth2AuthenticationSuccessHandler.java
│   │   │               │       └── user/
│   │   │               │           ├── GoogleOAuth2UserInfo.java
│   │   │               │           ├── KakaoOAuth2UserInfo.java
│   │   │               │           ├── OAuth2UserInfo.java
│   │   │               │           └── OAuth2UserInfoFactory.java
│   │   │               └── service/
│   │   │                   └── UserService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__Initial_Schema.sql
│   │       │       └── V2__Create_users_table.sql
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/
│               └── hanihome/
│                   └── api/
│                       └── HanihomeAuApiApplicationTests.java
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── Dockerfile
└── HELP.md
```

### 1.3 메인 애플리케이션 클래스

#### HanihomeAuApiApplication.java
```java
package com.hanihome.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class HanihomeAuApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanihomeAuApiApplication.class, args);
    }
}
```

### 1.4 JPA 및 QueryDSL 설정

#### JpaConfig.java
```java
package com.hanihome.hanihome_au_api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.hanihome.hanihome_au_api.repository")
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaConfig {

    private final EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

#### QueryDslConfig.java
```java
package com.hanihome.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

---

## 2. PostgreSQL 데이터베이스 설정 및 연동

### 2.1 데이터베이스 설정

#### application.yml (데이터베이스 설정)
```yaml
spring:
  application:
    name: hanihome-au-api
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:hanihome_au}
    username: ${DB_USERNAME:hanihome_user}
    password: ${DB_PASSWORD:hanihome_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    open-in-view: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 2.2 데이터베이스 엔티티

#### User.java (사용자 엔티티)
```java
package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_oauth", columnList = "oauth_provider, oauth_id"),
    @Index(name = "idx_user_role", columnList = "role")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String name;
    
    private String profileImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;
    
    private String oauthId;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 비즈니스 메서드
    public void updateProfile(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
    
    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
}
```

#### DatabaseInfo.java (데이터베이스 정보 엔티티)
```java
package com.hanihome.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "database_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "database_name")
    private String databaseName;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "connection_status")
    private String connectionStatus;
}
```

### 2.3 Flyway 마이그레이션

#### V1__Initial_Schema.sql
```sql
-- Initial schema creation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create database_info table
CREATE TABLE database_info (
    id BIGSERIAL PRIMARY KEY,
    database_name VARCHAR(255),
    version VARCHAR(255),
    connection_status VARCHAR(50)
);

-- Insert initial data
INSERT INTO database_info (database_name, version, connection_status) 
VALUES ('hanihome_au', '1.0.0', 'active');

-- Create indexes
CREATE INDEX idx_database_info_name ON database_info(database_name);
```

#### V2__Create_users_table.sql
```sql
-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    profile_image_url VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    oauth_provider VARCHAR(50),
    oauth_id VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_oauth ON users(oauth_provider, oauth_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_active ON users(is_active);

-- Create unique constraint for OAuth users
CREATE UNIQUE INDEX idx_user_oauth_unique ON users(oauth_provider, oauth_id) 
WHERE oauth_provider IS NOT NULL AND oauth_id IS NOT NULL;

-- Add comments
COMMENT ON TABLE users IS 'User accounts and profile information';
COMMENT ON COLUMN users.role IS 'User role: TENANT, LANDLORD, AGENT, ADMIN';
COMMENT ON COLUMN users.oauth_provider IS 'OAuth provider: GOOGLE, KAKAO, APPLE';
```

### 2.4 Repository 구현

#### UserRepository.java
```java
package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByIsActiveTrue();
    
    List<User> findByIsActiveFalse();
    
    boolean existsByEmail(String email);
    
    boolean existsByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    List<User> findUsersCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.name LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
}
```

---

## 3. Docker 컨테이너화 설정

### 3.1 Dockerfile 구성

#### Dockerfile
```dockerfile
# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Install necessary tools
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (for better layer caching)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Production stage
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r hanihome && useradd -r -g hanihome hanihome

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Copy JAR file from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to hanihome user
RUN chown -R hanihome:hanihome /app

# Switch to non-root user
USER hanihome

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 3.2 docker-compose 설정

#### docker-compose.dev.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: hanihome-postgres-dev
    environment:
      POSTGRES_DB: hanihome_au
      POSTGRES_USER: hanihome_user
      POSTGRES_PASSWORD: hanihome_password
      PGDATA: /var/lib/postgresql/data/pgdata
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql:ro
    networks:
      - hanihome-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hanihome_user -d hanihome_au"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: hanihome-redis-dev
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - hanihome-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend/hanihome-au-api
      dockerfile: Dockerfile
    container_name: hanihome-backend-dev
    environment:
      SPRING_PROFILES_ACTIVE: development
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: hanihome_au
      DB_USERNAME: hanihome_user
      DB_PASSWORD: hanihome_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET:-dev-secret-key-change-in-production}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - hanihome-network
    volumes:
      - ./logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local

networks:
  hanihome-network:
    driver: bridge
```

---

## 4. AWS 기본 인프라 설정

### 4.1 AWS 리소스 구성

#### 주요 AWS 서비스
- **EC2**: 애플리케이션 서버 (Auto Scaling Group)
- **RDS**: PostgreSQL 관리형 데이터베이스
- **ElastiCache**: Redis 캐시 클러스터
- **ELB**: Application Load Balancer
- **S3**: 정적 파일 및 백업 저장소
- **ECR**: Docker 이미지 레지스트리
- **CloudWatch**: 모니터링 및 로깅
- **IAM**: 권한 관리

#### 환경별 인프라 설정
```yaml
# infrastructure/aws/development.yml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'HaniHome AU Development Infrastructure'

Parameters:
  Environment:
    Type: String
    Default: development
    AllowedValues: [development, staging, production]
  
  VpcCidr:
    Type: String
    Default: '10.0.0.0/16'
  
  DatabaseInstanceClass:
    Type: String
    Default: db.t3.micro
    AllowedValues: [db.t3.micro, db.t3.small, db.t3.medium]

Resources:
  # VPC Configuration
  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: !Ref VpcCidr
      EnableDnsHostnames: true
      EnableDnsSupport: true
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-hanihome-vpc'

  # RDS PostgreSQL
  DatabaseSubnetGroup:
    Type: AWS::RDS::DBSubnetGroup
    Properties:
      DBSubnetGroupDescription: 'Subnet group for HaniHome database'
      SubnetIds:
        - !Ref PrivateSubnet1
        - !Ref PrivateSubnet2
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-hanihome-db-subnet-group'

  Database:
    Type: AWS::RDS::DBInstance
    Properties:
      DBInstanceIdentifier: !Sub '${Environment}-hanihome-postgres'
      DBInstanceClass: !Ref DatabaseInstanceClass
      Engine: postgres
      EngineVersion: '15.4'
      MasterUsername: hanihome_user
      MasterUserPassword: !Ref DatabasePassword
      DBName: hanihome_au
      AllocatedStorage: 20
      StorageType: gp2
      StorageEncrypted: true
      VpcSecurityGroups:
        - !Ref DatabaseSecurityGroup
      DBSubnetGroupName: !Ref DatabaseSubnetGroup
      BackupRetentionPeriod: 7
      MultiAZ: false
      PubliclyAccessible: false
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-hanihome-postgres'

  # ElastiCache Redis
  RedisSubnetGroup:
    Type: AWS::ElastiCache::SubnetGroup
    Properties:
      Description: 'Subnet group for HaniHome Redis'
      SubnetIds:
        - !Ref PrivateSubnet1
        - !Ref PrivateSubnet2

  RedisCluster:
    Type: AWS::ElastiCache::CacheCluster
    Properties:
      ClusterName: !Sub '${Environment}-hanihome-redis'
      CacheNodeType: cache.t3.micro
      Engine: redis
      NumCacheNodes: 1
      CacheSubnetGroupName: !Ref RedisSubnetGroup
      VpcSecurityGroupIds:
        - !Ref RedisSecurityGroup
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-hanihome-redis'
```

### 4.2 보안 그룹 설정

#### 데이터베이스 보안 그룹
```yaml
DatabaseSecurityGroup:
  Type: AWS::EC2::SecurityGroup
  Properties:
    GroupDescription: 'Security group for HaniHome database'
    VpcId: !Ref VPC
    SecurityGroupIngress:
      - IpProtocol: tcp
        FromPort: 5432
        ToPort: 5432
        SourceSecurityGroupId: !Ref ApplicationSecurityGroup
        Description: 'PostgreSQL access from application servers'
    Tags:
      - Key: Name
        Value: !Sub '${Environment}-hanihome-db-sg'

ApplicationSecurityGroup:
  Type: AWS::EC2::SecurityGroup
  Properties:
    GroupDescription: 'Security group for HaniHome application servers'
    VpcId: !Ref VPC
    SecurityGroupIngress:
      - IpProtocol: tcp
        FromPort: 8080
        ToPort: 8080
        SourceSecurityGroupId: !Ref LoadBalancerSecurityGroup
        Description: 'HTTP access from load balancer'
      - IpProtocol: tcp
        FromPort: 22
        ToPort: 22
        CidrIp: 10.0.0.0/16
        Description: 'SSH access from VPC'
    SecurityGroupEgress:
      - IpProtocol: -1
        CidrIp: 0.0.0.0/0
        Description: 'All outbound traffic'
    Tags:
      - Key: Name
        Value: !Sub '${Environment}-hanihome-app-sg'
```

---

## 5. CI/CD 파이프라인 구현

### 5.1 GitHub Actions 워크플로우

#### .github/workflows/backend-ci-cd.yml
```yaml
name: Backend CI/CD Pipeline

on:
  push:
    branches: [main, develop]
    paths: ['backend/**']
  pull_request:
    branches: [main]
    paths: ['backend/**']

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: hanihome-au/backend

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_password
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
      
      redis:
        image: redis:7
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Make gradlew executable
        run: chmod +x ./backend/hanihome-au-api/gradlew

      - name: Run tests
        working-directory: ./backend/hanihome-au-api
        env:
          SPRING_PROFILES_ACTIVE: test
          DB_HOST: localhost
          DB_PORT: 5432
          DB_NAME: test_db
          DB_USERNAME: test_user
          DB_PASSWORD: test_password
          REDIS_HOST: localhost
          REDIS_PORT: 6379
        run: ./gradlew test

      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Backend Test Results
          path: backend/hanihome-au-api/build/test-results/test/*.xml
          reporter: java-junit

      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          file: ./backend/hanihome-au-api/build/reports/jacoco/test/jacocoTestReport.xml
          flags: backend

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.event_name == 'push'

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=sha,prefix={{branch}}-

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./backend/hanihome-au-api
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy-staging:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: staging

    steps:
      - name: Deploy to staging
        run: |
          echo "Deploying to staging environment"
          # AWS ECS deployment commands here

  deploy-production:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production

    steps:
      - name: Deploy to production
        run: |
          echo "Deploying to production environment"
          # AWS ECS deployment commands here
```

### 5.2 배포 스크립트

#### scripts/deploy.sh
```bash
#!/bin/bash

set -e

# Configuration
ENVIRONMENT=${1:-staging}
IMAGE_TAG=${2:-latest}
AWS_REGION=${AWS_REGION:-ap-southeast-2}
ECR_REPOSITORY=${ECR_REPOSITORY:-hanihome-au/backend}
ECS_CLUSTER=${ECS_CLUSTER:-hanihome-${ENVIRONMENT}}
ECS_SERVICE=${ECS_SERVICE:-hanihome-backend-${ENVIRONMENT}}

echo "Deploying to ${ENVIRONMENT} environment"
echo "Image tag: ${IMAGE_TAG}"

# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPOSITORY}

# Update ECS service
aws ecs update-service \
    --cluster ${ECS_CLUSTER} \
    --service ${ECS_SERVICE} \
    --force-new-deployment \
    --region ${AWS_REGION}

# Wait for deployment to complete
echo "Waiting for deployment to complete..."
aws ecs wait services-stable \
    --cluster ${ECS_CLUSTER} \
    --services ${ECS_SERVICE} \
    --region ${AWS_REGION}

echo "Deployment completed successfully!"

# Health check
echo "Performing health check..."
LOAD_BALANCER_URL=$(aws elbv2 describe-load-balancers \
    --names "hanihome-${ENVIRONMENT}-alb" \
    --query 'LoadBalancers[0].DNSName' \
    --output text \
    --region ${AWS_REGION})

for i in {1..30}; do
    if curl -f "http://${LOAD_BALANCER_URL}/actuator/health"; then
        echo "Health check passed!"
        break
    else
        echo "Health check failed, retrying in 10 seconds..."
        sleep 10
    fi
done
```

---

## 6. 보안 및 비밀 관리

### 6.1 Spring Security 설정

#### SecurityConfig.java
```java
package com.hanihome.hanihome_au_api.config;

import com.hanihome.hanihome_au_api.security.CustomPermissionEvaluator;
import com.hanihome.hanihome_au_api.security.jwt.JwtAuthenticationEntryPoint;
import com.hanihome.hanihome_au_api.security.jwt.JwtAuthenticationFilter;
import com.hanihome.hanihome_au_api.security.oauth2.CustomOAuth2UserService;
import com.hanihome.hanihome_au_api.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.hanihome.hanihome_au_api.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomPermissionEvaluator customPermissionEvaluator;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", 
                    "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                .requestMatchers("/api/v1/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                
                // Role-based access control
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/agent/**").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/v1/landlord/**").hasAnyRole("LANDLORD", "AGENT", "ADMIN")
                .requestMatchers("/api/v1/tenant/**").hasAnyRole("TENANT", "LANDLORD", "AGENT", "ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/v1/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/v1/oauth2/callback/*"))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );

        // Add JWT authentication filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configure allowed origins (should be configured via properties)
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:3001", 
            "https://*.hanihome.com.au"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", 
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
```

### 6.2 JWT 토큰 관리

#### JwtTokenProvider.java
```java
package com.hanihome.hanihome_au_api.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration:3600000}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration:86400000}") long refreshTokenExpiration,
            RedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.redisTemplate = redisTemplate;
    }

    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("type", "ACCESS")
                .setIssuedAt(now)
                .setExpirationTime(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpirationTime(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Store refresh token in Redis
        String redisKey = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public String getTokenTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                String blacklistKey = "blacklist:" + token;
                redisTemplate.opsForValue().set(blacklistKey, "true", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (JwtException e) {
            log.error("Error blacklisting token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    public boolean validateRefreshToken(String refreshToken, Long userId) {
        try {
            if (!validateToken(refreshToken)) {
                return false;
            }

            String tokenType = getTokenTypeFromToken(refreshToken);
            if (!"REFRESH".equals(tokenType)) {
                return false;
            }

            Long tokenUserId = getUserIdFromToken(refreshToken);
            if (!userId.equals(tokenUserId)) {
                return false;
            }

            // Check if refresh token exists in Redis
            String redisKey = "refresh_token:" + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);
            return refreshToken.equals(storedToken);

        } catch (Exception e) {
            log.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    public void deleteRefreshToken(Long userId) {
        String redisKey = "refresh_token:" + userId;
        redisTemplate.delete(redisKey);
    }
}
```

### 6.3 환경 변수 보안 관리

#### application.yml (보안 설정)
```yaml
# Security Configuration
security:
  jwt:
    secret: ${JWT_SECRET:default-secret-key-change-in-production}
    access-token:
      expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000} # 1 hour
    refresh-token:
      expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:86400000} # 24 hours

  oauth2:
    google:
      client-id: ${GOOGLE_CLIENT_ID:}
      client-secret: ${GOOGLE_CLIENT_SECRET:}
    kakao:
      client-id: ${KAKAO_CLIENT_ID:}
      client-secret: ${KAKAO_CLIENT_SECRET:}

# Actuator Security
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      roles: ADMIN
    info:
      enabled: true
  health:
    redis:
      enabled: true
    db:
      enabled: true
  security:
    enabled: true

# Logging Configuration
logging:
  level:
    com.hanihome: ${LOG_LEVEL:INFO}
    org.springframework.security: DEBUG
    org.hibernate.SQL: ${SQL_LOG_LEVEL:WARN}
    org.hibernate.type.descriptor.sql.BasicBinder: ${SQL_PARAM_LOG_LEVEL:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE_PATH:./logs/hanihome-au-api.log}
    max-size: 10MB
    max-history: 30
```

---

## 7. 성능 최적화

### 7.1 데이터베이스 최적화

#### 연결 풀 최적화
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${DB_POOL_MAX_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      idle-timeout: ${DB_POOL_IDLE_TIMEOUT:300000}
      connection-timeout: ${DB_POOL_CONNECTION_TIMEOUT:20000}
      max-lifetime: ${DB_POOL_MAX_LIFETIME:1800000}
      leak-detection-threshold: ${DB_POOL_LEAK_DETECTION:60000}
      
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: ${HIBERNATE_BATCH_SIZE:20}
          fetch_size: ${HIBERNATE_FETCH_SIZE:50}
        cache:
          use_second_level_cache: ${HIBERNATE_L2_CACHE:true}
          use_query_cache: ${HIBERNATE_QUERY_CACHE:true}
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
        generate_statistics: ${HIBERNATE_STATS:false}
```

#### 인덱스 전략
```sql
-- 자주 사용되는 쿼리에 대한 인덱스
CREATE INDEX CONCURRENTLY idx_users_role_active ON users(role, is_active);
CREATE INDEX CONCURRENTLY idx_users_oauth_lookup ON users(oauth_provider, oauth_id) WHERE oauth_provider IS NOT NULL;
CREATE INDEX CONCURRENTLY idx_users_email_lower ON users(LOWER(email));
CREATE INDEX CONCURRENTLY idx_users_created_at_desc ON users(created_at DESC);

-- 복합 인덱스 for common queries
CREATE INDEX CONCURRENTLY idx_users_search ON users USING gin(to_tsvector('english', name || ' ' || email));
```

### 7.2 캐싱 전략

#### RedisConfig.java
```java
package com.hanihome.hanihome_au_api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for both keys and values
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Different TTL for different cache types
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("properties", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("menu", defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurations.put("features", defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

### 7.3 API 응답 최적화

#### ApiResponse 압축
```java
@RestController
@RequestMapping("/api/v1")
public class BaseController {
    
    @GetMapping(value = "/large-data", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Object>>> getLargeData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // Enable compression for large responses
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Encoding", "gzip");
        
        List<Object> data = fetchLargeData(page, size);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("Data retrieved successfully", data));
    }
}
```

---

## 8. 배포 자동화

### 8.1 ECS 태스크 정의

#### ecs-task-definition.json
```json
{
  "family": "hanihome-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "hanihome-backend",
      "image": "ACCOUNT.dkr.ecr.REGION.amazonaws.com/hanihome-au/backend:latest",
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
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:hanihome/db-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT:secret:hanihome/jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/hanihome-backend",
          "awslogs-region": "ap-southeast-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

### 8.2 Terraform 인프라 코드

#### main.tf
```hcl
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "hanihome-terraform-state"
    key    = "infrastructure/terraform.tfstate"
    region = "ap-southeast-2"
  }
}

provider "aws" {
  region = var.aws_region
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "${var.environment}-hanihome-cluster"
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
  
  tags = {
    Environment = var.environment
    Project     = "hanihome-au"
  }
}

# ECS Service
resource "aws_ecs_service" "backend" {
  name            = "${var.environment}-hanihome-backend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = var.backend_desired_count
  launch_type     = "FARGATE"
  
  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.backend.id]
    assign_public_ip = false
  }
  
  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "hanihome-backend"
    container_port   = 8080
  }
  
  depends_on = [aws_lb_listener.backend]
  
  tags = {
    Environment = var.environment
    Project     = "hanihome-au"
  }
}

# Auto Scaling
resource "aws_appautoscaling_target" "backend" {
  max_capacity       = var.backend_max_capacity
  min_capacity       = var.backend_min_capacity
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.backend.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "backend_cpu" {
  name               = "${var.environment}-hanihome-backend-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.backend.resource_id
  scalable_dimension = aws_appautoscaling_target.backend.scalable_dimension
  service_namespace  = aws_appautoscaling_target.backend.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 70.0
  }
}
```

---

## 결론 및 다음 단계

### 완료된 작업 요약
1. ✅ **Spring Boot 3.x 프로젝트 초기 설정**: 최신 Java 17, Gradle 빌드 시스템
2. ✅ **PostgreSQL 데이터베이스 연동**: Flyway 마이그레이션, JPA/QueryDSL 설정
3. ✅ **Docker 컨테이너화**: 멀티스테이지 빌드, 보안 최적화
4. ✅ **AWS 인프라 설정**: ECS, RDS, ElastiCache, Load Balancer 구성
5. ✅ **CI/CD 파이프라인**: GitHub Actions, 자동 테스트/빌드/배포
6. ✅ **보안 구현**: Spring Security, JWT, OAuth2, 권한 기반 접근 제어
7. ✅ **성능 최적화**: 연결 풀, 캐싱, 인덱스 전략
8. ✅ **환경 설정 관리**: 개발/스테이징/운영 환경 분리

### 아키텍처 특징
- **마이크로서비스 준비**: 모듈화된 패키지 구조
- **클라우드 네이티브**: AWS 기반 완전 관리형 서비스 활용
- **보안 중심**: 다층 보안 구조 및 권한 기반 접근 제어
- **확장성**: Auto Scaling, 로드 밸런싱 지원
- **관찰성**: 종합적인 모니터링 및 로깅 시스템

### 다음 문서 연계
- **frontend-documentation-3.md**: 프론트엔드 초기 설정 및 통합
- **security-documentation**: 보안 아키텍처 및 인증 시스템 상세
- **monitoring-documentation**: APM, 로깅, 알림 시스템 구축

### 운영 가이드라인
1. **모니터링**: CloudWatch, APM 도구를 통한 24/7 모니터링
2. **보안**: 정기적인 보안 취약점 스캔 및 패치 관리
3. **백업**: 자동화된 데이터베이스 백업 및 복구 절차
4. **성능**: 정기적인 성능 튜닝 및 최적화
5. **배포**: 무중단 배포 및 롤백 전략

이 문서는 HaniHome AU 백엔드 시스템의 견고한 기반을 제공하며, 향후 비즈니스 요구사항 변화에 따른 확장과 개선을 위한 준비를 완료했습니다.