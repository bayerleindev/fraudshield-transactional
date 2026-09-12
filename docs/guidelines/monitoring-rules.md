# Monitoring Rules

## Goal

Define how FraudShield Transactional should be monitored as it evolves from MVP to production-like behavior.

Monitoring should detect:

- Broken availability.
- Elevated latency.
- Fraud decision anomalies.
- Persistence failures.
- Validation spikes.
- Rule behavior regressions.

## MVP 1 Position

MVP 1 does not require Prometheus, Grafana, alerting, or dashboards.

However, each phase should avoid decisions that make future monitoring difficult.

## Golden Signals

Track these signals when metrics are introduced:

- Traffic: number of evaluations per minute.
- Errors: failed evaluations and invalid requests.
- Latency: request and risk evaluation duration.
- Saturation: database connection pool usage and thread pool pressure.

## Fraud-Specific Signals

Future dashboards should include:

- Total evaluations by decision.
- Approval rate.
- Challenge rate.
- Review rate.
- Denial rate.
- Average risk score.
- Score distribution.
- Top triggered rules.
- Rule trigger rate over time.
- Validation error rate.
- Persistence failure rate.

## Alert Candidates

Potential alerts:

- Evaluation error rate above threshold.
- No evaluations received for an unexpected period.
- `DENY` rate spikes above baseline.
- `APPROVE` rate drops sharply.
- `REVIEW` queue grows after case management exists.
- Database connection pool exhaustion.
- Flyway migration failure on startup.

Do not add alerts before the signal is meaningful and measured.

## Dashboard Principles

Dashboards should be operational, not decorative.

Recommended dashboard sections:

- API health.
- Decision distribution.
- Rule activity.
- Latency percentiles.
- Error breakdown.
- Database health.

Avoid dashboards that only show vanity counts without actionable interpretation.

## SLO Candidates

Future service-level objectives:

- API availability: 99.5% for portfolio/local production-like target.
- Evaluation latency: 95% under 300 ms.
- Persistence success: 99.9% for successful evaluations.
- Decision audit completeness: 100% of successful evaluations have decision and reasons persisted.

These are target examples, not MVP 1 requirements.

## Monitoring Rules For Developers

- Expose meaningful low-cardinality dimensions.
- Do not use user-specific identifiers as metric labels.
- Keep decision enums stable.
- Keep reason codes stable.
- Make failure modes explicit so errors can be counted.
- Treat missing audit persistence as a serious operational failure.

## Acceptance Criteria

Monitoring readiness is acceptable when:

- Important future metrics are known.
- High-cardinality labels are avoided.
- Decision and reason code fields are stable enough for dashboards.
- Application failure modes can be mapped to operational signals.
