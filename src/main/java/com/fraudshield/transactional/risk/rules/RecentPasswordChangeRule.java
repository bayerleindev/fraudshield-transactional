package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public final class RecentPasswordChangeRule implements RiskRule {
	private static final Duration WINDOW = Duration.ofHours(24);
	private static final int IMPACT = 20;
	private static final String DESCRIPTION = "Customer password changed within the last 24 hours.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (context.lastPasswordChangeAt() == null || context.lastPasswordChangeAt().isAfter(context.evaluatedAt())) {
			return Optional.empty();
		}
		if (!context.lastPasswordChangeAt().isBefore(context.evaluatedAt().minus(WINDOW))) {
			return Optional.of(new RiskReason(RiskReasonCode.RECENT_PASSWORD_CHANGE, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
