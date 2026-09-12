# Observability Rules

## Goal

Make FraudShield Transactional easy to understand during development, debugging, and future production-like operation.

Observability should answer:

- What happened?
- Where did it happen?
- Why did the system make that fraud decision?
- How long did the request take?
- Which customer, transaction, rule version, and decision were involved?

## MVP 1 Position

Full observability tooling is out of MVP 1. Do not add Prometheus, Grafana, OpenTelemetry collectors, Loki, Tempo, or tracing infrastructure unless explicitly requested.

MVP 1 should still prepare the code for observability through clean logging, stable identifiers, and audit-friendly structures.

## Logging Rules

Use structured, concise logs.

Every transaction evaluation should eventually log:

- `transactionId`
- `customerId`
- `decision`
- `score`
- `rulesVersion`
- `reasonCodes`
- `durationMs`

Do not log:

- raw credentials
- tokens
- secrets
- full personal documents
- full card numbers
- sensitive payloads without masking

Recommended log levels:

- `INFO`: successful evaluation summary.
- `WARN`: suspicious but expected business states, such as missing contextual data.
- `ERROR`: unexpected failures that prevent evaluation or persistence.
- `DEBUG`: local-only details for troubleshooting.

## Correlation Rules

Every request should have a correlation identifier.

Preferred order:

1. Use incoming `X-Correlation-Id` if present.
2. Generate one if absent.
3. Return it in the response headers once controllers exist.
4. Include it in logs.

For MVP 1, this can be documented and introduced when the API controller is implemented.

## Audit Vs Logs

Audit data and logs are different.

Audit:

- Must persist decision facts.
- Must support later investigation.
- Should be queryable.
- Should include decision, score, rules, reasons, and timestamps.

Logs:

- Help developers and operators diagnose runtime behavior.
- Can be sampled or rotated.
- Must not be the only source of decision history.

Never rely on logs as the source of truth for fraud decisions.

## Metrics To Prepare For

Do not implement a metrics stack in MVP 1 unless requested, but design names and dimensions with future monitoring in mind.

Future metrics:

- `fraudshield.evaluations.total`
- `fraudshield.evaluations.duration`
- `fraudshield.decisions.total`
- `fraudshield.rules.triggered.total`
- `fraudshield.validation.errors.total`
- `fraudshield.persistence.errors.total`

Useful labels:

- `decision`
- `payment_method`
- `rule_code`
- `rules_version`

Avoid high-cardinality labels:

- `transactionId`
- `customerId`
- `deviceId`
- `ipAddress`

## Tracing To Prepare For

Future trace spans should represent:

- HTTP request handling.
- Transaction normalization.
- Context lookup.
- Risk engine evaluation.
- Persistence of transaction.
- Persistence of decision and reasons.

Trace attributes can include low-cardinality fields:

- `fraud.decision`
- `fraud.score_bucket`
- `fraud.rules_version`
- `payment.method`

Do not add customer or transaction identifiers as trace attributes unless the privacy model explicitly allows it.

## Implementation Rules For Developers

- Keep risk decision explanations explicit in domain/application objects.
- Measure evaluation duration at the application service boundary once Phase 5 exists.
- Add log statements at orchestration boundaries, not inside every tiny domain object.
- Use stable reason codes instead of free-form strings as operational identifiers.
- Prefer a single evaluation summary log over noisy per-rule logs.

## Acceptance Criteria

Observability is acceptable when:

- Decision data is audit-friendly.
- Logs can explain request flow without leaking sensitive data.
- Future metrics and tracing can be added without rewriting the domain.
- High-cardinality identifiers are kept out of metric labels.
