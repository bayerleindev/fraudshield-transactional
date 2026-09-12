package com.fraudshield.transactional.shared.config;

import com.fraudshield.transactional.risk.application.RiskEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class ApplicationConfig {
	@Bean
	RiskEngine riskEngine() {
		return new RiskEngine();
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
