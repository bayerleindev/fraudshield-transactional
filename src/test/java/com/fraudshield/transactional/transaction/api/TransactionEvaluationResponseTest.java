package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.risk.api.RiskReasonResponse;
import com.fraudshield.transactional.risk.domain.RiskAssessment;
import com.fraudshield.transactional.risk.domain.RiskDecision;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEvaluationResponseTest {

	@Test
	void mapsRiskAssessmentToApiResponse() {
		var evaluatedAt = Instant.parse("2026-09-12T14:30:01Z");
		var assessment = new RiskAssessment(
				RiskDecision.REVIEW,
				75,
				List.of(
						new RiskReason(
								RiskReasonCode.HIGH_AMOUNT,
								"Transaction amount is above the configured threshold.",
								30
						),
						new RiskReason(
								RiskReasonCode.NEW_BENEFICIARY,
								"Customer has no previous relationship with this beneficiary.",
								25
						)
				),
				"v1",
				evaluatedAt
		);

		var response = TransactionEvaluationResponse.from("tx-001", assessment);

		assertThat(response.transactionId()).isEqualTo("tx-001");
		assertThat(response.decision()).isEqualTo(RiskDecision.REVIEW);
		assertThat(response.score()).isEqualTo(75);
		assertThat(response.rulesVersion()).isEqualTo("v1");
		assertThat(response.evaluatedAt()).isEqualTo(evaluatedAt);
		assertThat(response.reasons())
				.extracting(RiskReasonResponse::code)
				.containsExactly(RiskReasonCode.HIGH_AMOUNT, RiskReasonCode.NEW_BENEFICIARY);
		assertThat(response.reasons())
				.extracting(RiskReasonResponse::description)
				.containsExactly(
						"Transaction amount is above the configured threshold.",
						"Customer has no previous relationship with this beneficiary."
				);
		assertThat(response.reasons())
				.extracting(RiskReasonResponse::scoreImpact)
				.containsExactly(30, 25);
	}
}
