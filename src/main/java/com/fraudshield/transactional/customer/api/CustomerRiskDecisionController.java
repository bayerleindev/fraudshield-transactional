package com.fraudshield.transactional.customer.api;

import com.fraudshield.transactional.customer.application.GetCustomerRiskDecisionsService;
import com.fraudshield.transactional.risk.api.RiskDecisionSummaryResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/customers")
class CustomerRiskDecisionController {
	static final int DEFAULT_LIMIT = 20;
	static final int MAX_LIMIT = 100;

	private final GetCustomerRiskDecisionsService getCustomerRiskDecisionsService;

	CustomerRiskDecisionController(GetCustomerRiskDecisionsService getCustomerRiskDecisionsService) {
		this.getCustomerRiskDecisionsService = Objects.requireNonNull(
				getCustomerRiskDecisionsService,
				"getCustomerRiskDecisionsService must not be null"
		);
	}

	@GetMapping("/{customerId}/risk-decisions")
	ResponseEntity<List<RiskDecisionSummaryResponse>> getRiskDecisions(
			@PathVariable String customerId,
			@RequestParam(defaultValue = "" + DEFAULT_LIMIT) @Min(1) @Max(MAX_LIMIT) int limit
	) {
		return ResponseEntity.ok(getCustomerRiskDecisionsService.getRecentDecisions(customerId, limit));
	}
}
