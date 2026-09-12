package com.fraudshield.transactional.risk.application;

import com.fraudshield.transactional.risk.domain.RiskDecision;
import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RiskEngineTest {

	private static final Instant EVALUATED_AT = Instant.parse("2026-09-12T14:30:01Z");

	private final RiskEngine engine = new RiskEngine();

	@Test
	void approvesLowRiskContextWithNoReasons() {
		var assessment = engine.evaluate(lowRiskContext().build());

		assertThat(assessment.decision()).isEqualTo(RiskDecision.APPROVE);
		assertThat(assessment.score()).isZero();
		assertThat(assessment.reasons()).isEmpty();
		assertThat(assessment.rulesVersion()).isEqualTo("v1");
		assertThat(assessment.evaluatedAt()).isEqualTo(EVALUATED_AT);
	}

	@Test
	void highAmountOnlyReturnsChallenge() {
		var assessment = engine.evaluate(lowRiskContext()
				.amount("5000.00")
				.build());

		assertThat(assessment.decision()).isEqualTo(RiskDecision.CHALLENGE);
		assertThat(assessment.score()).isEqualTo(30);
		assertThat(assessment.reasons())
				.singleElement()
				.satisfies(reason -> {
					assertThat(reason.code()).isEqualTo(RiskReasonCode.HIGH_AMOUNT);
					assertThat(reason.description()).isNotBlank();
					assertThat(reason.scoreImpact()).isEqualTo(30);
				});
	}

	@ParameterizedTest
	@CsvSource({
			"4999.99, 0",
			"5000.00, 30",
			"19999.99, 30",
			"20000.00, 50"
	})
	void amountRulesApplyAtDocumentedBoundaries(String amount, int expectedScore) {
		var assessment = engine.evaluate(lowRiskContext()
				.amount(amount)
				.build());

		assertThat(assessment.score()).isEqualTo(expectedScore);
	}

	@Test
	void veryHighAmountSupersedesHighAmount() {
		var assessment = engine.evaluate(lowRiskContext()
				.amount("20000.00")
				.build());

		assertThat(assessment.decision()).isEqualTo(RiskDecision.CHALLENGE);
		assertThat(assessment.score()).isEqualTo(50);
		assertThat(assessment.reasons())
				.extracting(reason -> reason.code())
				.containsExactly(RiskReasonCode.VERY_HIGH_AMOUNT);
	}

	@Test
	void newDeviceContributesExpectedScore() {
		var assessment = engine.evaluate(lowRiskContext()
				.deviceKnown(false)
				.deviceTrusted(false)
				.build());

		assertSingleReason(assessment, RiskReasonCode.NEW_DEVICE, 20);
	}

	@Test
	void untrustedKnownDeviceContributesExpectedScore() {
		var assessment = engine.evaluate(lowRiskContext()
				.deviceKnown(true)
				.deviceTrusted(false)
				.build());

		assertSingleReason(assessment, RiskReasonCode.UNTRUSTED_DEVICE, 15);
	}

	@Test
	void newBeneficiaryContributesExpectedScore() {
		var assessment = engine.evaluate(lowRiskContext()
				.beneficiaryKnown(false)
				.build());

		assertSingleReason(assessment, RiskReasonCode.NEW_BENEFICIARY, 25);
	}

	@Test
	void recentPasswordChangeContributesExpectedScore() {
		var assessment = engine.evaluate(lowRiskContext()
				.lastPasswordChangeAt(EVALUATED_AT.minusSeconds(24 * 60 * 60))
				.build());

		assertSingleReason(assessment, RiskReasonCode.RECENT_PASSWORD_CHANGE, 20);
	}

	@Test
	void newAccountContributesExpectedScore() {
		var assessment = engine.evaluate(lowRiskContext()
				.customerCreatedAt(EVALUATED_AT.minusSeconds(7 * 24 * 60 * 60))
				.build());

		assertSingleReason(assessment, RiskReasonCode.NEW_ACCOUNT, 20);
	}

	@Test
	void combinedScoreCrossesReviewThreshold() {
		var assessment = engine.evaluate(lowRiskContext()
				.amount("5000.00")
				.deviceKnown(false)
				.beneficiaryKnown(false)
				.build());

		assertThat(assessment.decision()).isEqualTo(RiskDecision.REVIEW);
		assertThat(assessment.score()).isEqualTo(75);
		assertThat(assessment.reasons())
				.extracting(reason -> reason.code())
				.containsExactly(
						RiskReasonCode.HIGH_AMOUNT,
						RiskReasonCode.NEW_DEVICE,
						RiskReasonCode.NEW_BENEFICIARY
				);
	}

	@Test
	void combinedScoreCrossesDenyThreshold() {
		var assessment = engine.evaluate(lowRiskContext()
				.amount("20000.00")
				.deviceKnown(false)
				.beneficiaryKnown(false)
				.lastPasswordChangeAt(EVALUATED_AT.minusSeconds(60))
				.customerCreatedAt(EVALUATED_AT.minusSeconds(60))
				.build());

		assertThat(assessment.decision()).isEqualTo(RiskDecision.DENY);
		assertThat(assessment.score()).isEqualTo(135);
		assertThat(assessment.reasons())
				.extracting(reason -> reason.code())
				.containsExactly(
						RiskReasonCode.VERY_HIGH_AMOUNT,
						RiskReasonCode.NEW_DEVICE,
						RiskReasonCode.NEW_BENEFICIARY,
						RiskReasonCode.RECENT_PASSWORD_CHANGE,
						RiskReasonCode.NEW_ACCOUNT
				);
	}

	private static void assertSingleReason(
			com.fraudshield.transactional.risk.domain.RiskAssessment assessment,
			RiskReasonCode expectedCode,
			int expectedImpact
	) {
		assertThat(assessment.score()).isEqualTo(expectedImpact);
		assertThat(assessment.reasons())
				.singleElement()
				.satisfies(reason -> {
					assertThat(reason.code()).isEqualTo(expectedCode);
					assertThat(reason.description()).isNotBlank();
					assertThat(reason.scoreImpact()).isEqualTo(expectedImpact);
				});
	}

	private static RiskEvaluationContextBuilder lowRiskContext() {
		return new RiskEvaluationContextBuilder();
	}

	private static final class RiskEvaluationContextBuilder {
		private BigDecimal amount = new BigDecimal("100.00");
		private Instant customerCreatedAt = EVALUATED_AT.minusSeconds(30 * 24 * 60 * 60);
		private Instant lastPasswordChangeAt = EVALUATED_AT.minusSeconds(10 * 24 * 60 * 60);
		private boolean deviceKnown = true;
		private boolean deviceTrusted = true;
		private boolean beneficiaryKnown = true;
		private Instant evaluatedAt = EVALUATED_AT;

		private RiskEvaluationContextBuilder amount(String amount) {
			this.amount = new BigDecimal(amount);
			return this;
		}

		private RiskEvaluationContextBuilder customerCreatedAt(Instant customerCreatedAt) {
			this.customerCreatedAt = customerCreatedAt;
			return this;
		}

		private RiskEvaluationContextBuilder lastPasswordChangeAt(Instant lastPasswordChangeAt) {
			this.lastPasswordChangeAt = lastPasswordChangeAt;
			return this;
		}

		private RiskEvaluationContextBuilder deviceKnown(boolean deviceKnown) {
			this.deviceKnown = deviceKnown;
			return this;
		}

		private RiskEvaluationContextBuilder deviceTrusted(boolean deviceTrusted) {
			this.deviceTrusted = deviceTrusted;
			return this;
		}

		private RiskEvaluationContextBuilder beneficiaryKnown(boolean beneficiaryKnown) {
			this.beneficiaryKnown = beneficiaryKnown;
			return this;
		}

		private RiskEvaluationContext build() {
			return new RiskEvaluationContext(
					amount,
					customerCreatedAt,
					lastPasswordChangeAt,
					deviceKnown,
					deviceTrusted,
					beneficiaryKnown,
					evaluatedAt
			);
		}
	}
}
