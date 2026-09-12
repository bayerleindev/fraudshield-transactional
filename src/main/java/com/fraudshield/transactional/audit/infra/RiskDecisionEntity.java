package com.fraudshield.transactional.audit.infra;

import com.fraudshield.transactional.risk.domain.RiskDecision;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "risk_decisions")
public class RiskDecisionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transaction_id", nullable = false, length = 64)
	private String transactionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private RiskDecision decision;

	@Column(nullable = false)
	private int score;

	@Column(name = "rules_version", nullable = false, length = 32)
	private String rulesVersion;

	@Column(name = "evaluated_at", nullable = false)
	private Instant evaluatedAt;

	@OneToMany(mappedBy = "riskDecision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("id asc")
	private List<RiskReasonEntity> reasons = new ArrayList<>();

	protected RiskDecisionEntity() {
	}

	public RiskDecisionEntity(String transactionId, RiskDecision decision, int score, String rulesVersion, Instant evaluatedAt) {
		this.transactionId = requireNotBlank(transactionId, "transactionId");
		this.decision = Objects.requireNonNull(decision, "decision must not be null");
		this.score = score;
		this.rulesVersion = requireNotBlank(rulesVersion, "rulesVersion");
		this.evaluatedAt = Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");
		if (score < 0) {
			throw new IllegalArgumentException("score must not be negative");
		}
	}

	public void addReason(RiskReasonEntity reason) {
		Objects.requireNonNull(reason, "reason must not be null");
		reason.attachTo(this);
		reasons.add(reason);
	}

	public Long getId() {
		return id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public RiskDecision getDecision() {
		return decision;
	}

	public int getScore() {
		return score;
	}

	public String getRulesVersion() {
		return rulesVersion;
	}

	public Instant getEvaluatedAt() {
		return evaluatedAt;
	}

	public List<RiskReasonEntity> getReasons() {
		return Collections.unmodifiableList(reasons);
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
