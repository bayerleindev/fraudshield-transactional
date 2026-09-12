package com.fraudshield.transactional.device.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "devices")
public class DeviceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false, length = 64)
	private String customerId;

	@Column(name = "device_id", nullable = false, length = 64)
	private String deviceId;

	@Column(name = "first_seen_at", nullable = false)
	private Instant firstSeenAt;

	@Column(name = "last_seen_at", nullable = false)
	private Instant lastSeenAt;

	@Column(nullable = false)
	private boolean trusted;

	protected DeviceEntity() {
	}

	public DeviceEntity(String customerId, String deviceId, Instant firstSeenAt, Instant lastSeenAt, boolean trusted) {
		this.customerId = requireNotBlank(customerId, "customerId");
		this.deviceId = requireNotBlank(deviceId, "deviceId");
		this.firstSeenAt = Objects.requireNonNull(firstSeenAt, "firstSeenAt must not be null");
		this.lastSeenAt = Objects.requireNonNull(lastSeenAt, "lastSeenAt must not be null");
		this.trusted = trusted;
	}

	public Long getId() {
		return id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public Instant getFirstSeenAt() {
		return firstSeenAt;
	}

	public Instant getLastSeenAt() {
		return lastSeenAt;
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
