# Backend Best Practices

## Goal

Keep FraudShield Transactional clean, demonstrable, and credible as a backend portfolio project.

The target rigor is portfolio: intentional architecture, meaningful tests, and realistic tradeoffs without excessive enterprise ceremony.

## Architecture Rules

- Start as a modular monolith.
- Keep package boundaries aligned to domain language.
- Keep application services responsible for orchestration.
- Keep domain logic independent from HTTP and persistence when practical.
- Do not introduce microservices in MVP 1.
- Do not add Kafka, Redis, ML, graph detection, or authentication unless explicitly requested.

## Package Boundaries

Expected direction:

```text
transaction/
risk/
customer/
device/
beneficiary/
audit/
shared/
```

Rules:

- `transaction/api` owns HTTP request and response models.
- `transaction/application` owns transaction evaluation orchestration.
- `transaction/domain` owns transaction concepts.
- `risk/domain` owns risk decisions, reasons, and assessment concepts.
- `risk/application` owns risk engine coordination.
- `risk/rules` owns deterministic rule implementations.
- `infra` packages own database mappings and repository adapters.
- `shared` should stay small and boring.

## Coding Rules

- Prefer explicit code over clever abstractions.
- Use records for immutable DTO/domain data when appropriate.
- Use enums for stable operational values.
- Avoid free-form strings for decision and reason codes.
- Keep methods small enough to test easily.
- Add comments only when they clarify non-obvious decisions.
- Do not mix validation, scoring, persistence, and HTTP mapping in the same class.

## Validation Rules

- Validate external input at API boundaries.
- Enforce important domain invariants in domain constructors or factories.
- Keep error handling consistent.
- Do not rely on database constraints as the only validation layer.

## Persistence Rules

- Use Flyway for schema changes.
- Do not use Hibernate auto-DDL for real schema creation.
- Keep `spring.jpa.hibernate.ddl-auto=validate`.
- Add indexes for lookup paths used by the application.
- Keep audit persistence reliable and explicit.

## Testing Rules

Test pyramid direction:

- Unit tests for domain and risk logic.
- Integration tests for API and persistence.
- Testcontainers for database-backed integration tests.

Rules:

- Every risk rule should have focused tests.
- Decision thresholds should have boundary tests.
- API validation should have negative tests.
- Persistence behavior should be verified once repositories exist.
- Tests should own their data and avoid hidden dependencies.

## Documentation Rules

Update docs when changing:

- public API contract
- risk rules
- decision policy
- local run commands
- database schema
- agent workflow
- accepted architectural decisions

Keep docs practical. Prefer examples and acceptance criteria over long essays.

## Git Rules

- Keep commits focused.
- Use clear commit messages.
- Do not mix unrelated phases in one commit when avoidable.
- Run relevant tests before committing.
- Every development cycle must happen on its own pushed branch before handoff.
- Use a branch type that matches the work: `feature/*` for new behavior, `bugfix/*` for defect corrections, and `hotfix/*` for urgent production-style fixes.
- Push the branch to the remote after implementation and validation so reviewers and follow-up agents can inspect the exact delivered state.
- Mention known limitations in the final handoff.

## Review Rules

Code review should prioritize:

- behavioral correctness
- incorrect fraud decisions
- missing audit trail
- validation gaps
- persistence risks
- test gaps
- architecture drift

Style-only comments should not block unless they affect maintainability.

## Acceptance Criteria

Backend quality is acceptable when:

- The code matches the documented architecture.
- The behavior is covered by relevant tests.
- Domain decisions are explicit.
- Operational fields are stable.
- The project remains understandable to a new reviewer.
