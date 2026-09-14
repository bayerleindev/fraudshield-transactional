package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.risk.domain.RiskAssessment;
import com.fraudshield.transactional.risk.domain.RiskDecision;
import com.fraudshield.transactional.risk.api.RiskReasonResponse;

import java.time.Instant;
import java.util.List;

public record TransactionEvaluationResponse(
		String transactionId,
		RiskDecision decision,
		int score,
		List<RiskReasonResponse> reasons,
		String rulesVersion,
		Instant evaluatedAt
) {
	public static TransactionEvaluationResponse from(String transactionId, RiskAssessment assessment) {
		return new TransactionEvaluationResponse(
				transactionId,
				assessment.decision(),
				assessment.score(),
				assessment.reasons().stream()
						.map(RiskReasonResponse::from)
						.toList(),
				assessment.rulesVersion(),
				assessment.evaluatedAt()
		);
	}
}
