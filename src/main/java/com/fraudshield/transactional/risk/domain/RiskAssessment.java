package com.fraudshield.transactional.risk.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record RiskAssessment(
		RiskDecision decision,
		int score,
		List<RiskReason> reasons,
		String rulesVersion,
		Instant evaluatedAt
) {
	public RiskAssessment {
		Objects.requireNonNull(decision, "decision must not be null");
		Objects.requireNonNull(reasons, "reasons must not be null");
		Objects.requireNonNull(rulesVersion, "rulesVersion must not be null");
		Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");

		reasons = List.copyOf(reasons);

		if (score < 0) {
			throw new IllegalArgumentException("score must not be negative");
		}

		if (rulesVersion.isBlank()) {
			throw new IllegalArgumentException("rulesVersion must not be blank");
		}
	}
}
