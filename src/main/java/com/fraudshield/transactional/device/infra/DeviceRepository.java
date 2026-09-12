package com.fraudshield.transactional.device.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {
	Optional<DeviceEntity> findByCustomerIdAndDeviceId(String customerId, String deviceId);

	boolean existsByCustomerIdAndDeviceId(String customerId, String deviceId);
}
