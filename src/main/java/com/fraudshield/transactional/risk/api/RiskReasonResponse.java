package com.fraudshield.transactional.risk.api;

import com.fraudshield.transactional.audit.infra.RiskReasonEntity;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;

public record RiskReasonResponse(
		RiskReasonCode code,
		String description,
		int scoreImpact
) {
	public static RiskReasonResponse from(RiskReason reason) {
		return new RiskReasonResponse(reason.code(), reason.description(), reason.scoreImpact());
	}

	public static RiskReasonResponse from(RiskReasonEntity reason) {
		return new RiskReasonResponse(reason.getCode(), reason.getDescription(), reason.getScoreImpact());
	}
}
