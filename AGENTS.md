# FraudShield Transactional

## Project Goal

Build a transactional anti-fraud backend that receives financial transaction events, calculates risk through deterministic rules, returns an explainable decision, and persists the full audit trail.

The MVP 1 flow is:

```text
Transaction Request -> Feature Extraction -> Rule Evaluation -> Risk Score -> Decision -> Audit Log
```

## MVP 1 Scope

Implement a REST API for transaction risk evaluation:

- `POST /transactions/evaluate`
- Decisions: `APPROVE`, `CHALLENGE`, `REVIEW`, `DENY`
- Explainable reasons for each decision
- Deterministic in-memory rules
- PostgreSQL persistence
- Flyway migrations
- Unit tests for the risk engine
- Integration tests for the main endpoint
- Docker Compose for local infrastructure

Keep Kafka, Redis, ML, dashboards, graph detection, authentication, Kubernetes, and full observability out of MVP 1 unless explicitly requested.

## Preferred Stack

- Java 21
- Spring Boot 3.x
- Gradle
- PostgreSQL
- Flyway
- JUnit 5
- AssertJ
- Testcontainers
- Docker Compose

## Architecture Direction

Start as a modular monolith with package boundaries aligned to the domain:

```text
src/main/java/com/fraudshield/transactional/
  transaction/
  risk/
  customer/
  device/
  beneficiary/
  audit/
  shared/
```

Prefer domain clarity and explicit orchestration over premature microservices.

## Initial Risk Rules

- `HIGH_AMOUNT`: amount >= 5000, impact 30
- `VERY_HIGH_AMOUNT`: amount >= 20000, impact 50
- `NEW_DEVICE`: device not associated with customer, impact 20
- `UNTRUSTED_DEVICE`: known but untrusted device, impact 15
- `NEW_BENEFICIARY`: beneficiary not known for customer, impact 25
- `RECENT_PASSWORD_CHANGE`: password changed in the last 24 hours, impact 20
- `NEW_ACCOUNT`: customer account created in the last 7 days, impact 20

## Decision Policy

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Implementation Order

1. Bootstrap Spring Boot project, Gradle, PostgreSQL Docker Compose, Flyway, and application config.
2. Create domain enums, entities, request/response DTOs, and validation.
3. Add database migrations and repositories.
4. Implement `RiskRule`, initial rules, `RiskEngine`, and `DecisionPolicy`.
5. Implement `TransactionEvaluationController` and application service.
6. Add unit and integration tests.
7. Add README with run instructions and API examples.

## Reference Docs

The detailed MVP roadmap lives at:

- `docs/mvp-1-roadmap.md`
- `docs/mvp-1-phases/`

## Agent Workflow

Project-specific agents live under `.agents/`:

- `.agents/loop-controller.md`: orchestrates the delivery loop and updates shared context.
- `.agents/developer.md`: implements code, tests, configuration, and documentation.
- `.agents/qa.md`: validates behavior and creates tests when gaps are found.
- `.agents/code-reviewer.md`: reviews changes and blocks serious issues.
- `.agents/shared-context.md`: shared project memory maintained by the Loop Controller.

Agent files use YAML frontmatter for discoverability and plain Markdown for operating instructions.

Use the Loop Controller as the entry point for non-trivial implementation tasks.
