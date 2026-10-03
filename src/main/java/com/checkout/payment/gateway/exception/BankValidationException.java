package com.checkout.payment.gateway.exception;

public class BankValidationException extends RuntimeException {
  public BankValidationException(String message) {
    super(message);
  }
}
