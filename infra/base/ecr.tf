resource "aws_ecr_repository" "app" {
  name = "ci-failure-analyst"

  image_scanning_configuration {
    scan_on_push = true # free vulnerability scan on every push
  }
}

# Keep storage tiny (~$0.10/GB-mo): expire all but the last 5 images.
resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep only the last 5 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = { type = "expire" }
    }]
  })
}
