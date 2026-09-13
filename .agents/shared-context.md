---
name: shared-context
role: shared_memory
entrypoint: false
owned_by: loop-controller
can_modify_code: false
updated_by:
  - loop-controller
read_by:
  - loop-controller
  - developer
  - qa
  - code-reviewer
---

# Shared Context

## Project

FraudShield Transactional is a backend portfolio project for transactional anti-fraud decisioning.

The MVP 1 goal is to expose a REST API that receives a financial transaction, calculates risk through deterministic rules, returns an explainable decision, and persists the full audit trail.

Core flow:

```text
Transaction Request -> Feature Extraction -> Rule Evaluation -> Risk Score -> Decision -> Audit Log
```

## Current Product Scope

MVP 1 includes:

- Transaction risk evaluation endpoint.
- Deterministic in-memory risk rules.
- Explainable decisions with reason codes.
- PostgreSQL persistence.
- Flyway migrations.
- Unit tests for risk logic.
- Integration tests for the main endpoint.
- Docker Compose for local infrastructure.
- README and project documentation.

MVP 1 excludes:

- Kafka.
- Redis.
- Machine learning.
- Frontend dashboard.
- Manual review workflow.
- Graph-based fraud detection.
- Authentication and authorization.
- Kubernetes deployment.
- Full observability stack.

## Confirmed Stack

- Java 21
- Spring Boot 3.x
- Gradle
- PostgreSQL
- Flyway
- Docker Compose
- JUnit 5
- AssertJ
- Testcontainers

## Architecture

Use a modular monolith with pragmatic DDD boundaries.

Expected package direction:

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

Application services orchestrate use cases. Domain packages should remain cohesive. Avoid premature service extraction.

## Risk Decisions

Allowed decisions:

- `APPROVE`
- `CHALLENGE`
- `REVIEW`
- `DENY`

Decision policy:

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Initial Risk Rules

- `HIGH_AMOUNT`: amount >= 5000, impact 30
- `VERY_HIGH_AMOUNT`: amount >= 20000, impact 50
- `NEW_DEVICE`: device not associated with customer, impact 20
- `UNTRUSTED_DEVICE`: known but untrusted device, impact 15
- `NEW_BENEFICIARY`: beneficiary not known for customer, impact 25
- `RECENT_PASSWORD_CHANGE`: password changed in the last 24 hours, impact 20
- `NEW_ACCOUNT`: customer account created in the last 7 days, impact 20

## Quality Bar

Project rigor: portfolio.

This means the code should be clean, demonstrable, tested, and architecturally intentional without adding enterprise-level ceremony too early.

## Completion Criteria For A Task

A task can be considered done when:

- The intended behavior is implemented.
- The build passes.
- Relevant tests pass.
- QA approves the behavior or only raises non-blocking concerns.
- Functional Tester passes, or explicitly reports `NOT_APPLICABLE` for phases with no functional surface.
- Security Reviewer has no blocking findings.
- Code Reviewer has no blocking findings.
- Documentation is updated when behavior, setup, architecture, or public API changes.

## Controller-Maintained Notes

The Loop Controller must update this section when project decisions, constraints, endpoints, workflows, or known risks change.

### Decisions

