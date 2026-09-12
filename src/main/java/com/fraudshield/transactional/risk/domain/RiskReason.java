package com.fraudshield.transactional.risk.domain;

import java.util.Objects;

public record RiskReason(
		RiskReasonCode code,
		String description,
		int scoreImpact
) {
	public RiskReason {
		Objects.requireNonNull(code, "code must not be null");
		Objects.requireNonNull(description, "description must not be null");

		if (description.isBlank()) {
			throw new IllegalArgumentException("description must not be blank");
		}

		if (scoreImpact <= 0) {
			throw new IllegalArgumentException("scoreImpact must be greater than zero");
		}
	}
}
