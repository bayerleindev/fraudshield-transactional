package com.fraudshield.transactional.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public class DomainException extends RuntimeException {
	private final String code;
	private final HttpStatus status;

	public DomainException(String code, String message, HttpStatus status) {
		super(message);
		this.code = requireNotBlank(code, "code");
		this.status = Objects.requireNonNull(status, "status must not be null");
	}

	public static DomainException customerNotFound() {
		return new DomainException("CUSTOMER_NOT_FOUND", "Customer was not found.", HttpStatus.BAD_REQUEST);
	}

	public static DomainException duplicateTransaction() {
		return new DomainException("DUPLICATE_TRANSACTION", "Transaction has already been evaluated.", HttpStatus.CONFLICT);
	}

	public static DomainException transactionConflict() {
		return new DomainException(
				"TRANSACTION_CONFLICT",
				"Transaction has already been evaluated with different data.",
				HttpStatus.CONFLICT
		);
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	private static String requireNotBlank(String value, String fieldName) {
		Objects.requireNonNull(value, fieldName + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
