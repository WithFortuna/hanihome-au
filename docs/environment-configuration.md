# HaniHome AU - Environment Configuration Guide

## Overview

This document outlines the environment configuration strategy for HaniHome AU, including environment variables, configuration files, and secrets management across development, staging, and production environments.

## Table of Contents

1. [Environment Structure](#environment-structure)
2. [Configuration Files](#configuration-files)
3. [Environment Variables](#environment-variables)
4. [Secrets Management](#secrets-management)
5. [Security Best Practices](#security-best-practices)
6. [Deployment Configuration](#deployment-configuration)
7. [Troubleshooting](#troubleshooting)

## Environment Structure

### Development Environment
- **Purpose**: Local development and testing
- **Database**: Local PostgreSQL instance
- **Security**: Relaxed settings for debugging
- **Logging**: Verbose logging enabled
- **Features**: All debugging tools enabled

### Staging Environment
- **Purpose**: Pre-production testing and QA
- **Database**: AWS RDS PostgreSQL (staging instance)
- **Security**: Production-like security settings
- **Logging**: Info level logging
- **Features**: Production-like configuration

### Production Environment
- **Purpose**: Live application serving real users
- **Database**: AWS RDS PostgreSQL (production instance)
- **Security**: Maximum security settings
- **Logging**: Error/Warning level logging
- **Features**: Optimized for performance and security

## Configuration Files

### Backend Configuration (Spring Boot)

#### Base Configuration
- **File**: `backend/hanihome-au-api/src/main/resources/application.yml`
- **Purpose**: Base configuration with environment variable placeholders
- **Profiles**: Development, Staging, Production

#### Key Configuration Sections

##### Database Configuration
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/hanihome_au_dev}
    username: ${DATABASE_USERNAME:hanihome_user}
    password: ${DATABASE_PASSWORD:hanihome_password}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
```

##### Security Configuration
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
```

##### AWS Integration
```yaml
aws:
  region: ${AWS_REGION:ap-southeast-2}
  s3:
    bucket-assets: ${AWS_S3_BUCKET_ASSETS:hanihome-au-assets-dev}
```

### Frontend Configuration (Next.js)

#### Environment Files
- `.env.local.example` - Template for local development
- `.env.development` - Development environment
- `.env.staging` - Staging environment  
- `.env.production` - Production environment

#### Key Configuration Sections

##### API Configuration
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_API_TIMEOUT=30000
```

##### Authentication
```bash
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your-nextauth-secret
```

##### External Services
```bash
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your-google-client-id
NEXT_PUBLIC_GA_ID=your-analytics-id
```

## Environment Variables

### Required Environment Variables

#### Database Configuration
- `DATABASE_URL` - Full database connection string
- `DATABASE_HOST` - Database host
- `DATABASE_PORT` - Database port (default: 5432)
- `DATABASE_NAME` - Database name
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password

#### Authentication & Security
- `JWT_SECRET` - JWT signing secret (minimum 256 bits)
- `JWT_EXPIRATION` - JWT token expiration time
- `NEXTAUTH_SECRET` - NextAuth.js secret
- `NEXTAUTH_JWT_SECRET` - NextAuth.js JWT secret

#### OAuth Configuration
- `GOOGLE_CLIENT_ID` - Google OAuth client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth client secret
- `KAKAO_CLIENT_ID` - Kakao OAuth client ID
- `KAKAO_CLIENT_SECRET` - Kakao OAuth client secret

#### AWS Configuration
- `AWS_REGION` - AWS region (default: ap-southeast-2)
- `AWS_ACCESS_KEY_ID` - AWS access key
- `AWS_SECRET_ACCESS_KEY` - AWS secret key
- `AWS_S3_BUCKET_ASSETS` - S3 bucket for assets
- `AWS_S3_BUCKET_BACKUPS` - S3 bucket for backups
- `AWS_S3_BUCKET_LOGS` - S3 bucket for logs

#### Optional Environment Variables

#### Redis Configuration
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_PASSWORD` - Redis password
- `REDIS_DATABASE` - Redis database number (default: 0)

#### Email Configuration
- `MAIL_HOST` - SMTP host
- `MAIL_PORT` - SMTP port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `MAIL_FROM` - Default sender email

#### Monitoring & Analytics
- `SENTRY_DSN` - Sentry error tracking DSN
- `NEXT_PUBLIC_GA_ID` - Google Analytics tracking ID

## Secrets Management

### AWS Secrets Manager Integration

#### Secret Structure
Secrets are organized by environment with the following naming convention:
- `hanihome-au-development` - Development secrets
- `hanihome-au-staging` - Staging secrets
- `hanihome-au-production` - Production secrets
- `hanihome-au-db-{environment}` - Database credentials

#### Setting Up Secrets
1. Run the setup script:
```bash
./scripts/setup-secrets.sh development
./scripts/setup-secrets.sh staging
./scripts/setup-secrets.sh production
```

2. Update placeholder values in AWS Secrets Manager console

3. Retrieve secrets for deployment:
```bash
./scripts/get-secrets.sh production
```

#### Secret Rotation
- JWT secrets should be rotated quarterly
- OAuth secrets should be rotated when compromised
- Database passwords should be rotated monthly
- Use AWS Secrets Manager automatic rotation where possible

## Security Best Practices

### Development Environment
- Use non-production OAuth applications
- Use local or development-specific API keys
- Enable debug logging for troubleshooting
- Use relaxed CORS settings for local development

### Staging Environment
- Use production-like security settings
- Use staging-specific OAuth applications
- Enable error tracking and monitoring
- Restrict access to staging resources

### Production Environment
- Use strong, unique secrets for all services
- Enable all security headers and HTTPS
- Restrict CORS to production domains only
- Use minimal logging to avoid sensitive data exposure
- Enable comprehensive monitoring and alerting

### Environment Variable Security
1. **Never commit secrets to version control**
2. **Use environment-specific .env files**
3. **Use AWS Secrets Manager for sensitive data**
4. **Implement least-privilege IAM policies**
5. **Regularly audit and rotate secrets**

## Deployment Configuration

### Docker Configuration
Environment variables are injected into Docker containers through:
- Environment files during build
- AWS Secrets Manager during runtime
- Environment variables passed to container

### GitHub Actions Integration
Secrets are configured in GitHub repository settings:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `DOCKER_HUB_USERNAME`
- `DOCKER_HUB_TOKEN`

### Environment-Specific Deployment
Each environment uses its own:
- AWS resources (RDS, S3, etc.)
- Domain names and SSL certificates
- OAuth application configurations
- Monitoring and logging configurations

## Configuration Validation

### Environment Validation Script
Create a validation script to ensure all required environment variables are set:

```bash
#!/bin/bash
# validate-env.sh

REQUIRED_VARS=(
    "DATABASE_URL"
    "JWT_SECRET"
    "NEXTAUTH_SECRET"
    "AWS_REGION"
)

for var in "${REQUIRED_VARS[@]}"; do
    if [[ -z "${!var}" ]]; then
        echo "Error: Required environment variable $var is not set"
        exit 1
    fi
done

echo "All required environment variables are set"
```

### Configuration Testing
- Unit tests should mock external dependencies
- Integration tests should use test-specific configurations
- End-to-end tests should use staging environment

## Troubleshooting

### Common Issues

#### Database Connection Issues
1. Check `DATABASE_URL` format
2. Verify database credentials
3. Ensure database is accessible from application
4. Check security group and firewall rules

#### OAuth Authentication Issues
1. Verify OAuth client IDs and secrets
2. Check redirect URIs configuration
3. Ensure OAuth applications are configured for correct environment
4. Verify CORS settings

#### AWS Integration Issues
1. Check AWS credentials and permissions
2. Verify S3 bucket names and regions
3. Ensure IAM roles have required permissions
4. Check AWS service availability

#### Environment Variable Issues
1. Verify environment file is loaded correctly
2. Check for typos in variable names
3. Ensure variables are properly escaped
4. Verify environment-specific configurations

### Debugging Tips
1. Use environment-specific logging levels
2. Enable debug mode in development
3. Check application health endpoints
4. Monitor AWS CloudWatch logs
5. Use AWS X-Ray for distributed tracing

### Health Checks
Implement health check endpoints that verify:
- Database connectivity
- Redis connectivity
- AWS service accessibility
- Required environment variables presence

## Environment Migration

### Moving Between Environments
1. Export configuration from source environment
2. Update environment-specific values
3. Import configuration to target environment
4. Validate configuration
5. Test application functionality

### Configuration Backup
1. Regular backup of environment configurations
2. Version control for configuration templates
3. Document configuration changes
4. Maintain configuration change log

## Conclusion

This configuration management strategy ensures:
- **Security**: Sensitive data is properly protected
- **Flexibility**: Easy environment switching and deployment
- **Maintainability**: Clear separation of concerns
- **Scalability**: Configuration can grow with application needs
- **Reliability**: Robust error handling and validation

Follow this guide to maintain consistent, secure, and reliable application deployments across all environments.