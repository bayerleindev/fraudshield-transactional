package com.fraudshield.transactional.transaction.infra;

import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transaction_id", nullable = false, unique = true, length = 64)
	private String transactionId;

	@Column(name = "customer_id", nullable = false, length = 64)
	private String customerId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 32)
	private PaymentMethod paymentMethod;

	@Column(name = "beneficiary_id", nullable = false, length = 64)
	private String beneficiaryId;

	@Column(name = "device_id", nullable = false, length = 64)
	private String deviceId;

	@Column(name = "ip_address", nullable = false, length = 45)
	private String ipAddress;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "request_fingerprint", nullable = false, length = 64)
	private String requestFingerprint;

	@Column(name = "first_evaluated_at", nullable = false)
	private Instant firstEvaluatedAt;

	protected TransactionEntity() {
	}

	public TransactionEntity(
			String transactionId,
			String customerId,
			BigDecimal amount,
			String currency,
			PaymentMethod paymentMethod,
			String beneficiaryId,
			String deviceId,
			String ipAddress,
			Instant occurredAt,
			Instant createdAt,
			String requestFingerprint,
			Instant firstEvaluatedAt
	) {
		this.transactionId = requireNotBlank(transactionId, "transactionId");
		this.customerId = requireNotBlank(customerId, "customerId");
		this.amount = Objects.requireNonNull(amount, "amount must not be null");
		this.currency = requireNotBlank(currency, "currency");
		this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
		this.beneficiaryId = requireNotBlank(beneficiaryId, "beneficiaryId");
		this.deviceId = requireNotBlank(deviceId, "deviceId");
		this.ipAddress = requireNotBlank(ipAddress, "ipAddress");
		this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.requestFingerprint = requireNotBlank(requestFingerprint, "requestFingerprint");
		this.firstEvaluatedAt = Objects.requireNonNull(firstEvaluatedAt, "firstEvaluatedAt must not be null");
	}

	public Long getId() {
		return id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getBeneficiaryId() {
		return beneficiaryId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getRequestFingerprint() {
		return requestFingerprint;
	}

	public Instant getFirstEvaluatedAt() {
		return firstEvaluatedAt;
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