- 2026-09-12: Agents live under the project root in `.agents/`.
- 2026-09-12: Loop Controller is planning-only. It creates the implementation plan, delegates required work to Developer, QA, and Code Reviewer, and updates shared context; it must not edit production code, tests, build files, runtime configuration, or infrastructure configuration.
- 2026-09-12: For implementation tasks, Developer must be explicitly delegated before code changes are made.
- 2026-09-12: Developer may make implementation changes without additional approval inside the agreed MVP 1 scope.
- 2026-09-12: QA may create tests when gaps are found, but should not change production code without surfacing the issue.
- 2026-09-12: Code Reviewer is blocking for serious bugs and regressions.
- 2026-09-12: Agent instructions and project docs should be written in English.
- 2026-09-12: Phase 1 uses Spring Boot 3.3.5 because the MVP stack is locked to Spring Boot 3.x.
- 2026-09-12: The Spring Initializr was used only to generate the project skeleton and Gradle Wrapper; generated Spring Boot 4 settings were adjusted back to Spring Boot 3.x.
- 2026-09-12: Project guidelines for backend best practices, observability, monitoring, and security live under `docs/guidelines/` and should be consulted when relevant.
- 2026-09-12: Phase delivery loops should use real subagents when explicitly requested; the Loop Controller must report spawned subagent IDs instead of simulating Developer, QA, or Code Reviewer roles in the same agent.
- 2026-09-12: Agent instructions were aligned with official subagent guidance: keep delegated tasks narrow, avoid concurrent writes, prefer read/test/review subagents for QA and Code Reviewer, and treat empty or interrupted subagent results as inconclusive.
- 2026-09-12: Phase 4 amount-rule policy is `VERY_HIGH_AMOUNT` supersedes `HIGH_AMOUNT` for MVP 1. Transactions with amount `>= 20000` produce `VERY_HIGH_AMOUNT` and do not also produce `HIGH_AMOUNT`.
- 2026-09-12: When the user explicitly invokes the Loop Controller or requests the project agent workflow, creation of the required Developer, QA, Functional Tester, Security Reviewer, Code Reviewer, and Guideline Compliance Reviewer subagents is pre-approved. Do not ask for additional conversational confirmation before spawning them.
- 2026-09-12: The project agent loop now includes Functional Tester for executable user-facing/API behavior and Security Reviewer for MVP-appropriate security risks.
- 2026-09-12: After Developer completes implementation, QA, Functional Tester, Security Reviewer, and Code Reviewer run in parallel when their scopes are read-only or otherwise non-overlapping. The Loop Controller waits for all reports before deciding whether to send consolidated corrections back to Developer.
- 2026-09-12: For API phases, Functional Tester must start the dockerized application through Docker or Docker Compose and validate endpoints with real `curl` requests. MockMvc-only validation is insufficient when a dockerized app runtime exists.
- 2026-09-12: Phase 5 missing customer behavior is `400 CUSTOMER_NOT_FOUND`; missing customers are not treated as `NEW_ACCOUNT` risk context in MVP 1.
- 2026-09-13: API requests use `X-Correlation-Id`; the application preserves incoming values, generates one when absent, returns it in the response header, and stores it in MDC during request handling.
- 2026-09-13: MVP observability uses a single successful evaluation summary log at the application service boundary with decision, score, rules version, reason codes, and duration in milliseconds. Full request payloads, IP addresses, device IDs, and beneficiary IDs are not logged.
- 2026-09-13: The Loop Controller workflow includes Guideline Compliance Reviewer as the final strict `docs/guidelines/` gate. It runs after QA, Functional Tester, Security Reviewer, and Code Reviewer have no blockers, and its approval is required before marking a workflow complete.

### Current Milestone

MVP 1 - Risk Decision API.

### Current Status

Phase 5 - Transaction Evaluation API is completed.

Implemented:

