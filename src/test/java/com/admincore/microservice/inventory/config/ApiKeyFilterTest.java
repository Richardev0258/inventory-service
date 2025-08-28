package com.admincore.microservice.inventory.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

class ApiKeyFilterTest {

    private static final String EXPECTED_KEY = "TEST_KEY";

    private ApiKeyFilter apiKeyFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private PrintWriter mockWriter;

    @BeforeEach
    void setUp() {
        apiKeyFilter = new ApiKeyFilter(EXPECTED_KEY);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        mockWriter = mock(PrintWriter.class);
        try {
            when(response.getWriter()).thenReturn(mockWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldAllowRequestWhenApiKeyIsValid() throws ServletException, IOException {
        when(request.getHeader("X-API-KEY")).thenReturn(EXPECTED_KEY);
        apiKeyFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, never()).getWriter();
    }

    @Test
    void shouldRejectRequestWhenApiKeyIsMissing() throws ServletException, IOException {
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        apiKeyFilter.doFilter(request, response, filterChain);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).getWriter();
        verify(mockWriter, times(1)).write("Invalid or missing API Key");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWhenApiKeyIsInvalid() throws ServletException, IOException {
        when(request.getHeader("X-API-KEY")).thenReturn("WRONG_KEY");
        apiKeyFilter.doFilter(request, response, filterChain);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).getWriter();
        verify(mockWriter, times(1)).write("Invalid or missing API Key");
        verify(filterChain, never()).doFilter(request, response);
    }
}