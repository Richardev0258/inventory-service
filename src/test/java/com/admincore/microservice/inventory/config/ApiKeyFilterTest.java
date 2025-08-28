package com.admincore.microservice.inventory.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    @InjectMocks
    private ApiKeyFilter apiKeyFilter;

    @Mock
    private FilterChain filterChain;

    private static final String VALID_API_KEY = "INVENTORY_SERVICE_KEY";
    private static final String INVALID_API_KEY = "wrong-api-key";
    private static final String HEADER_NAME = "x-api-key";

    @BeforeEach
    void setUp() throws Exception {
        Field apiKeyField = ApiKeyFilter.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(apiKeyFilter, VALID_API_KEY);
    }

    @Test
    void doFilter_shouldAllowRequestWithValidApiKey() throws ServletException, java.io.IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, VALID_API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilter_shouldRejectRequestWithInvalidApiKey() throws ServletException, java.io.IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, INVALID_API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    void doFilter_shouldRejectRequestWithMissingApiKeyHeader() throws ServletException, java.io.IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    void doFilter_shouldRejectRequestWithEmptyApiKeyHeader() throws ServletException, java.io.IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
    @Test
    void destroy_shouldCallSuperDestroy() {
        assertDoesNotThrow(() -> apiKeyFilter.destroy());
    }
}