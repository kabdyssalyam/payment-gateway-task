package com.checkout.payment.gateway.dto;

import com.checkout.payment.gateway.validation.IsCardExpired;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@IsCardExpired
public record PostPaymentRequest(

  @NotNull
  @Size(min = 14, max = 19)
  @Pattern(regexp = "^[0-9]+$")
  @Schema(description = "14-19 digit card number", example = "4242424242424243")
  @JsonProperty("card_number")
  String cardNumber,

  @NotNull
  @Min(value = 1)
  @Max(value = 12)
  @Schema(description = "Expiry month. 1-12", example = "10")
  @JsonProperty("expiry_month")
  Integer expiryMonth,

  @NotNull
  @Schema(description = "Expiry year", example = "2026")
  @JsonProperty("expiry_year")
  Integer expiryYear,

  @Pattern(regexp = "^(USD|EUR|GBP)$")
  @NotNull
  @Schema(description = "Currency. 3 letter ISO-4217 currency code", example = "EUR")
  String currency,

  @NotNull
  @Min(value = 1)
  @Schema(description = "Amount in minor units. $0.01 would be supplied as 1", example = "1000")
  Integer amount,

  @NotNull
  @Size(min = 3, max = 4)
  @Pattern(regexp = "^[0-9]+$")
  @Schema(description = "CVV", example = "333")
  String cvv
) {
  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
