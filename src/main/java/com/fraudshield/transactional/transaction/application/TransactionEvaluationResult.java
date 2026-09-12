package com.fraudshield.transactional.transaction.application;

import com.fraudshield.transactional.risk.domain.RiskAssessment;
import com.fraudshield.transactional.transaction.api.TransactionEvaluationResponse;

import java.util.Objects;

public record TransactionEvaluationResult(
		String transactionId,
		RiskAssessment assessment
) {
	public TransactionEvaluationResult {
		Objects.requireNonNull(transactionId, "transactionId must not be null");
		Objects.requireNonNull(assessment, "assessment must not be null");

		if (transactionId.isBlank()) {
			throw new IllegalArgumentException("transactionId must not be blank");
		}
	}

	public TransactionEvaluationResponse toResponse() {
		return TransactionEvaluationResponse.from(transactionId, assessment);
	}
}
