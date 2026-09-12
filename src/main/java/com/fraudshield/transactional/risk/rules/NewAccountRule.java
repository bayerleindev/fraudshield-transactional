package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public final class NewAccountRule implements RiskRule {
	private static final Duration WINDOW = Duration.ofDays(7);
	private static final int IMPACT = 20;
	private static final String DESCRIPTION = "Customer account was created within the last 7 days.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (context.customerCreatedAt().isAfter(context.evaluatedAt())) {
			return Optional.empty();
		}
		if (!context.customerCreatedAt().isBefore(context.evaluatedAt().minus(WINDOW))) {
			return Optional.of(new RiskReason(RiskReasonCode.NEW_ACCOUNT, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
