package com.admincore.microservice.inventory.client;

import com.admincore.microservice.inventory.exception.ProductServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class ProductServiceClientTest {

    private WebClient mockWebClient;
    private WebClient.RequestHeadersUriSpec uriSpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;
    private ProductServiceClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockWebClient = mock(WebClient.class);
        uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
        objectMapper = new ObjectMapper();

        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.build()).thenReturn(mockWebClient);

        client = new ProductServiceClient(builder, "/products", "TEST_KEY");

        when(mockWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.accept(any(MediaType.class))).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void isProductAvailable_returnsTrue_whenDataPresent() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"id\": 1}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertTrue(client.isProductAvailable(1L));
    }

    @Test
    void isProductAvailable_returnsFalse_whenNotFound() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(
                WebClientResponseException.create(404, "Not Found", null, null, null)
        );
        assertFalse(client.isProductAvailable(999L));
    }

    @Test
    void isProductAvailable_clientError_throwsException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(
                WebClientResponseException.create(400, "Bad Request", null, null, null)
        );
        ProductServiceException ex = assertThrows(ProductServiceException.class, () -> client.isProductAvailable(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void isProductAvailable_serverError_throwsException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(
                WebClientResponseException.create(500, "Internal Error", null, null, null)
        );
        ProductServiceException ex = assertThrows(ProductServiceException.class, () -> client.isProductAvailable(1L));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void getProductName_returnsName_whenPresent() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"attributes\": {\"name\": \"Test Product\"}}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Test Product", client.getProductName(1L));
    }

    @Test
    void getProductName_returnsUnknown_whenNameMissing() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"attributes\": {}}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(1L));
    }

    @Test
    void getProductName_returnsUnknown_onException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(new RuntimeException("Error"));
        assertEquals("Unknown Product", client.getProductName(1L));
    }

    @Test
    void isProductAvailable_shouldSendApiKeyHeader() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"id\": 1}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertTrue(client.isProductAvailable(1L));
        verify(headersSpec).header(eq("X-API-KEY"), eq("TEST_KEY"));
    }
}