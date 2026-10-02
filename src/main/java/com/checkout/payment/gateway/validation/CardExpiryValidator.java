package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

public class CardExpiryValidator implements ConstraintValidator<IsCardExpired, PostPaymentRequest> {

  @Override
  public void initialize(IsCardExpired constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(PostPaymentRequest postPaymentRequest,
      ConstraintValidatorContext constraintValidatorContext) {
    if (postPaymentRequest == null) {
      return true;
    }

    YearMonth currentMonth = YearMonth.now();
    YearMonth cardExpiry = YearMonth.of(postPaymentRequest.getExpiryYear(), postPaymentRequest.getExpiryMonth());

    return !cardExpiry.isBefore(currentMonth);
  }
}
