# Phase 6 - Integration Tests

## Goal

Validate the full API and persistence behavior using realistic application wiring and PostgreSQL through Testcontainers.

## Outcomes

- Testcontainers PostgreSQL is configured.
- Main endpoint is covered end to end.
- Database persistence is verified.
- Invalid payload behavior is verified.
- Regression scenarios exist for all decision outcomes.

## Scope

Included:

- Spring Boot integration tests.
- PostgreSQL Testcontainers setup.
- HTTP-level tests for `POST /transactions/evaluate`.
- Database assertions through repositories or JDBC.
- Validation error tests.

Excluded:

- Load testing.
- Contract testing with external consumers.
- Kafka integration tests.
- Redis integration tests.
- Browser or frontend tests.

## Suggested Files

```text
src/test/java/com/fraudshield/transactional/support/PostgresIntegrationTest.java
src/test/java/com/fraudshield/transactional/transaction/api/TransactionEvaluationControllerIT.java
```

## Recommended Test Style

Use Spring Boot integration tests with either:

- `MockMvc`, for fast HTTP-style controller tests with full application context.
- `TestRestTemplate`, for real HTTP server tests.

For MVP 1, `MockMvc` plus Testcontainers is enough.

## Required Scenarios

- Low-risk transaction returns `APPROVE`.
- High amount returns `CHALLENGE`.
- High amount, new device, and new beneficiary returns `REVIEW`.
- Very high amount plus enough additional risk returns `DENY`.
- Missing `transactionId` returns `400`.
- `amount <= 0` returns `400`.
- Successful evaluation persists one transaction row.
- Successful evaluation persists one risk decision row.
- Successful evaluation persists all risk reason rows.

## Test Data

Each test should control its own data:

- Insert customer records needed by the scenario.
- Insert known devices and beneficiaries only when the scenario requires them.
- Avoid relying on global seed data unless the seed is explicitly part of the test setup.

## Validation Commands

Run:

```bash
./gradlew test
./gradlew build
```

## Acceptance Criteria

- Integration tests are deterministic.
- Tests do not depend on a locally running PostgreSQL container.
- Every decision status has at least one integration test.
- Persistence is verified for successful evaluations.
- Invalid payload behavior is verified.
