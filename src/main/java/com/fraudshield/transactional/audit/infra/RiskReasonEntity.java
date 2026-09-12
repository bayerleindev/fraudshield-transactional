package com.fraudshield.transactional.audit.infra;

import com.fraudshield.transactional.risk.domain.RiskReasonCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "risk_reasons")
public class RiskReasonEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "risk_decision_id", nullable = false)
	private RiskDecisionEntity riskDecision;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private RiskReasonCode code;

	@Column(nullable = false, length = 255)
	private String description;

	@Column(name = "score_impact", nullable = false)
	private int scoreImpact;

	protected RiskReasonEntity() {
	}

	public RiskReasonEntity(RiskReasonCode code, String description, int scoreImpact) {
		this.code = Objects.requireNonNull(code, "code must not be null");
		this.description = requireNotBlank(description, "description");
		this.scoreImpact = scoreImpact;
		if (scoreImpact <= 0) {
			throw new IllegalArgumentException("scoreImpact must be greater than zero");
		}
	}

	void attachTo(RiskDecisionEntity riskDecision) {
		this.riskDecision = Objects.requireNonNull(riskDecision, "riskDecision must not be null");
	}

	public Long getId() {
		return id;
	}

	public RiskDecisionEntity getRiskDecision() {
		return riskDecision;
	}

	public RiskReasonCode getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public int getScoreImpact() {
		return scoreImpact;
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
