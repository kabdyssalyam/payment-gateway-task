package com.checkout.payment.gateway.exception;

public class PaymentEventNotFoundException extends RuntimeException {
  public PaymentEventNotFoundException(String message) {
    super(message);
  }
}
