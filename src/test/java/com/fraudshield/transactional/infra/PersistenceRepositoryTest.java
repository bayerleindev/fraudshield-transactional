package com.fraudshield.transactional.infra;

import com.fraudshield.transactional.audit.infra.RiskDecisionEntity;
import com.fraudshield.transactional.audit.infra.RiskDecisionRepository;
import com.fraudshield.transactional.audit.infra.RiskReasonEntity;
import com.fraudshield.transactional.beneficiary.infra.BeneficiaryEntity;
import com.fraudshield.transactional.beneficiary.infra.BeneficiaryRepository;
import com.fraudshield.transactional.customer.infra.CustomerEntity;
import com.fraudshield.transactional.customer.infra.CustomerRepository;
import com.fraudshield.transactional.device.infra.DeviceEntity;
import com.fraudshield.transactional.device.infra.DeviceRepository;
import com.fraudshield.transactional.risk.domain.RiskDecision;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;
import com.fraudshield.transactional.support.PostgresIntegrationTest;
import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import com.fraudshield.transactional.transaction.infra.TransactionEntity;
import com.fraudshield.transactional.transaction.infra.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PersistenceRepositoryTest extends PostgresIntegrationTest {
	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private BeneficiaryRepository beneficiaryRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private RiskDecisionRepository riskDecisionRepository;

	@Test
	void persistsContextualEntitiesAndSupportsEvaluationLookups() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.save(new CustomerEntity("cus-123", now.minusSeconds(86400), now.minusSeconds(3600), "ACTIVE"));
		deviceRepository.save(new DeviceEntity("cus-123", "dev-abc", now.minusSeconds(7200), now, true));
		beneficiaryRepository.save(new BeneficiaryEntity("cus-123", "ben-999", now.minusSeconds(600), true));

		assertThat(customerRepository.findByCustomerId("cus-123"))
				.isPresent()
				.get()
				.extracting(CustomerEntity::getStatus)
				.isEqualTo("ACTIVE");
		assertThat(deviceRepository.findByCustomerIdAndDeviceId("cus-123", "dev-abc"))
				.isPresent()
				.get()
				.extracting(DeviceEntity::isTrusted)
				.isEqualTo(true);
		assertThat(beneficiaryRepository.existsByCustomerIdAndBeneficiaryId("cus-123", "ben-999")).isTrue();
	}

	@Test
	void preventsDuplicateTransactionIds() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.saveAndFlush(new CustomerEntity("cus-duplicate", now, null, "ACTIVE"));
		transactionRepository.saveAndFlush(transaction("tx-duplicate", "cus-duplicate", "ben-1", "dev-1", now));

		assertThatThrownBy(() -> transactionRepository.saveAndFlush(transaction("tx-duplicate", "cus-duplicate", "ben-2", "dev-2", now)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsDuplicateCustomerIds() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.saveAndFlush(new CustomerEntity("cus-duplicate-context", now, null, "ACTIVE"));

		assertThatThrownBy(() -> customerRepository.saveAndFlush(
				new CustomerEntity("cus-duplicate-context", now.plusSeconds(1), null, "ACTIVE")
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsDuplicateDevicesForCustomer() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.saveAndFlush(new CustomerEntity("cus-device-constraint", now, null, "ACTIVE"));
		deviceRepository.saveAndFlush(new DeviceEntity("cus-device-constraint", "dev-duplicate", now, now, true));

		assertThatThrownBy(() -> deviceRepository.saveAndFlush(
				new DeviceEntity("cus-device-constraint", "dev-duplicate", now, now.plusSeconds(1), false)
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsDuplicateBeneficiariesForCustomer() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.saveAndFlush(new CustomerEntity("cus-beneficiary-constraint", now, null, "ACTIVE"));
		beneficiaryRepository.saveAndFlush(new BeneficiaryEntity("cus-beneficiary-constraint", "ben-duplicate", now, true));

		assertThatThrownBy(() -> beneficiaryRepository.saveAndFlush(
				new BeneficiaryEntity("cus-beneficiary-constraint", "ben-duplicate", now.plusSeconds(1), false)
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void preventsAuditDecisionWithoutPersistedTransaction() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		RiskDecisionEntity decision = new RiskDecisionEntity("tx-missing", RiskDecision.CHALLENGE, 30, "v1", now);

		assertThatThrownBy(() -> riskDecisionRepository.saveAndFlush(decision))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void persistsAuditDecisionWithMultipleReasons() {
		Instant now = Instant.parse("2026-09-12T14:30:00Z");
		customerRepository.saveAndFlush(new CustomerEntity("cus-audit", now.minusSeconds(10), now.minusSeconds(5), "ACTIVE"));
		transactionRepository.saveAndFlush(transaction("tx-audit", "cus-audit", "ben-audit", "dev-audit", now));

		RiskDecisionEntity decision = new RiskDecisionEntity("tx-audit", RiskDecision.REVIEW, 75, "v1", now.plusSeconds(1));
		decision.addReason(new RiskReasonEntity(
				RiskReasonCode.HIGH_AMOUNT,
				"Transaction amount is above the configured threshold.",
				30
		));
		decision.addReason(new RiskReasonEntity(
				RiskReasonCode.NEW_BENEFICIARY,
				"Customer has no previous relationship with this beneficiary.",
				25
		));
		decision.addReason(new RiskReasonEntity(
				RiskReasonCode.RECENT_PASSWORD_CHANGE,
				"Customer password changed recently.",
				20
		));

		riskDecisionRepository.saveAndFlush(decision);

		List<RiskDecisionEntity> decisions = riskDecisionRepository.findByTransactionId("tx-audit");
		assertThat(decisions).hasSize(1);
		RiskDecisionEntity persisted = decisions.getFirst();
		assertThat(persisted.getDecision()).isEqualTo(RiskDecision.REVIEW);
		assertThat(persisted.getScore()).isEqualTo(75);
		assertThat(persisted.getReasons())
				.extracting(RiskReasonEntity::getCode)
				.containsExactly(RiskReasonCode.HIGH_AMOUNT, RiskReasonCode.NEW_BENEFICIARY, RiskReasonCode.RECENT_PASSWORD_CHANGE);
		assertThat(persisted.getReasons())
				.extracting(RiskReasonEntity::getScoreImpact)
				.containsExactly(30, 25, 20);
	}

	private static TransactionEntity transaction(
			String transactionId,
			String customerId,
			String beneficiaryId,
			String deviceId,
			Instant occurredAt
	) {
		return new TransactionEntity(
				transactionId,
				customerId,
				new BigDecimal("8500.00"),
				"BRL",
				PaymentMethod.PIX,
				beneficiaryId,
				deviceId,
				"177.10.20.30",
				occurredAt,
				occurredAt.plusSeconds(1)
		);
	}
}
