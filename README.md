# FraudShield Transactional

FraudShield Transactional is a transactional anti-fraud backend that receives financial transaction evaluations, applies deterministic risk rules, returns an explainable decision, and persists the audit trail in PostgreSQL.

Integration documentation for GitHub Pages lives in `docs/index.html`.

MVP 1 focuses on a small, runnable backend portfolio project:

```text
Transaction Request -> Feature Extraction -> Rule Evaluation -> Risk Score -> Decision -> Audit Log
```

## MVP 1 Features

- REST endpoint for transaction risk evaluation.
- Deterministic in-memory risk rules.
- Explainable decisions with reason codes, descriptions, and score impact.
- PostgreSQL persistence for transactions, risk decisions, and risk reasons.
- Flyway migrations for schema management.
- Correlation ID response header and log context.
- Unit and integration tests with JUnit 5, AssertJ, Spring Boot Test, and Testcontainers.
- Docker Compose setup for local PostgreSQL and application execution.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Gradle
- JUnit 5
- AssertJ
- Testcontainers
- Docker Compose

## Architecture

The MVP starts as a modular monolith. Packages are organized around domain boundaries so the code stays simple for local development while preserving clear ownership if the system evolves later.

```text
src/main/java/com/fraudshield/transactional/
  transaction/    REST DTOs, controller, orchestration, transaction persistence
  risk/           decision policy, risk engine, domain model, deterministic rules
  customer/       customer persistence model
  device/         device persistence model
  beneficiary/    beneficiary persistence model
  audit/          risk decision and reason persistence
  shared/         configuration, exceptions, correlation ID support
```

The main application flow is handled by `EvaluateTransactionService`: it validates duplicate transactions, loads customer/device/beneficiary context, evaluates risk, persists the transaction and audit records, and returns the API response.

## Running Locally

Requirements:

- Java 21
- Docker Desktop or another Docker daemon
- A shell that can run the Gradle wrapper

Start PostgreSQL only:

```bash
docker compose up -d postgres
```

Run the application from the host:

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

You can also build and run the app container with PostgreSQL:

```bash
docker compose up --build -d app
```

Compose binds the app and PostgreSQL to `127.0.0.1` for local development:

- API: `http://localhost:8080`
- PostgreSQL: `127.0.0.1:5432`
- Database: `fraudshield`
- User: `fraudshield`
- Password: `fraudshield`

Stop the local services:

```bash
docker compose down
```

Remove the local database volume if you want a clean schema and data set:

```bash
docker compose down -v
```

## API

### Evaluate Transaction

```text
POST /transactions/evaluate
```

Headers:

- `Content-Type: application/json`
- `X-Correlation-Id` is optional. When provided, the same value is returned. When omitted, the API generates one.

Request body:

```json
{
  "transactionId": "tx-demo-001",
  "customerId": "cus-demo-001",
  "amount": 8500.00,
  "currency": "BRL",
  "paymentMethod": "PIX",
  "beneficiaryId": "ben-demo-001",
  "deviceId": "dev-demo-001",
  "ipAddress": "177.10.20.30",
  "occurredAt": "2026-09-12T14:30:00Z"
}
```

Field notes:

- `transactionId`, `customerId`, `beneficiaryId`, `deviceId`, and `ipAddress` are required and must not be blank.
- `amount` is required and must be greater than `0`.
- `currency` is required and must be `BRL` for MVP 1.
- `paymentMethod` is required. Supported values are `PIX` and `CARD`.
- `occurredAt` is required and must be an ISO-8601 instant.
- `customerId` must already exist in the database.
- `transactionId` is unique. Reusing it returns `DUPLICATE_TRANSACTION`.

Seed a local demo customer before running the successful request:

```bash
docker exec fraudshield-postgres psql -U fraudshield -d fraudshield -c \
  "insert into customers (customer_id, created_at, last_password_change_at, status)
   values ('cus-demo-001', now() - interval '30 days', null, 'ACTIVE')
   on conflict (customer_id) do nothing;"
```

Example request:

```bash
curl -i \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: local-demo-001' \
  -d '{
    "transactionId": "tx-demo-001",
    "customerId": "cus-demo-001",
    "amount": 8500.00,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "beneficiaryId": "ben-demo-001",
    "deviceId": "dev-demo-001",
    "ipAddress": "177.10.20.30",
    "occurredAt": "2026-09-12T14:30:00Z"
  }' \
  http://localhost:8080/transactions/evaluate
```

Example response:

```json
{
  "transactionId": "tx-demo-001",
  "decision": "REVIEW",
  "score": 75,
  "reasons": [
    {
      "code": "HIGH_AMOUNT",
      "description": "Transaction amount is above the configured threshold.",
      "scoreImpact": 30
    },
    {
      "code": "NEW_DEVICE",
      "description": "Transaction originated from a device not seen before for this customer.",
      "scoreImpact": 20
    },
    {
      "code": "NEW_BENEFICIARY",
      "description": "Customer has no previous relationship with this beneficiary.",
      "scoreImpact": 25
    }
  ],
  "rulesVersion": "v1",
  "evaluatedAt": "2026-09-12T14:30:01Z"
}
```

Possible decisions:

- `APPROVE`
- `CHALLENGE`
- `REVIEW`
- `DENY`

