package com.checkout.payment.gateway.dto;

import com.checkout.payment.gateway.enums.PaymentStatus;

public record BankResponse(
  boolean authorized,
  String authorization_code
){}
