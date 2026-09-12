package com.fraudshield.transactional.risk.domain;

public enum RiskReasonCode {
	HIGH_AMOUNT,
	VERY_HIGH_AMOUNT,
	NEW_DEVICE,
	UNTRUSTED_DEVICE,
	NEW_BENEFICIARY,
	RECENT_PASSWORD_CHANGE,
	NEW_ACCOUNT
}
