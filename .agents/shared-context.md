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
- Code Reviewer has no blocking findings.
- Documentation is updated when behavior, setup, architecture, or public API changes.

## Controller-Maintained Notes

The Loop Controller must update this section when project decisions, constraints, endpoints, workflows, or known risks change.

### Decisions

- 2026-09-12: Agents live under the project root in `.agents/`.
- 2026-09-12: Loop Controller only orchestrates and updates shared context; it must not edit production code.
- 2026-09-12: Developer may make implementation changes without additional approval inside the agreed MVP 1 scope.
- 2026-09-12: QA may create tests when gaps are found, but should not change production code without surfacing the issue.
- 2026-09-12: Code Reviewer is blocking for serious bugs and regressions.
- 2026-09-12: Agent instructions and project docs should be written in English.
- 2026-09-12: Phase 1 uses Spring Boot 3.3.5 because the MVP stack is locked to Spring Boot 3.x.
- 2026-09-12: The Spring Initializr was used only to generate the project skeleton and Gradle Wrapper; generated Spring Boot 4 settings were adjusted back to Spring Boot 3.x.

### Current Milestone

MVP 1 - Risk Decision API.

### Current Status

Phase 1 - Bootstrap is completed.

Implemented:

- Gradle project with Gradle Wrapper.
- Java 21 toolchain.
- Spring Boot 3.3.5.
- PostgreSQL Docker Compose service.
- `application.yml` datasource, JPA, Flyway, and server configuration.
- Flyway migration directory.
- Modular package skeleton.
- Git repository initialized.

Validated:

- `./gradlew build` passes.
- `./gradlew test` passes.
- `docker compose up -d postgres` starts PostgreSQL.
- `docker compose ps` reports PostgreSQL healthy.
- `docker compose exec postgres pg_isready -U fraudshield -d fraudshield` reports accepting connections.
- `./gradlew bootRun --args='--server.port=0'` starts successfully, connects to PostgreSQL, runs Flyway, initializes JPA, and starts Tomcat.

Review:

- QA status: PASS.
- Code Reviewer status: APPROVED.

### Open Risks

- Port `8080` was already in use during local validation. Use `./gradlew bootRun --args='--server.port=0'` or free port `8080` when needed.
- Flyway is enabled, but no migrations exist yet. This is expected until Phase 3.
- Database schema and API contracts are documented but not implemented.
- Test strategy is defined at a high level but not wired into the build yet.

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
