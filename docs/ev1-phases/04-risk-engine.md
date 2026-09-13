# EV1 Phase 4 - Motor De Risco

## Goal

Implementar o nucleo deterministico da EV1: regras de risco, calculo de score, motivos explicaveis e politica de decisao.

O motor deve ser simples, testavel e independente de HTTP e banco de dados.

## Outcomes

- Contrato `RiskRule`.
- Regras iniciais implementadas.
- `RiskEngine` executando todas as regras.
- `DecisionPolicy` centralizando thresholds.
- `RiskEvaluationContext` montavel pela camada de aplicacao.
- Testes unitarios para regras, score e decisoes.

## Scope

Included:

- `HIGH_AMOUNT`: amount >= 5000, impact 30.
- `VERY_HIGH_AMOUNT`: amount >= 20000, impact 50.
- `NEW_DEVICE`: device not associated with customer, impact 20.
- `UNTRUSTED_DEVICE`: known but untrusted device, impact 15.
- `NEW_BENEFICIARY`: beneficiary not known for customer, impact 25.
- `RECENT_PASSWORD_CHANGE`: password changed in the last 24 hours, impact 20.
- `NEW_ACCOUNT`: customer account created in the last 7 days, impact 20.
- Decision thresholds.
- Rules version, initially `v1`.

Excluded:

- Dynamic rules.
- Rule persistence.
- Runtime configuration.
- ML scoring.
- Velocity windows.
- Redis.
- Graph analysis.

## Suggested Files

```text
src/main/java/com/fraudshield/transactional/risk/application/RiskEngine.java
src/main/java/com/fraudshield/transactional/risk/application/DecisionPolicy.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskEvaluationContext.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskAssessment.java
src/main/java/com/fraudshield/transactional/risk/domain/RiskReason.java
src/main/java/com/fraudshield/transactional/risk/rules/RiskRule.java
src/main/java/com/fraudshield/transactional/risk/rules/HighAmountRule.java
src/main/java/com/fraudshield/transactional/risk/rules/VeryHighAmountRule.java
src/main/java/com/fraudshield/transactional/risk/rules/NewDeviceRule.java
src/main/java/com/fraudshield/transactional/risk/rules/UntrustedDeviceRule.java
src/main/java/com/fraudshield/transactional/risk/rules/NewBeneficiaryRule.java
src/main/java/com/fraudshield/transactional/risk/rules/RecentPasswordChangeRule.java
src/main/java/com/fraudshield/transactional/risk/rules/NewAccountRule.java
src/test/java/com/fraudshield/transactional/risk/application/RiskEngineTest.java
src/test/java/com/fraudshield/transactional/risk/application/DecisionPolicyTest.java
src/test/java/com/fraudshield/transactional/risk/rules/
```

## Evaluation Context

The engine should receive all facts needed for evaluation:

- Transaction amount.
- Customer creation timestamp.
- Customer last password change timestamp.
- Whether the device is known for the customer.
- Whether the device is trusted.
- Whether the beneficiary is known for the customer.
- Evaluation timestamp.

The engine must not query repositories. Context assembly belongs to the transaction application service.

## Decision Policy

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Important Decisions Before Coding

### Amount Rule Stacking

Define whether `VERY_HIGH_AMOUNT` stacks with `HIGH_AMOUNT`.

Recommended EV1 behavior:

- `VERY_HIGH_AMOUNT` supersedes `HIGH_AMOUNT` to avoid double-counting the same signal.

If a different choice is made, update `docs/ev1-roadmap.md` and `.agents/shared-context.md`.

### Missing Customer Context

Define whether a missing customer returns a domain error or is treated as a new account.

Recommended EV1 behavior:

- Customer must exist.
- Missing customer should return a clear domain error when the API phase is implemented.

If a different choice is made, update `docs/ev1-roadmap.md` and `.agents/shared-context.md`.

## Required Unit Tests

- Low-risk context returns score `0` and `APPROVE`.
- `HIGH_AMOUNT` contributes `30`.
- `VERY_HIGH_AMOUNT` behavior matches the chosen stacking policy.
- `NEW_DEVICE` contributes `20`.
- `UNTRUSTED_DEVICE` contributes `15`.
- `NEW_BENEFICIARY` contributes `25`.
- `RECENT_PASSWORD_CHANGE` contributes `20`.
- `NEW_ACCOUNT` contributes `20`.
- Combined score reaches `CHALLENGE`.
- Combined score reaches `REVIEW`.
- Combined score reaches `DENY`.
- Boundary scores map to the correct decisions.

## Validation

Run:

```bash
./gradlew test
```

Expected result:

- Risk tests pass without requiring Spring context or PostgreSQL.

## Acceptance Criteria

- Risk logic is deterministic.
- Risk engine is independent from HTTP and persistence.
- Each triggered reason includes code, description and score impact.
- Decision policy is centralized and covered by tests.
- Every initial rule has focused unit coverage.
