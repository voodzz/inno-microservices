package com.innowise.userservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiErrorHandlerTests {

  @Test
  @DisplayName("handleGeneralException returns 500 with message")
  void handleGeneralException_Returns500() {
    ApiErrorHandler handler = new ApiErrorHandler();
    ResponseEntity<String> resp = handler.handleGeneralException(new RuntimeException("boom"));
    assertEquals(500, resp.getStatusCode().value());
    assertEquals("boom", resp.getBody());
  }

  @Test
  @DisplayName("handleNotValidException returns 400 with field errors map")
  void handleNotValid_Returns400() {
    ApiErrorHandler handler = new ApiErrorHandler();
    BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "obj");
    result.addError(new FieldError("obj", "name", "must not be blank"));
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, result);

    ResponseEntity<Map<String, String>> resp = handler.handleNotValidException(ex);
    assertEquals(400, resp.getStatusCode().value());
    assertEquals("must not be blank", resp.getBody().get("name"));
  }

  @Test
  @DisplayName("handleUpdateException returns 400 with error and timestamp")
  void handleUpdateException_Returns400() {
    ApiErrorHandler handler = new ApiErrorHandler();
    ResponseEntity<Map<String, String>> resp =
        handler.handleUpdateException(new UpdateException("update failed"));
    assertEquals(400, resp.getStatusCode().value());
    assertEquals("update failed", resp.getBody().get("Error"));
    assertFalse(resp.getBody().get("Timestamp").isEmpty());
  }
}
