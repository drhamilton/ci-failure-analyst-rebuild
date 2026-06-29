terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
  }
}

provider "aws" {
  region = "us-east-1"
  # credentials come from the SSO "default" profile (or AWS_PROFILE).
  # No static keys, no LocalStack override here — this is the real account.

  default_tags {
    tags = {
      Project   = "ci-failure-analyst"
      ManagedBy = "terraform"
      Stack     = "base"
    }
  }
}