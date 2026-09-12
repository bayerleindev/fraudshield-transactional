package com.fraudshield.transactional.transaction.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionEvaluationTest {

	@ParameterizedTest
	@MethodSource("blankTextFields")
	void rejectsBlankTextFields(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder> customizer, String message) {
		assertThatThrownBy(() -> customizer.apply(validTransactionBuilder()).build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(message);
	}

	@ParameterizedTest
	@MethodSource("nullRequiredFields")
	void rejectsNullRequiredFields(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder> customizer, String message) {
		assertThatThrownBy(() -> customizer.apply(validTransactionBuilder()).build())
				.isInstanceOf(NullPointerException.class)
				.hasMessage(message);
	}

	@Test
	void rejectsCurrencyOutsideMvpScope() {
		assertThatThrownBy(() -> validTransactionBuilder().currency("USD").build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("currency must be BRL for MVP 1");
	}

	@Test
	void rejectsNonPositiveAmount() {
		assertThatThrownBy(() -> validTransactionBuilder().amount(BigDecimal.ZERO).build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("amount must be greater than zero");
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> blankTextFields() {
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.transactionId(""),
						"transactionId must not be blank"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.customerId(""),
						"customerId must not be blank"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.currency(""),
						"currency must not be blank"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.beneficiaryId(""),
						"beneficiaryId must not be blank"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.deviceId(""),
						"deviceId must not be blank"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.ipAddress(""),
						"ipAddress must not be blank"
				)
		);
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> nullRequiredFields() {
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.transactionId(null),
						"transactionId must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.customerId(null),
						"customerId must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.amount(null),
						"amount must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.currency(null),
						"currency must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.paymentMethod(null),
						"paymentMethod must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.beneficiaryId(null),
						"beneficiaryId must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.deviceId(null),
						"deviceId must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.ipAddress(null),
						"ipAddress must not be null"
				),
				org.junit.jupiter.params.provider.Arguments.of(
						(Function<TransactionEvaluationBuilder, TransactionEvaluationBuilder>) builder -> builder.occurredAt(null),
						"occurredAt must not be null"
				)
		);
	}

	private static TransactionEvaluationBuilder validTransactionBuilder() {
		return new TransactionEvaluationBuilder();
	}

	private static final class TransactionEvaluationBuilder {
		private String transactionId = "tx-001";
		private String customerId = "cus-123";
		private BigDecimal amount = new BigDecimal("100.00");
		private String currency = "BRL";
		private PaymentMethod paymentMethod = PaymentMethod.PIX;
		private String beneficiaryId = "ben-999";
		private String deviceId = "dev-abc";
		private String ipAddress = "177.10.20.30";
		private Instant occurredAt = Instant.parse("2026-09-12T14:30:00Z");

		private TransactionEvaluationBuilder transactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		private TransactionEvaluationBuilder customerId(String customerId) {
			this.customerId = customerId;
			return this;
		}

		private TransactionEvaluationBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		private TransactionEvaluationBuilder currency(String currency) {
			this.currency = currency;
			return this;
		}

		private TransactionEvaluationBuilder paymentMethod(PaymentMethod paymentMethod) {
			this.paymentMethod = paymentMethod;
			return this;
		}

		private TransactionEvaluationBuilder beneficiaryId(String beneficiaryId) {
			this.beneficiaryId = beneficiaryId;
			return this;
		}

		private TransactionEvaluationBuilder deviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}

		private TransactionEvaluationBuilder ipAddress(String ipAddress) {
			this.ipAddress = ipAddress;
			return this;
		}

		private TransactionEvaluationBuilder occurredAt(Instant occurredAt) {
			this.occurredAt = occurredAt;
			return this;
		}

		private TransactionEvaluation build() {
			return new TransactionEvaluation(
					transactionId,
					customerId,
					amount,
					currency,
					paymentMethod,
					beneficiaryId,
					deviceId,
					ipAddress,
					occurredAt
			);
		}
	}
}
