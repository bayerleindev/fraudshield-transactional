package com.fraudshield.transactional.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
	public static final String HEADER_NAME = "X-Correlation-Id";
	public static final String MDC_KEY = "correlationId";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		var correlationId = resolveCorrelationId(request);
		response.setHeader(HEADER_NAME, correlationId);
		MDC.put(MDC_KEY, correlationId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	private static String resolveCorrelationId(HttpServletRequest request) {
		var incoming = request.getHeader(HEADER_NAME);
		if (incoming == null || incoming.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return incoming.trim();
	}
}
