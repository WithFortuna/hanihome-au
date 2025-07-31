#!/bin/bash

# HaniHome AU - Configuration Validation Script
# This script validates environment configuration for different environments

set -e

# Configuration
ENVIRONMENT=${1:-"development"}
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}  HaniHome AU - Configuration Validation${NC}"
    echo -e "${BLUE}  Environment: ${ENVIRONMENT}${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Validation functions
validate_required_vars() {
    local env_file=$1
    local errors=0
    
    print_step "Validating required environment variables..."
    
    # Required variables for all environments
    local required_vars=(
        "DATABASE_URL"
        "DATABASE_USERNAME"
        "DATABASE_PASSWORD"
        "JWT_SECRET"
        "NEXTAUTH_SECRET"
        "AWS_REGION"
    )
    
    # Additional required variables for staging/production
    if [[ "$ENVIRONMENT" != "development" ]]; then
        required_vars+=(
            "GOOGLE_CLIENT_ID"
            "GOOGLE_CLIENT_SECRET"
            "KAKAO_CLIENT_ID"
            "KAKAO_CLIENT_SECRET"
            "AWS_S3_BUCKET_ASSETS"
            "SENTRY_DSN"
        )
    fi
    
    # Source environment file if it exists
    if [[ -f "$env_file" ]]; then
        source "$env_file"
    else
        print_error "Environment file '$env_file' not found"
        return 1
    fi
    
    # Check each required variable
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            print_error "Required environment variable '$var' is not set or empty"
            ((errors++))
        elif [[ "${!var}" == *"REPLACE-WITH"* ]] || [[ "${!var}" == *"your-"* ]]; then
            print_warning "Environment variable '$var' contains placeholder value: ${!var}"
            ((errors++))
        else
            echo "  ✓ $var is set"
        fi
    done
    
    return $errors
}

