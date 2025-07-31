# IAM Resources

# IAM User for Application
resource "aws_iam_user" "app" {
  name = "${local.name_prefix}-app-user"
  path = "/"

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-app-user"
  })
}

# Access Key for Application User
resource "aws_iam_access_key" "app" {
  user = aws_iam_user.app.name
}

# IAM Policy for S3 Access
resource "aws_iam_policy" "s3_access" {
  name        = "${local.name_prefix}-s3-access"
  path        = "/"
  description = "IAM policy for S3 access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = [
          "${aws_s3_bucket.assets.arn}/*",
          "${aws_s3_bucket.backups.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.assets.arn,
          aws_s3_bucket.backups.arn
        ]
      }
    ]
  })

  tags = local.common_tags
}

# IAM Policy for Secrets Manager Access
resource "aws_iam_policy" "secrets_access" {
  name        = "${local.name_prefix}-secrets-access"
  path        = "/"
  description = "IAM policy for Secrets Manager access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          aws_secretsmanager_secret.db_password.arn,
          aws_secretsmanager_secret.app_secrets.arn
        ]
      }
    ]
  })

  tags = local.common_tags
}

# IAM Policy for CloudWatch Logs
resource "aws_iam_policy" "cloudwatch_logs" {
  name        = "${local.name_prefix}-cloudwatch-logs"
  path        = "/"
  description = "IAM policy for CloudWatch Logs access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:DescribeLogGroups"
        ]
        Resource = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:*"
      }
    ]
  })

  tags = local.common_tags
}

# Attach policies to application user
resource "aws_iam_user_policy_attachment" "s3_access" {
  user       = aws_iam_user.app.name
  policy_arn = aws_iam_policy.s3_access.arn
}

resource "aws_iam_user_policy_attachment" "secrets_access" {
  user       = aws_iam_user.app.name
  policy_arn = aws_iam_policy.secrets_access.arn
}

resource "aws_iam_user_policy_attachment" "cloudwatch_logs" {
  user       = aws_iam_user.app.name
  policy_arn = aws_iam_policy.cloudwatch_logs.arn
}

# IAM Role for EC2 Instances
resource "aws_iam_role" "ec2" {
  name = "${local.name_prefix}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

# IAM Instance Profile for EC2
resource "aws_iam_instance_profile" "ec2" {
  name = "${local.name_prefix}-ec2-profile"
  role = aws_iam_role.ec2.name

  tags = local.common_tags
}

# Attach policies to EC2 role
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_cloudwatch" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy_attachment" "ec2_s3_access" {
  role       = aws_iam_role.ec2.name
  policy_arn = aws_iam_policy.s3_access.arn
}

resource "aws_iam_role_policy_attachment" "ec2_secrets_access" {
  role       = aws_iam_role.ec2.name
  policy_arn = aws_iam_policy.secrets_access.arn
}

# Store Application Secrets
resource "aws_secretsmanager_secret" "app_secrets" {
  name                    = "${local.name_prefix}-app-secrets"
  description             = "Application secrets for HaniHome AU"
  recovery_window_in_days = 7

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "app_secrets" {
  secret_id = aws_secretsmanager_secret.app_secrets.id
  secret_string = jsonencode({
    JWT_SECRET                = "CHANGE_ME_STRONG_JWT_SECRET_IN_PRODUCTION"
    GOOGLE_CLIENT_ID          = "your-google-client-id"
    GOOGLE_CLIENT_SECRET      = "your-google-client-secret"
    KAKAO_CLIENT_ID           = "your-kakao-client-id"
    KAKAO_CLIENT_SECRET       = "your-kakao-client-secret"
    AWS_ACCESS_KEY_ID         = aws_iam_access_key.app.id
    AWS_SECRET_ACCESS_KEY     = aws_iam_access_key.app.secret
  })
}