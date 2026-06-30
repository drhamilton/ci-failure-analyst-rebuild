# CI Failure Analyst

A Spring Boot service that ingests CI build webhooks, fetches the failing build's
logs, analyzes them, archives the raw log, and stores the results for retrieval.

It's structured as a small hexagonal-style app: a provider-agnostic `core` module
defines the domain ports (`WebhookParser`, `BuildLogFetcher`, `FailureAnalyzer`,
`AnalysisResultStore`, `LogArchive`, `Notifier`), and the `app` module wires in the
concrete adapters. Persistence and log archiving are pluggable via Spring profiles —
the default profile runs on Google Cloud Datastore, and the `aws` profile runs on
DynamoDB + S3.

## How it works

1. A CI provider POSTs a webhook to `/webhook/{provider}`.
2. The request signature is verified (HMAC), then the matching `WebhookParser`
   turns the payload into a `BuildEvent`.
3. `BuildLogFetcher` pulls the build log (GitHub Actions job logs via the REST API,
   with retry/backoff on transient failures).
4. `LogArchive` stores the raw log and returns a key.
5. `FailureAnalyzer` inspects the log and produces an `AnalysisResult` (tagged with
   the archived log's key).
6. The result is persisted and exposed at `GET /results`. A `Notifier` is invoked
   via an outbox relay so notifications survive transient failures.

## Endpoints

| Method | Path                  | Description                              |
|--------|-----------------------|------------------------------------------|
| POST   | `/webhook/{provider}` | Ingest a webhook (`github`, `buildkite`) |
| GET    | `/results`            | List stored analysis results (basic auth)|
| GET    | `/actuator/health`    | Health check                             |

## Requirements

- JDK 21
- A persistence backend, depending on profile:
  - **default** — Google Cloud Datastore (or the Datastore emulator for local runs)
  - **`aws`** — DynamoDB + S3 (or LocalStack for local runs)

## Configuration

Configuration lives in `app/src/main/resources/`, split by profile:
`application.properties` (default), `application-local.properties` (Datastore
emulator), and `application-aws.properties` (DynamoDB/S3). Key settings:

| Property                                | Default                  | Notes                                   |
|-----------------------------------------|--------------------------|-----------------------------------------|
| `spring.cloud.gcp.datastore.project-id` | `ci-failure-analyst`     | GCP project / Datastore namespace (default profile) |
| `aws.region`                            | `us-east-1`              | AWS region (`aws` profile)              |
| `aws.dynamodb.table-name`               | `analysis_results`       | DynamoDB table (`aws` profile)          |
| `github.api.base-url`                   | `https://api.github.com` | GitHub REST API base URL                |
| `github.token`                          | `${GITHUB_TOKEN:}`       | GitHub token for log fetching           |
| `github.webhook.secret`                 | `${GITHUB_WEBHOOK_SECRET:}` | HMAC secret for webhook verification |
| `spring.security.user.name`             | `${RESULTS_USER:results}`| Basic-auth username for `/results`      |
| `spring.security.user.password`         | `${RESULTS_PASSWORD}`    | Basic-auth password (required — no default) |

Secrets are supplied via environment variables; nothing sensitive is committed:

```bash
export GITHUB_TOKEN=ghp_your_token_here
export GITHUB_WEBHOOK_SECRET=your_webhook_secret
export RESULTS_PASSWORD=choose_a_password
```

## Running locally

The `local` profile enables the Datastore emulator:

```bash
# start the Datastore emulator (gcloud SDK)
gcloud beta emulators datastore start

# run the app against it
RESULTS_PASSWORD=dev ./gradlew :app:bootRun --args='--spring.profiles.active=local'
```

To run against the AWS adapters locally, point them at LocalStack and activate the
`aws` profile (`--spring.profiles.active=aws`, with `aws.dynamodb.endpoint` set to
the LocalStack endpoint).

## Build & test

```bash
./gradlew build      # compile + run tests
./gradlew :app:test  # app module tests only
```

## Docker

```bash
docker build -t ci-failure-analyst .
docker run -p 8080:8080 \
  -e GITHUB_TOKEN=ghp_... \
  -e RESULTS_PASSWORD=... \
  ci-failure-analyst
```

## AWS deployment

Terraform for running the service on AWS lives in `infra/`, split into a near-free
`base/` stack (ECR, DynamoDB, SSM secrets, cost guardrails) that stays up, and an
ephemeral `ecs/` stack (Fargate task + service) you spin up and tear down per
session. The repo-root `Justfile` wraps the day-to-day flow:

```bash
just push     # build the image and push to ECR
just up       # apply ecs/ (Fargate desired=1), ingress locked to your IP
just status   # service counts + the task's public URL
just logs     # tail CloudWatch logs
just pause    # scale to 0 (stop Fargate billing, keep the stack)
just down     # destroy the ecs/ stack
```

See [`infra/README.md`](infra/README.md) for the stack layout and IAM model.

## License

[MIT](LICENSE)
