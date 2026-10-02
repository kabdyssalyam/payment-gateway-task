package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.validation.IsCardExpired;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;


@IsCardExpired
public class PostPaymentRequest implements Serializable {

  @NotNull
  @Size(min = 14, max = 19)
  @Pattern(regexp = "^[0-9]+$")
  @JsonProperty("card_number")
  private int cardNumber;

  @NotNull
  @JsonProperty("expiry_month")
  @Size(min = 1, max = 12)
  private int expiryMonth;

  @NotNull
  @JsonProperty("expiry_year")
  private int expiryYear;

  @NotNull
  private String currency;

  @NotNull
  @Min(value = 1)
  private int amount;

  @NotNull
  @Size(min = 3, max = 4)
  @Pattern(regexp = "^[0-9]+$")
  private String cvv;

  public int getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(int cardNumber) {
    this.cardNumber = cardNumber;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
