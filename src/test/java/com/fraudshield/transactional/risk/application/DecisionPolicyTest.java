package com.fraudshield.transactional.risk.application;

import com.fraudshield.transactional.risk.domain.RiskDecision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecisionPolicyTest {

	private final DecisionPolicy policy = new DecisionPolicy();

	@ParameterizedTest
	@CsvSource({
			"0, APPROVE",
			"29, APPROVE",
			"30, CHALLENGE",
			"59, CHALLENGE",
			"60, REVIEW",
			"89, REVIEW",
			"90, DENY"
	})
	void mapsScoreThresholdBoundaries(int score, RiskDecision expectedDecision) {
		assertThat(policy.decide(score)).isEqualTo(expectedDecision);
	}

	@Test
	void rejectsNegativeScore() {
		assertThatThrownBy(() -> policy.decide(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("score must not be negative");
	}
}
