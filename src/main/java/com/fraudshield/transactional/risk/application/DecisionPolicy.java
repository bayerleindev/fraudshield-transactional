package com.fraudshield.transactional.risk.application;

import com.fraudshield.transactional.risk.domain.RiskDecision;

public final class DecisionPolicy {

	public RiskDecision decide(int score) {
		if (score < 0) {
			throw new IllegalArgumentException("score must not be negative");
		}
		if (score >= 90) {
			return RiskDecision.DENY;
		}
		if (score >= 60) {
			return RiskDecision.REVIEW;
		}
		if (score >= 30) {
			return RiskDecision.CHALLENGE;
		}
		return RiskDecision.APPROVE;
	}
}
