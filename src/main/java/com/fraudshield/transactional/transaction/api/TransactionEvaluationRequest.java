package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import com.fraudshield.transactional.transaction.domain.TransactionEvaluation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvaluationRequest(
		@NotBlank String transactionId,
		@NotBlank String customerId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
		@NotBlank @Pattern(regexp = "BRL", message = "currency must be BRL for MVP 1") String currency,
		@NotNull PaymentMethod paymentMethod,
		@NotBlank String beneficiaryId,
		@NotBlank String deviceId,
		@NotBlank String ipAddress,
		@NotNull Instant occurredAt
) {
	public TransactionEvaluation toDomain() {
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
