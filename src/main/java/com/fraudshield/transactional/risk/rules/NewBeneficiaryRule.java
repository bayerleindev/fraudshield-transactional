package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.util.Objects;
import java.util.Optional;

public final class NewBeneficiaryRule implements RiskRule {
	private static final int IMPACT = 25;
	private static final String DESCRIPTION = "Customer has no previous relationship with this beneficiary.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (!context.beneficiaryKnown()) {
			return Optional.of(new RiskReason(RiskReasonCode.NEW_BENEFICIARY, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
