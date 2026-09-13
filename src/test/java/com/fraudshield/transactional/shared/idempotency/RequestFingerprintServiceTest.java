package com.fraudshield.transactional.shared.idempotency;

import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import com.fraudshield.transactional.transaction.domain.TransactionEvaluation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RequestFingerprintServiceTest {
	private final RequestFingerprintService service = new RequestFingerprintService();

	@Test
	void returnsSameFingerprintForEquivalentNormalizedPayload() {
		var first = transaction(new BigDecimal("5000.00"));
		var second = transaction(new BigDecimal("5000.0"));

		assertThat(service.fingerprint(first)).isEqualTo(service.fingerprint(second));
	}

	@Test
	void returnsDifferentFingerprintWhenPayloadDiffers() {
		var first = transaction(new BigDecimal("5000.00"));
		var second = new TransactionEvaluation(
				"tx-fingerprint",
				"cus-fingerprint",
				new BigDecimal("5000.00"),
				"BRL",
				PaymentMethod.PIX,
				"ben-other",
				"dev-fingerprint",
				"177.10.20.30",
				Instant.parse("2026-09-12T14:30:00Z")
		);

		assertThat(service.fingerprint(first)).isNotEqualTo(service.fingerprint(second));
	}

	private static TransactionEvaluation transaction(BigDecimal amount) {
		return new TransactionEvaluation(
				"tx-fingerprint",
				"cus-fingerprint",
				amount,
				"BRL",
				PaymentMethod.PIX,
				"ben-fingerprint",
				"dev-fingerprint",
				"177.10.20.30",
				Instant.parse("2026-09-12T14:30:00Z")
		);
	}
}
