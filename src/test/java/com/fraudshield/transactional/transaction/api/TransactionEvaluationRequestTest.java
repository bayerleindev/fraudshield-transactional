package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEvaluationRequestTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void acceptsValidPixTransactionRequest() {
		var request = validRequest();

		assertThat(validator.validate(request)).isEmpty();
	}

	@Test
	void rejectsMissingTransactionId() {
		var request = validRequestBuilder().transactionId("").build();

		assertThat(validator.validate(request))
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("transactionId"));
	}

	@ParameterizedTest
	@MethodSource("requestsMissingRequiredFields")
	void rejectsMissingRequiredFields(TransactionEvaluationRequest request, String fieldName) {
		assertThat(validator.validate(request))
				.anyMatch(violation -> violation.getPropertyPath().toString().equals(fieldName));
	}

	@Test
	void rejectsNonPositiveAmount() {
		var request = validRequestBuilder().amount(BigDecimal.ZERO).build();

		assertThat(validator.validate(request))
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("amount"));
	}

	@Test
	void rejectsCurrencyOutsideMvpScope() {
		var request = validRequestBuilder().currency("USD").build();

		assertThat(validator.validate(request))
				.anyMatch(violation -> violation.getPropertyPath().toString().equals("currency"));
	}

	@Test
	void mapsRequestToDomainEvaluation() {
		var request = validRequest();

		var evaluation = request.toDomain();

		assertThat(evaluation.transactionId()).isEqualTo("tx-001");
		assertThat(evaluation.customerId()).isEqualTo("cus-123");
		assertThat(evaluation.amount()).isEqualByComparingTo("8500.00");
		assertThat(evaluation.currency()).isEqualTo("BRL");
		assertThat(evaluation.paymentMethod()).isEqualTo(PaymentMethod.PIX);
		assertThat(evaluation.beneficiaryId()).isEqualTo("ben-999");
		assertThat(evaluation.deviceId()).isEqualTo("dev-abc");
		assertThat(evaluation.ipAddress()).isEqualTo("177.10.20.30");
		assertThat(evaluation.occurredAt()).isEqualTo(Instant.parse("2026-09-12T14:30:00Z"));
	}

	private static TransactionEvaluationRequest validRequest() {
		return validRequestBuilder().build();
	}

	private static Stream<org.junit.jupiter.params.provider.Arguments> requestsMissingRequiredFields() {
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().customerId("").build(), "customerId"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().amount(null).build(), "amount"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().currency("").build(), "currency"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().paymentMethod(null).build(), "paymentMethod"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().beneficiaryId("").build(), "beneficiaryId"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().deviceId("").build(), "deviceId"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().ipAddress("").build(), "ipAddress"),
				org.junit.jupiter.params.provider.Arguments.of(validRequestBuilder().occurredAt(null).build(), "occurredAt")
		);
	}

	private static RequestBuilder validRequestBuilder() {
		return new RequestBuilder();
	}

	private static final class RequestBuilder {
		private String transactionId = "tx-001";
		private String customerId = "cus-123";
		private BigDecimal amount = new BigDecimal("8500.00");
		private String currency = "BRL";
		private PaymentMethod paymentMethod = PaymentMethod.PIX;
		private String beneficiaryId = "ben-999";
		private String deviceId = "dev-abc";
		private String ipAddress = "177.10.20.30";
		private Instant occurredAt = Instant.parse("2026-09-12T14:30:00Z");

		private RequestBuilder transactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		private RequestBuilder customerId(String customerId) {
			this.customerId = customerId;
			return this;
		}

		private RequestBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		private RequestBuilder currency(String currency) {
			this.currency = currency;
			return this;
		}

		private RequestBuilder paymentMethod(PaymentMethod paymentMethod) {
			this.paymentMethod = paymentMethod;
			return this;
		}

		private RequestBuilder beneficiaryId(String beneficiaryId) {
			this.beneficiaryId = beneficiaryId;
			return this;
		}

		private RequestBuilder deviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}

		private RequestBuilder ipAddress(String ipAddress) {
			this.ipAddress = ipAddress;
			return this;
		}

		private RequestBuilder occurredAt(Instant occurredAt) {
			this.occurredAt = occurredAt;
			return this;
		}

		private TransactionEvaluationRequest build() {
			return new TransactionEvaluationRequest(
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
