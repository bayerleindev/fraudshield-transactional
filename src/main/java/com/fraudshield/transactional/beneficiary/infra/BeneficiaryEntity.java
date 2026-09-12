package com.fraudshield.transactional.beneficiary.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "beneficiaries")
public class BeneficiaryEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false, length = 64)
	private String customerId;

	@Column(name = "beneficiary_id", nullable = false, length = 64)
	private String beneficiaryId;

	@Column(name = "first_transaction_at", nullable = false)
	private Instant firstTransactionAt;

	@Column(nullable = false)
	private boolean trusted;

	protected BeneficiaryEntity() {
	}

	public BeneficiaryEntity(String customerId, String beneficiaryId, Instant firstTransactionAt, boolean trusted) {
		this.customerId = requireNotBlank(customerId, "customerId");
		this.beneficiaryId = requireNotBlank(beneficiaryId, "beneficiaryId");
		this.firstTransactionAt = Objects.requireNonNull(firstTransactionAt, "firstTransactionAt must not be null");
		this.trusted = trusted;
	}

	public Long getId() {
		return id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getBeneficiaryId() {
		return beneficiaryId;
	}

	public Instant getFirstTransactionAt() {
		return firstTransactionAt;
	}

	public boolean isTrusted() {
		return trusted;
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
