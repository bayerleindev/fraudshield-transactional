package com.fraudshield.transactional.beneficiary.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, Long> {
	Optional<BeneficiaryEntity> findByCustomerIdAndBeneficiaryId(String customerId, String beneficiaryId);

	boolean existsByCustomerIdAndBeneficiaryId(String customerId, String beneficiaryId);
}
