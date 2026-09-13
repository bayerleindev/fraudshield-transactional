package com.fraudshield.transactional.shared.idempotency;

import java.util.Objects;

public record RequestFingerprint(String value) {
	public RequestFingerprint {
		Objects.requireNonNull(value, "value must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("value must not be blank");
		}
	}
}
