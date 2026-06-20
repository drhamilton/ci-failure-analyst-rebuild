# CI Failure Analyst

A Spring Boot service that ingests CI build webhooks, fetches the failing build's
logs, analyzes them, and stores the results for retrieval.

It's structured as a small hexagonal-style app: a provider-agnostic `core` module
defines the domain ports (`WebhookParser`, `BuildLogFetcher`, `FailureAnalyzer`,
`AnalysisResultStore`, `Notifier`), and the `app` module wires in the concrete
adapters (GitHub/Buildkite webhook parsing, GitHub log fetching, Google Cloud
Datastore persistence).

## How it works

1. A CI provider POSTs a webhook to `/webhook/{provider}`.
2. The matching `WebhookParser` turns the payload into a `BuildEvent`.
3. `BuildLogFetcher` pulls the build log (GitHub Actions job logs via the REST API).
4. `FailureAnalyzer` inspects the log and produces an `AnalysisResult`.
5. The result is persisted and exposed at `GET /results`.

## Endpoints

| Method | Path                  | Description                          |
|--------|-----------------------|--------------------------------------|
| POST   | `/webhook/{provider}` | Ingest a webhook (`github`, etc.)    |
| GET    | `/results`            | List stored analysis results         |
| GET    | `/actuator/health`    | Health check                         |

## Requirements

- JDK 21
- A Google Cloud Datastore backend (or the Datastore emulator for local runs)

## Configuration

Configuration lives in `app/src/main/resources/application.properties`. Key settings:

| Property                                    | Default                  | Notes                          |
|---------------------------------------------|--------------------------|--------------------------------|
| `spring.cloud.gcp.datastore.project-id`     | `ci-failure-analyst`     | GCP project / Datastore namespace |
| `github.api.base-url`                        | `https://api.github.com` | GitHub REST API base URL       |
| `github.token`                               | `${GITHUB_TOKEN:}`       | GitHub token for log fetching  |

Set the GitHub token via environment variable — no secrets are committed:

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

## Running locally

The `local` profile enables the Datastore emulator:

```bash
# start the Datastore emulator (gcloud SDK)
gcloud beta emulators datastore start

# run the app against it
./gradlew :app:bootRun --args='--spring.profiles.active=local'
```

## Build & test

```bash
./gradlew build      # compile + run tests
./gradlew :app:test  # app module tests only
```

## Docker

```bash
docker build -t ci-failure-analyst .
docker run -p 8080:8080 -e GITHUB_TOKEN=ghp_... ci-failure-analyst
```

## License

[MIT](LICENSE)
