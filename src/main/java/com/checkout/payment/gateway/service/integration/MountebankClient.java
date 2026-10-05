package com.checkout.payment.gateway.service.integration;

import com.checkout.payment.gateway.dto.BankRequest;
import com.checkout.payment.gateway.dto.BankResponse;
import com.checkout.payment.gateway.enums.BankClientType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MountebankClient implements BankClient {

  private final RestTemplate restTemplate;
  private final String clientUrl;

  public MountebankClient(RestTemplate restTemplate, @Value("${acquirer.mountebank.url}") String clientUrl) {
    this.restTemplate = restTemplate;
    this.clientUrl = clientUrl;
  }

  @Override
  public BankResponse processPayment(BankRequest request) {
    String endpoint = this.clientUrl + "/payments";
    return restTemplate.postForObject(endpoint, request, BankResponse.class);
  }

  @Override
  public BankClientType getType() {
    return BankClientType.MOUNTEBANK;
  }
}
