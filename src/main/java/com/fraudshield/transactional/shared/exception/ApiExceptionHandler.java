package com.fraudshield.transactional.shared.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(DomainException.class)
	ResponseEntity<ApiErrorResponse> handleDomainException(DomainException exception) {
		return ResponseEntity
				.status(exception.getStatus())
				.body(ApiErrorResponse.from(exception.getCode(), exception.getMessage(), exception.getStatus(), List.of()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation() {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.from(
						"DUPLICATE_OR_INVALID_REFERENCE",
						"Transaction could not be persisted because it conflicts with existing data.",
						HttpStatus.CONFLICT,
						List.of()
				));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		var fieldErrors = exception.getConstraintViolations().stream()
				.map(violation -> new ApiFieldError(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.sorted(Comparator.comparing(ApiFieldError::field))
				.toList();

		return ResponseEntity
				.badRequest()
				.body(ApiErrorResponse.from(
						"VALIDATION_ERROR",
						"Request validation failed.",
						HttpStatus.BAD_REQUEST,
						fieldErrors
				));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		var fieldName = exception.getName() == null ? "request" : exception.getName();
		return ResponseEntity
				.badRequest()
				.body(ApiErrorResponse.from(
						"VALIDATION_ERROR",
						"Request validation failed.",
						HttpStatus.BAD_REQUEST,
						List.of(new ApiFieldError(fieldName, "must be a valid value"))
				));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		var fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
				.sorted(Comparator.comparing(ApiFieldError::field))
				.toList();

		return ResponseEntity
				.badRequest()
				.body(ApiErrorResponse.from(
						"VALIDATION_ERROR",
						"Request validation failed.",
						HttpStatus.BAD_REQUEST,
						fieldErrors
				));
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		var message = exception.getCause() instanceof InvalidFormatException
				? "Request contains an invalid value."
				: "Request body is malformed.";

		return ResponseEntity
				.badRequest()
				.body(ApiErrorResponse.from("INVALID_REQUEST", message, HttpStatus.BAD_REQUEST, List.of()));
	}

	public record ApiErrorResponse(
			String code,
			String message,
			int status,
			Instant timestamp,
			List<ApiFieldError> errors
	) {
		private static ApiErrorResponse from(String code, String message, HttpStatus status, List<ApiFieldError> errors) {
			return new ApiErrorResponse(code, message, status.value(), Instant.now(), errors);
		}
	}

	public record ApiFieldError(String field, String message) {
	}
}
