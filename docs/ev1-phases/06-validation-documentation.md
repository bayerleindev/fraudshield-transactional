# EV1 Phase 6 - Validacao Executavel E Documentacao

## Goal

Provar que a EV1 funciona de ponta a ponta e deixar a entrega demonstravel para outra pessoa executar localmente.

Esta fase fecha a entrega com testes, runbook e exemplos praticos.

## Outcomes

- Testes unitarios do motor de risco passando.
- Testes de integracao do endpoint principal passando.
- Persistencia auditavel validada por testes.
- Cenarios de decisao cobertos.
- README atualizado com setup e exemplos.
- Limitacoes da EV1 documentadas.

## Scope

Included:

- Testcontainers com PostgreSQL.
- Testes HTTP de `POST /transactions/evaluate`.
- Testes de validacao de payload.
- Testes de persistencia de transacao, decisao e motivos.
- Cenarios para `APPROVE`, `CHALLENGE`, `REVIEW` e `DENY`.
- README com comandos de build, testes, banco local e `curl`.

Excluded:

- Testes de carga.
- Testes E2E com frontend.
- Contract testing com consumidores externos.
- Testes de Kafka, Redis ou Kubernetes.
- Pipeline CI completo, salvo se explicitamente solicitado.

## Suggested Files

```text
README.md
src/test/java/com/fraudshield/transactional/support/PostgresIntegrationTest.java
src/test/java/com/fraudshield/transactional/transaction/api/TransactionEvaluationControllerIT.java
src/test/java/com/fraudshield/transactional/risk/application/RiskEngineTest.java
src/test/java/com/fraudshield/transactional/risk/application/DecisionPolicyTest.java
```

## Required Integration Scenarios

- Low-risk transaction returns `APPROVE`.
- High amount returns `CHALLENGE`.
- High amount plus new device and new beneficiary returns `REVIEW`.
- Very high amount plus enough additional risk returns `DENY`.
- Missing `transactionId` returns `400`.
- `amount <= 0` returns `400`.
- Unsupported currency returns `400`.
- Successful evaluation persists one transaction row.
- Successful evaluation persists one risk decision row.
- Successful evaluation persists all risk reason rows.

## Test Data Rules

- Tests should own their data.
- Insert customer, device and beneficiary records required by each scenario.
- Avoid hidden dependency on global seed data.
- Keep timestamps explicit enough to make `NEW_ACCOUNT` and `RECENT_PASSWORD_CHANGE` deterministic.
- Clean data between tests through transaction rollback, repository cleanup or isolated identifiers.

## README Requirements

The README should include:

- Project objective.
- EV1 scope.
- Stack.
- Requirements to run locally.
- Docker Compose command for PostgreSQL.
- Gradle commands.
- Endpoint contract.
- Example request.
- Example response.
- Decision thresholds.
- Initial rules.
- How to run tests.
- Known EV1 exclusions.

## Validation Commands

Run:

```bash
./gradlew test
./gradlew build
docker compose up -d postgres
./gradlew bootRun
```

Optional manual validation:

```bash
curl -X POST http://localhost:8080/transactions/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
    "transactionId": "tx-demo-review",
    "customerId": "cus-123",
    "amount": 8500.00,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "beneficiaryId": "ben-new",
    "deviceId": "dev-new",
    "ipAddress": "177.10.20.30",
    "occurredAt": "2026-09-12T14:30:00Z"
  }'
```

## Demonstration Checklist

- PostgreSQL starts from a clean checkout.
- Application starts with migrations applied.
- A valid `curl` returns an explainable decision.
- Database contains transaction, decision and reason records after success.
- Invalid payload returns a client error.
- Tests pass without relying on a manually running database.

## Acceptance Criteria

- `./gradlew test` passes.
- `./gradlew build` passes.
- Integration tests use Testcontainers PostgreSQL.
- Every EV1 decision has at least one executable scenario.
- README is sufficient for local demonstration.
- EV1 exclusions remain documented and unimplemented.
