# Phase 4 - Risk Engine

## Goal

Implement the deterministic rule engine that evaluates transaction context, produces risk reasons, calculates a score, and maps that score to a decision.

## Outcomes

- `RiskRule` contract exists.
- Initial rules are implemented.
- `RiskEngine` evaluates all rules.
- `DecisionPolicy` maps score to decision.
- Unit tests cover rules, scoring, and thresholds.

## Scope

Included:

- Rule interface.
- Rule implementations for MVP 1.
- Score aggregation.
- Decision threshold policy.
- Rules version constant, initially `v1`.
- Unit tests for deterministic behavior.

Excluded:

- Runtime rule management.
- Rule persistence.
- Dynamic configuration UI.
- ML scoring.
- Redis velocity windows.

## Suggested Files

```text
risk/application/RiskEngine.java
risk/application/DecisionPolicy.java
risk/domain/RiskAssessment.java
risk/domain/RiskEvaluationContext.java
risk/domain/RiskReason.java
risk/rules/RiskRule.java
risk/rules/HighAmountRule.java
risk/rules/VeryHighAmountRule.java
risk/rules/NewDeviceRule.java
risk/rules/UntrustedDeviceRule.java
risk/rules/NewBeneficiaryRule.java
risk/rules/RecentPasswordChangeRule.java
risk/rules/NewAccountRule.java
```

## Evaluation Context

The engine should receive a context object containing:

- Transaction amount.
- Customer creation date.
- Customer last password change date.
- Whether the device is known.
- Whether the device is trusted.
- Whether the beneficiary is known.
- Evaluation timestamp.

The engine should not query repositories directly. Context assembly belongs in the application layer.

## Rules

Initial impacts:

- `HIGH_AMOUNT`: 30
- `VERY_HIGH_AMOUNT`: 50
- `NEW_DEVICE`: 20
- `UNTRUSTED_DEVICE`: 15
- `NEW_BENEFICIARY`: 25
- `RECENT_PASSWORD_CHANGE`: 20
- `NEW_ACCOUNT`: 20

Decision thresholds:

```text
0  - 29  -> APPROVE
30 - 59  -> CHALLENGE
60 - 89  -> REVIEW
90+      -> DENY
```

## Important Design Choice

If both `HIGH_AMOUNT` and `VERY_HIGH_AMOUNT` match, decide whether they stack.

Recommended MVP 1 behavior:

- They stack only if explicitly documented.
- Simpler alternative: `VERY_HIGH_AMOUNT` supersedes `HIGH_AMOUNT`.

Choose one before implementation and update `.agents/shared-context.md`.

## Test Cases

Required unit tests:

- Low-risk context returns score `0` and `APPROVE`.
- High amount only returns `CHALLENGE`.
- Very high amount behavior matches the chosen stacking policy.
- New device contributes expected score.
- Untrusted device contributes expected score.
- New beneficiary contributes expected score.
- Recent password change contributes expected score.
- New account contributes expected score.
- Combined score crosses `REVIEW`.
- Combined score crosses `DENY`.

## Acceptance Criteria

- Risk logic is deterministic.
- Each reason includes code, description, and score impact.
- Decision policy is centralized.
- Rule code is easy to test without Spring.
- Unit tests cover all rules and thresholds.
