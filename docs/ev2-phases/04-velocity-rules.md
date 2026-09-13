# EV2 Phase 4 - Regras De Comportamento Recente

## Goal

Adicionar sinais temporais simples de risco calculados a partir do historico persistido no PostgreSQL.

Esta fase aumenta a qualidade da decisao sem introduzir Redis, Kafka ou ML.

## Outcomes

- Servico de calculo de features recentes.
- Queries PostgreSQL para janelas temporais simples.
- Novas regras deterministicas.
- Testes unitarios das novas regras.
- Testes de integracao para features recentes.

## Scope

Included:

- Contagem de transacoes recentes por cliente.
- Soma de valores recentes por cliente.
- Contagem de novos beneficiarios recentes.
- Contagem de denies recentes.
- Regras:
  - `HIGH_FREQUENCY_TRANSACTIONS`
  - `HIGH_RECENT_AMOUNT`
  - `MULTIPLE_NEW_BENEFICIARIES`
  - `REPEATED_DENIED_ATTEMPTS`

Excluded:

- Redis.
- Sliding windows em memoria distribuida.
- Streaming.
- Agregacoes pre-computadas.
- Modelos estatisticos ou ML.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/risk/application/RecentActivityFeatureService.java
src/main/java/com/fraudshield/transactional/risk/domain/RecentActivityFeatures.java
src/main/java/com/fraudshield/transactional/risk/rules/HighFrequencyTransactionsRule.java
src/main/java/com/fraudshield/transactional/risk/rules/HighRecentAmountRule.java
src/main/java/com/fraudshield/transactional/risk/rules/MultipleNewBeneficiariesRule.java
src/main/java/com/fraudshield/transactional/risk/rules/RepeatedDeniedAttemptsRule.java
src/test/java/com/fraudshield/transactional/risk/rules/
```

## Suggested Thresholds

`HIGH_FREQUENCY_TRANSACTIONS`:

- More than 5 transactions in 10 minutes.
- Impact: 20.

`HIGH_RECENT_AMOUNT`:

- Total amount >= 15000 in 10 minutes.
- Impact: 25.

`MULTIPLE_NEW_BENEFICIARIES`:

- 3 or more new beneficiaries in 24 hours.
- Impact: 20.

`REPEATED_DENIED_ATTEMPTS`:

- 2 or more `DENY` decisions in 24 hours.
- Impact: 30.

## Design Notes

- Keep thresholds as constants in rule classes for EV2.
- Keep rule version as `v2`.
- Queries should filter by customer and timestamp.
- Add indexes needed by query paths before relying on these checks.

## Required Tests

- Each new rule triggers at the configured threshold.
- Each new rule does not trigger below threshold.
- Combined EV1 and EV2 rules produce expected score.
- Recent activity service returns deterministic counts and sums.
- Old transactions outside the window are ignored.

## Acceptance Criteria

- Recent behavior features are calculated from PostgreSQL.
- Rules remain deterministic.
- Risk engine remains independent from repositories.
- New reasons include code, description and impact.
- Tests cover threshold boundaries.
