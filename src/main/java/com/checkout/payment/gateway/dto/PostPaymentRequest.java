package com.checkout.payment.gateway.dto;

import com.checkout.payment.gateway.validation.IsCardExpired;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("card_number")
  String cardNumber, //int ?

  @NotNull
  @JsonProperty("expiry_month")
  @Min(value = 1)
  @Max(value = 12)
  Integer expiryMonth,

  @NotNull
  @JsonProperty("expiry_year")
  Integer expiryYear,

  @Pattern(regexp = "^(USD|EUR|GBP)$")
  @NotNull
  String currency,

  @NotNull
  @Min(value = 1)
  Integer amount,

  @NotNull
  @Size(min = 3, max = 4)
  @Pattern(regexp = "^[0-9]+$")
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
