# App secrets, injected into the ECS task later via the task def `secrets:` block
# (never `environment:`). SecureString uses the free AWS-managed key (alias/aws/ssm).
#
# Values are NOT stored here. Terraform owns the parameter; you own the secret:
# each resource seeds a placeholder once, then `ignore_changes = [value]` lets you
# set the real value out-of-band (`aws ssm put-parameter --overwrite`) without
# Terraform reverting it or the secret ever touching .tf / state on later applies.

locals {
  ssm_prefix = "/ci-failure-analyst"
}

resource "aws_ssm_parameter" "github_token" {
  name        = "${local.ssm_prefix}/github-token"
  description = "GitHub PAT used by BuildLogFetcher. Set the real value via the CLI."
  type        = "SecureString"
  value       = "PLACEHOLDER-set-via-cli"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "github_webhook_secret" {
  name        = "${local.ssm_prefix}/github-webhook-secret"
  description = "HMAC secret; must match the GitHub webhook config. Set via the CLI."
  type        = "SecureString"
  value       = "PLACEHOLDER-set-via-cli"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "results_password" {
  name        = "${local.ssm_prefix}/results-password"
  description = "Basic-auth password for /results. Set via the CLI."
  type        = "SecureString"
  value       = "PLACEHOLDER-set-via-cli"

  lifecycle {
    ignore_changes = [value]
  }
}

# Param ARNs the ECS task execution role will be granted ssm:GetParameters on
# (consumed by the ecs/ stack later). Names only — never the values.
output "secret_param_arns" {
  description = "ARNs of the app secret parameters in SSM."
  value = {
    github_token          = aws_ssm_parameter.github_token.arn
    github_webhook_secret = aws_ssm_parameter.github_webhook_secret.arn
    results_password      = aws_ssm_parameter.results_password.arn
  }
}
