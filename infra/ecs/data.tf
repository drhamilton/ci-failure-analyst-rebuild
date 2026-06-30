data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# The image repo and table, looked up by name (decoupled from base/'s state).
data "aws_ecr_repository" "app" {
  name = "ci-failure-analyst"
}

data "aws_dynamodb_table" "analysis_results" {
  name = "analysis_results"
}

# Build-log bucket from base/ (same account-id-suffixed name base/s3.tf creates).
data "aws_s3_bucket" "logs" {
  bucket = "ci-failure-analyst-logs-${data.aws_caller_identity.current.account_id}"
}

# Default VPC + its public subnets (cheap path: task gets a public IP, no NAT).
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# SSM secret ARNs built by string (NOT a data lookup) so secret *values*
# never get read into this stack's state.
locals {
  ssm_prefix   = "/ci-failure-analyst"
  ssm_arn_base = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter${local.ssm_prefix}"
  secret_arns = {
    github_token          = "${local.ssm_arn_base}/github-token"
    github_webhook_secret = "${local.ssm_arn_base}/github-webhook-secret"
    results_password      = "${local.ssm_arn_base}/results-password"
  }
}
