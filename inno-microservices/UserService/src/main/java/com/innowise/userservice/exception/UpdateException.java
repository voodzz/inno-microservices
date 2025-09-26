package com.innowise.userservice.exception;

import java.io.Serial;

public class UpdateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1193729871561656374L;
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
