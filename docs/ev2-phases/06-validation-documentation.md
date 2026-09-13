# EV2 Phase 6 - Validacao Executavel E Documentacao

## Goal

Fechar a EV2 com testes automatizados, validacao funcional e documentacao suficiente para demonstrar os novos fluxos.

## Outcomes

- Testes unitarios das novas regras.
- Testes de integracao para idempotencia.
- Testes de integracao para consultas.
- Testes de integracao para reavaliacao.
- Testes de integracao para velocity checks.
- README atualizado com exemplos da EV2.
- Limitacoes e proximos passos documentados.

## Scope

Included:

- Testcontainers PostgreSQL.
- Cenarios HTTP para novos endpoints.
- Assertions de persistencia.
- Assertions de ausencia de duplicidade.
- Assertions de historico.
- Exemplos de `curl`.

Excluded:

- CI completo.
- Testes de carga.
- Testes de seguranca automatizados.
- Testes de contrato com sistemas externos.
- Frontend.

## Suggested Files

```text
README.md
src/test/java/com/fraudshield/transactional/transaction/api/TransactionIdempotencyIT.java
src/test/java/com/fraudshield/transactional/transaction/api/TransactionDecisionQueryIT.java
src/test/java/com/fraudshield/transactional/customer/api/CustomerRiskDecisionHistoryIT.java
src/test/java/com/fraudshield/transactional/transaction/api/TransactionReevaluationIT.java
src/test/java/com/fraudshield/transactional/risk/application/RecentActivityFeatureServiceIT.java
src/test/java/com/fraudshield/transactional/risk/rules/
```

## Required Integration Scenarios

- First evaluation creates original decision.
- Equivalent retry returns existing decision.
- Conflicting retry returns `409`.
- Query by transaction returns latest decision.
- Query unknown transaction returns `404`.
- Customer history returns decisions newest-first.
- Customer history enforces max limit.
- Reavaliating existing transaction creates new decision.
- Reavaliating unknown transaction returns `404`.
- Recent transactions affect velocity features.
- Transactions outside the window do not affect velocity features.

## README Requirements

The README should include:

- EV2 objective.
- New endpoints.
- Idempotency behavior.
- Reavaliation behavior.
- Query examples.
- New rules and thresholds.
- Feature snapshot explanation.
- Local run commands.
- Test commands.
- Known EV2 exclusions.

## Validation Commands

Run:

```bash
./gradlew test
./gradlew build
docker compose up -d postgres
./gradlew bootRun
```

Manual smoke examples:

```bash
curl http://localhost:8080/transactions/tx-001/decision
curl 'http://localhost:8080/customers/cus-123/risk-decisions?limit=20'
curl -X POST http://localhost:8080/transactions/tx-001/reevaluate
```

## Acceptance Criteria

- All EV2 tests pass.
- New APIs are covered by integration tests.
- New rules are covered by unit tests.
- Idempotency behavior is covered and documented.
- Reavaliation preserves audit history.
- README is enough for local demonstration.
- Out-of-scope items remain unimplemented.
