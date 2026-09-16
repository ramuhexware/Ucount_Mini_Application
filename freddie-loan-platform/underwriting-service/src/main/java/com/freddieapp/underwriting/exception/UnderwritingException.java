package com.freddieapp.underwriting.exception;

import org.springframework.http.HttpStatus;

public class UnderwritingException extends RuntimeException {
    private final HttpStatus status;

    public UnderwritingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
