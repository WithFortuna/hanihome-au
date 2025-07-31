# AWS Infrastructure Setup Guide

## Overview
This document outlines the AWS infrastructure setup for the HaniHome AU project, including EC2, RDS, S3, VPC, and Application Load Balancer configurations.

## Prerequisites
- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Terraform or AWS CDK for infrastructure as code (recommended)

## 1. IAM Setup

### Create IAM User for Application
```bash
# Create IAM user for the application
aws iam create-user --user-name hanihome-au-app

# Create access key
aws iam create-access-key --user-name hanihome-au-app

# Attach necessary policies
aws iam attach-user-policy --user-name hanihome-au-app --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess
aws iam attach-user-policy --user-name hanihome-au-app --policy-arn arn:aws:iam::aws:policy/AmazonRDSDataFullAccess
```

### Custom IAM Policy for HaniHome
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::hanihome-au-*/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::hanihome-au-*"
    }
  ]
}
```

## 2. VPC Configuration

### VPC Setup
- **CIDR Block**: 10.0.0.0/16
- **Region**: ap-southeast-2 (Sydney) - closest to Australia
- **Availability Zones**: ap-southeast-2a, ap-southeast-2b

### Subnets
```
Public Subnets:
- hanihome-public-1a: 10.0.1.0/24 (ap-southeast-2a)
- hanihome-public-1b: 10.0.2.0/24 (ap-southeast-2b)

Private Subnets:
- hanihome-private-1a: 10.0.10.0/24 (ap-southeast-2a)
- hanihome-private-1b: 10.0.11.0/24 (ap-southeast-2b)

Database Subnets:
- hanihome-db-1a: 10.0.20.0/24 (ap-southeast-2a)
- hanihome-db-1b: 10.0.21.0/24 (ap-southeast-2b)
```

### Security Groups

#### ALB Security Group
```bash
# Create security group for ALB
aws ec2 create-security-group \
  --group-name hanihome-alb-sg \
  --description "Security group for HaniHome ALB" \
  --vpc-id vpc-xxxxxxxxx

# Allow HTTP traffic
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxxx \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

# Allow HTTPS traffic
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxxx \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0
```

#### EC2 Security Group
```bash
# Create security group for EC2 instances
aws ec2 create-security-group \
  --group-name hanihome-ec2-sg \
  --description "Security group for HaniHome EC2 instances" \
  --vpc-id vpc-xxxxxxxxx

# Allow traffic from ALB
aws ec2 authorize-security-group-ingress \
  --group-id sg-yyyyyyyyy \
  --protocol tcp \
  --port 8080 \
  --source-group sg-xxxxxxxxx

# Allow SSH access (restrict to your IP)
aws ec2 authorize-security-group-ingress \
  --group-id sg-yyyyyyyyy \
  --protocol tcp \
  --port 22 \
  --cidr YOUR_IP/32
```

#### RDS Security Group
```bash
# Create security group for RDS
aws ec2 create-security-group \
  --group-name hanihome-rds-sg \
  --description "Security group for HaniHome RDS" \
  --vpc-id vpc-xxxxxxxxx

# Allow PostgreSQL access from EC2
aws ec2 authorize-security-group-ingress \
  --group-id sg-zzzzzzzzz \
  --protocol tcp \
  --port 5432 \
  --source-group sg-yyyyyyyyy
```

## 3. S3 Configuration

### Create S3 Buckets
```bash
# Create main assets bucket
aws s3 mb s3://hanihome-au-assets-prod --region ap-southeast-2

# Create backup bucket
aws s3 mb s3://hanihome-au-backups-prod --region ap-southeast-2

# Create logs bucket
aws s3 mb s3://hanihome-au-logs-prod --region ap-southeast-2
```

### S3 Bucket Policy for Assets
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::hanihome-au-assets-prod/public/*"
    },
    {
      "Sid": "ApplicationAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT-ID:user/hanihome-au-app"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::hanihome-au-assets-prod/*"
    }
  ]
}
```

### S3 CORS Configuration
```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "POST", "PUT", "DELETE"],
    "AllowedOrigins": [
      "https://hanihome.com.au",
      "https://www.hanihome.com.au",
      "http://localhost:3000"
    ],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

## 4. RDS PostgreSQL Setup

### Create DB Subnet Group
```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name hanihome-db-subnet-group \
  --db-subnet-group-description "Subnet group for HaniHome RDS" \
  --subnet-ids subnet-xxxxxxxxx subnet-yyyyyyyyy
```

### Create RDS Instance
```bash
aws rds create-db-instance \
  --db-instance-identifier hanihome-au-prod \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 16.9 \
  --master-username hanihome_admin \
  --master-user-password CHANGE_ME_STRONG_PASSWORD \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids sg-zzzzzzzzz \
  --db-subnet-group-name hanihome-db-subnet-group \
  --backup-retention-period 7 \
  --multi-az \
  --storage-encrypted \
  --deletion-protection
