# Phase 2 - Domain And API Models

## Goal

Define the core domain concepts and external API models for transaction evaluation before wiring persistence or complex orchestration.

## Outcomes

- Enums for risk decisions and reason codes.
- Request and response DTOs for `POST /transactions/evaluate`.
- Basic domain objects for risk evaluation.
- Validation rules for incoming payloads.
- Clear separation between API models and domain concepts.

## Scope

Included:

- Transaction evaluation request.
- Transaction evaluation response.
- Risk decision enum.
- Risk reason code enum.
- Payment method enum.
- Currency validation for MVP 1.
- Domain input object for the risk engine.

Excluded:

- JPA mappings.
- Repositories.
- Actual risk rule execution.
- Persistence.

## Suggested Files

```text
transaction/api/TransactionEvaluationRequest.java
transaction/api/TransactionEvaluationResponse.java
transaction/domain/PaymentMethod.java
transaction/domain/TransactionEvaluation.java
risk/domain/RiskDecision.java
risk/domain/RiskReasonCode.java
risk/domain/RiskReason.java
risk/domain/RiskAssessment.java
```

## API Contract

Endpoint:

```http
POST /transactions/evaluate
```

Required request fields:

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

- `transactionId`, `customerId`, `beneficiaryId`, `deviceId`, `ipAddress`, `currency`, and `paymentMethod` must be present.
- `amount` must be greater than zero.
- `occurredAt` must be present.
- `currency` can start as `BRL` only for MVP 1.

## Design Notes

Keep DTOs boring and explicit. Domain objects should express risk evaluation language, not HTTP or persistence concerns.

Avoid putting scoring logic in DTOs.

## Validation

At this phase, validation can be checked with focused unit tests or controller validation tests once the controller exists.

## Acceptance Criteria

- Domain vocabulary matches the roadmap.
- API request and response shapes match `docs/mvp-1-roadmap.md`.
- Invalid requests can be rejected using Bean Validation once the controller is added.
- No infrastructure concerns leak into risk domain objects.
