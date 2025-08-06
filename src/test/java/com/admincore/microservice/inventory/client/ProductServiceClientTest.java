package com.admincore.microservice.inventory.client;

import com.admincore.microservice.inventory.exception.ProductServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceClientTest {

    private MockWebServer mockWebServer;
    private ProductServiceClient client;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString());

        client = new ProductServiceClient(webClientBuilder);

        Field productServiceUrlField = ProductServiceClient.class.getDeclaredField("productServiceUrl");
        productServiceUrlField.setAccessible(true);
        productServiceUrlField.set(client, "/products");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void isProductAvailable_shouldReturnTrue_whenProductExists() throws InterruptedException {
        Long productId = 1L;
        String jsonResponse = "{ \"data\": { \"attributes\": { \"id\": 1, \"name\": \"Test Product\" } } }";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        boolean result = client.isProductAvailable(productId);

        assertTrue(result);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void isProductAvailable_shouldReturnFalse_whenProductNotFound() throws InterruptedException {
        Long productId = 999L;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{}"));

        boolean result = client.isProductAvailable(productId);

        assertFalse(result);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/999", request.getPath());
    }

    @Test
    void isProductAvailable_shouldThrowProductServiceException_withCause_whenClientErrorOccurs() throws InterruptedException {
        Long productId = 1L;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Bad Request\"}"));

        ProductServiceException exception = assertThrows(ProductServiceException.class, () -> {
            client.isProductAvailable(productId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Client error"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof WebClientResponseException);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void isProductAvailable_shouldThrowProductServiceException_withCause_whenServerErrorOccurs() throws InterruptedException {
        Long productId = 1L;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal Server Error\"}"));

        ProductServiceException exception = assertThrows(ProductServiceException.class, () -> {
            client.isProductAvailable(productId);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertTrue(exception.getMessage().contains("Server error"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof WebClientResponseException);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void isProductAvailable_shouldThrowProductServiceException_withCause_whenUnexpectedRuntimeExceptionOccurs() {
        WebClient.Builder failingWebClientBuilder = WebClient.builder()
                .baseUrl("http://198.51.100.1:81/")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024));

        ProductServiceClient failingClient = new ProductServiceClient(failingWebClientBuilder);

        try {
            Field productServiceUrlField = ProductServiceClient.class.getDeclaredField("productServiceUrl");
            productServiceUrlField.setAccessible(true);
            productServiceUrlField.set(failingClient, "/products");
        } catch (Exception e) {
            fail("Failed to set productServiceUrl via reflection: " + e.getMessage());
        }

        ProductServiceException exception = assertThrows(ProductServiceException.class, () -> {
            failingClient.isProductAvailable(1L);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertTrue(exception.getMessage().contains("Unexpected error communicating with product service"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RuntimeException ||
                exception.getCause().getCause() instanceof java.net.ConnectException);
    }

    @Test
    void getProductName_shouldReturnName_whenProductExists() throws InterruptedException {
        Long productId = 1L;
        String expectedName = "Test Product";
        String jsonResponse = "{ \"data\": { \"attributes\": { \"id\": 1, \"name\": \"" + expectedName + "\" } } }";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        String result = client.getProductName(productId);

        assertEquals(expectedName, result);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void getProductName_shouldReturnUnknownProduct_whenResponseIsInvalid() throws InterruptedException {
        Long productId = 1L;
        String jsonResponse = "{ \"data\": { \"attributes\": { \"id\": 1, \"price\": 100.0 } } }";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        String result = client.getProductName(productId);

        assertEquals("Unknown Product", result);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void getProductName_shouldReturnUnknownProduct_whenExceptionOccurs() throws InterruptedException {
        Long productId = 1L;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal Server Error\"}"));

        String result = client.getProductName(productId);

        assertEquals("Unknown Product", result);

        okhttp3.mockwebserver.RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/products/1", request.getPath());
    }

    @Test
    void getProductName_shouldReturnUnknownProduct_whenNetworkExceptionOccurs() {
        WebClient.Builder failingWebClientBuilder = WebClient.builder()
                .baseUrl("http://198.51.100.1:81/")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024));

        ProductServiceClient failingClient = new ProductServiceClient(failingWebClientBuilder);

        try {
            Field productServiceUrlField = ProductServiceClient.class.getDeclaredField("productServiceUrl");
            productServiceUrlField.setAccessible(true);
            productServiceUrlField.set(failingClient, "/products");
        } catch (Exception e) {
            fail("Failed to set productServiceUrl via reflection: " + e.getMessage());
        }

        String result = failingClient.getProductName(1L);

        assertEquals("Unknown Product", result);
    }
}