# The SSO role you actually assume. Looked up dynamically so the random suffix
# (and any re-provision of the permission set) doesn't break this.
data "aws_iam_roles" "admin_sso" {
  name_regex  = "AWSReservedSSO_AdministratorAccess_.*"
  path_prefix = "/aws-reserved/sso.amazonaws.com/"
}

# --- The deny policy Budgets attaches at 100% ---
# DENY on creation only. No Delete* / no scale-down actions, so `terraform
# destroy` and "scale ECS to 0" still work even after this fires.
data "aws_iam_policy_document" "deny_new_spend" {
  statement {
    sid    = "DenyNewBillableResources"
    effect = "Deny"
    actions = [
      "ec2:RunInstances",
      "ec2:CreateNatGateway",
      "ec2:AllocateAddress",
      "elasticloadbalancing:CreateLoadBalancer",
      "eks:CreateCluster",
      "eks:CreateNodegroup",
      "eks:CreateFargateProfile",
      "rds:CreateDBInstance",
      "rds:CreateDBCluster",
      "ecs:CreateService",
      "ecs:RunTask",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "deny_new_spend" {
  name        = "ci-failure-analyst-deny-new-spend"
  description = "Attached by AWS Budgets at 100% of budget. Blocks creating new billable resources; never blocks delete/scale-down."
  policy      = data.aws_iam_policy_document.deny_new_spend.json
}

# --- Role that AWS Budgets assumes to attach the policy ---
data "aws_iam_policy_document" "budget_action_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["budgets.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "budget_action" {
  name               = "ci-failure-analyst-budget-action"
  assume_role_policy = data.aws_iam_policy_document.budget_action_assume.json
}

data "aws_iam_policy_document" "budget_action_perms" {
  statement {
    effect    = "Allow"
    actions   = ["iam:AttachRolePolicy", "iam:DetachRolePolicy"]
    resources = data.aws_iam_roles.admin_sso.arns
  }
  statement {
    effect = "Allow"
    actions = [
      "iam:GetRole",
      "iam:ListAttachedRolePolicies",
      "iam:GetPolicy",
      "iam:GetPolicyVersion",
      "iam:ListPolicyVersions",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "budget_action" {
  name   = "attach-deny-policy"
  role   = aws_iam_role.budget_action.id
  policy = data.aws_iam_policy_document.budget_action_perms.json
}

# --- The action: at 100% actual, attach the deny policy to your SSO role ---
resource "aws_budgets_budget_action" "deny_at_100" {
  budget_name        = aws_budgets_budget.monthly.name
  action_type        = "APPLY_IAM_POLICY"
  approval_model     = "AUTOMATIC"
  notification_type  = "ACTUAL"
  execution_role_arn = aws_iam_role.budget_action.arn

  action_threshold {
    action_threshold_type  = "PERCENTAGE"
    action_threshold_value = 100
  }

  definition {
    iam_action_definition {
      policy_arn = aws_iam_policy.deny_new_spend.arn
      roles      = tolist(data.aws_iam_roles.admin_sso.names)
    }
  }

  subscriber {
    address           = local.alert_email
    subscription_type = "EMAIL"
  }
}

# Makes the deny target explicit after every apply (the data-source lookup is
# otherwise invisible). Prints the SSO role the deny policy lands on.
output "deny_action_target_role" {
  description = "SSO role the budget deny policy attaches to at 100%."
  value       = tolist(data.aws_iam_roles.admin_sso.names)
}
