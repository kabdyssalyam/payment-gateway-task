package com.checkout.payment.gateway.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.checkout.payment.gateway.entity.PaymentEvent;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository {

  private final Map<UUID, PaymentEvent> payments = new ConcurrentHashMap<>();

  public void add(PaymentEvent paymentEvent) {
    payments.put(paymentEvent.getId(), paymentEvent);
  }

  public Optional<PaymentEvent> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
