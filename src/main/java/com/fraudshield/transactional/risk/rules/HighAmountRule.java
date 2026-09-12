package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class HighAmountRule implements RiskRule {
	private static final BigDecimal THRESHOLD = new BigDecimal("5000");
	private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("20000");
	private static final int IMPACT = 30;
	private static final String DESCRIPTION = "Transaction amount is above the configured threshold.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (context.amount().compareTo(THRESHOLD) >= 0
				&& context.amount().compareTo(VERY_HIGH_AMOUNT_THRESHOLD) < 0) {
			return Optional.of(new RiskReason(RiskReasonCode.HIGH_AMOUNT, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
