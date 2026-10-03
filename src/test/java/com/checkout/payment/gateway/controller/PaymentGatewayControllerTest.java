package com.checkout.payment.gateway.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.dto.PostPaymentRequest;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.BankValidationException;
import com.checkout.payment.gateway.exception.PaymentEventNotFoundException;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.checkout.payment.gateway.service.integration.BankClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  @MockBean
  private BankClient bankClient;
  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @Test
  void whenPaymentIsAuthorisedThenReturnOk() throws Exception {
    PostPaymentRequest paymentRequest = new PostPaymentRequest(
        "4242424242424242", 9, 2028, "EUR", 1000, "123"
    );
    UUID paymentId = UUID.randomUUID();
    PostPaymentResponse paymentResponse = new PostPaymentResponse(
        paymentId, PaymentStatus.AUTHORIZED, "4242", 9, 2028, "EUR", 1000
    );
    when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(paymentResponse);

    String request = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 9,
          "expiry_year": 2028,
          "currency": "EUR",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders
            .post("/payment")
            .contentType("application/json")
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(paymentId.toString()))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("4242"))
        .andExpect(jsonPath("$.expiryMonth").value(9))
        .andExpect(jsonPath("$.expiryYear").value(2028))
        .andExpect(jsonPath("$.currency").value("EUR"))
        .andExpect(jsonPath("$.amount").value(1000));

    verify(paymentGatewayService).processPayment(paymentRequest);
  }

  @Test
  void whenPaymentIsDeclinedThenReturnOk() throws Exception {
    PostPaymentRequest paymentRequest = new PostPaymentRequest(
        "4242424242424242", 9, 2028, "EUR", 1000, "123"
    );
    UUID paymentId = UUID.randomUUID();
    PostPaymentResponse paymentResponse = new PostPaymentResponse(
        paymentId, PaymentStatus.DECLINED, "4242", 9, 2028, "EUR", 1000
    );
    when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(paymentResponse);

    String request = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 9,
          "expiry_year": 2028,
          "currency": "EUR",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders
            .post("/payment")
            .contentType("application/json")
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(paymentId.toString()))
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("4242"))
        .andExpect(jsonPath("$.expiryMonth").value(9))
        .andExpect(jsonPath("$.expiryYear").value(2028))
        .andExpect(jsonPath("$.currency").value("EUR"))
        .andExpect(jsonPath("$.amount").value(1000));

    verify(paymentGatewayService).processPayment(paymentRequest);
  }

  @Test
  void whenBankValidationFailedThenReturnBadRequest() throws Exception {
    PostPaymentRequest paymentRequest = new PostPaymentRequest(
        "4242424242424242", 9, 2028, "EUR", 1000, "123"
    );

    when(paymentGatewayService.processPayment(paymentRequest)).thenThrow(new BankValidationException("Validation failed"));

    String request = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 9,
          "expiry_year": 2028,
          "currency": "EUR",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders
            .post("/payment")
            .contentType("application/json")
            .content(request))
        .andExpect(status().isBadRequest());

    verify(paymentGatewayService).processPayment(paymentRequest);
  }


  @Test
  void whenBankIsUnavailableThenReturnServiceUnavailable() throws Exception {
    PostPaymentRequest paymentRequest = new PostPaymentRequest(
        "4242424242424242", 9, 2028, "EUR", 1000, "123"
    );
    UUID paymentId = UUID.randomUUID();
    when(paymentGatewayService.processPayment(paymentRequest)).thenThrow(new BankUnavailableException("Timeout"));

    String request = """
        {
          "card_number": "4242424242424242",
          "expiry_month": 9,
          "expiry_year": 2028,
          "currency": "EUR",
          "amount": 1000,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders
            .post("/payment")
            .contentType("application/json")
            .content(request))
        .andExpect(status().isServiceUnavailable());

    verify(paymentGatewayService).processPayment(paymentRequest);
  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse paymentResponse = new PostPaymentResponse(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        "4321",
        12,
        2024,
        "USD",
        10
    );

    when(paymentGatewayService.getPaymentById(paymentResponse.id())).thenReturn(paymentResponse);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentResponse.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(paymentResponse.status().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(paymentResponse.cardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(paymentResponse.expiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(paymentResponse.expiryYear()))
        .andExpect(jsonPath("$.currency").value(paymentResponse.currency()))
        .andExpect(jsonPath("$.amount").value(paymentResponse.amount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID paymentEventUUID = UUID.randomUUID();
    String expectedMessage = String.format("Payment with id %s not found", paymentEventUUID);

    when(paymentGatewayService.getPaymentById(paymentEventUUID))
        .thenThrow(new PaymentEventNotFoundException(
            String.format("Payment with id %s not found", paymentEventUUID))
        );

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

  //Possible flaky test
  @Test
  void whenCardIsExpiredThenReturnBadRequest() throws Exception {
    String request = """
            {
                "card_number": "4242424242424242",
                "expiry_month": 12,
                "expiry_year": 2025,
                "currency": "EUR",
                "amount": 1000,
                "cvv": "444"
            }
            """;

    mvc.perform(MockMvcRequestBuilders
        .post("/payment")
        .contentType("application/json")
        .content(request)
    ).andExpect(status().isBadRequest());
  }
}
