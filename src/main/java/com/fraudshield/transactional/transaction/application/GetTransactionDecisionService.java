package com.fraudshield.transactional.transaction.application;

import com.fraudshield.transactional.audit.infra.RiskDecisionRepository;
import com.fraudshield.transactional.risk.api.RiskDecisionDetailResponse;
import com.fraudshield.transactional.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class GetTransactionDecisionService {
	private final RiskDecisionRepository riskDecisionRepository;

	public GetTransactionDecisionService(RiskDecisionRepository riskDecisionRepository) {
		this.riskDecisionRepository = Objects.requireNonNull(riskDecisionRepository, "riskDecisionRepository must not be null");
	}

	@Transactional(readOnly = true)
	public RiskDecisionDetailResponse getLatestDecision(String transactionId) {
		return riskDecisionRepository.findFirstByTransactionIdOrderByEvaluatedAtDescIdDesc(transactionId)
				.map(RiskDecisionDetailResponse::from)
				.orElseThrow(DomainException::transactionDecisionNotFound);
	}
}