validate_jwt_secret() {
    print_step "Validating JWT secret strength..."
    
    if [[ -z "$JWT_SECRET" ]]; then
        print_error "JWT_SECRET is not set"
        return 1
    fi
    
    # Check minimum length (32 characters for 256-bit security)
    if [[ ${#JWT_SECRET} -lt 32 ]]; then
        print_error "JWT_SECRET is too short (${#JWT_SECRET} characters). Minimum 32 characters required."
        return 1
    fi
    
    # Check for development-only secrets in production
    if [[ "$ENVIRONMENT" == "production" ]] && [[ "$JWT_SECRET" == *"dev"* ]]; then
        print_error "JWT_SECRET appears to be a development secret in production environment"
        return 1
    fi
    
    print_success "JWT secret validation passed"
    return 0
}

validate_database_config() {
    print_step "Validating database configuration..."
    
    if [[ -z "$DATABASE_URL" ]]; then
        print_error "DATABASE_URL is not set"
        return 1
    fi
    
    # Parse database URL
    if [[ "$DATABASE_URL" =~ postgresql://([^:]+):([^@]+)@([^:]+):([0-9]+)/(.+) ]]; then
        local db_user="${BASH_REMATCH[1]}"
        local db_pass="${BASH_REMATCH[2]}"
        local db_host="${BASH_REMATCH[3]}"
        local db_port="${BASH_REMATCH[4]}"
        local db_name="${BASH_REMATCH[5]}"
        
        echo "  ✓ Database URL format is valid"
        echo "    Host: $db_host:$db_port"
        echo "    Database: $db_name"
        echo "    User: $db_user"
        
        # Check for weak passwords in production
        if [[ "$ENVIRONMENT" == "production" ]] && [[ ${#db_pass} -lt 12 ]]; then
            print_warning "Database password is short for production environment"
        fi
        
    else
        print_error "DATABASE_URL format is invalid. Expected: postgresql://user:pass@host:port/dbname"
        return 1
    fi
    
    return 0
}

validate_aws_config() {
    print_step "Validating AWS configuration..."
    
    local errors=0
    
    # Check AWS region
    if [[ -z "$AWS_REGION" ]]; then
        print_error "AWS_REGION is not set"
        ((errors++))
    elif [[ "$AWS_REGION" != "ap-southeast-2" ]]; then
        print_warning "AWS_REGION is '$AWS_REGION', expected 'ap-southeast-2'"
    fi
    
    # Check S3 buckets for non-development environments
    if [[ "$ENVIRONMENT" != "development" ]]; then
        local s3_buckets=("AWS_S3_BUCKET_ASSETS" "AWS_S3_BUCKET_BACKUPS" "AWS_S3_BUCKET_LOGS")
        
        for bucket_var in "${s3_buckets[@]}"; do
            if [[ -z "${!bucket_var}" ]]; then
                print_error "$bucket_var is not set"
                ((errors++))
            elif [[ "${!bucket_var}" != *"$ENVIRONMENT"* ]]; then
                print_warning "$bucket_var (${!bucket_var}) doesn't contain environment name"
            fi
        done
    fi
    
    if [[ $errors -eq 0 ]]; then
        print_success "AWS configuration validation passed"
    fi
    
    return $errors
}

validate_oauth_config() {
    print_step "Validating OAuth configuration..."
    
    local errors=0
    
    # Check Google OAuth
    if [[ -n "$GOOGLE_CLIENT_ID" ]] && [[ -n "$GOOGLE_CLIENT_SECRET" ]]; then
        if [[ "$GOOGLE_CLIENT_ID" == *".apps.googleusercontent.com"* ]]; then
            echo "  ✓ Google OAuth configuration appears valid"
        else
            print_warning "Google Client ID format may be invalid"
        fi
    elif [[ "$ENVIRONMENT" != "development" ]]; then
        print_error "Google OAuth credentials are required for $ENVIRONMENT environment"
        ((errors++))
    fi
    
    # Check Kakao OAuth
    if [[ -n "$KAKAO_CLIENT_ID" ]] && [[ -n "$KAKAO_CLIENT_SECRET" ]]; then
        echo "  ✓ Kakao OAuth configuration is set"
    elif [[ "$ENVIRONMENT" != "development" ]]; then
        print_error "Kakao OAuth credentials are required for $ENVIRONMENT environment"
        ((errors++))
    fi
    
    return $errors
}

validate_frontend_config() {
    print_step "Validating frontend configuration..."
    
    local frontend_env_file="$PROJECT_ROOT/frontend/hanihome-au/.env.$ENVIRONMENT"
    local errors=0
    
    if [[ ! -f "$frontend_env_file" ]]; then
        print_error "Frontend environment file not found: $frontend_env_file"
        return 1
    fi
    
    # Source frontend environment file
    source "$frontend_env_file"
    
    # Check required frontend variables
    local frontend_vars=("NEXT_PUBLIC_API_URL" "NEXT_PUBLIC_APP_URL" "NEXTAUTH_URL")
    
    for var in "${frontend_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            print_error "Frontend variable '$var' is not set"
            ((errors++))
        fi
    done
    
    # Validate URL formats
    if [[ -n "$NEXT_PUBLIC_API_URL" ]]; then
        if [[ "$NEXT_PUBLIC_API_URL" =~ ^https?://[^/]+/api/v1$ ]]; then
            echo "  ✓ NEXT_PUBLIC_API_URL format is valid"
        else
            print_error "NEXT_PUBLIC_API_URL format is invalid: $NEXT_PUBLIC_API_URL"
            ((errors++))
        fi
    fi
    
    # Check HTTPS enforcement for production
    if [[ "$ENVIRONMENT" == "production" ]]; then
        if [[ "$NEXT_PUBLIC_APP_URL" != "https://"* ]]; then
            print_error "NEXT_PUBLIC_APP_URL must use HTTPS in production"
            ((errors++))
        fi
        
        if [[ "$NEXT_PUBLIC_FORCE_HTTPS" != "true" ]]; then
            print_warning "NEXT_PUBLIC_FORCE_HTTPS should be 'true' in production"
        fi
    fi
    
    return $errors
}

validate_spring_config() {
    print_step "Validating Spring Boot configuration..."
    
    local spring_config_file="$PROJECT_ROOT/backend/hanihome-au-api/src/main/resources/application.yml"
    
    if [[ ! -f "$spring_config_file" ]]; then
        print_error "Spring configuration file not found: $spring_config_file"
        return 1
    fi
    
    # Check if environment profile exists
    if grep -q "on-profile: $ENVIRONMENT" "$spring_config_file"; then
        echo "  ✓ Spring profile '$ENVIRONMENT' is configured"
    elif [[ "$ENVIRONMENT" == "development" ]] && grep -q "on-profile: development" "$spring_config_file"; then
        echo "  ✓ Spring profile 'development' is configured"
    else
        print_warning "Spring profile '$ENVIRONMENT' not found in application.yml"
    fi
    
    return 0
}

validate_docker_config() {
    print_step "Validating Docker configuration..."
    
    local docker_compose_file="$PROJECT_ROOT/docker-compose.$ENVIRONMENT.yml"
    
    if [[ "$ENVIRONMENT" == "development" ]]; then
        docker_compose_file="$PROJECT_ROOT/docker-compose.dev.yml"
    fi
    
    if [[ ! -f "$docker_compose_file" ]]; then
        print_warning "Docker Compose file not found: $docker_compose_file"
        return 1
    fi
    
    echo "  ✓ Docker Compose file exists: $docker_compose_file"
    
    # Check if required services are defined
    local required_services=("backend" "frontend")
    
    for service in "${required_services[@]}"; do
        if grep -q "^  $service:" "$docker_compose_file"; then
            echo "  ✓ Service '$service' is defined"
        else
            print_error "Service '$service' is not defined in Docker Compose file"
            return 1
        fi
    done
    
    return 0
}

validate_security_settings() {
    print_step "Validating security settings..."
    
    local errors=0
    
    # Check for development secrets in production
    if [[ "$ENVIRONMENT" == "production" ]]; then
        local dev_indicators=("dev" "development" "test" "demo" "example")
        
        for indicator in "${dev_indicators[@]}"; do
            if [[ "$JWT_SECRET" == *"$indicator"* ]]; then
                print_error "JWT_SECRET contains development indicator '$indicator' in production"
                ((errors++))
            fi
            
            if [[ "$NEXTAUTH_SECRET" == *"$indicator"* ]]; then
                print_error "NEXTAUTH_SECRET contains development indicator '$indicator' in production"
                ((errors++))
            fi
        done
        
        # Check for HTTPS enforcement
        if [[ "$NEXT_PUBLIC_FORCE_HTTPS" != "true" ]]; then
            print_error "HTTPS enforcement is not enabled in production"
            ((errors++))
        fi
        
        # Check for debug mode disabled
        if [[ "$NEXT_PUBLIC_DEBUG_MODE" == "true" ]]; then
            print_error "Debug mode is enabled in production"
            ((errors++))
        fi
    fi
    
    if [[ $errors -eq 0 ]]; then
        print_success "Security settings validation passed"
    fi
    
    return $errors
}

# Test database connectivity (optional)
test_database_connection() {
    print_step "Testing database connection (optional)..."
    
    if command -v psql &> /dev/null && [[ -n "$DATABASE_URL" ]]; then
        if timeout 10 psql "$DATABASE_URL" -c "SELECT 1;" &> /dev/null; then
            print_success "Database connection test passed"
        else
            print_warning "Database connection test failed (this may be expected if DB is not accessible)"
        fi
    else
        print_warning "Skipping database connection test (psql not available or DATABASE_URL not set)"
    fi
}

# Main validation function
run_validation() {
    local env_file="$PROJECT_ROOT/.env.$ENVIRONMENT"
    local total_errors=0
    
    print_header
    
    # Validate environment parameter
    if [[ ! "$ENVIRONMENT" =~ ^(development|staging|production)$ ]]; then
        print_error "Invalid environment: $ENVIRONMENT. Must be one of: development, staging, production"
        exit 1
    fi
    
    # Run all validations
    validate_required_vars "$env_file" || ((total_errors++))
    validate_jwt_secret || ((total_errors++))
    validate_database_config || ((total_errors++))
    validate_aws_config || ((total_errors++))
    validate_oauth_config || ((total_errors++))
    validate_frontend_config || ((total_errors++))
    validate_spring_config || ((total_errors++))
    validate_docker_config || ((total_errors++))
    validate_security_settings || ((total_errors++))
    
    # Optional tests
    test_database_connection
    
    # Summary
    echo ""
    echo -e "${BLUE}================================================${NC}"
    if [[ $total_errors -eq 0 ]]; then
        print_success "All validations passed for $ENVIRONMENT environment!"
        echo -e "${GREEN}Configuration is ready for deployment.${NC}"
        exit 0
    else
        print_error "$total_errors validation(s) failed for $ENVIRONMENT environment"
        echo -e "${RED}Please fix the issues before deploying.${NC}"
        exit 1
    fi
}

# Usage information
usage() {
    echo "Usage: $0 <environment>"
    echo "Environments: development, staging, production"
    echo ""
    echo "Examples:"
    echo "  $0 development"
    echo "  $0 staging"
    echo "  $0 production"
}

# Main execution
if [[ $# -eq 0 ]]; then
    usage
    exit 1
fi

run_validation