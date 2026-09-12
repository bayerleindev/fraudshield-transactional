package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.util.Objects;
import java.util.Optional;

public final class NewDeviceRule implements RiskRule {
	private static final int IMPACT = 20;
	private static final String DESCRIPTION = "Transaction originated from a device not seen before for this customer.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (!context.deviceKnown()) {
			return Optional.of(new RiskReason(RiskReasonCode.NEW_DEVICE, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
