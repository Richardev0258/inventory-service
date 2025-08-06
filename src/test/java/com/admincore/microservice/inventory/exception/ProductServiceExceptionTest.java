package com.admincore.microservice.inventory.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceExceptionTest {

    @Test
    void testConstructorAndGetters() {
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ProductServiceException exception = new ProductServiceException(message, status);

        assertEquals(message, exception.getMessage());
        assertEquals(status, exception.getStatus());
        assertInstanceOf(RuntimeException.class, exception);
    }
}