### Get Transaction Decision

```text
GET /transactions/{transactionId}/decision
```

Returns the latest stored decision for a transaction, including reasons, rule version, evaluation type, and evaluation timestamp. The response intentionally does not expose the original request payload, IP address, device ID, beneficiary ID, or internal database IDs.

Example request:

```bash
curl -i http://localhost:8080/transactions/tx-demo-001/decision
```

Example response:

```json
{
  "transactionId": "tx-demo-001",
  "decision": "REVIEW",
  "score": 75,
  "reasons": [
    {
      "code": "HIGH_AMOUNT",
      "description": "Transaction amount is above the configured threshold.",
      "scoreImpact": 30
    }
  ],
  "rulesVersion": "v1",
  "evaluationType": "ORIGINAL",
  "evaluatedAt": "2026-09-12T14:30:01Z"
}
```

### Get Customer Risk Decisions

```text
GET /customers/{customerId}/risk-decisions?limit=20
```

Returns recent decisions for a customer in descending `evaluatedAt` order. The `limit` query parameter defaults to `20` and must be between `1` and `100`.

Example request:

```bash
curl -i 'http://localhost:8080/customers/cus-demo-001/risk-decisions?limit=20'
```

Example response:

```json
[
  {
    "transactionId": "tx-demo-001",
    "decision": "REVIEW",
    "score": 75,
    "reasons": [
      {
        "code": "HIGH_AMOUNT",
        "description": "Transaction amount is above the configured threshold.",
        "scoreImpact": 30
      }
    ],
    "rulesVersion": "v1",
    "evaluationType": "ORIGINAL",
    "evaluatedAt": "2026-09-12T14:30:01Z"
  }
]
```

### Error Responses

Validation error example:

```bash
curl -i \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:8080/transactions/evaluate
```

Validation response shape:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "status": 400,
  "timestamp": "2026-09-12T14:30:01Z",
  "errors": [
    {
      "field": "amount",
      "message": "must not be null"
    }
  ]
}
```

Common error codes:

- `VALIDATION_ERROR`: request fields failed validation.
- `INVALID_REQUEST`: request JSON is malformed or contains an invalid enum/value.
- `CUSTOMER_NOT_FOUND`: the requested customer does not exist.
- `DUPLICATE_TRANSACTION`: the transaction was already evaluated.
- `DUPLICATE_OR_INVALID_REFERENCE`: persistence failed because data conflicted with existing rows or references.
- `TRANSACTION_DECISION_NOT_FOUND`: the requested transaction has no stored risk decision.

## Risk Rules

Rules are deterministic and evaluated in memory. The final score is the sum of all triggered rule impacts.

| Code | Condition | Impact |
| --- | --- | ---: |
| `HIGH_AMOUNT` | `amount >= 5000` and `< 20000` | 30 |
| `VERY_HIGH_AMOUNT` | `amount >= 20000` | 50 |
| `NEW_DEVICE` | Device is not associated with the customer | 20 |
| `UNTRUSTED_DEVICE` | Device is known for the customer but not trusted | 15 |
| `NEW_BENEFICIARY` | Beneficiary is not known for the customer | 25 |
| `RECENT_PASSWORD_CHANGE` | Customer password changed in the last 24 hours | 20 |
| `NEW_ACCOUNT` | Customer account was created in the last 7 days | 20 |

`HIGH_AMOUNT` and `VERY_HIGH_AMOUNT` are mutually exclusive in the current implementation.

## Decision Policy

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Running Tests

Run the full test suite:

```bash
./gradlew test
```

The full suite includes Spring/Testcontainers integration tests, so Docker must be running.

Run focused risk engine unit tests:

```bash
./gradlew test --tests 'com.fraudshield.transactional.risk.application.RiskEngineTest' --tests 'com.fraudshield.transactional.risk.application.DecisionPolicyTest'
```

Run the main endpoint integration test:

```bash
./gradlew test --tests 'com.fraudshield.transactional.transaction.api.TransactionEvaluationControllerIT'
```

Build the application and run all checks attached to the Gradle build lifecycle:

```bash
./gradlew build
```

## Project Structure

```text
.
  build.gradle.kts
  docker-compose.yml
  Dockerfile
  docs/
    mvp-1-roadmap.md
    mvp-1-phases/
    guidelines/
  src/main/java/com/fraudshield/transactional/
  src/main/resources/
    application.yml
    db/migration/
  src/test/java/com/fraudshield/transactional/
```

## Limitations

- Rules are deterministic and in memory.
- There are no Redis-backed velocity checks yet.
- There are no Kafka events or asynchronous workflows yet.
- There is no authentication or authorization yet.
- There is no manual review workflow yet.
- There is no graph-based fraud detection yet.
- There is no machine learning model yet.
- There is no dashboard frontend yet.
- Observability is intentionally lightweight for MVP 1 and does not include Prometheus/Grafana.
- Local Docker Compose credentials are development-only defaults.

## Roadmap

MVP 1 is documented in:

- `docs/mvp-1-roadmap.md`
- `docs/mvp-1-phases/`
- `docs/guidelines/`

Likely next steps after MVP 1 include authenticated API access, richer audit query flows, manual review operations, Redis-backed velocity rules, Kafka event publication, graph analysis, and production-grade observability.
