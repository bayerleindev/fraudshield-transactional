# Security Rules

## Goal

Keep FraudShield Transactional safe by default while preserving the MVP 1 scope.

Security decisions should protect:

- transaction data
- customer identifiers
- device identifiers
- beneficiary identifiers
- IP addresses
- audit integrity
- local development secrets

## MVP 1 Position

Authentication and authorization are out of MVP 1 unless explicitly requested.

Even without auth, the code should avoid unsafe patterns that would be expensive to undo later.

## Data Handling Rules

Treat these fields as sensitive:

- `customerId`
- `beneficiaryId`
- `deviceId`
- `ipAddress`
- transaction amount
- transaction timestamp
- risk decision and reasons

Do not log full request payloads by default.

If payload logging is needed locally, mask or omit sensitive fields.

## Secrets Rules

- Do not commit real secrets.
- Local Docker Compose credentials may use obvious development values.
- Production-like credentials must come from environment variables or secret management.
- Do not put tokens, private keys, or cloud credentials in docs, tests, or examples.

## API Validation Rules

All external inputs must be validated at the boundary.

For MVP 1:

- Required identifiers must not be blank.
- `amount` must be greater than zero.
- `currency` is limited to `BRL`.
- `paymentMethod` must be a known enum value.
- `occurredAt` must be present.

Future validation:

- IP address format validation.
- Request timestamp skew checks.
- Payload size limits.
- Idempotency key validation.

## Error Handling Rules

Error responses should be useful but not revealing.

Do:

- Return clear validation errors.
- Use stable error codes when practical.
- Log internal exception details server-side.

Do not:

- expose stack traces
- expose SQL errors
- expose configuration values
- reveal whether sensitive identifiers exist unless the API contract requires it

## Audit Integrity

Fraud decisions must be auditable.

Rules:

- A successful evaluation must persist its decision.
- A successful evaluation must persist its reasons.
- A decision must include `rulesVersion`.
- Audit updates should be append-only where possible in later phases.
- Manual correction or replay should create new records instead of silently overwriting history.

## Dependency Rules

- Prefer Spring Boot managed dependency versions.
- Avoid adding libraries without a clear need.
- Do not add dependencies for MVP-excluded features unless explicitly requested.
- Keep database driver, Flyway, and framework dependencies current within the chosen Spring Boot line.

## Local Development Rules

- Docker Compose credentials are development-only.
- Do not reuse local passwords outside this project.
- Do not expose local services beyond localhost unless necessary.
- Avoid committing generated database data or local IDE state.

## Future Security Work

Post-MVP candidates:

- Authentication.
- Authorization.
- API client credentials.
- Rate limiting.
- Idempotency enforcement.
- Request signing for trusted producers.
- Field-level masking in logs.
- Security headers.
- Dependency vulnerability scanning.

## Acceptance Criteria

Security posture is acceptable when:

- Inputs are validated.
- Sensitive data is not casually logged.
- Secrets are not committed.
- Audit records are reliable.
- Errors do not expose internals.
- Out-of-scope security features are documented for future phases.
