package com.checkout.payment.gateway.service.integration;

import com.checkout.payment.gateway.dto.BankRequest;
import com.checkout.payment.gateway.dto.BankResponse;
import org.springframework.web.client.RestTemplate;

public class MountebankClient implements BankClient {

  private final RestTemplate restTemplate;

  public MountebankClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public BankResponse processPayment(BankRequest request) {
    return restTemplate.postForObject("http://localhost:8080/bank/payments", request, BankResponse.class);
  }
}
