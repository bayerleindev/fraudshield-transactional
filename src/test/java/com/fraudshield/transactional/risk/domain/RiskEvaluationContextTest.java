package com.fraudshield.transactional.risk.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskEvaluationContextTest {

	private static final Instant EVALUATED_AT = Instant.parse("2026-09-12T14:30:01Z");

	@Test
	void rejectsNullAmount() {
		assertThatThrownBy(() -> new RiskEvaluationContext(
				null,
				EVALUATED_AT.minusSeconds(30 * 24 * 60 * 60),
				EVALUATED_AT.minusSeconds(10 * 24 * 60 * 60),
				true,
				true,
				true,
				EVALUATED_AT
		)).isInstanceOf(NullPointerException.class)
				.hasMessage("amount must not be null");
	}

	@Test
	void rejectsNullCustomerCreatedAt() {
		assertThatThrownBy(() -> new RiskEvaluationContext(
				new BigDecimal("100.00"),
				null,
				EVALUATED_AT.minusSeconds(10 * 24 * 60 * 60),
				true,
				true,
				true,
				EVALUATED_AT
		)).isInstanceOf(NullPointerException.class)
				.hasMessage("customerCreatedAt must not be null");
	}

	@Test
	void rejectsNullEvaluatedAt() {
		assertThatThrownBy(() -> new RiskEvaluationContext(
				new BigDecimal("100.00"),
				EVALUATED_AT.minusSeconds(30 * 24 * 60 * 60),
				EVALUATED_AT.minusSeconds(10 * 24 * 60 * 60),
				true,
				true,
				true,
				null
		)).isInstanceOf(NullPointerException.class)
				.hasMessage("evaluatedAt must not be null");
	}

	@Test
	void rejectsNonPositiveAmount() {
		assertThatThrownBy(() -> new RiskEvaluationContext(
				BigDecimal.ZERO,
				EVALUATED_AT.minusSeconds(30 * 24 * 60 * 60),
				EVALUATED_AT.minusSeconds(10 * 24 * 60 * 60),
				true,
				true,
				true,
				EVALUATED_AT
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("amount must be greater than zero");
	}
}