- Gradle project with Gradle Wrapper.
- Java 21 toolchain.
- Spring Boot 3.3.5.
- PostgreSQL Docker Compose service.
- `application.yml` datasource, JPA, Flyway, and server configuration.
- Flyway migration directory.
- Modular package skeleton.
- Git repository initialized.
- Flyway migrations for `customers`, `devices`, `beneficiaries`, `transactions`, `risk_decisions`, and `risk_reasons`.
- JPA entities and repositories for customer, device, beneficiary, transaction, and audit persistence.
- Persistence tests for contextual lookups, duplicate transaction IDs, and audit decision persistence with multiple reasons.
- Deterministic in-memory risk engine with `RiskRule` contract, `RiskEvaluationContext`, `RiskEngine`, and centralized `DecisionPolicy`.
- Initial MVP 1 risk rules for high amount, very high amount, new device, untrusted device, new beneficiary, recent password change, and new account.
- Risk engine returns explainable `RiskAssessment` output with score aggregation, reason codes/descriptions/impacts, `rulesVersion = "v1"`, and the supplied evaluation timestamp.
- Unit tests for risk rule behavior, amount boundaries, score aggregation, decision thresholds, no-risk approval, REVIEW and DENY crossings, very-high amount supersession, and evaluation context invariants.
- `POST /transactions/evaluate` REST endpoint.
- `EvaluateTransactionService` orchestration for request domain mapping, customer/device/beneficiary context lookup, risk evaluation, and audit persistence.
- API error handling for validation errors, malformed request bodies, missing customer, duplicate transaction IDs, and persistence conflicts.
- Transactional persistence of transaction payload, risk decision, and risk reasons.
- Spring beans for `RiskEngine` and UTC `Clock`.
- Dockerized application runtime with a multi-stage `Dockerfile`, `.dockerignore`, and Docker Compose `app` service wired to PostgreSQL.
- Docker Compose app and PostgreSQL published ports are bound to `127.0.0.1`.
- Endpoint tests for successful evaluation, `APPROVE`, `CHALLENGE`, `REVIEW`, `DENY`, invalid payload, invalid payment method, missing customer, duplicate transaction ID, and audit persistence.
- Correlation-id filter for `X-Correlation-Id` request/response handling and MDC.
- Evaluation summary logging with `durationMs`.
- README with local Docker Compose, Gradle build/test, endpoint, correlation header, and curl examples.

Validated:

- `./gradlew build` passes.
- `./gradlew test` passes.
- `docker compose up -d postgres` starts PostgreSQL.
- `docker compose ps` reports PostgreSQL healthy.
- `docker compose exec postgres pg_isready -U fraudshield -d fraudshield` reports accepting connections.
- `./gradlew bootRun --args='--server.port=0'` starts successfully, connects to PostgreSQL, runs Flyway, initializes JPA, and starts Tomcat.
- 2026-09-12: `docker compose up -d postgres` reports PostgreSQL running.
- 2026-09-12: `./gradlew test` passes with 37 tests.
- 2026-09-12: `./gradlew bootRun --args='--server.port=0'` starts successfully with Flyway validating 7 migrations and JPA schema validation passing.
- 2026-09-12: Phase 4 `./gradlew test --tests 'com.fraudshield.transactional.risk.*'` passes.
- 2026-09-12: Phase 4 `./gradlew test` passes.
- 2026-09-12: Phase 5 `./gradlew test --tests 'com.fraudshield.transactional.transaction.api.TransactionEvaluationControllerTest'` passes.
- 2026-09-12: Phase 5 `./gradlew test` passes.
- 2026-09-12: Phase 5 `./gradlew build` passes.
- 2026-09-12: `docker compose config --services` reports `postgres` and `app`.
- 2026-09-12: `docker compose config` confirms app and PostgreSQL host port bindings use `127.0.0.1`.
- 2026-09-12: `docker compose up --build -d app` starts PostgreSQL and the application container.
- 2026-09-12: Functional Tester validated `POST /transactions/evaluate` through Docker Compose plus `curl`, including a successful request, invalid payload, missing customer, and database persistence checks.
- 2026-09-13: Guidelines audit correction `./gradlew test --tests 'com.fraudshield.transactional.transaction.api.TransactionEvaluationControllerTest'` passes.
- 2026-09-13: Guidelines audit correction `./gradlew test` passes.
- 2026-09-13: Guidelines audit correction `./gradlew build` passes.
- 2026-09-13: Dockerized functional validation with `curl` confirms incoming `X-Correlation-Id` is preserved, absent correlation IDs are generated, invalid payloads and missing customers return stable `400` errors with correlation headers, successful evaluations persist audit records, and summary logs include `durationMs`.

Review:

