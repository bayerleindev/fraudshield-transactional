package com.fraudshield.transactional.risk.rules;

import com.fraudshield.transactional.risk.domain.RiskEvaluationContext;
import com.fraudshield.transactional.risk.domain.RiskReason;

import java.util.Optional;

public interface RiskRule {
	Optional<RiskReason> evaluate(RiskEvaluationContext context);
}
