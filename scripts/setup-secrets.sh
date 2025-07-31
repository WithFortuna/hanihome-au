#!/bin/bash

# HaniHome AU - AWS Secrets Manager Setup Script
# This script sets up secrets in AWS Secrets Manager for different environments

set -e

# Configuration
PROJECT_NAME="hanihome-au"
AWS_REGION="ap-southeast-2"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}  HaniHome AU - AWS Secrets Setup${NC}"
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

# Check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured. Please run 'aws configure' first."
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed. Please install it first."
        exit 1
    fi
    
    print_step "Prerequisites check passed!"
}

# Create secret for environment
create_environment_secrets() {
    local environment=$1
    local secret_name="${PROJECT_NAME}-${environment}"
    
    print_step "Creating secrets for ${environment} environment..."
    
    # Check if secret already exists
    if aws secretsmanager describe-secret --secret-id "$secret_name" --region "$AWS_REGION" &>/dev/null; then
        print_warning "Secret '$secret_name' already exists. Updating..."
        
        # Update existing secret
        aws secretsmanager update-secret \
            --secret-id "$secret_name" \
            --secret-string "$(generate_secret_json $environment)" \
            --region "$AWS_REGION"
    else
        print_step "Creating new secret '$secret_name'..."
        
        # Create new secret
        aws secretsmanager create-secret \
            --name "$secret_name" \
            --description "HaniHome AU application secrets for ${environment} environment" \
            --secret-string "$(generate_secret_json $environment)" \
            --region "$AWS_REGION"
    fi
    
    print_step "Secret '$secret_name' configured successfully!"
}

# Generate secret JSON for environment
generate_secret_json() {
    local environment=$1
    
    case $environment in
        "development")
            cat << EOF
{
  "JWT_SECRET": "dev-jwt-secret-key-for-development-only-replace-in-production",
  "GOOGLE_CLIENT_ID": "dev-google-client-id-replace-with-actual",
  "GOOGLE_CLIENT_SECRET": "dev-google-client-secret-replace-with-actual",
  "KAKAO_CLIENT_ID": "dev-kakao-client-id-replace-with-actual",
  "KAKAO_CLIENT_SECRET": "dev-kakao-client-secret-replace-with-actual",
  "NEXTAUTH_SECRET": "dev-nextauth-secret-key-for-development-only",
  "NEXTAUTH_JWT_SECRET": "dev-nextauth-jwt-secret-key-for-development-only",
  "DATABASE_PASSWORD": "dev-database-password-replace-with-actual",
  "MAIL_PASSWORD": "dev-mail-password-replace-with-actual",
  "SENTRY_DSN": "",
  "GA_TRACKING_ID": ""
}
EOF
            ;;
        "staging")
            cat << EOF
{
  "JWT_SECRET": "REPLACE-WITH-SECURE-JWT-SECRET-256-BITS-MINIMUM",
  "GOOGLE_CLIENT_ID": "REPLACE-WITH-STAGING-GOOGLE-CLIENT-ID",
  "GOOGLE_CLIENT_SECRET": "REPLACE-WITH-STAGING-GOOGLE-CLIENT-SECRET",
  "KAKAO_CLIENT_ID": "REPLACE-WITH-STAGING-KAKAO-CLIENT-ID",
  "KAKAO_CLIENT_SECRET": "REPLACE-WITH-STAGING-KAKAO-CLIENT-SECRET",
  "NEXTAUTH_SECRET": "REPLACE-WITH-SECURE-NEXTAUTH-SECRET-KEY",
  "NEXTAUTH_JWT_SECRET": "REPLACE-WITH-SECURE-NEXTAUTH-JWT-SECRET",
  "DATABASE_PASSWORD": "REPLACE-WITH-STAGING-DATABASE-PASSWORD",
  "MAIL_PASSWORD": "REPLACE-WITH-STAGING-MAIL-PASSWORD",
  "SENTRY_DSN": "REPLACE-WITH-STAGING-SENTRY-DSN",
  "GA_TRACKING_ID": "REPLACE-WITH-STAGING-GA-TRACKING-ID"
}
EOF
            ;;
        "production")
            cat << EOF
{
  "JWT_SECRET": "REPLACE-WITH-PRODUCTION-JWT-SECRET-256-BITS-MINIMUM",
  "GOOGLE_CLIENT_ID": "REPLACE-WITH-PRODUCTION-GOOGLE-CLIENT-ID",
  "GOOGLE_CLIENT_SECRET": "REPLACE-WITH-PRODUCTION-GOOGLE-CLIENT-SECRET",
  "KAKAO_CLIENT_ID": "REPLACE-WITH-PRODUCTION-KAKAO-CLIENT-ID",
  "KAKAO_CLIENT_SECRET": "REPLACE-WITH-PRODUCTION-KAKAO-CLIENT-SECRET",
  "NEXTAUTH_SECRET": "REPLACE-WITH-PRODUCTION-NEXTAUTH-SECRET-KEY",
  "NEXTAUTH_JWT_SECRET": "REPLACE-WITH-PRODUCTION-NEXTAUTH-JWT-SECRET",
  "DATABASE_PASSWORD": "REPLACE-WITH-PRODUCTION-DATABASE-PASSWORD",
  "MAIL_PASSWORD": "REPLACE-WITH-PRODUCTION-MAIL-PASSWORD",
  "SENTRY_DSN": "REPLACE-WITH-PRODUCTION-SENTRY-DSN",
  "GA_TRACKING_ID": "REPLACE-WITH-PRODUCTION-GA-TRACKING-ID",
  "GOOGLE_MAPS_API_KEY": "REPLACE-WITH-PRODUCTION-GOOGLE-MAPS-API-KEY"
}
EOF
            ;;
    esac
}

