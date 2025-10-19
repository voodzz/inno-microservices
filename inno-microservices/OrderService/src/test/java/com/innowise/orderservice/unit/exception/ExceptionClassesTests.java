package com.innowise.orderservice.unit.exception;

import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.RetrieveUserException;
import com.innowise.orderservice.exception.UpdateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTests {

  @Test
  @DisplayName("NotFoundException should create with default constructor")
  void notFoundException_ShouldCreateWithDefaultConstructor() {
    NotFoundException exception = new NotFoundException();

    assertThat(exception).isInstanceOf(NotFoundException.class);
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("NotFoundException should create with message")
  void notFoundException_ShouldCreateWithMessage() {
    String message = "Custom not found message";
    NotFoundException exception = new NotFoundException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("NotFoundException should create with id")
  void notFoundException_ShouldCreateWithId() {
    Long id = 123L;
    NotFoundException exception = new NotFoundException(id);

    assertThat(exception.getMessage()).isEqualTo("Entity with id '123' not found");
  }

  @Test
  @DisplayName("NotFoundException should create with message and cause")
  void notFoundException_ShouldCreateWithMessageAndCause() {
    String message = "Not found with cause";
    Throwable cause = new RuntimeException("Root cause");
    NotFoundException exception = new NotFoundException(message, cause);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("NotFoundException should create with cause only")
  void notFoundException_ShouldCreateWithCause() {
    Throwable cause = new RuntimeException("Root cause");
    NotFoundException exception = new NotFoundException(cause);

    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("RetrieveUserException should create with default constructor")
  void retrieveUserException_ShouldCreateWithDefaultConstructor() {
    RetrieveUserException exception = new RetrieveUserException();

    assertThat(exception).isInstanceOf(RetrieveUserException.class);
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("RetrieveUserException should create with message")
  void retrieveUserException_ShouldCreateWithMessage() {
    String message = "User retrieval failed";
    RetrieveUserException exception = new RetrieveUserException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("RetrieveUserException should create with message and cause")
  void retrieveUserException_ShouldCreateWithMessageAndCause() {
    String message = "User retrieval failed";
    Throwable cause = new RuntimeException("Network error");
    RetrieveUserException exception = new RetrieveUserException(message, cause);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("RetrieveUserException should create with cause only")
  void retrieveUserException_ShouldCreateWithCause() {
    Throwable cause = new RuntimeException("Database error");
    RetrieveUserException exception = new RetrieveUserException(cause);

    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("UpdateException should create with default constructor")
  void updateException_ShouldCreateWithDefaultConstructor() {
    UpdateException exception = new UpdateException();

    assertThat(exception).isInstanceOf(UpdateException.class);
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  @DisplayName("UpdateException should create with message")
  void updateException_ShouldCreateWithMessage() {
    String message = "Custom update message";
    UpdateException exception = new UpdateException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  @DisplayName("UpdateException should create with id")
  void updateException_ShouldCreateWithId() {
    Long id = 789L;
    UpdateException exception = new UpdateException(id);

    assertThat(exception.getMessage()).isEqualTo("Update failed for entity with id '789'");
  }

  @Test
  @DisplayName("UpdateException should create with message and cause")
  void updateException_ShouldCreateWithMessageAndCause() {
    String message = "Update failed with cause";
    Throwable cause = new RuntimeException("Constraint violation");
    UpdateException exception = new UpdateException(message, cause);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("UpdateException should create with cause only")
  void updateException_ShouldCreateWithCause() {
    Throwable cause = new RuntimeException("Database timeout");
    UpdateException exception = new UpdateException(cause);

    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  @DisplayName("NotFoundException should have correct serialVersionUID")
  void notFoundException_ShouldHaveCorrectSerialVersionUID()
      throws NoSuchFieldException, IllegalAccessException {
    var field = NotFoundException.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    long serialVersionUID = field.getLong(null);

    assertThat(serialVersionUID).isEqualTo(6141188726446312623L);
  }

  @Test
  @DisplayName("RetrieveUserException should have correct serialVersionUID")
  void retrieveUserException_ShouldHaveCorrectSerialVersionUID()
      throws NoSuchFieldException, IllegalAccessException {
    var field = RetrieveUserException.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    long serialVersionUID = field.getLong(null);

    assertThat(serialVersionUID).isEqualTo(-3782361036625980740L);
  }

  @Test
  @DisplayName("UpdateException should have correct serialVersionUID")
  void updateException_ShouldHaveCorrectSerialVersionUID()
      throws NoSuchFieldException, IllegalAccessException {
    var field = UpdateException.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    long serialVersionUID = field.getLong(null);

    assertThat(serialVersionUID).isEqualTo(-2013232181406717713L);
  }
}
