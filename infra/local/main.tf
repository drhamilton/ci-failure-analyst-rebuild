terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
  }
}

provider "aws" {
    region = "us-east-1"
}

resource "aws_dynamodb_table" "analysis_results" {
    name    = "analysis_results"
    billing_mode = "PAY_PER_REQUEST"

    hash_key = "id"

    attribute {
        name = "id"
        type = "N"
    }
}