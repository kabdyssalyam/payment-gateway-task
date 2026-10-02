package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.PostPaymentRequest;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.entity.PaymentEvent;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentEventNotFoundException;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;

  public PaymentGatewayService(PaymentsRepository paymentsRepository) {
    this.paymentsRepository = paymentsRepository;
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
    PaymentEvent paymentEvent = new PaymentEvent();
    paymentEvent.setId(UUID.randomUUID());
    paymentEvent.setStatus(PaymentStatus.AUTHORIZED);
    paymentEvent.setAmount(paymentRequest.amount());
    paymentEvent.setCurrency(paymentRequest.currency());
    paymentEvent.setExpiryMonth(paymentRequest.expiryMonth());
    paymentEvent.setExpiryYear(paymentRequest.expiryYear());
    paymentEvent.setCardNumberLastFour(1234);

    paymentsRepository.add(paymentEvent);

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
