package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.transaction.application.EvaluateTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/transactions")
class TransactionEvaluationController {
	private final EvaluateTransactionService evaluateTransactionService;

	TransactionEvaluationController(EvaluateTransactionService evaluateTransactionService) {
		this.evaluateTransactionService = Objects.requireNonNull(
				evaluateTransactionService,
				"evaluateTransactionService must not be null"
		);
	}

	@PostMapping("/evaluate")
	ResponseEntity<TransactionEvaluationResponse> evaluate(@Valid @RequestBody TransactionEvaluationRequest request) {
		var result = evaluateTransactionService.evaluate(request.toDomain());
		return ResponseEntity.ok(result.toResponse());
	}
}
