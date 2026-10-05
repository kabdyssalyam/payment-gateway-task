package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.BankRequest;
import com.checkout.payment.gateway.dto.BankResponse;
import com.checkout.payment.gateway.dto.PostPaymentRequest;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.entity.PaymentEvent;
import com.checkout.payment.gateway.enums.BankClientType;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.BankValidationException;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.service.integration.BankClientFactory;
import com.checkout.payment.gateway.service.integration.MountebankClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentGatewayServiceTest {

  private BankClientFactory factory;
  private MountebankClient bankClient;
  private PaymentsRepository paymentsRepository;
  private PaymentGatewayService paymentService;

  @BeforeEach
  void setUp() {
    bankClient = mock(MountebankClient.class);

    when(bankClient.getType()).thenReturn(BankClientType.MOUNTEBANK);

    factory = new BankClientFactory(List.of(bankClient));

    paymentsRepository = spy(new PaymentsRepository());

    paymentService = new PaymentGatewayService(paymentsRepository, factory);
  }

  @Test
  void whenRequestIsValidThenReturnAuthorised() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");
    BankResponse bankResponse = new BankResponse(true, "0bb07405-6d44-4b50-a14f-7ae0beff13ad");
    when(bankClient.processPayment(any(BankRequest.class))).thenReturn(bankResponse);

    PostPaymentResponse response = paymentService.processPayment(request);

    assertEquals(PaymentStatus.AUTHORIZED, response.status());
    assertEquals("4242", response.cardNumberLastFour());

    PaymentEvent expectedEvent = new PaymentEvent();
    expectedEvent.setStatus(PaymentStatus.AUTHORIZED);
    expectedEvent.setAmount(1000);
    expectedEvent.setCurrency("EUR");
    expectedEvent.setExpiryMonth(9);
    expectedEvent.setExpiryYear(2028);
    expectedEvent.setCardNumberLastFour("4242");
    expectedEvent.setAuthoriationCode("0bb07405-6d44-4b50-a14f-7ae0beff13ad");

    verify(paymentsRepository).add(refEq(expectedEvent, "id"));
  }

  @Test
  void whenRequestIsInvalidThenReturnDeclined() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");
    BankResponse bankResponse = new BankResponse(false, null);
    when(bankClient.processPayment(any(BankRequest.class))).thenReturn(bankResponse);

    PostPaymentResponse response = paymentService.processPayment(request);

    assertEquals(PaymentStatus.DECLINED, response.status());

    PaymentEvent expectedEvent = new PaymentEvent();
    expectedEvent.setStatus(PaymentStatus.DECLINED);
    expectedEvent.setAmount(1000);
    expectedEvent.setCurrency("EUR");
    expectedEvent.setExpiryMonth(9);
    expectedEvent.setExpiryYear(2028);
    expectedEvent.setCardNumberLastFour("4242");

    verify(paymentsRepository).add(refEq(expectedEvent, "id"));
  }

  @Test
  void whenBankUnavalableThenReturnServiceUnavailable() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");

    when(bankClient.processPayment(any(BankRequest.class)))
        .thenThrow(HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            null,
            null,
            null
        ));

    assertThrows(BankUnavailableException.class, () -> paymentService.processPayment(request));

    PaymentEvent expectedEvent = new PaymentEvent();
    expectedEvent.setStatus(PaymentStatus.REJECTED);
    expectedEvent.setAmount(1000);
    expectedEvent.setCurrency("EUR");
    expectedEvent.setExpiryMonth(9);
    expectedEvent.setExpiryYear(2028);
    expectedEvent.setCardNumberLastFour("4242");

    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenRequestTimedOutThenReturnServiceUnavailable() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");

    when(bankClient.processPayment(any(BankRequest.class)))
        .thenThrow(HttpServerErrorException.create(
            HttpStatus.GATEWAY_TIMEOUT,
            "Gateway Timeout",
            null,
            null,
            null
        ));

    assertThrows(BankUnavailableException.class, () -> paymentService.processPayment(request));

    PaymentEvent expectedEvent = new PaymentEvent();
    expectedEvent.setStatus(PaymentStatus.REJECTED);
    expectedEvent.setAmount(1000);
    expectedEvent.setCurrency("EUR");
    expectedEvent.setExpiryMonth(9);
    expectedEvent.setExpiryYear(2028);
    expectedEvent.setCardNumberLastFour("4242");

    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenRequestIsInvalidThenReturnBadRequest() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");

    HttpClientErrorException.BadRequest badRequestException = (HttpClientErrorException.BadRequest) HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST, "Bad Request", null, "Missing CVV".getBytes(StandardCharsets.UTF_8), null
    );
    when(bankClient.processPayment(any(BankRequest.class))).thenThrow(badRequestException);

    assertThrows(BankValidationException.class,
        () -> paymentService.processPayment(request)
    );

    PaymentEvent expectedEvent = new PaymentEvent();
    expectedEvent.setStatus(PaymentStatus.REJECTED);
    expectedEvent.setAmount(1000);
    expectedEvent.setCurrency("EUR");
    expectedEvent.setExpiryMonth(9);
    expectedEvent.setExpiryYear(2028);
    expectedEvent.setCardNumberLastFour("4242");

    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenFailedToSavePaymentThenReturnRuntimeException() {
    PostPaymentRequest request = new PostPaymentRequest("4242424242424242", 9, 2028, "EUR", 1000, "123");
    BankResponse bankResponse = new BankResponse(true, "0bb07405-6d44-4b50-a14f-7ae0beff13ad");
    when(bankClient.processPayment(any(BankRequest.class))).thenReturn(bankResponse);

    doThrow(new RuntimeException("Cannot save data")).when(paymentsRepository).add(any(PaymentEvent.class));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentService.processPayment(request)
    );
    assertEquals("Cannot save data", exception.getMessage());
  }
}
