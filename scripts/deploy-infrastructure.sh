#!/bin/bash

# HaniHome AU Infrastructure Deployment Script
# This script deploys the AWS infrastructure using Terraform

set -e

# Configuration
PROJECT_NAME="hanihome-au"
ENVIRONMENT=${1:-"prod"}
AWS_REGION="ap-southeast-2"
TERRAFORM_DIR="../infrastructure/terraform"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}  HaniHome AU Infrastructure Deployment${NC}"
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

check_prerequisites() {
    print_step "Checking prerequisites..."
    
    # Check if AWS CLI is installed
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check if Terraform is installed
    if ! command -v terraform &> /dev/null; then
        print_error "Terraform is not installed. Please install it first."
        exit 1
    fi
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured. Please run 'aws configure' first."
        exit 1
    fi
    
    print_step "Prerequisites check passed!"
}

create_terraform_backend() {
    print_step "Setting up Terraform backend..."
    
    # Create S3 bucket for Terraform state
    BUCKET_NAME="${PROJECT_NAME}-terraform-state"
    TABLE_NAME="${PROJECT_NAME}-terraform-locks"
    
    # Check if bucket exists
    if ! aws s3 ls "s3://${BUCKET_NAME}" 2>/dev/null; then
        print_step "Creating S3 bucket for Terraform state..."
        aws s3 mb "s3://${BUCKET_NAME}" --region ${AWS_REGION}
        
        # Enable versioning
        aws s3api put-bucket-versioning \
            --bucket ${BUCKET_NAME} \
            --versioning-configuration Status=Enabled
        
        # Enable encryption
        aws s3api put-bucket-encryption \
            --bucket ${BUCKET_NAME} \
            --server-side-encryption-configuration '{
                "Rules": [
                    {
                        "ApplyServerSideEncryptionByDefault": {
                            "SSEAlgorithm": "AES256"
                        }
                    }
                ]
            }'
    else
        print_step "Terraform state bucket already exists"
    fi
    
    # Check if DynamoDB table exists
    if ! aws dynamodb describe-table --table-name ${TABLE_NAME} --region ${AWS_REGION} &>/dev/null; then
        print_step "Creating DynamoDB table for Terraform locks..."
        aws dynamodb create-table \
            --table-name ${TABLE_NAME} \
            --attribute-definitions AttributeName=LockID,AttributeType=S \
            --key-schema AttributeName=LockID,KeyType=HASH \
            --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
            --region ${AWS_REGION}
        
        # Wait for table to be created
        aws dynamodb wait table-exists --table-name ${TABLE_NAME} --region ${AWS_REGION}
    else
        print_step "Terraform locks table already exists"
    fi
}

