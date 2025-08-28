package com.admincore.microservice.inventory.exception;

import org.springframework.http.HttpStatusCode;

public class ProductServiceException extends RuntimeException {
    private final HttpStatusCode status;

    public ProductServiceException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

    public ProductServiceException(String message, HttpStatusCode status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}