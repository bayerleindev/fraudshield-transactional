# Phase 5 - Transaction Evaluation API

## Goal

Expose the MVP 1 use case through `POST /transactions/evaluate`, orchestrating validation, context lookup, risk evaluation, persistence, and response mapping.

## Outcomes

- REST controller exists.
- Application service orchestrates the evaluation.
- Transaction payload is persisted.
- Risk decision is persisted.
- Risk reasons are persisted.
- Response matches the documented contract.

## Scope

Included:

- Controller.
- Request validation.
- Application service.
- Context assembly from repositories.
- Risk engine invocation.
- Transactional persistence.
- Response DTO mapping.
- Basic error response for invalid input.

Excluded:

- Authentication.
- Rate limiting.
- Async event publishing.
- Idempotency beyond database uniqueness unless explicitly added.
- Manual review case creation.

## Suggested Files

```text
transaction/api/TransactionEvaluationController.java
transaction/application/EvaluateTransactionService.java
transaction/application/TransactionEvaluationResult.java
transaction/infra/TransactionEntity.java
transaction/infra/TransactionRepository.java
risk/infra/RiskDecisionEntity.java
risk/infra/RiskDecisionRepository.java
risk/infra/RiskReasonEntity.java
risk/infra/RiskReasonRepository.java
shared/exception/ApiExceptionHandler.java
```

## Endpoint

```http
POST /transactions/evaluate
Content-Type: application/json
```

Expected response status:

- `200 OK` for successful evaluation.
- `400 Bad Request` for invalid payload.

## Orchestration Flow

1. Validate request.
2. Normalize transaction data.
3. Load customer context.
4. Load device context.
5. Load beneficiary context.
6. Build `RiskEvaluationContext`.
7. Execute `RiskEngine`.
8. Persist transaction.
9. Persist risk decision.
10. Persist risk reasons.
11. Return response.

## Missing Context Behavior

Recommended MVP 1 behavior:

- Missing customer can be treated as a new account risk context only if this is explicitly modeled.
- Better portfolio behavior: require customer to exist and return a clear domain error if it does not.

Choose one before implementation and update `.agents/shared-context.md`.

## Transaction Boundary

The evaluation use case should persist transaction, decision, and reasons in one database transaction.

If persistence fails, the API should not return a successful decision.

## Acceptance Criteria

- Endpoint matches documented request and response.
- Invalid payloads return `400`.
- Evaluation response includes decision, score, reasons, rules version, and evaluated timestamp.
- Persistence includes transaction, decision, and all reasons.
- Application service remains the orchestration boundary.
- Risk engine stays independent from HTTP and database concerns.
