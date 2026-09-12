package com.fraudshield.transactional.risk.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record RiskEvaluationContext(
		BigDecimal amount,
		Instant customerCreatedAt,
		Instant lastPasswordChangeAt,
		boolean deviceKnown,
		boolean deviceTrusted,
		boolean beneficiaryKnown,
		Instant evaluatedAt
) {
	public RiskEvaluationContext {
		Objects.requireNonNull(amount, "amount must not be null");
		Objects.requireNonNull(customerCreatedAt, "customerCreatedAt must not be null");
		Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");

		if (amount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be greater than zero");
		}
	}
}
