package com.innowise.paymentservice.exception;

import java.io.Serial;

public class PaymentCreationException extends RuntimeException {
  @Serial private static final long serialVersionUID = 4559460124811604383L;

  public PaymentCreationException() {}

  public PaymentCreationException(String message) {
    super(message);
  }

  public PaymentCreationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PaymentCreationException(Throwable cause) {
    super(cause);
  }
}
