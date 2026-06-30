# CI Failure Analyst — AWS ops (slice 2)
# Spin up / pause / tear down the ephemeral ecs/ Fargate stack.
# base/ is the "leave up" near-free stack; destroying it is guarded (down-base).

set shell := ["bash", "-cu"]

ecs_dir := "infra/ecs"
base_dir := "infra/base"
cluster := "ci-failure-analyst"
repo := "ci-failure-analyst"
log_group := "/ecs/ci-failure-analyst"

# Show available recipes
default:
    @just --list

# Build the image and push to ECR as :latest (+ the short git SHA for traceability).
# The task def pulls :latest, so run this before `just up` whenever app/ code changes.
push:
    #!/usr/bin/env bash
    set -euo pipefail
    ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
    REGION=${AWS_REGION:-$(aws configure get region)}
    REGISTRY="$ACCOUNT.dkr.ecr.$REGION.amazonaws.com"
    REPO="$REGISTRY/{{repo}}"
    SHA=$(git rev-parse --short HEAD)
    echo "building + pushing $REPO :latest + :$SHA"
    aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"
    docker build -t "$REPO:latest" -t "$REPO:$SHA" .
    docker push "$REPO:latest"
    docker push "$REPO:$SHA"
    echo "pushed. (re)deploy with: just up"

# Spin up the Fargate task (desired=1), ingress locked to your current IP
up:
    #!/usr/bin/env bash
    set -euo pipefail
    IP=$(curl -s https://checkip.amazonaws.com | tr -d '[:space:]')
    echo "allowing your IP: $IP/32"
    terraform -chdir={{ecs_dir}} apply -auto-approve -var "allowed_cidr=$IP/32" -var desired_count=1
    echo "task starting (~90s to boot on 0.25 vCPU). Then run: just status"

# Pause: scale to 0 (stops Fargate billing, keeps the stack to resume later)
pause:
    #!/usr/bin/env bash
    set -euo pipefail
    IP=$(curl -s https://checkip.amazonaws.com | tr -d '[:space:]')
    terraform -chdir={{ecs_dir}} apply -auto-approve -var "allowed_cidr=$IP/32" -var desired_count=0
    echo "scaled to 0 — Fargate billing stops once the task drains (~30s)."

# Tear down the whole ecs/ stack (service, SG, cluster, task def, roles, logs)
down:
    #!/usr/bin/env bash
    set -euo pipefail
    IP=$(curl -s https://checkip.amazonaws.com | tr -d '[:space:]')
    terraform -chdir={{ecs_dir}} destroy -auto-approve -var "allowed_cidr=$IP/32"
    echo "ecs/ destroyed — back to ~\$0 idle. base/ still up."

# Service desired/running counts + the task's public URL
status:
    #!/usr/bin/env bash
    set -euo pipefail
    aws ecs describe-services --cluster {{cluster}} --services {{cluster}} \
      --query 'services[0].{desired:desiredCount,running:runningCount,status:status}' --output table
    just ip

# Print the current task's public URL (or note if paused/down)
ip:
    #!/usr/bin/env bash
    set -euo pipefail
    TASK=$(aws ecs list-tasks --cluster {{cluster}} --service-name {{cluster}} --query 'taskArns[0]' --output text)
    if [[ "$TASK" == "None" || -z "$TASK" ]]; then echo "no running task (paused or down)"; exit 0; fi
    ENI=$(aws ecs describe-tasks --cluster {{cluster}} --tasks "$TASK" \
      --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value | [0]" --output text)
    IP=$(aws ec2 describe-network-interfaces --network-interface-ids "$ENI" \
      --query 'NetworkInterfaces[0].Association.PublicIp' --output text)
    echo "app:    http://$IP:8080"
    echo "health: http://$IP:8080/actuator/health"

# Tail the app's CloudWatch logs (ctrl-c to stop)
logs:
    aws logs tail {{log_group}} --follow --format short

# DANGER: destroy base/ — ECR image, DynamoDB DATA, SSM secrets, AND cost guardrails. Rarely needed.
down-base:
    #!/usr/bin/env bash
    set -euo pipefail
    echo "This destroys base/: ECR repo+image, DynamoDB table+DATA, SSM secrets, and the cost guardrails."
    read -r -p "Type 'destroy base' to confirm: " ans
    [[ "$ans" == "destroy base" ]] || { echo "aborted."; exit 1; }
    terraform -chdir={{base_dir}} destroy
