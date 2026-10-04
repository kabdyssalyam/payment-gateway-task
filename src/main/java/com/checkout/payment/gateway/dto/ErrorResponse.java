package com.checkout.payment.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponse {
  @Schema(description = "Detailed error message", example = "Bank failed to process payment")
  private final String message;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }
}
