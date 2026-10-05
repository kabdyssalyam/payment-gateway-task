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
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.PaymentEventNotFoundException;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.checkout.payment.gateway.service.integration.BankClient;
import com.checkout.payment.gateway.service.integration.BankClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.GatewayTimeout;
import org.springframework.web.client.ResourceAccessException;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;

  private final BankClientFactory clientFactory;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      BankClientFactory clientFactory) {
    this.paymentsRepository = paymentsRepository;
    this.clientFactory = clientFactory;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);

    return paymentsRepository.get(id)
        .map(paymentEvent -> new PostPaymentResponse(
            paymentEvent.getId(),
            paymentEvent.getStatus(),
            paymentEvent.getCardNumberLastFour(),
            paymentEvent.getExpiryMonth(),
            paymentEvent.getExpiryYear(),
            paymentEvent.getCurrency(),
            paymentEvent.getAmount()
        ))
        .orElseThrow(() -> new PaymentEventNotFoundException(
            String.format("Payment with id %s not found", id))
        );
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    String cardNumber = paymentRequest.cardNumber();
    String lastFour = cardNumber.substring(cardNumber.length() - 4);

    PaymentEvent paymentEvent = new PaymentEvent();
    paymentEvent.setId(UUID.randomUUID());
    paymentEvent.setStatus(PaymentStatus.AUTHORIZED);
    paymentEvent.setAmount(paymentRequest.amount());
    paymentEvent.setCurrency(paymentRequest.currency());
    paymentEvent.setExpiryMonth(paymentRequest.expiryMonth());
    paymentEvent.setExpiryYear(paymentRequest.expiryYear());
    paymentEvent.setCardNumberLastFour(lastFour);

    try {
      String formattedExpiry = String.format("%02d/%d",
          paymentRequest.expiryMonth(),
          paymentRequest.expiryYear()
      );

      BankRequest bankRequest = new BankRequest(
          paymentRequest.cardNumber(),
          formattedExpiry,
          paymentRequest.currency(),
          paymentRequest.amount(),
          paymentRequest.cvv()
      );

      BankClient bankClient = clientFactory.getClient(BankClientType.MOUNTEBANK);

      BankResponse response = bankClient.processPayment(bankRequest);

      paymentEvent.setStatus(response.authorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);
      paymentEvent.setAuthorizationCode(response.authorization_code());
      paymentsRepository.add(paymentEvent);
    } catch (HttpClientErrorException.BadRequest e) {
      throw new BankValidationException(e.getMessage());
    } catch (HttpServerErrorException.ServiceUnavailable | GatewayTimeout |
             ResourceAccessException e) {
      throw new BankUnavailableException(e.getMessage());
    } catch (Exception e) {
      throw new EventProcessingException(e.getMessage());
    }

    return new PostPaymentResponse(
        paymentEvent.getId(),
        paymentEvent.getStatus(),
        paymentEvent.getCardNumberLastFour(),
        paymentEvent.getExpiryMonth(),
        paymentEvent.getExpiryYear(),
        paymentEvent.getCurrency(),
        paymentEvent.getAmount()
    );
  }
}
