# Raw build logs live here (the claim-check pattern): the log *body* goes to S3, and
# DynamoDB keeps only the (small) key. Near-free to leave up; a lifecycle rule expires
# objects so storage stays tiny.

data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "logs" {
  # S3 bucket names are GLOBALLY unique — suffix with the account id to avoid collisions.
  bucket = "ci-failure-analyst-logs-${data.aws_caller_identity.current.account_id}"
}

# No public access, ever — these are internal build logs.
resource "aws_s3_bucket_public_access_block" "logs" {
  bucket                  = aws_s3_bucket.logs.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Cost discipline: drop logs after 30 days (S3 is cheap, not free).
resource "aws_s3_bucket_lifecycle_configuration" "logs" {
  bucket = aws_s3_bucket.logs.id

  rule {
    id     = "expire-old-logs"
    status = "Enabled"
    filter {}

    expiration {
      days = 30
    }
  }
}

output "logs_bucket_name" {
  description = "Name of the build-log bucket (the ecs/ stack looks it up by name)."
  value       = aws_s3_bucket.logs.bucket
}
