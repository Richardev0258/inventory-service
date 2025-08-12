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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        client = new ProductServiceClient(builder);
        setProductServiceUrl(client, "/products");

        when(mockWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.accept(any(MediaType.class))).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void setProductServiceUrl(ProductServiceClient client, String value) {
        try {
            var field = ProductServiceClient.class.getDeclaredField("productServiceUrl");
            field.setAccessible(true);
            field.set(client, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void isProductAvailable_unexpectedHttpStatus_throwsException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(
                WebClientResponseException.create(302, "Redirect", null, null, null)
        );
        ProductServiceException ex = assertThrows(ProductServiceException.class, () -> client.isProductAvailable(1L));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void isProductAvailable_unexpectedException_throwsException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(new RuntimeException("Boom"));
        ProductServiceException ex = assertThrows(ProductServiceException.class, () -> client.isProductAvailable(1L));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void isProductAvailable_allBranchesOfAndCondition() throws Exception {
        JsonNode withData = objectMapper.readTree("{\"data\": {\"id\": 1}}");
        JsonNode withoutData = objectMapper.readTree("{\"noData\": true}");
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(withData))
                .thenReturn(Mono.just(withoutData));
        assertTrue(client.isProductAvailable(1L));
        assertFalse(client.isProductAvailable(2L));
    }

    @Test
    void isProductAvailable_nullResponse_returnsFalse() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.justOrEmpty(null));
        assertFalse(client.isProductAvailable(3L));
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
    void getProductName_returnsUnknown_whenDataMissing() throws Exception {
        JsonNode json = objectMapper.readTree("{}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(1L));
    }

    @Test
    void getProductName_returnsUnknown_onException() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenThrow(new RuntimeException("Error"));
        assertEquals("Unknown Product", client.getProductName(1L));
    }

    @Test
    void getProductName_allBranchesOfNestedIfs() throws Exception {
        JsonNode noAttributes = objectMapper.readTree("{\"data\": {}}");
        JsonNode noName = objectMapper.readTree("{\"data\": {\"attributes\": {\"id\": 5}}}");
        when(responseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(noAttributes))
                .thenReturn(Mono.just(noName));
        assertEquals("Unknown Product", client.getProductName(1L));
        assertEquals("Unknown Product", client.getProductName(2L));
    }

    @Test
    void getProductName_nullResponse_returnsUnknown() {
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.justOrEmpty(null));
        assertEquals("Unknown Product", client.getProductName(10L));
    }

    @Test
    void getProductName_nullDataNode_returnsUnknown() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": null}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(11L));
    }

    @Test
    void getProductName_nullAttributesNode_returnsUnknown() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"attributes\": null}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(12L));
    }

    @Test
    void getProductName_shouldReturnUnknownProduct_whenDataNodeIsNullButDataExists() throws Exception {
        Long productId = 13L;
        String jsonResponse = "{ \"data\": null }";
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(objectMapper.readTree(jsonResponse)));
        String result = client.getProductName(productId);
        assertEquals("Unknown Product", result);
    }

    @Test
    void getProductName_shouldReturnUnknownProduct_whenAttributesNodeIsNullButAttributesExist() throws Exception {
        Long productId = 14L;
        String jsonResponse = "{ \"data\": { \"attributes\": null } }";
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(objectMapper.readTree(jsonResponse)));
        String result = client.getProductName(productId);
        assertEquals("Unknown Product", result);
    }

    @Test
    void getProductName_dataNodeWithoutAttributes_returnsUnknown() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"other\": \"value\"}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(20L));
    }

    @Test
    void getProductName_attributesNodeWithoutName_returnsUnknown() throws Exception {
        JsonNode json = objectMapper.readTree("{\"data\": {\"attributes\": {\"other\": \"value\"}}}");
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        assertEquals("Unknown Product", client.getProductName(21L));
    }
}