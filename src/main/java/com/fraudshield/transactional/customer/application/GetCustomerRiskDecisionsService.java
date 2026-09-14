package com.fraudshield.transactional.customer.application;

import com.fraudshield.transactional.audit.infra.RiskDecisionRepository;
import com.fraudshield.transactional.customer.infra.CustomerRepository;
import com.fraudshield.transactional.risk.api.RiskDecisionSummaryResponse;
import com.fraudshield.transactional.shared.exception.DomainException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class GetCustomerRiskDecisionsService {
	private final CustomerRepository customerRepository;
	private final RiskDecisionRepository riskDecisionRepository;

	public GetCustomerRiskDecisionsService(
			CustomerRepository customerRepository,
			RiskDecisionRepository riskDecisionRepository
	) {
		this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
		this.riskDecisionRepository = Objects.requireNonNull(riskDecisionRepository, "riskDecisionRepository must not be null");
	}

	@Transactional(readOnly = true)
	public List<RiskDecisionSummaryResponse> getRecentDecisions(String customerId, int limit) {
		if (!customerRepository.existsByCustomerId(customerId)) {
			throw DomainException.customerResourceNotFound();
		}

		return riskDecisionRepository.findRecentByCustomerId(customerId, PageRequest.of(0, limit)).stream()
				.map(RiskDecisionSummaryResponse::from)
				.toList();
	}
}
