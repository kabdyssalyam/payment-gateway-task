package com.checkout.payment.gateway.service.integration;

import com.checkout.payment.gateway.dto.BankRequest;
import com.checkout.payment.gateway.dto.BankResponse;
import com.checkout.payment.gateway.enums.BankClientType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MountebankClient implements BankClient {

  private final RestTemplate restTemplate;

  public MountebankClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public BankResponse processPayment(BankRequest request) {
    return restTemplate.postForObject("http://localhost:8080/payments", request, BankResponse.class);
  }

  @Override
  public BankClientType getType() {
    return BankClientType.MOUNTEBANK;
  }
}
