# EV1 Phase 5 - API De Avaliacao

## Goal

Expor o fluxo principal da EV1 por HTTP: receber uma transacao, validar entrada, montar contexto, executar o motor de risco, persistir auditoria e retornar uma decisao explicavel.

Esta fase transforma os blocos anteriores em valor consumivel.

## Outcomes

- Endpoint `POST /transactions/evaluate`.
- Controller com validacao de request.
- Application service como orquestrador do caso de uso.
- Contexto de risco montado a partir dos repositories.
- Execucao do `RiskEngine`.
- Persistencia transacional de transacao, decisao e motivos.
- Response aderente ao contrato da EV1.
- Tratamento consistente de erros de validacao e dominio.

## Scope

Included:

- Controller REST.
- Bean Validation no request.
- Handler de erros para `400 Bad Request`.
- Application service com boundary transacional.
- Lookup de cliente, dispositivo e beneficiario.
- Mapeamento de request para dominio.
- Mapeamento de resultado de risco para response.
- Persistencia de auditoria.

Excluded:

- Autenticacao.
- Autorizacao.
- Rate limiting.
- Idempotencia robusta alem da unicidade de `transactionId`.
- Publicacao de eventos.
- Case management de revisao manual.
- API de consulta historica.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/transaction/api/TransactionEvaluationController.java
src/main/java/com/fraudshield/transactional/transaction/application/EvaluateTransactionService.java
src/main/java/com/fraudshield/transactional/transaction/application/TransactionEvaluationResult.java
src/main/java/com/fraudshield/transactional/transaction/application/CustomerContextNotFoundException.java
src/main/java/com/fraudshield/transactional/shared/exception/ApiExceptionHandler.java
src/main/java/com/fraudshield/transactional/shared/exception/ApiErrorResponse.java
src/main/java/com/fraudshield/transactional/shared/time/ClockProvider.java
```

## Endpoint

```http
POST /transactions/evaluate
Content-Type: application/json
```

Successful response:

```text
200 OK
```

Invalid request response:

```text
400 Bad Request
```

Recommended missing customer response:

```text
404 Not Found
```

If the implementation chooses another missing-context behavior, document it in `docs/ev1-roadmap.md`.

## Orchestration Flow

1. Validate request.
2. Normalize transaction data.
3. Load customer context.
4. Load device context for the customer.
5. Load beneficiary context for the customer.
6. Build `RiskEvaluationContext`.
7. Execute `RiskEngine`.
8. Persist transaction.
9. Persist risk decision.
10. Persist risk reasons.
11. Return response.

## Transaction Boundary

The application service should be the transaction boundary.

Rules:

- A successful evaluation persists transaction, decision and reasons.
- If persistence fails, API must not return a successful decision.
- Avoid splitting audit writes across independent transactions.

## Error Handling

Validation errors should be clear and stable enough for clients.

Do:

- Return useful validation messages.
- Avoid stack traces in responses.
- Avoid exposing SQL or internal exception details.

Do not:

- Log full request payloads by default.
- Reveal sensitive internals.
- Mix exception mapping into the risk engine.

## Acceptance Tests To Enable

This phase should make the following integration tests possible:

- Valid low-risk transaction returns `APPROVE`.
- Valid high-risk transaction returns `REVIEW` or `DENY` depending on score.
- Missing required field returns `400`.
- Invalid amount returns `400`.
- Successful evaluation persists audit records.

## Validation

Run:

```bash
docker compose up -d postgres
./gradlew bootRun
```

Manual smoke test:

```bash
curl -X POST http://localhost:8080/transactions/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
    "transactionId": "tx-001",
    "customerId": "cus-123",
    "amount": 8500.00,
    "currency": "BRL",
    "paymentMethod": "PIX",
    "beneficiaryId": "ben-999",
    "deviceId": "dev-abc",
    "ipAddress": "177.10.20.30",
    "occurredAt": "2026-09-12T14:30:00Z"
  }'
```

## Acceptance Criteria

- Endpoint path and payload match the EV1 contract.
- Valid request returns decision, score, reasons, rules version and evaluation timestamp.
- Invalid payload returns `400`.
- Successful response implies audit persistence succeeded.
- Application service owns orchestration.
- Risk engine remains independent from HTTP and database code.
