package com.fraudshield.transactional.shared.idempotency;

import com.fraudshield.transactional.transaction.domain.TransactionEvaluation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
public class RequestFingerprintService {

	public RequestFingerprint fingerprint(TransactionEvaluation transaction) {
		Objects.requireNonNull(transaction, "transaction must not be null");
		var canonicalPayload = String.join("\n",
				field("transactionId", transaction.transactionId()),
				field("customerId", transaction.customerId()),
				field("amount", normalizeAmount(transaction.amount())),
				field("currency", transaction.currency()),
				field("paymentMethod", transaction.paymentMethod().name()),
				field("beneficiaryId", transaction.beneficiaryId()),
				field("deviceId", transaction.deviceId()),
				field("ipAddress", transaction.ipAddress()),
				field("occurredAt", transaction.occurredAt().toString())
		);

		return new RequestFingerprint(sha256(canonicalPayload));
	}

	private static String field(String name, String value) {
		return name + "=" + value.length() + ":" + value;
	}

	private static String normalizeAmount(BigDecimal amount) {
		return amount.stripTrailingZeros().toPlainString();
	}

	private static String sha256(String value) {
		try {
			var digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
