package com.checkout.payment.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponse {
  @Schema(description = "Detailed error message", example = "Bank failed to process payment")
  private final String message;

  @Schema(description = "Status", example = "Rejected")
  private final String status;

  public ErrorResponse(String message) {
    this.message = message;
    this.status = null;
  }

  public ErrorResponse(String message, String status) {
    this.message = message;
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        "status='" + status + '\'' +
        '}';
  }
}
