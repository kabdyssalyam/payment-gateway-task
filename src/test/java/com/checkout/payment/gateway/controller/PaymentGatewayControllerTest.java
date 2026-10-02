package com.checkout.payment.gateway.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.entity.PaymentEvent;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PaymentEvent payment = new PaymentEvent();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour(4321);

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID paymentEventUUID = UUID.randomUUID();
    String expectedMessage = String.format("Payment with id %s not found", paymentEventUUID);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentEventUUID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(expectedMessage));
  }

  @Test
  void whenPaymentIdIsNotValidUuidThenBadRequestReturned() throws Exception {
    String expectedMessage = "Invalid UUID format '1' for parameter 'id'";

    mvc.perform(MockMvcRequestBuilders.get("/payment/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(expectedMessage));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"111", "1234567891012131415161718", "letters"})
  void whenCardNumberIsNotValidThenBadRequestReturned(String invalidCardNumber) throws Exception {
    String request = """
            {
                "card_number": "%s",
                "expiry_month": 12,
                "expiry_year": 2028,
                "currency": "EUR",
                "amount": 100,
                "cvv": "123"
            }
            """.formatted(invalidCardNumber);

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)
    ).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(ints = {0, 13})
  void whenExpiryMonthIsNotValidThenBadRequestReturned(Integer invalidMonth) throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": %s,
                "expiry_year": 2028,
                "currency": "EUR",
                "amount": 100,
                "cvv": "123"
            }
            """.formatted(invalidMonth);

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)

    ).andExpect(status().isBadRequest());
  }

  @Test
  void whenExpiryYearIsNotValidThenBadRequestReturned() throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": 10,
                "currency": "EUR",
                "amount": 100,
                "cvv": "123"
            }
            """;

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)

    ).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"AAA", "A", "AAAA"})
  void whenCurrencyIsNotValidThenBadRequestReturned(String invalidCurrency) throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": 12,
                "expiry_year": 2028,
                "currency": "%s",
                "amount": 100,
                "cvv": "123"
            }
            """.formatted(invalidCurrency);

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)
    ).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(ints = {-1, 0})
  void whenCurrencyIsNotValidThenBadRequestReturned(Integer invalidAmount) throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": 12,
                "expiry_year": 2028,
                "currency": "EUR",
                "amount": %s,
                "cvv": "123"
            }
            """.formatted(invalidAmount);

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)
    ).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"11", "11111", "1a1"})
  void whenCvvIsNotValidThenBadRequestReturned(String cvv) throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": 12,
                "expiry_year": 2028,
                "currency": "EUR",
                "amount": 1000,
                "cvv": "%s"
            }
            """.formatted(cvv);

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)
    ).andExpect(status().isBadRequest());
  }
}
