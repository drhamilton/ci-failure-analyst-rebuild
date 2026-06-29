data "aws_iam_policy_document" "ecs_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# Default key SecureStrings are encrypted with — execution role needs to decrypt.
data "aws_kms_alias" "ssm" {
  name = "alias/aws/ssm"
}

# --- EXECUTION role: the stagehand (pull image, fetch+decrypt secrets) ---
resource "aws_iam_role" "execution" {
  name               = "ci-failure-analyst-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
}

# AWS-managed: ECR pull + CloudWatch Logs write.
resource "aws_iam_role_policy_attachment" "execution_managed" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Read exactly our 3 SSM secrets, and decrypt them with the default SSM key.
data "aws_iam_policy_document" "execution_secrets" {
  statement {
    actions   = ["ssm:GetParameters"]
    resources = values(local.secret_arns)
  }
  statement {
    actions   = ["kms:Decrypt"]
    resources = [data.aws_kms_alias.ssm.target_key_arn]
  }
}

resource "aws_iam_role_policy" "execution_secrets" {
  name   = "read-app-secrets"
  role   = aws_iam_role.execution.id
  policy = data.aws_iam_policy_document.execution_secrets.json
}

# --- TASK role: the app's own runtime identity (DynamoDB access only) ---
resource "aws_iam_role" "task" {
  name               = "ci-failure-analyst-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
}

data "aws_iam_policy_document" "task_dynamodb" {
  statement {
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Query",
      "dynamodb:Scan",
    ]
    resources = [data.aws_dynamodb_table.analysis_results.arn]
  }
}

resource "aws_iam_role_policy" "task_dynamodb" {
  name   = "analysis-results-access"
  role   = aws_iam_role.task.id
  policy = data.aws_iam_policy_document.task_dynamodb.json
}
