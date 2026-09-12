package com.fraudshield.transactional.risk.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskAssessmentTest {

	@Test
	void makesReasonsImmutable() {
		var reasons = new ArrayList<RiskReason>();
		reasons.add(new RiskReason(
				RiskReasonCode.HIGH_AMOUNT,
				"Transaction amount is above the configured threshold.",
				30
		));

		var assessment = new RiskAssessment(
				RiskDecision.CHALLENGE,
				30,
				reasons,
				"v1",
				Instant.parse("2026-09-12T14:30:01Z")
		);

		reasons.clear();

		assertThat(assessment.reasons()).hasSize(1);
		assertThatThrownBy(() -> assessment.reasons().add(new RiskReason(
				RiskReasonCode.NEW_DEVICE,
				"Transaction originated from a device not seen before for this customer.",
				20
		))).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void rejectsNegativeScore() {
		assertThatThrownBy(() -> new RiskAssessment(
				RiskDecision.APPROVE,
				-1,
				List.of(),
				"v1",
				Instant.parse("2026-09-12T14:30:01Z")
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("score must not be negative");
	}
}
