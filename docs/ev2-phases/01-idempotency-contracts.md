# EV2 Phase 1 - Idempotencia E Contratos

## Goal

Tornar `POST /transactions/evaluate` seguro para retries e definir contratos de erro mais previsiveis para a evolucao operacional da API.

## Outcomes

- Comportamento idempotente documentado e implementavel.
- Estrategia para detectar request repetido equivalente.
- Estrategia para rejeitar request repetido conflitante.
- Erros padronizados para validacao, conflito e recurso inexistente.
- Base de persistencia preparada para idempotencia.

## Scope

Included:

- Suporte a `Idempotency-Key` opcional ou uso de `transactionId` como chave natural.
- Fingerprint canonico do payload normalizado.
- Persistencia do fingerprint associado a transacao.
- Resposta reutilizada para retry equivalente.
- Erro `409 Conflict` para retry com mesmo identificador e payload diferente.
- Modelo de erro estavel.

Excluded:

- Idempotencia distribuida com Redis.
- Locks distribuidos.
- Expiracao automatica de chaves.
- Autenticacao de cliente.
- Rate limiting.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/shared/idempotency/RequestFingerprint.java
src/main/java/com/fraudshield/transactional/shared/idempotency/RequestFingerprintService.java
src/main/java/com/fraudshield/transactional/transaction/application/DuplicateTransactionConflictException.java
src/main/java/com/fraudshield/transactional/shared/exception/ApiErrorResponse.java
src/main/java/com/fraudshield/transactional/shared/exception/ApiExceptionHandler.java
src/main/resources/db/migration/V8__add_transaction_idempotency_fields.sql
```

## Contract Decisions

Recommended behavior:

- `transactionId` is the primary idempotency key.
- `Idempotency-Key` may be accepted later, but is not required for EV2.
- The normalized request payload receives a deterministic fingerprint.
- Same `transactionId` and same fingerprint returns the stored decision.
- Same `transactionId` and different fingerprint returns `409 Conflict`.

## Error Shape

Recommended error response:

```json
{
  "code": "TRANSACTION_CONFLICT",
  "message": "Transaction has already been evaluated with different data.",
  "details": []
}
```

Validation errors should use the same envelope with field-level details when practical.

## Data Model Notes

Add to `transactions`:

- `request_fingerprint`
- `first_evaluated_at`

Recommended constraint:

- Keep unique `transaction_id`.

## Acceptance Criteria

- Equivalent retry does not create duplicate original decisions.
- Conflicting retry returns `409 Conflict`.
- Error responses do not expose stack traces or SQL details.
- Fingerprint generation is deterministic.
- Behavior is documented in README during the documentation phase.
