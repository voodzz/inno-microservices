package com.innowise.userservice.exception;

public class UpdateException extends RuntimeException {
    private static final String MESSAGE_TEMP = "Update failed for entity with id '%d'";

    public UpdateException() {
    }

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(Long id) {
        super(MESSAGE_TEMP.formatted(id));
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }
}
