package com.fraudshield.transactional.transaction.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
	Optional<TransactionEntity> findByTransactionId(String transactionId);

	boolean existsByTransactionId(String transactionId);
}
