package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.risk.api.RiskDecisionDetailResponse;
import com.fraudshield.transactional.transaction.application.GetTransactionDecisionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Validated
@RestController
@RequestMapping("/transactions")
class TransactionDecisionQueryController {
	private final GetTransactionDecisionService getTransactionDecisionService;

	TransactionDecisionQueryController(GetTransactionDecisionService getTransactionDecisionService) {
		this.getTransactionDecisionService = Objects.requireNonNull(
				getTransactionDecisionService,
				"getTransactionDecisionService must not be null"
		);
	}

	@GetMapping("/{transactionId}/decision")
	ResponseEntity<RiskDecisionDetailResponse> getDecision(@PathVariable @NotBlank String transactionId) {
		return ResponseEntity.ok(getTransactionDecisionService.getLatestDecision(transactionId));
	}
}