```

### RDS Configuration
- **Instance Class**: db.t3.micro (production should use larger)
- **Engine**: PostgreSQL 16.9
- **Storage**: 20GB GP2 (auto-scaling enabled)
- **Backup Retention**: 7 days
- **Multi-AZ**: Enabled for high availability
- **Encryption**: Enabled

## 5. EC2 Configuration

### Launch Template
```bash
aws ec2 create-launch-template \
  --launch-template-name hanihome-au-template \
  --launch-template-data '{
    "ImageId": "ami-0c55b159cbfafe1d0",
    "InstanceType": "t3.small",
    "SecurityGroupIds": ["sg-yyyyyyyyy"],
    "IamInstanceProfile": {
      "Name": "hanihome-ec2-profile"
    },
    "UserData": "BASE64_ENCODED_USER_DATA"
  }'
```

### Auto Scaling Group
```bash
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name hanihome-au-asg \
  --launch-template LaunchTemplateName=hanihome-au-template,Version=1 \
  --min-size 1 \
  --max-size 3 \
  --desired-capacity 2 \
  --vpc-zone-identifier "subnet-xxxxxxxxx,subnet-yyyyyyyyy" \
  --target-group-arns arn:aws:elasticloadbalancing:region:account:targetgroup/hanihome/xxxxx
```

## 6. Application Load Balancer

### Create ALB
```bash
aws elbv2 create-load-balancer \
  --name hanihome-au-alb \
  --subnets subnet-xxxxxxxxx subnet-yyyyyyyyy \
  --security-groups sg-xxxxxxxxx \
  --scheme internet-facing \
  --type application
```

### Create Target Group
```bash
aws elbv2 create-target-group \
  --name hanihome-au-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id vpc-xxxxxxxxx \
  --health-check-path /api/v1/actuator/health \
  --health-check-protocol HTTP \
  --health-check-port 8080
```

### Create Listener
```bash
aws elbv2 create-listener \
  --load-balancer-arn arn:aws:elasticloadbalancing:region:account:loadbalancer/app/hanihome-au-alb/xxxxx \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:region:account:targetgroup/hanihome-au-tg/xxxxx
```

## 7. CloudWatch and Monitoring

### CloudWatch Log Groups
```bash
# Create log groups
aws logs create-log-group --log-group-name /aws/ec2/hanihome-au
aws logs create-log-group --log-group-name /aws/rds/hanihome-au
aws logs create-log-group --log-group-name /aws/alb/hanihome-au
```

### CloudWatch Alarms
```bash
# High CPU alarm
aws cloudwatch put-metric-alarm \
  --alarm-name hanihome-au-high-cpu \
  --alarm-description "High CPU utilization" \
  --metric-name CPUUtilization \
  --namespace AWS/EC2 \
  --statistic Average \
  --period 300 \
  --threshold 80 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2
```

## 8. DNS and SSL

### Route 53 Configuration
- Domain: hanihome.com.au
- Subdomain: api.hanihome.com.au (for API)
- Subdomain: assets.hanihome.com.au (for S3 assets)

### ACM SSL Certificate
```bash
aws acm request-certificate \
  --domain-name hanihome.com.au \
  --subject-alternative-names www.hanihome.com.au api.hanihome.com.au \
  --validation-method DNS
```

## 9. Environment Configuration

### Production Environment Variables
```bash
# Database
DATABASE_URL=postgresql://hanihome_admin:PASSWORD@hanihome-au-prod.xxxxxxxxx.ap-southeast-2.rds.amazonaws.com:5432/hanihome_au
DATABASE_USERNAME=hanihome_admin
DATABASE_PASSWORD=STRONG_PASSWORD

# AWS
AWS_REGION=ap-southeast-2
AWS_S3_BUCKET_ASSETS=hanihome-au-assets-prod
AWS_S3_BUCKET_BACKUPS=hanihome-au-backups-prod
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# Application
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=CHANGE_ME_STRONG_JWT_SECRET
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

## 10. Deployment Strategy

### Blue-Green Deployment
1. Deploy new version to new target group
2. Test new version
3. Update ALB to route traffic to new target group
4. Monitor and rollback if necessary

### Rolling Updates
- Use Auto Scaling Group rolling updates
- Health check grace period: 300 seconds
- Update batch size: 1 instance at a time

## 11. Security Considerations

### Network Security
- All private subnets use NAT Gateway for outbound internet access
- Database subnets have no internet access
- Security groups follow principle of least privilege

### Data Security
- RDS encryption at rest enabled
- S3 buckets encrypted with AES-256
- SSL/TLS for all communications
- Regular security patches via Systems Manager

### Access Control
- IAM roles with minimal required permissions
- MFA required for AWS console access
- Regular access key rotation

## 12. Cost Optimization

### Right-sizing
- Start with t3.micro for RDS
- t3.small for EC2 instances
- Monitor and adjust based on usage

### Reserved Instances
- Consider 1-year reserved instances after usage patterns stabilize
- Use Savings Plans for compute resources

### Storage Optimization
- S3 Intelligent Tiering for infrequently accessed data
- Regular cleanup of old logs and backups

## 13. Disaster Recovery

### Backup Strategy
- RDS automated backups (7 days retention)
- Cross-region S3 replication for critical assets
- Application configuration in version control

### Recovery Procedures
- RDS point-in-time recovery
- Infrastructure as Code for quick environment recreation
- Database migration scripts for schema recovery

## Next Steps

1. Set up development/staging environments
2. Implement Infrastructure as Code (Terraform/CDK)
3. Set up monitoring and alerting
4. Configure automated backups
5. Implement CI/CD pipeline integration
6. Security audit and penetration testing
7. Performance testing and optimization