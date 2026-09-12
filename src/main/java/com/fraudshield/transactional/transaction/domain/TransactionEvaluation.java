package com.fraudshield.transactional.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record TransactionEvaluation(
		String transactionId,
		String customerId,
		BigDecimal amount,
		String currency,
		PaymentMethod paymentMethod,
		String beneficiaryId,
		String deviceId,
		String ipAddress,
		Instant occurredAt
) {
	public TransactionEvaluation {
		Objects.requireNonNull(transactionId, "transactionId must not be null");
		Objects.requireNonNull(customerId, "customerId must not be null");
		Objects.requireNonNull(amount, "amount must not be null");
		Objects.requireNonNull(currency, "currency must not be null");
		Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
		Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
		Objects.requireNonNull(deviceId, "deviceId must not be null");
		Objects.requireNonNull(ipAddress, "ipAddress must not be null");
		Objects.requireNonNull(occurredAt, "occurredAt must not be null");

		if (transactionId.isBlank()) {
			throw new IllegalArgumentException("transactionId must not be blank");
		}
		if (customerId.isBlank()) {
			throw new IllegalArgumentException("customerId must not be blank");
		}
		if (currency.isBlank()) {
			throw new IllegalArgumentException("currency must not be blank");
		}
		if (!currency.equals("BRL")) {
			throw new IllegalArgumentException("currency must be BRL for MVP 1");
		}
		if (beneficiaryId.isBlank()) {
			throw new IllegalArgumentException("beneficiaryId must not be blank");
		}
		if (deviceId.isBlank()) {
			throw new IllegalArgumentException("deviceId must not be blank");
		}
		if (ipAddress.isBlank()) {
			throw new IllegalArgumentException("ipAddress must not be blank");
		}

		if (amount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be greater than zero");
		}
	}
}
