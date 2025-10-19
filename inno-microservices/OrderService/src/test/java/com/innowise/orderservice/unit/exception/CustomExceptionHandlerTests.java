package com.innowise.orderservice.unit.exception;

import com.innowise.orderservice.exception.CustomExceptionHandler;
import com.innowise.orderservice.exception.RetrieveUserException;
import com.innowise.orderservice.exception.UpdateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTests {

    private final CustomExceptionHandler exceptionHandler = new CustomExceptionHandler();

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("General error message");

        ResponseEntity<String> response = exceptionHandler.handleGeneralException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("General error message");
    }

    @Test
    void handleNotValidException_ShouldReturnBadRequestWithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "field1", "must not be null");
        FieldError fieldError2 = new FieldError("objectName", "field2", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .hasSize(2)
                .containsEntry("field1", "must not be null")
                .containsEntry("field2", "must not be blank");
    }

    @Test
    void handleUpdateException_ShouldReturnBadRequestWithErrorAndTimestamp() {
        UpdateException exception = new UpdateException("Custom update error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUpdateException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .hasSize(2)
                .containsKey("Error")
                .containsKey("Timestamp");
        assertThat(response.getBody().get("Error")).isEqualTo("Custom update error");

        LocalDateTime timestamp = LocalDateTime.parse(response.getBody().get("Timestamp"));
        assertThat(timestamp).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void handleUpdateException_WithIdConstructor_ShouldReturnFormattedMessage() {
        UpdateException exception = new UpdateException(456L);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUpdateException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("Error"))
                .isEqualTo("Update failed for entity with id '456'");
    }

    @Test
    void handleRetrieveUserException_ShouldReturnInternalServerErrorWithDetails() {
        RuntimeException cause = new RuntimeException("Network timeout");
        RetrieveUserException exception = new RetrieveUserException("Failed to retrieve user", cause);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRetrieveUserException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .hasSize(3)
                .containsKey("Timestamp")
                .containsKey("cause")
                .containsKey("Error");

        assertThat(response.getBody().get("Error")).isEqualTo("Failed to retrieve user");
        assertThat(response.getBody().get("cause")).isEqualTo(cause);

        LocalDateTime timestamp = (LocalDateTime) response.getBody().get("Timestamp");
        assertThat(timestamp).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void handleRetrieveUserException_WithoutCause_ShouldReturnInternalServerError() {
        RetrieveUserException exception = new RetrieveUserException("User service unavailable");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRetrieveUserException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("Error")).isEqualTo("User service unavailable");
        assertThat(response.getBody().get("cause")).isNull();
    }

    @Test
    void handleNotValidException_WithEmptyFieldErrors_ShouldReturnEmptyMap() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleGeneralException_WithNullMessage_ShouldReturnNullBody() {
        Exception exception = new RuntimeException();

        ResponseEntity<String> response = exceptionHandler.handleGeneralException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }
}