package com.checkout.payment.gateway.dto;

public record BankRequest(
    String card_number,
    String expiry_date,
    String currency,
    Integer amount,
    String cvv
){}
