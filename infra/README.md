# Infrastructure

Terraform for running the CI Failure Analyst on AWS. Two stacks:

| Stack | What's in it | Lifecycle |
|---|---|---|
| **`base/`** | ECR repo, DynamoDB table, SSM secrets, cost budget + deny action | Apply once, **leave up** (~$0 idle) |
| **`ecs/`** | IAM roles, ECS cluster, Fargate task def + service, security group | **Ephemeral** — `just up` / `just down` per session (Fargate is metered) |

`ecs/` reads `base/`'s resources by name (data sources), so the two stacks share no state.

## Day-to-day (Justfile, repo root)

```
just up       # apply ecs/ (desired=1), ingress locked to your current IP
just status   # service counts + the task's public URL
just logs      # tail the app's CloudWatch logs
just pause    # desired=0 — stops Fargate billing, keeps the stack
just down     # destroy the ecs/ stack
just down-base # DANGER: destroy base/ (data + secrets + guardrails) — guarded
```

## How the app actually runs: the "two badges" model

An **IAM role is a badge on a hook** — a named set of permissions ("which doors it
opens"), plus a note saying **who is allowed to wear it**. A badge does nothing on
its own; someone has to put it on (*assume* it) to get its powers, temporarily.

We define **two badges**, both wearable only by **Fargate** (the ECS managed service,
principal `ecs-tasks.amazonaws.com`):

- 🎬 **execution role** — opens ECR (pull image) + SSM (read & decrypt secrets). Worn
  by the *platform* to **set the container up**.
- 🏃 **task role** — opens DynamoDB (`GetItem/PutItem/Query/Scan` on the table only).
  Worn by the *running app* as its **runtime identity** (no static keys — temp creds
  come from the Fargate metadata endpoint).

The **task definition is a work order** naming which badges this job wears and which
image to run.

```
  DEFINED IN TERRAFORM (declarations in your repo)
  ─────────────────────────────────────────────────────────────

  iam.tf  ── hangs two badges on hooks ──────────────┐
                                                      │
   🎬 badge "ecs-execution"          🏃 badge "ecs-task"
      doors it opens:                   doors it opens:
        • pull image (ECR)                • read/write DynamoDB
        • read secrets (SSM)            note: "only Fargate
      note: "only Fargate                      may wear me"
             may wear me"                  (assume_role_policy)
        (assume_role_policy)

  task.tf  ── the WORK ORDER ────────────────────────┐
      • run image:  <ECR repo>:latest                │
      • wear:       execution_role_arn  → 🎬          │
      • wear:       task_role_arn       → 🏃          │


  AT LAUNCH  (you run `just up`)
  ─────────────────────────────────────────────────────────────

        Fargate reads the work order
                 │
                 ├─ grabs 🎬 (allowed: badge names Fargate)
                 │        │
                 │        ├─🚪→ ECR   : pull the app image
                 │        └─🚪→ SSM   : read + decrypt 3 secrets
                 │                       └─ inject into container as env vars
                 │
                 │   ░░░ container now assembled & started ░░░
                 │
                 └─ hands 🏃 to the running app
                          │
                          └─🚪→ DynamoDB : GetItem / PutItem / Query / Scan
                                           (app's runtime calls)


  WHO DOES WHAT
  ─────────────────────────────────────────────────────────────
   caller (Fargate / the app)  → makes the request, wearing a badge
   door   (ECR / SSM / DynamoDB)→ the service doing the actual work
   guard  (IAM / STS)          → checks the badge before any door opens
```

**Two moments, two badges:** 🎬 sets the container up (pull + secrets) *before* it
runs; 🏃 is the app's identity *while* it runs. The whole role story lives in just two
files — `ecs/iam.tf` (the badges) and `ecs/task.tf` (the work order).

## Secrets

Three SSM SecureStrings under `/ci-failure-analyst/` (`github-token`,
`github-webhook-secret`, `results-password`). Values are **never** in Terraform source
— each param seeds a placeholder; the real value is set out-of-band:

```
aws ssm put-parameter --name /ci-failure-analyst/<name> --type SecureString --overwrite --value <value>
```

The execution role injects them into the container at launch via the task def
`secrets:` block (not `environment:`).
