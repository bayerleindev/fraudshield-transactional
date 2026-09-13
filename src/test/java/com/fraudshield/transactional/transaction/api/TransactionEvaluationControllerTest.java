package com.fraudshield.transactional.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fraudshield.transactional.transaction.infra.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class TransactionEvaluationControllerTest extends PostgresIntegrationTest {
	private static final Instant NOW = Instant.parse("2026-09-12T14:30:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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

	@AfterEach
	void cleanUp() {
		riskDecisionRepository.deleteAll();
		transactionRepository.deleteAll();
		beneficiaryRepository.deleteAll();
		deviceRepository.deleteAll();
		customerRepository.deleteAll();
	}

	@Test
	void evaluatesTransactionAndPersistsAuditTrail() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-review", NOW.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));

		mockMvc.perform(post("/transactions/evaluate")
						.header("X-Correlation-Id", "corr-api-review")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-api-review")
								.customerId("cus-api-review")
								.amount(new BigDecimal("8500.00"))
								.build())))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-Id", "corr-api-review"))
				.andExpect(jsonPath("$.transactionId").value("tx-api-review"))
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.score").value(75))
				.andExpect(jsonPath("$.rulesVersion").value("v1"))
				.andExpect(jsonPath("$.evaluatedAt").isNotEmpty())
				.andExpect(jsonPath("$.reasons", hasSize(3)))
				.andExpect(jsonPath("$.reasons[*].code", contains("HIGH_AMOUNT", "NEW_DEVICE", "NEW_BENEFICIARY")))
				.andExpect(jsonPath("$.reasons[*].scoreImpact", contains(30, 20, 25)));

		assertThat(transactionRepository.findByTransactionId("tx-api-review")).isPresent();
		assertThat(riskDecisionRepository.findByTransactionId("tx-api-review"))
				.singleElement()
				.satisfies(decision -> {
					assertThat(decision.getDecision()).isEqualTo(RiskDecision.REVIEW);
					assertThat(decision.getScore()).isEqualTo(75);
					assertThat(decision.getRulesVersion()).isEqualTo("v1");
					assertThat(decision.getReasons())
							.extracting(RiskReasonEntity::getCode)
							.containsExactly(
									RiskReasonCode.HIGH_AMOUNT,
									RiskReasonCode.NEW_DEVICE,
									RiskReasonCode.NEW_BENEFICIARY
							);
				});
	}

	@Test
	void returnsApprovalForKnownLowRiskContext() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-approve", NOW.minusSeconds(30 * 24 * 60 * 60), NOW.minusSeconds(10 * 24 * 60 * 60), "ACTIVE"));
		deviceRepository.save(new DeviceEntity("cus-api-approve", "dev-known", NOW.minusSeconds(20 * 24 * 60 * 60), NOW, true));
		beneficiaryRepository.save(new BeneficiaryEntity("cus-api-approve", "ben-known", NOW.minusSeconds(20 * 24 * 60 * 60), true));

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-api-approve")
								.customerId("cus-api-approve")
								.amount(new BigDecimal("100.00"))
								.beneficiaryId("ben-known")
								.deviceId("dev-known")
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("APPROVE"))
				.andExpect(jsonPath("$.score").value(0))
				.andExpect(jsonPath("$.reasons", hasSize(0)));
	}

	@Test
	void returnsChallengeForHighAmountKnownContext() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-challenge", NOW.minusSeconds(30 * 24 * 60 * 60), NOW.minusSeconds(10 * 24 * 60 * 60), "ACTIVE"));
		deviceRepository.save(new DeviceEntity("cus-api-challenge", "dev-known", NOW.minusSeconds(20 * 24 * 60 * 60), NOW, true));
		beneficiaryRepository.save(new BeneficiaryEntity("cus-api-challenge", "ben-known", NOW.minusSeconds(20 * 24 * 60 * 60), true));

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-api-challenge")
								.customerId("cus-api-challenge")
								.amount(new BigDecimal("5000.00"))
								.beneficiaryId("ben-known")
								.deviceId("dev-known")
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("CHALLENGE"))
				.andExpect(jsonPath("$.score").value(30))
				.andExpect(jsonPath("$.reasons", hasSize(1)))
				.andExpect(jsonPath("$.reasons[*].code", contains("HIGH_AMOUNT")));
	}

	@Test
	void returnsDenyForVeryHighRiskContext() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-deny", NOW.minusSeconds(60), NOW.minusSeconds(60), "ACTIVE"));

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-api-deny")
								.customerId("cus-api-deny")
								.amount(new BigDecimal("20000.00"))
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("DENY"))
				.andExpect(jsonPath("$.score").value(135))
				.andExpect(jsonPath("$.reasons[*].code", contains(
						"VERY_HIGH_AMOUNT",
						"NEW_DEVICE",
						"NEW_BENEFICIARY",
						"RECENT_PASSWORD_CHANGE",
						"NEW_ACCOUNT"
				)));
	}

	@Test
	void rejectsInvalidPayload() throws Exception {
		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("")
								.amount(BigDecimal.ZERO)
								.build())))
				.andExpect(status().isBadRequest())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[*].field").value(contains("amount", "transactionId")));
	}

	@Test
	void rejectsInvalidPaymentMethod() throws Exception {
		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "transactionId": "tx-invalid-payment-method",
								  "customerId": "cus-api-123",
								  "amount": 100.00,
								  "currency": "BRL",
								  "paymentMethod": "WIRE",
								  "beneficiaryId": "ben-api-999",
								  "deviceId": "dev-api-abc",
								  "ipAddress": "177.10.20.30",
								  "occurredAt": "2026-09-12T14:30:00Z"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request contains an invalid value."))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void returnsDomainErrorWhenCustomerDoesNotExist() throws Exception {
		mockMvc.perform(post("/transactions/evaluate")
						.header("X-Correlation-Id", "corr-missing-customer")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-missing-customer")
								.customerId("cus-missing")
								.build())))
				.andExpect(status().isBadRequest())
				.andExpect(header().string("X-Correlation-Id", "corr-missing-customer"))
				.andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Customer was not found."))
				.andExpect(jsonPath("$.status").value(400));

		assertThat(transactionRepository.findByTransactionId("tx-missing-customer")).isEmpty();
		assertThat(riskDecisionRepository.findByTransactionId("tx-missing-customer")).isEmpty();
	}

	@Test
	void equivalentRetryReturnsStoredDecisionWithoutDuplicateAudit() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-duplicate", NOW.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));

		var request = validRequestBuilder()
				.transactionId("tx-api-duplicate")
				.customerId("cus-api-duplicate")
				.build();

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionId").value("tx-api-duplicate"))
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.score").value(75))
				.andExpect(jsonPath("$.evaluatedAt").value("2026-09-12T14:30:01Z"));

		assertThat(transactionRepository.findAll()).hasSize(1);
		assertThat(riskDecisionRepository.findByTransactionId("tx-api-duplicate")).hasSize(1);
	}

	@Test
	void conflictingRetryReturnsTransactionConflict() throws Exception {
		customerRepository.save(new CustomerEntity("cus-api-conflict", NOW.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));

		var request = validRequestBuilder()
				.transactionId("tx-api-conflict")
				.customerId("cus-api-conflict")
				.build();

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-api-conflict")
								.customerId("cus-api-conflict")
								.amount(new BigDecimal("9000.00"))
								.build())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("TRANSACTION_CONFLICT"))
				.andExpect(jsonPath("$.message").value("Transaction has already been evaluated with different data."))
				.andExpect(jsonPath("$.status").value(409));

		assertThat(transactionRepository.findAll()).hasSize(1);
		assertThat(riskDecisionRepository.findByTransactionId("tx-api-conflict")).hasSize(1);
	}

	private static RequestBuilder validRequestBuilder() {
		return new RequestBuilder();
	}

	@TestConfiguration
	static class FixedClockConfig {
		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(NOW.plusSeconds(1), ZoneOffset.UTC);
		}
	}

	private static final class RequestBuilder {
		private String transactionId = "tx-api-001";
		private String customerId = "cus-api-123";
		private BigDecimal amount = new BigDecimal("8500.00");
		private String currency = "BRL";
		private PaymentMethod paymentMethod = PaymentMethod.PIX;
		private String beneficiaryId = "ben-api-999";
		private String deviceId = "dev-api-abc";
		private String ipAddress = "177.10.20.30";
		private Instant occurredAt = NOW;

		private RequestBuilder transactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

		private RequestBuilder customerId(String customerId) {
			this.customerId = customerId;
			return this;
		}

		private RequestBuilder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		private RequestBuilder beneficiaryId(String beneficiaryId) {
			this.beneficiaryId = beneficiaryId;
			return this;
		}

		private RequestBuilder deviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}

		private TransactionEvaluationRequest build() {
			return new TransactionEvaluationRequest(
					transactionId,
					customerId,
					amount,
					currency,
					paymentMethod,
					beneficiaryId,
					deviceId,
					ipAddress,
					occurredAt
			);
		}
	}
}