- QA status: PASS.
- Functional Tester status: PASS.
- Security Reviewer status: APPROVED.
- Code Reviewer status: APPROVED.
- Phase 3 real-subagent orchestration audit: Loop Controller `01a0976f-c68c-7fb3-b7ff-59b3d2bafe23`, Developer `01a09770-31e4-7670-8e02-7e830059b2d6`, QA `01a09773-3147-70f0-9d6c-f80cbd5500d5`, Code Reviewer `01a09775-5b7d-7653-a7f6-eb9bd5da04e0`.
- Phase 4 real-subagent orchestration audit: Developer `01a09785-e0c2-7423-b107-17677504113d`, QA `01a09789-5419-7823-b975-672fd74948b1`, Code Reviewer `01a0978b-3973-7a31-8710-8f0b238d67b4`.
- Phase 5 real-subagent orchestration audit: Loop Controller `01a09793-9bbd-7f80-a2b7-aabb5c03539d`, Developer `01a09795-506e-7431-ab92-a1f6ffda0ff9`, QA `01a09799-eb19-7663-8f39-7bc69c4ec9a8`, Functional Tester `01a0979b-9f42-7880-9199-57062a1c83ec`, Security Reviewer `01a0979c-a890-7702-be51-96e177690871`, Docker correction Developer `01a0979f-a5f6-71a0-b67d-4520a71874dc`, parallel QA `01a097a3-15cd-7ec0-b491-d42f2c1326f0`, parallel Functional Tester `01a097a3-1615-7211-8412-a2ad292f9aa6`, parallel Security Reviewer `01a097a3-1637-7622-a8ff-fe3c42918efa`, parallel Code Reviewer `01a097a3-1663-7f42-9eec-62c8427e158b`, consolidated correction Developer `01a097a5-f39d-72f3-a599-f2d1a7419518`, final QA `01a097a7-03af-7b71-b2f2-f1ca2abe06ce`, final Functional Tester `01a097a7-03e1-7e81-9f36-ce60903055bb`, final Security Reviewer `01a097a7-0403-7b42-a478-4487e7b28bd3`, final Code Reviewer `01a097a7-0426-7f52-ba35-3362b7db4c02`.

### Open Risks

- Port `8080` must be free for `docker compose up --build -d app` because the app service maps `127.0.0.1:8080:8080`. Use `./gradlew bootRun --args='--server.port=0'` for non-Docker local startup when needed.
- Persistence tests are wired into the build and require the Docker Compose PostgreSQL service for local validation.
- Local Testcontainers execution was blocked by the machine-level `~/.testcontainers.properties` forcing a Docker client strategy that does not work with the active Docker Desktop context. Phase 3 persistence validation used the Docker Compose PostgreSQL service instead.
- Runtime Docker image currently uses the default container user. Security Reviewer marked this non-blocking for MVP local development; consider adding a non-root runtime user in a future hardening pass.
- Incoming correlation IDs are trimmed and echoed without length or character normalization. Security Reviewer marked this non-blocking for MVP; future hardening should cap length and restrict characters.
- Successful evaluation logging currently happens before transaction commit returns. Code Reviewer marked this non-blocking for MVP; future hardening can log after commit via transaction synchronization.

### Useful References

- `AGENTS.md`
- `docs/mvp-1-roadmap.md`
- `docs/mvp-1-phases/01-bootstrap.md`
- `docs/mvp-1-phases/02-domain.md`
- `docs/mvp-1-phases/03-database.md`
- `docs/mvp-1-phases/04-risk-engine.md`
- `docs/mvp-1-phases/05-api.md`
- `docs/mvp-1-phases/06-integration-tests.md`
- `docs/mvp-1-phases/07-documentation.md`
- `docs/guidelines/README.md`
- `docs/guidelines/backend-best-practices.md`
- `docs/guidelines/observability-rules.md`
- `docs/guidelines/monitoring-rules.md`
- `docs/guidelines/security-rules.md`
