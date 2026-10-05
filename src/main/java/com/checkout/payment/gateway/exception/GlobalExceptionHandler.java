package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.dto.ErrorResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(PaymentEventNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleException(PaymentEventNotFoundException ex) {
    LOG.warn("Payment not found", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = String.format("Invalid UUID format '%s' for parameter '%s'",
        ex.getValue(), ex.getName());

    LOG.warn(message);

    return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankValidationException.class)
  public ResponseEntity<ErrorResponse> handleBankValidationException(BankValidationException ex) {
    LOG.error("Invalid request for bank payment processing", ex);

    return new ResponseEntity<>(new ErrorResponse(PaymentStatus.REJECTED.getName(), "Rejected"), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailableException(BankUnavailableException ex) {
    LOG.error("Acquiring bank is unavailable", ex);

    return new ResponseEntity<>(new ErrorResponse(PaymentStatus.REJECTED.getName(), "Rejected"), HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    LOG.warn("Request payload validation failed: {}", errorMessage);

    return new ResponseEntity<>(new ErrorResponse(errorMessage, "Rejected"), HttpStatus.BAD_REQUEST);
  }
}
