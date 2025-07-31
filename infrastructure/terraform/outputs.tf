# Terraform Outputs

# VPC Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = aws_subnet.private[*].id
}

output "database_subnet_ids" {
  description = "IDs of the database subnets"
  value       = aws_subnet.database[*].id
}

# RDS Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
  sensitive   = true
}

output "rds_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

output "database_name" {
  description = "Name of the database"
  value       = aws_db_instance.main.db_name
}

output "database_username" {
  description = "Database username"
  value       = aws_db_instance.main.username
  sensitive   = true
}

# S3 Outputs
output "s3_bucket_assets" {
  description = "Name of the assets S3 bucket"
  value       = aws_s3_bucket.assets.id
}

output "s3_bucket_backups" {
  description = "Name of the backups S3 bucket"
  value       = aws_s3_bucket.backups.id
}

output "s3_bucket_logs" {
  description = "Name of the logs S3 bucket"
  value       = aws_s3_bucket.logs.id
}

output "s3_bucket_assets_domain_name" {
  description = "Domain name of the assets S3 bucket"
  value       = aws_s3_bucket.assets.bucket_domain_name
}

# Security Group Outputs
output "alb_security_group_id" {
  description = "ID of the ALB security group"
  value       = aws_security_group.alb.id
}

output "ec2_security_group_id" {
  description = "ID of the EC2 security group"
  value       = aws_security_group.ec2.id
}

output "rds_security_group_id" {
  description = "ID of the RDS security group"
  value       = aws_security_group.rds.id
}

# IAM Outputs
output "ec2_instance_profile_name" {
  description = "Name of the EC2 instance profile"
  value       = aws_iam_instance_profile.ec2.name
}

output "app_user_access_key_id" {
  description = "Access key ID for application user"
  value       = aws_iam_access_key.app.id
  sensitive   = true
}

output "app_user_secret_access_key" {
  description = "Secret access key for application user"
  value       = aws_iam_access_key.app.secret
  sensitive   = true
}

# Secrets Manager Outputs
output "db_password_secret_arn" {
  description = "ARN of the database password secret"
  value       = aws_secretsmanager_secret.db_password.arn
}

output "app_secrets_secret_arn" {
  description = "ARN of the application secrets"
  value       = aws_secretsmanager_secret.app_secrets.arn
}

# Region and Account
output "aws_region" {
  description = "AWS region"
  value       = var.aws_region
}

output "account_id" {
  description = "AWS account ID"
  value       = data.aws_caller_identity.current.account_id
}

# Environment Variables for Application
output "environment_variables" {
  description = "Environment variables for the application"
  value = {
    AWS_REGION                = var.aws_region
    AWS_S3_BUCKET_ASSETS      = aws_s3_bucket.assets.id
    AWS_S3_BUCKET_BACKUPS     = aws_s3_bucket.backups.id
    AWS_S3_BUCKET_LOGS        = aws_s3_bucket.logs.id
    DATABASE_HOST             = aws_db_instance.main.endpoint
    DATABASE_PORT             = aws_db_instance.main.port
    DATABASE_NAME             = aws_db_instance.main.db_name
    DATABASE_USERNAME         = aws_db_instance.main.username
    DB_PASSWORD_SECRET_ARN    = aws_secretsmanager_secret.db_password.arn
    APP_SECRETS_SECRET_ARN    = aws_secretsmanager_secret.app_secrets.arn
    SPRING_PROFILES_ACTIVE    = var.environment
  }
  sensitive = true
}