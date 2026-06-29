resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/ci-failure-analyst"
  retention_in_days = 7 # cost discipline: don't keep logs forever
}

resource "aws_ecs_cluster" "main" {
  name = "ci-failure-analyst"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "ci-failure-analyst"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256" # 0.25 vCPU
  memory                   = "512" # 0.5 GB
  execution_role_arn       = aws_iam_role.execution.arn # stagehand
  task_role_arn            = aws_iam_role.task.arn       # app identity

  container_definitions = jsonencode([{
    name      = "app"
    image     = "${data.aws_ecr_repository.app.repository_url}:latest"
    essential = true
    portMappings = [{ containerPort = 8080 }]

    # plaintext config
    environment = [{ name = "SPRING_PROFILES_ACTIVE", value = "aws" }]

    # references resolved + decrypted at launch by the execution role
    secrets = [
      { name = "RESULTS_PASSWORD", valueFrom = local.secret_arns.results_password },
      { name = "GITHUB_TOKEN", valueFrom = local.secret_arns.github_token },
      { name = "GITHUB_WEBHOOK_SECRET", valueFrom = local.secret_arns.github_webhook_secret },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = data.aws_region.current.name
        "awslogs-stream-prefix" = "app"
      }
    }
  }])
}
