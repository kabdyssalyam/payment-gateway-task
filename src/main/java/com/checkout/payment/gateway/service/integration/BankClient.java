package com.checkout.payment.gateway.service.integration;

import com.checkout.payment.gateway.dto.BankRequest;
import com.checkout.payment.gateway.dto.BankResponse;

public interface BankClient {
  BankResponse processPayment(BankRequest request);
}
