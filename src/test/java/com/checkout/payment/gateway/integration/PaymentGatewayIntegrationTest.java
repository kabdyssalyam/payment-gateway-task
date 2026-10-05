package com.checkout.payment.gateway.integration;

import com.checkout.payment.gateway.dto.ErrorResponse;
import com.checkout.payment.gateway.dto.PostPaymentRequest;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentGatewayIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void whenPaymentIsValidAndAuthorizedThenReturnOk() {
    int amount = 1000;
    int expiryMonth = 9;
    int expiryYear = 2028;
    String currency = "EUR";

    PostPaymentRequest request = new PostPaymentRequest(
        "4242424242424243", expiryMonth, expiryYear, currency, amount, "123"
    );

    ResponseEntity<PostPaymentResponse> response = restTemplate.postForEntity(
        "/payment", request, PostPaymentResponse.class
    );

    PostPaymentResponse responseBody = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(responseBody.amount()).isEqualTo(amount);
    assertThat(responseBody.expiryMonth()).isEqualTo(expiryMonth);
    assertThat(responseBody.expiryYear()).isEqualTo(expiryYear);
    assertThat(responseBody.currency()).isEqualTo(currency);
  }

  @Test
  void whenPaymentIsValidAndDeclinedThenReturnOk() {
    int amount = 1000;
    int expiryMonth = 9;
    int expiryYear = 2028;
    String currency = "EUR";

    PostPaymentRequest request = new PostPaymentRequest(
        "4242424242424242", expiryMonth, expiryYear, currency, amount, "123"
    );

    ResponseEntity<PostPaymentResponse> response = restTemplate.postForEntity(
        "/payment", request, PostPaymentResponse.class
    );

    PostPaymentResponse responseBody = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.status()).isEqualTo(PaymentStatus.DECLINED);
    assertThat(responseBody.amount()).isEqualTo(amount);
    assertThat(responseBody.expiryMonth()).isEqualTo(expiryMonth);
    assertThat(responseBody.expiryYear()).isEqualTo(expiryYear);
    assertThat(responseBody.currency()).isEqualTo(currency);
  }

  @Test
  void whenPaymentIsInvalidThenReturnBadRequest() {
    int amount = 1000;
    int expiryMonth = 9;
    int expiryYear = 2028;
    String currency = "EUR";

    PostPaymentRequest request = new PostPaymentRequest(
        "1", expiryMonth, expiryYear, currency, amount, "1"
    );

    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/payment", request, ErrorResponse.class
    );

    ErrorResponse responseBody = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.getStatus()).isEqualTo(PaymentStatus.REJECTED.getName());
  }

  @Test
  void whenBankIsUnavailableThenReturnServiceUnavailable() {
    int amount = 1000;
    int expiryMonth = 9;
    int expiryYear = 2028;
    String currency = "EUR";

    PostPaymentRequest request = new PostPaymentRequest(
        "4242424242424240", expiryMonth, expiryYear, currency, amount, "177"
    );

    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/payment", request, ErrorResponse.class
    );

    ErrorResponse responseBody = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.getStatus()).isEqualTo(PaymentStatus.REJECTED.getName());
  }
}