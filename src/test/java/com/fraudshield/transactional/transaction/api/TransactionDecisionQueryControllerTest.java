package com.fraudshield.transactional.transaction.api;

import com.fraudshield.transactional.audit.infra.RiskDecisionEntity;
import com.fraudshield.transactional.audit.infra.RiskDecisionRepository;
import com.fraudshield.transactional.audit.infra.RiskReasonEntity;
import com.fraudshield.transactional.customer.infra.CustomerEntity;
import com.fraudshield.transactional.customer.infra.CustomerRepository;
import com.fraudshield.transactional.risk.domain.RiskDecision;
import com.fraudshield.transactional.risk.domain.RiskReasonCode;
import com.fraudshield.transactional.support.PostgresIntegrationTest;
import com.fraudshield.transactional.transaction.domain.PaymentMethod;
import com.fraudshield.transactional.transaction.infra.TransactionEntity;
import com.fraudshield.transactional.transaction.infra.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class TransactionDecisionQueryControllerTest extends PostgresIntegrationTest {
	private static final Instant BASE_TIME = Instant.parse("2026-09-12T14:30:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private RiskDecisionRepository riskDecisionRepository;

	@AfterEach
	void cleanUp() {
		riskDecisionRepository.deleteAll();
		transactionRepository.deleteAll();
		customerRepository.deleteAll();
	}

	@Test
	void returnsLatestDecisionForTransactionWithoutSensitivePayloadFields() throws Exception {
		saveCustomer("cus-query-detail");
		saveTransaction("tx-query-detail", "cus-query-detail", BASE_TIME);
		saveDecision("tx-query-detail", RiskDecision.CHALLENGE, 30, BASE_TIME.plusSeconds(1), RiskReasonCode.HIGH_AMOUNT);
		saveDecision("tx-query-detail", RiskDecision.REVIEW, 75, BASE_TIME.plusSeconds(30), RiskReasonCode.NEW_BENEFICIARY);

		mockMvc.perform(get("/transactions/tx-query-detail/decision")
						.header("X-Correlation-Id", "corr-query-detail"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-Id", "corr-query-detail"))
				.andExpect(jsonPath("$.transactionId").value("tx-query-detail"))
				.andExpect(jsonPath("$.decision").value("REVIEW"))
				.andExpect(jsonPath("$.score").value(75))
				.andExpect(jsonPath("$.rulesVersion").value("v1"))
				.andExpect(jsonPath("$.evaluationType").value("ORIGINAL"))
				.andExpect(jsonPath("$.evaluatedAt").value("2026-09-12T14:30:30Z"))
				.andExpect(jsonPath("$.reasons", hasSize(1)))
				.andExpect(jsonPath("$.reasons[*].code", contains("NEW_BENEFICIARY")))
				.andExpect(jsonPath("$.ipAddress").doesNotExist())
				.andExpect(jsonPath("$.customerId").doesNotExist())
				.andExpect(jsonPath("$.deviceId").doesNotExist())
				.andExpect(jsonPath("$.beneficiaryId").doesNotExist())
				.andExpect(jsonPath("$.id").doesNotExist());
	}

	@Test
	void returnsNotFoundWhenTransactionDecisionDoesNotExist() throws Exception {
		mockMvc.perform(get("/transactions/tx-query-missing/decision"))
				.andExpect(status().isNotFound())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(jsonPath("$.code").value("TRANSACTION_DECISION_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Transaction decision was not found."))
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void returnsCustomerRiskDecisionHistoryNewestFirstWithLimit() throws Exception {
		saveCustomer("cus-query-history");
		saveCustomer("cus-query-other");
		saveTransaction("tx-query-old", "cus-query-history", BASE_TIME);
		saveTransaction("tx-query-new", "cus-query-history", BASE_TIME.plusSeconds(10));
		saveTransaction("tx-query-other", "cus-query-other", BASE_TIME.plusSeconds(20));
		saveDecision("tx-query-old", RiskDecision.APPROVE, 0, BASE_TIME.plusSeconds(1));
		saveDecision("tx-query-new", RiskDecision.DENY, 95, BASE_TIME.plusSeconds(20), RiskReasonCode.VERY_HIGH_AMOUNT);
		saveDecision("tx-query-other", RiskDecision.REVIEW, 75, BASE_TIME.plusSeconds(30), RiskReasonCode.NEW_DEVICE);

		mockMvc.perform(get("/customers/cus-query-history/risk-decisions")
						.param("limit", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].transactionId", contains("tx-query-new", "tx-query-old")))
				.andExpect(jsonPath("$[0].decision").value("DENY"))
				.andExpect(jsonPath("$[0].score").value(95))
				.andExpect(jsonPath("$[0].evaluationType").value("ORIGINAL"))
				.andExpect(jsonPath("$[0].reasons[*].code", contains("VERY_HIGH_AMOUNT")))
				.andExpect(jsonPath("$[0].ipAddress").doesNotExist())
				.andExpect(jsonPath("$[0].customerId").doesNotExist())
				.andExpect(jsonPath("$[0].id").doesNotExist());
	}

	@Test
	void defaultHistoryLimitReturnsAtMostTwentyDecisions() throws Exception {
		saveCustomer("cus-query-default-limit");
		for (int index = 1; index <= 21; index++) {
			var transactionId = "tx-query-default-" + index;
			saveTransaction(transactionId, "cus-query-default-limit", BASE_TIME.plusSeconds(index));
			saveDecision(transactionId, RiskDecision.APPROVE, 0, BASE_TIME.plusSeconds(index));
		}

		mockMvc.perform(get("/customers/cus-query-default-limit/risk-decisions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(20)))
				.andExpect(jsonPath("$[0].transactionId").value("tx-query-default-21"))
				.andExpect(jsonPath("$[19].transactionId").value("tx-query-default-2"));
	}

	@Test
	void rejectsHistoryLimitAboveMaximum() throws Exception {
		mockMvc.perform(get("/customers/cus-query-history/risk-decisions")
						.param("limit", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void returnsNotFoundForUnknownCustomerHistory() throws Exception {
		mockMvc.perform(get("/customers/cus-query-missing/risk-decisions"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Customer was not found."))
				.andExpect(jsonPath("$.status").value(404));
	}

	private void saveCustomer(String customerId) {
		customerRepository.save(new CustomerEntity(customerId, BASE_TIME.minusSeconds(30 * 24 * 60 * 60), null, "ACTIVE"));
	}

	private void saveTransaction(String transactionId, String customerId, Instant occurredAt) {
		transactionRepository.save(new TransactionEntity(
				transactionId,
				customerId,
				new BigDecimal("100.00"),
				"BRL",
				PaymentMethod.PIX,
				"ben-" + transactionId,
				"dev-" + transactionId,
				"177.10.20.30",
				occurredAt,
				occurredAt.plusSeconds(1),
				"2d711642b726b04401627ca9fbac32f5c8530fb1903cc4db02258717921a4881",
				occurredAt.plusSeconds(1)
		));
	}

	private void saveDecision(String transactionId, RiskDecision decision, int score, Instant evaluatedAt, RiskReasonCode... reasons) {
		var riskDecision = new RiskDecisionEntity(transactionId, decision, score, "v1", evaluatedAt);
		for (RiskReasonCode reason : reasons) {
			riskDecision.addReason(new RiskReasonEntity(reason, descriptionFor(reason), scoreImpactFor(reason)));
		}
		riskDecisionRepository.save(riskDecision);
	}

	private static String descriptionFor(RiskReasonCode reason) {
		return switch (reason) {
			case HIGH_AMOUNT -> "Transaction amount is above the configured threshold.";
			case VERY_HIGH_AMOUNT -> "Transaction amount is far above the configured threshold.";
			case NEW_DEVICE -> "Transaction originated from a device not seen before for this customer.";
			case UNTRUSTED_DEVICE -> "Transaction originated from a known but untrusted device.";
			case NEW_BENEFICIARY -> "Customer has no previous relationship with this beneficiary.";
			case RECENT_PASSWORD_CHANGE -> "Customer password changed recently.";
			case NEW_ACCOUNT -> "Customer account is new.";
		};
	}

	private static int scoreImpactFor(RiskReasonCode reason) {
		return switch (reason) {
			case HIGH_AMOUNT -> 30;
			case VERY_HIGH_AMOUNT -> 50;
			case NEW_DEVICE -> 20;
			case UNTRUSTED_DEVICE -> 15;
			case NEW_BENEFICIARY -> 25;
			case RECENT_PASSWORD_CHANGE, NEW_ACCOUNT -> 20;
		};
	}
}