deploy_infrastructure() {
    print_step "Deploying infrastructure with Terraform..."
    
    cd ${TERRAFORM_DIR}
    
    # Initialize Terraform
    print_step "Initializing Terraform..."
    terraform init
    
    # Validate configuration
    print_step "Validating Terraform configuration..."
    terraform validate
    
    # Plan deployment
    print_step "Planning infrastructure deployment..."
    terraform plan \
        -var="environment=${ENVIRONMENT}" \
        -var="project_name=${PROJECT_NAME}" \
        -var="aws_region=${AWS_REGION}" \
        -out=tfplan
    
    # Ask for confirmation
    echo -e "${YELLOW}Do you want to proceed with the deployment? (y/N)${NC}"
    read -r response
    if [[ ! "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_warning "Deployment cancelled by user"
        exit 0
    fi
    
    # Apply configuration
    print_step "Applying Terraform configuration..."
    terraform apply tfplan
    
    # Clean up plan file
    rm -f tfplan
    
    print_step "Infrastructure deployment completed!"
}

save_outputs() {
    print_step "Saving Terraform outputs..."
    
    cd ${TERRAFORM_DIR}
    
    # Save outputs to file
    OUTPUT_FILE="../../../scripts/terraform-outputs.json"
    terraform output -json > ${OUTPUT_FILE}
    
    print_step "Outputs saved to ${OUTPUT_FILE}"
}

generate_env_file() {
    print_step "Generating environment file..."
    
    cd ${TERRAFORM_DIR}
    
    # Generate .env file for application
    ENV_FILE="../../../.env.${ENVIRONMENT}"
    
    cat > ${ENV_FILE} << EOF
# HaniHome AU Environment Variables - ${ENVIRONMENT}
# Generated automatically by deploy-infrastructure.sh

# Database Configuration
DATABASE_URL=postgresql://\$(terraform output -raw database_username):\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw db_password_secret_arn) --query SecretString --output text | jq -r .password)@\$(terraform output -raw rds_endpoint)/\$(terraform output -raw database_name)
DATABASE_USERNAME=\$(terraform output -raw database_username)
DATABASE_HOST=\$(terraform output -raw rds_endpoint)
DATABASE_PORT=\$(terraform output -raw rds_port)
DATABASE_NAME=\$(terraform output -raw database_name)

# AWS Configuration
AWS_REGION=${AWS_REGION}
AWS_S3_BUCKET_ASSETS=\$(terraform output -raw s3_bucket_assets)
AWS_S3_BUCKET_BACKUPS=\$(terraform output -raw s3_bucket_backups)
AWS_S3_BUCKET_LOGS=\$(terraform output -raw s3_bucket_logs)

# Application Configuration
SPRING_PROFILES_ACTIVE=${ENVIRONMENT}
SERVER_PORT=8080

# Secrets (retrieve from AWS Secrets Manager)
# JWT_SECRET=\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw app_secrets_secret_arn) --query SecretString --output text | jq -r .JWT_SECRET)
# GOOGLE_CLIENT_ID=\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw app_secrets_secret_arn) --query SecretString --output text | jq -r .GOOGLE_CLIENT_ID)
# GOOGLE_CLIENT_SECRET=\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw app_secrets_secret_arn) --query SecretString --output text | jq -r .GOOGLE_CLIENT_SECRET)
# KAKAO_CLIENT_ID=\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw app_secrets_secret_arn) --query SecretString --output text | jq -r .KAKAO_CLIENT_ID)
# KAKAO_CLIENT_SECRET=\$(aws secretsmanager get-secret-value --secret-id \$(terraform output -raw app_secrets_secret_arn) --query SecretString --output text | jq -r .KAKAO_CLIENT_SECRET)

# Security Groups
ALB_SECURITY_GROUP_ID=\$(terraform output -raw alb_security_group_id)
EC2_SECURITY_GROUP_ID=\$(terraform output -raw ec2_security_group_id)
RDS_SECURITY_GROUP_ID=\$(terraform output -raw rds_security_group_id)

# IAM
EC2_INSTANCE_PROFILE=\$(terraform output -raw ec2_instance_profile_name)
EOF

    print_step "Environment file generated: ${ENV_FILE}"
    print_warning "Remember to replace placeholder values with actual secrets from AWS Secrets Manager"
}

print_next_steps() {
    print_step "Deployment completed successfully!"
    echo ""
    echo -e "${GREEN}Next Steps:${NC}"
    echo "1. Update application secrets in AWS Secrets Manager"
    echo "2. Configure domain and SSL certificates"
    echo "3. Set up EC2 instances and Auto Scaling Groups"
    echo "4. Configure Application Load Balancer"
    echo "5. Set up CloudWatch monitoring and alarms"
    echo "6. Test the infrastructure"
    echo ""
    echo -e "${BLUE}Important Files Created:${NC}"
    echo "- terraform-outputs.json: Infrastructure outputs"
    echo "- .env.${ENVIRONMENT}: Environment variables template"
    echo ""
    echo -e "${YELLOW}Remember to:${NC}"
    echo "- Store sensitive information securely"
    echo "- Review security group rules"
    echo "- Enable CloudTrail for auditing"
    echo "- Set up backup procedures"
}

cleanup_on_error() {
    print_error "Deployment failed! Cleaning up..."
    cd ${TERRAFORM_DIR}
    rm -f tfplan
    exit 1
}

# Main execution
main() {
    print_header
    
    # Set trap for error handling
    trap cleanup_on_error ERR
    
    # Validate environment parameter
    if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
        print_error "Invalid environment. Use: dev, staging, or prod"
        exit 1
    fi
    
    print_step "Deploying infrastructure for environment: ${ENVIRONMENT}"
    
    check_prerequisites
    create_terraform_backend
    deploy_infrastructure
    save_outputs
    generate_env_file
    print_next_steps
    
    echo -e "${GREEN}Infrastructure deployment completed successfully!${NC}"
}

# Run main function
main "$@"