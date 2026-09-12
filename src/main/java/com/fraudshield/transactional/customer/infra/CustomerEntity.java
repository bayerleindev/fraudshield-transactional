package com.fraudshield.transactional.customer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "customers")
public class CustomerEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false, unique = true, length = 64)
	private String customerId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_password_change_at")
	private Instant lastPasswordChangeAt;

	@Column(nullable = false, length = 32)
	private String status;

	protected CustomerEntity() {
	}

	public CustomerEntity(String customerId, Instant createdAt, Instant lastPasswordChangeAt, String status) {
		this.customerId = requireNotBlank(customerId, "customerId");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.lastPasswordChangeAt = lastPasswordChangeAt;
		this.status = requireNotBlank(status, "status");
	}

	public Long getId() {
		return id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastPasswordChangeAt() {
		return lastPasswordChangeAt;
	}

	public String getStatus() {
		return status;
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
