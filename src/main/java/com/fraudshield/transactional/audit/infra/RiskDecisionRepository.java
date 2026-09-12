package com.fraudshield.transactional.audit.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskDecisionRepository extends JpaRepository<RiskDecisionEntity, Long> {
	List<RiskDecisionEntity> findByTransactionId(String transactionId);
}
