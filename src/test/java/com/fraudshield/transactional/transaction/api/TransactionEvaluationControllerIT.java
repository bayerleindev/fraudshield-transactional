package com.fraudshield.transactional.transaction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class TransactionEvaluationControllerIT extends PostgresIntegrationTest {
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
	void returnsApproveForKnownLowRiskContext() throws Exception {
		saveStableCustomerContext("cus-it-approve", "dev-it-approve", "ben-it-approve");

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-it-approve")
								.customerId("cus-it-approve")
								.amount(new BigDecimal("100.00"))
								.deviceId("dev-it-approve")
								.beneficiaryId("ben-it-approve")
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("APPROVE"))
				.andExpect(jsonPath("$.score").value(0))
				.andExpect(jsonPath("$.reasons", hasSize(0)));
	}

	@Test
	void returnsChallengeForHighAmount() throws Exception {
		saveStableCustomerContext("cus-it-challenge", "dev-it-challenge", "ben-it-challenge");

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-it-challenge")
								.customerId("cus-it-challenge")
								.amount(new BigDecimal("5000.00"))
								.deviceId("dev-it-challenge")
								.beneficiaryId("ben-it-challenge")
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("CHALLENGE"))
				.andExpect(jsonPath("$.score").value(30))
				.andExpect(jsonPath("$.reasons", hasSize(1)))
				.andExpect(jsonPath("$.reasons[*].code", contains("HIGH_AMOUNT")));
	}

	@Test
	void returnsReviewForHighAmountNewDeviceAndNewBeneficiary() throws Exception {
		customerRepository.save(new CustomerEntity("cus-it-review", NOW.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-it-review")
								.customerId("cus-it-review")
								.amount(new BigDecimal("5000.00"))
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.score").value(75))
				.andExpect(jsonPath("$.reasons", hasSize(3)))
				.andExpect(jsonPath("$.reasons[*].code", contains("HIGH_AMOUNT", "NEW_DEVICE", "NEW_BENEFICIARY")));
	}

	@Test
	void returnsDenyForVeryHighAmountPlusAdditionalRisk() throws Exception {
		customerRepository.save(new CustomerEntity("cus-it-deny", NOW.minusSeconds(60), NOW.minusSeconds(60), "ACTIVE"));

		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-it-deny")
								.customerId("cus-it-deny")
								.amount(new BigDecimal("20000.00"))
								.build())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.decision").value("DENY"))
				.andExpect(jsonPath("$.score").value(135))
				.andExpect(jsonPath("$.reasons", hasSize(5)))
				.andExpect(jsonPath("$.reasons[*].code", contains(
						"VERY_HIGH_AMOUNT",
						"NEW_DEVICE",
						"NEW_BENEFICIARY",
						"RECENT_PASSWORD_CHANGE",
						"NEW_ACCOUNT"
				)));
	}

	@Test
	void missingTransactionIdReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("")
								.build())))
				.andExpect(status().isBadRequest())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[*].field", contains("transactionId")));
	}

	@Test
	void nonPositiveAmountReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/transactions/evaluate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.amount(BigDecimal.ZERO)
								.build())))
				.andExpect(status().isBadRequest())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[*].field", contains("amount")));
	}

	@Test
	void successfulEvaluationPersistsTransactionDecisionAndAllReasons() throws Exception {
		customerRepository.save(new CustomerEntity("cus-it-persistence", NOW.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));

		mockMvc.perform(post("/transactions/evaluate")
						.header("X-Correlation-Id", "corr-it-persistence")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequestBuilder()
								.transactionId("tx-it-persistence")
								.customerId("cus-it-persistence")
								.amount(new BigDecimal("5000.00"))
								.build())))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-Id", "corr-it-persistence"))
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.score").value(75));

		assertThat(transactionRepository.findByTransactionId("tx-it-persistence"))
				.isPresent()
				.get()
				.satisfies(transaction -> {
					assertThat(transaction.getCustomerId()).isEqualTo("cus-it-persistence");
					assertThat(transaction.getAmount()).isEqualByComparingTo("5000.00");
					assertThat(transaction.getCurrency()).isEqualTo("BRL");
					assertThat(transaction.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
				});

		assertThat(riskDecisionRepository.findByTransactionId("tx-it-persistence"))
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
					assertThat(decision.getReasons())
							.extracting(RiskReasonEntity::getScoreImpact)
							.containsExactly(30, 20, 25);
				});
	}

	private void saveStableCustomerContext(String customerId, String deviceId, String beneficiaryId) {
		customerRepository.save(new CustomerEntity(customerId, NOW.minusSeconds(30 * 24 * 60 * 60), NOW.minusSeconds(10 * 24 * 60 * 60), "ACTIVE"));
		deviceRepository.save(new DeviceEntity(customerId, deviceId, NOW.minusSeconds(20 * 24 * 60 * 60), NOW, true));
		beneficiaryRepository.save(new BeneficiaryEntity(customerId, beneficiaryId, NOW.minusSeconds(20 * 24 * 60 * 60), true));
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
		private String transactionId = "tx-it-001";
		private String customerId = "cus-it-123";
		private BigDecimal amount = new BigDecimal("8500.00");
		private String currency = "BRL";
		private PaymentMethod paymentMethod = PaymentMethod.PIX;
		private String beneficiaryId = "ben-it-999";
		private String deviceId = "dev-it-abc";
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
