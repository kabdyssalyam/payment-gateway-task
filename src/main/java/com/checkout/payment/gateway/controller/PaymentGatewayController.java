package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.dto.ErrorResponse;
import com.checkout.payment.gateway.dto.PostPaymentRequest;
import com.checkout.payment.gateway.dto.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
@Tag(name = "Payment Gateway", description = "Handles payment event related operations")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @GetMapping("/payment/{id}")
  @Operation(
      summary = "Retrieves payment event data"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Returns payment event data"),
      @ApiResponse(responseCode = "404", description = "Payment event not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  }
  )
  public ResponseEntity<PostPaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  @Operation(
      summary = "Process payment",
      description = "Process payment data and authorise payment"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment processed successfully. Returns Authorized or Declined(valid business response)"),
      @ApiResponse(responseCode = "400", description = "Invalid card details", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "503", description = "Bank is temporarily unavailable (includes connection timeout)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<PostPaymentResponse> processPayment(
      @RequestBody @Valid PostPaymentRequest request) {
    return new ResponseEntity<>(paymentGatewayService.processPayment(request), HttpStatus.OK);
  }
}
