package com.fraudshield.transactional.risk.application;

import com.fraudshield.transactional.risk.domain.RiskAssessment;
import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;
import com.fraudshield.transactional.risk.rules.HighAmountRule;
import com.fraudshield.transactional.risk.rules.NewAccountRule;
import com.fraudshield.transactional.risk.rules.NewBeneficiaryRule;
import com.fraudshield.transactional.risk.rules.NewDeviceRule;
import com.fraudshield.transactional.risk.rules.RecentPasswordChangeRule;
import com.fraudshield.transactional.risk.rules.RiskRule;
import com.fraudshield.transactional.risk.rules.UntrustedDeviceRule;
import com.fraudshield.transactional.risk.rules.VeryHighAmountRule;

import java.util.List;
import java.util.Objects;

public final class RiskEngine {
	public static final String RULES_VERSION = "v1";

	private final List<RiskRule> rules;
	private final DecisionPolicy decisionPolicy;

	public RiskEngine() {
		this(defaultRules(), new DecisionPolicy());
	}

	public RiskEngine(List<RiskRule> rules, DecisionPolicy decisionPolicy) {
		Objects.requireNonNull(rules, "rules must not be null");
		this.decisionPolicy = Objects.requireNonNull(decisionPolicy, "decisionPolicy must not be null");
		this.rules = List.copyOf(rules);
	}

	public RiskAssessment evaluate(RiskEvaluationContext context) {
		Objects.requireNonNull(context, "context must not be null");

		var reasons = rules.stream()
				.map(rule -> rule.evaluate(context))
				.flatMap(java.util.Optional::stream)
				.toList();
		var score = reasons.stream()
				.mapToInt(RiskReason::scoreImpact)
				.sum();

		return new RiskAssessment(
				decisionPolicy.decide(score),
				score,
				reasons,
				RULES_VERSION,
				context.evaluatedAt()
		);
	}

	public static List<RiskRule> defaultRules() {
		return List.of(
				new VeryHighAmountRule(),
				new HighAmountRule(),
				new NewDeviceRule(),
				new UntrustedDeviceRule(),
				new NewBeneficiaryRule(),
				new RecentPasswordChangeRule(),
				new NewAccountRule()
		);
	}
}