# Create database password secret
create_database_secrets() {
    local environment=$1
    local secret_name="${PROJECT_NAME}-db-${environment}"
    
    print_step "Creating database secrets for ${environment} environment..."
    
    # Generate secure password
    local db_password=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    
    local secret_json=$(cat << EOF
{
  "username": "hanihome_user_${environment}",
  "password": "$db_password",
  "engine": "postgres",
  "host": "REPLACE-WITH-RDS-ENDPOINT",
  "port": 5432,
  "dbname": "hanihome_au_${environment}"
}
EOF
)
    
    if aws secretsmanager describe-secret --secret-id "$secret_name" --region "$AWS_REGION" &>/dev/null; then
        print_warning "Database secret '$secret_name' already exists. Skipping..."
    else
        aws secretsmanager create-secret \
            --name "$secret_name" \
            --description "HaniHome AU database credentials for ${environment} environment" \
            --secret-string "$secret_json" \
            --region "$AWS_REGION"
        
        print_step "Database secret '$secret_name' created successfully!"
    fi
}

# Generate secret retrieval script
generate_retrieval_script() {
    print_step "Generating secret retrieval script..."
    
    cat > ../scripts/get-secrets.sh << 'EOF'
#!/bin/bash

# HaniHome AU - Secret Retrieval Script
# This script retrieves secrets from AWS Secrets Manager

set -e

ENVIRONMENT=${1:-"development"}
PROJECT_NAME="hanihome-au"
AWS_REGION="ap-southeast-2"

# Function to get secret value
get_secret() {
    local secret_name=$1
    local key=$2
    
    aws secretsmanager get-secret-value \
        --secret-id "$secret_name" \
        --region "$AWS_REGION" \
        --query "SecretString" \
        --output text | jq -r ".$key"
}

# Get application secrets
APP_SECRET_NAME="${PROJECT_NAME}-${ENVIRONMENT}"
DB_SECRET_NAME="${PROJECT_NAME}-db-${ENVIRONMENT}"

echo "# Retrieved secrets for $ENVIRONMENT environment"
echo "export JWT_SECRET=\"$(get_secret $APP_SECRET_NAME JWT_SECRET)\""
echo "export GOOGLE_CLIENT_ID=\"$(get_secret $APP_SECRET_NAME GOOGLE_CLIENT_ID)\""
echo "export GOOGLE_CLIENT_SECRET=\"$(get_secret $APP_SECRET_NAME GOOGLE_CLIENT_SECRET)\""
echo "export KAKAO_CLIENT_ID=\"$(get_secret $APP_SECRET_NAME KAKAO_CLIENT_ID)\""
echo "export KAKAO_CLIENT_SECRET=\"$(get_secret $APP_SECRET_NAME KAKAO_CLIENT_SECRET)\""
echo "export NEXTAUTH_SECRET=\"$(get_secret $APP_SECRET_NAME NEXTAUTH_SECRET)\""
echo "export DATABASE_PASSWORD=\"$(get_secret $DB_SECRET_NAME password)\""
echo "export DATABASE_USERNAME=\"$(get_secret $DB_SECRET_NAME username)\""
echo ""
echo "# To use these secrets, run:"
echo "# source <(./get-secrets.sh $ENVIRONMENT)"
EOF
    
    chmod +x ../scripts/get-secrets.sh
    print_step "Secret retrieval script created at ../scripts/get-secrets.sh"
}

# Print usage instructions
print_usage_instructions() {
    print_step "Setup completed successfully!"
    echo ""
    echo -e "${GREEN}Usage Instructions:${NC}"
    echo "1. Update the placeholder values in AWS Secrets Manager console"
    echo "2. Use the get-secrets.sh script to retrieve secrets:"
    echo "   ./scripts/get-secrets.sh development"
    echo "   ./scripts/get-secrets.sh staging"
    echo "   ./scripts/get-secrets.sh production"
    echo ""
    echo -e "${YELLOW}Important Security Notes:${NC}"
    echo "- Replace all placeholder values with actual secrets"
    echo "- Never commit actual secrets to version control"
    echo "- Use IAM roles with minimal permissions for applications"
    echo "- Regularly rotate secrets"
    echo "- Monitor secret access through CloudTrail"
}

# Main execution
main() {
    print_header
    
    local environment=${1:-""}
    
    if [[ -z "$environment" ]]; then
        echo "Usage: $0 <environment>"
        echo "Environments: development, staging, production, all"
        exit 1
    fi
    
    check_prerequisites
    
    case $environment in
        "development"|"staging"|"production")
            create_environment_secrets $environment
            create_database_secrets $environment
            ;;
        "all")
            create_environment_secrets "development"
            create_database_secrets "development"
            create_environment_secrets "staging"
            create_database_secrets "staging"
            create_environment_secrets "production"
            create_database_secrets "production"
            ;;
        *)
            print_error "Invalid environment. Use: development, staging, production, or all"
            exit 1
            ;;
    esac
    
    generate_retrieval_script
    print_usage_instructions
}

# Run main function
main "$@"