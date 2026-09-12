package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

import java.util.Objects;
import java.util.Optional;

public final class UntrustedDeviceRule implements RiskRule {
	private static final int IMPACT = 15;
	private static final String DESCRIPTION = "Transaction originated from a known device that is not trusted.";

	@Override
	public Optional<RiskReason> evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		if (context.deviceKnown() && !context.deviceTrusted()) {
			return Optional.of(new RiskReason(RiskReasonCode.UNTRUSTED_DEVICE, DESCRIPTION, IMPACT));
		}
		return Optional.empty();
	}
}
