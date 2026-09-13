# EV1 Phase 2 - Dominio E Contratos

## Goal

Definir a linguagem central da EV1 e o contrato externo do endpoint de avaliacao antes de acoplar o fluxo a banco, HTTP ou regras completas.

Esta fase deve deixar claro quais dados entram, quais decisoes saem e quais conceitos pertencem ao dominio.

## Outcomes

- Enums de decisao e motivos de risco.
- Enum de meio de pagamento.
- DTO de request para avaliacao de transacao.
- DTO de response com decisao explicavel.
- Objetos de dominio para representar avaliacao e resultado de risco.
- Validacoes de entrada definidas no limite da API.

## Scope

Included:

- Contrato de `POST /transactions/evaluate`.
- Campos obrigatorios do request.
- Response com `transactionId`, `decision`, `score`, `reasons`, `rulesVersion` e `evaluatedAt`.
- Validacao de `amount > 0`.
- Validacao inicial de `currency = BRL`.
- Representacao tipada para decisoes e reason codes.
- Separacao entre DTOs HTTP e dominio.

Excluded:

- Controller completo.
- JPA mappings.
- Repositories.
- Persistencia.
- Execucao real das regras.
- Tratamento global definitivo de erros.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/transaction/api/TransactionEvaluationRequest.java
src/main/java/com/fraudshield/transactional/transaction/api/TransactionEvaluationResponse.java
src/main/java/com/fraudshield/transactional/transaction/api/RiskReasonResponse.java
src/main/java/com/fraudshield/transactional/transaction/domain/PaymentMethod.java
src/main/java/com/fraudshield/transactional/transaction/domain/TransactionEvaluation.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskDecision.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskReasonCode.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskReason.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskAssessment.java
```

## Request Contract

Required fields:

- `transactionId`
- `customerId`
- `amount`
- `currency`
- `paymentMethod`
- `beneficiaryId`
- `deviceId`
- `ipAddress`
- `occurredAt`

Validation expectations:

- Identifiers must not be blank.
- `amount` must be greater than zero.
- `currency` must be `BRL` for EV1.
- `paymentMethod` must be a known enum value.
- `occurredAt` must be present.

## Response Contract

Successful evaluations return:

- `transactionId`
- `decision`
- `score`
- `reasons`
- `rulesVersion`
- `evaluatedAt`

Each reason returns:

- `code`
- `description`
- `scoreImpact`

## Domain Decisions

Use enums for stable operational values:

- `APPROVE`
- `CHALLENGE`
- `REVIEW`
- `DENY`

Use enums for initial reason codes:

- `HIGH_AMOUNT`
- `VERY_HIGH_AMOUNT`
- `NEW_DEVICE`
- `UNTRUSTED_DEVICE`
- `NEW_BENEFICIARY`
- `RECENT_PASSWORD_CHANGE`
- `NEW_ACCOUNT`

## Design Notes

- DTOs should not calculate score.
- Domain objects should not know about HTTP annotations.
- Avoid free-form strings for decisions and reason codes.
- Keep monetary values precise with `BigDecimal`.
- Use `Instant` or another explicit time type for timestamps.

## Validation

At this phase, validation can be checked with focused unit tests for DTO constraints or left to the API phase if no controller exists yet.

Minimum validation expectation:

```bash
./gradlew test
```

## Acceptance Criteria

- API request and response match `docs/ev1-roadmap.md`.
- Domain vocabulary is explicit and typed.
- External input constraints are represented at the API boundary.
- No persistence concerns leak into risk domain objects.
- No scoring logic is implemented inside DTOs.
