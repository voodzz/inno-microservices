package com.innowise.paymentservice.exception;

import java.io.Serial;

public class ExternalApiException extends RuntimeException {
  @Serial private static final long serialVersionUID = -8776358860545006730L;

  public ExternalApiException() {}

  public ExternalApiException(String message) {
    super(message);
  }

  public ExternalApiException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExternalApiException(Throwable cause) {
    super(cause);
  }
}
