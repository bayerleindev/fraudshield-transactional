package com.fraudshield.transactional.risk.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fraudshield.transactional.audit.infra.RiskDecisionEntity;
import com.fraudshield.transactional.risk.domain.RiskDecision;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RiskDecisionSummaryResponse(
		String transactionId,
		RiskDecision decision,
		int score,
		List<RiskReasonResponse> reasons,
		String rulesVersion,
		String evaluationType,
		Instant evaluatedAt,
		Map<String, Object> features
) {
	private static final String ORIGINAL_EVALUATION = "ORIGINAL";

	public static RiskDecisionSummaryResponse from(RiskDecisionEntity decision) {
		return new RiskDecisionSummaryResponse(
				decision.getTransactionId(),
				decision.getDecision(),
				decision.getScore(),
				decision.getReasons().stream()
						.map(RiskReasonResponse::from)
						.toList(),
				decision.getRulesVersion(),
				ORIGINAL_EVALUATION,
				decision.getEvaluatedAt(),
				null
		);
	}
}
