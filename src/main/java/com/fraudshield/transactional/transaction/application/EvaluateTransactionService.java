package com.fraudshield.transactional.transaction.application;

import com.fraudshield.transactional.audit.infra.RiskDecisionEntity;
import com.fraudshield.transactional.audit.infra.RiskDecisionRepository;
import com.fraudshield.transactional.audit.infra.RiskReasonEntity;
import com.fraudshield.transactional.beneficiary.infra.BeneficiaryRepository;
import com.fraudshield.transactional.customer.infra.CustomerRepository;
import com.fraudshield.transactional.device.infra.DeviceEntity;
import com.fraudshield.transactional.device.infra.DeviceRepository;
import com.fraudshield.transactional.risk.application.RiskEngine;
import com.fraudshield.transactional.risk.domain.RiskAssessment;
import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.shared.exception.DomainException;
import com.fraudshield.transactional.shared.idempotency.RequestFingerprintService;
import com.fraudshield.transactional.shared.observability.CorrelationIdFilter;
import com.fraudshield.transactional.transaction.domain.TransactionEvaluation;
import com.fraudshield.transactional.transaction.infra.TransactionEntity;
import com.fraudshield.transactional.transaction.infra.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class EvaluateTransactionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateTransactionService.class);

	private final CustomerRepository customerRepository;
	private final DeviceRepository deviceRepository;
	private final BeneficiaryRepository beneficiaryRepository;
	private final TransactionRepository transactionRepository;
	private final RiskDecisionRepository riskDecisionRepository;
	private final RiskEngine riskEngine;
	private final RequestFingerprintService requestFingerprintService;
	private final Clock clock;

	public EvaluateTransactionService(
			CustomerRepository customerRepository,
			DeviceRepository deviceRepository,
			BeneficiaryRepository beneficiaryRepository,
			TransactionRepository transactionRepository,
			RiskDecisionRepository riskDecisionRepository,
			RiskEngine riskEngine,
			RequestFingerprintService requestFingerprintService,
			Clock clock
	) {
		this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
		this.deviceRepository = Objects.requireNonNull(deviceRepository, "deviceRepository must not be null");
		this.beneficiaryRepository = Objects.requireNonNull(beneficiaryRepository, "beneficiaryRepository must not be null");
		this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
		this.riskDecisionRepository = Objects.requireNonNull(riskDecisionRepository, "riskDecisionRepository must not be null");
		this.riskEngine = Objects.requireNonNull(riskEngine, "riskEngine must not be null");
		this.requestFingerprintService = Objects.requireNonNull(
				requestFingerprintService,
				"requestFingerprintService must not be null"
		);
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	@Transactional
	public TransactionEvaluationResult evaluate(TransactionEvaluation transaction) {
		Objects.requireNonNull(transaction, "transaction must not be null");
		var startedAtNanos = System.nanoTime();
		var requestFingerprint = requestFingerprintService.fingerprint(transaction);

		var existingTransaction = transactionRepository.findByTransactionId(transaction.transactionId());
		if (existingTransaction.isPresent()) {
			if (existingTransaction.get().getRequestFingerprint().equals(requestFingerprint.value())) {
				return storedResult(transaction.transactionId());
			}
			throw DomainException.transactionConflict();
		}

		var customer = customerRepository.findByCustomerId(transaction.customerId())
				.orElseThrow(DomainException::customerNotFound);
		var device = deviceRepository.findByCustomerIdAndDeviceId(transaction.customerId(), transaction.deviceId());
		var beneficiaryKnown = beneficiaryRepository.existsByCustomerIdAndBeneficiaryId(
				transaction.customerId(),
				transaction.beneficiaryId()
		);
		var evaluatedAt = clock.instant();

		var assessment = riskEngine.evaluate(new RiskEvaluationContext(
				transaction.amount(),
				customer.getCreatedAt(),
				customer.getLastPasswordChangeAt(),
				device.isPresent(),
				device.map(DeviceEntity::isTrusted).orElse(false),
				beneficiaryKnown,
				evaluatedAt
		));

		persistAuditTrail(transaction, assessment, evaluatedAt, requestFingerprint.value());
		logSuccessfulEvaluation(assessment.rulesVersion(), startedAtNanos);

		return new TransactionEvaluationResult(transaction.transactionId(), assessment);
	}

	private TransactionEvaluationResult storedResult(String transactionId) {
		var decision = riskDecisionRepository.findFirstByTransactionIdOrderByEvaluatedAtAsc(transactionId)
				.orElseThrow(() -> new IllegalStateException("Stored transaction has no risk decision"));
		var reasons = decision.getReasons().stream()
				.map(reason -> new RiskReason(reason.getCode(), reason.getDescription(), reason.getScoreImpact()))
				.toList();
		var assessment = new RiskAssessment(
				decision.getDecision(),
				decision.getScore(),
				reasons,
				decision.getRulesVersion(),
				decision.getEvaluatedAt()
		);
		return new TransactionEvaluationResult(transactionId, assessment);
	}

	private void persistAuditTrail(
			TransactionEvaluation transaction,
			RiskAssessment assessment,
			Instant evaluatedAt,
			String requestFingerprint
	) {
		transactionRepository.save(new TransactionEntity(
				transaction.transactionId(),
				transaction.customerId(),
				transaction.amount(),
				transaction.currency(),
				transaction.paymentMethod(),
				transaction.beneficiaryId(),
				transaction.deviceId(),
				transaction.ipAddress(),
				transaction.occurredAt(),
				evaluatedAt,
				requestFingerprint,
				evaluatedAt
		));

		var decision = new RiskDecisionEntity(
				transaction.transactionId(),
				assessment.decision(),
				assessment.score(),
				assessment.rulesVersion(),
				assessment.evaluatedAt()
		);
		assessment.reasons().forEach(reason -> decision.addReason(new RiskReasonEntity(
				reason.code(),
				reason.description(),
				reason.scoreImpact()
		)));
		riskDecisionRepository.save(decision);
	}

	private static void logSuccessfulEvaluation(String rulesVersion, long startedAtNanos) {
		var durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos);

		LOGGER.info(
				"transaction_evaluation_completed correlationId={} rulesVersion={} durationMs={}",
				MDC.get(CorrelationIdFilter.MDC_KEY),
				rulesVersion,
				durationMs
		);
	}
}
