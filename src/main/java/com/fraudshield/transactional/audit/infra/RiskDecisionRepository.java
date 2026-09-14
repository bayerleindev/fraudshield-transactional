package com.fraudshield.transactional.audit.infra;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RiskDecisionRepository extends JpaRepository<RiskDecisionEntity, Long> {
	List<RiskDecisionEntity> findByTransactionId(String transactionId);

	Optional<RiskDecisionEntity> findFirstByTransactionIdOrderByEvaluatedAtAsc(String transactionId);

	Optional<RiskDecisionEntity> findFirstByTransactionIdOrderByEvaluatedAtDescIdDesc(String transactionId);

	@Query("""
			select decision
			from RiskDecisionEntity decision
			join TransactionEntity transaction on transaction.transactionId = decision.transactionId
			where transaction.customerId = :customerId
			order by decision.evaluatedAt desc, decision.id desc
			""")
	List<RiskDecisionEntity> findRecentByCustomerId(@Param("customerId") String customerId, Pageable pageable);
}
