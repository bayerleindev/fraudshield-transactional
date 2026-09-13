# EV2 Phase 3 - Snapshot De Features

## Goal

Persistir as features usadas em cada decisao para tornar a explicabilidade reproduzivel mesmo quando o contexto do cliente, dispositivo, beneficiario ou historico muda depois.

## Outcomes

- Modelo para snapshot de features.
- Persistencia do snapshot junto da decisao.
- Response de consulta inclui features relevantes.
- Reavaliacoes podem ter snapshots diferentes.

## Scope

Included:

- Features da EV1:
  - device known.
  - device trusted.
  - beneficiary known.
  - account age in days.
  - hours since last password change.
- Features da EV2:
  - transactions in recent window.
  - amount in recent window.
  - new beneficiaries in recent window.
  - denied attempts in recent window.
- Persistencia em coluna JSONB ou tabela relacional simples.

Excluded:

- Feature store dedicada.
- Streaming de features.
- Versionamento complexo de schema de features.
- Dados derivados por ML.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/risk/domain/RiskFeatureSnapshot.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskFeatureSnapshotEntity.java
src/main/java/com/fraudshield/transactional/risk/infra/RiskFeatureSnapshotRepository.java
src/main/resources/db/migration/V9__create_risk_feature_snapshots_table.sql
```

## Storage Decision

Recommended EV2 behavior:

- Use a relational table with `feature_name`, `feature_value` and `risk_decision_id`.

Reason:

- Keeps the project simple and database-portable.
- Avoids needing JSON mapping details in the first version.
- Makes tests and inspection straightforward.

Alternative:

- Use PostgreSQL `jsonb` if the implementation wants a compact snapshot.

If choosing `jsonb`, document the decision in `.agents/shared-context.md`.

## Snapshot Timing

Features should be captured before rule evaluation and persisted with the decision produced from those exact values.

Rules:

- Do not recompute features when returning historical decisions.
- Do not overwrite snapshots on revaluation.
- Store a new snapshot for each new decision.

## Acceptance Criteria

- Every successful new decision has a feature snapshot.
- Snapshot values match the context used by the risk engine.
- Query APIs can return the snapshot for a decision.
- Reavaliations preserve their own snapshots.
