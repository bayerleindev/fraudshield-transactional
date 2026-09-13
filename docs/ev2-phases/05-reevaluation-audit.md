# EV2 Phase 5 - Reavaliacao Auditavel

## Goal

Permitir que uma transacao ja avaliada seja reavaliada de forma controlada, criando uma nova decisao auditavel sem apagar ou alterar a decisao original.

## Outcomes

- Endpoint de reavaliacao.
- Modelo distingue decisao original de reavaliacao.
- Historico preserva multiplas decisoes para uma transacao.
- Cada reavaliacao recebe novo snapshot de features.
- Query APIs conseguem exibir historico coerente.

## Scope

Included:

- `POST /transactions/{transactionId}/reevaluate`.
- Campo `evaluationType`.
- Campo opcional `previousRiskDecisionId` ou equivalente.
- Nova decisao vinculada a transacao existente.
- Novos reasons e snapshot para cada reavaliacao.

Excluded:

- Workflow de aprovacao manual.
- Comentarios de analistas.
- SLA de revisao.
- Fila de casos.
- Reprocessamento em lote.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/transaction/api/TransactionReevaluationController.java
src/main/java/com/fraudshield/transactional/transaction/application/ReevaluateTransactionService.java
src/main/java/com/fraudshield/transactional/risk/domain/EvaluationType.java
src/main/resources/db/migration/V10__add_risk_decision_evaluation_type.sql
```

## Endpoint

```http
POST /transactions/{transactionId}/reevaluate
```

Recommended response:

- Same shape as transaction decision detail.
- `evaluationType` must be `REEVALUATION`.

## Reavaliation Rules

- Transaction must already exist.
- Reavaliating must not create a new transaction row.
- Reavaliating must create a new risk decision row.
- Reavaliating must create new risk reason rows.
- Reavaliating must create a new feature snapshot.
- Original decision must remain queryable through customer history.

## Data Model Notes

Add to `risk_decisions`:

- `evaluation_type`
- `previous_risk_decision_id` if useful for lineage.

Recommended values:

- `ORIGINAL`
- `REEVALUATION`

## Acceptance Criteria

- Existing transaction can be reavaliated.
- Unknown transaction returns `404`.
- Reavaliations do not overwrite original decisions.
- Customer history includes original and reavaliated decisions.
- Latest decision lookup returns the newest decision unless documented otherwise.
