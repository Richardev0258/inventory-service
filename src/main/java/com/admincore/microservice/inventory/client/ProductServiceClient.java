package com.admincore.microservice.inventory.client;

import com.admincore.microservice.inventory.exception.ProductServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class ProductServiceClient {

    private final WebClient webClient;
    private final String productServiceUrl;
    private final String productServiceApiKey;

    public ProductServiceClient(WebClient.Builder webClientBuilder,
                                @Value("${product.service.url}") String productServiceUrl,
                                @Value("${product.service.api-key}") String productServiceApiKey) {
        this.webClient = webClientBuilder.build();
        this.productServiceUrl = productServiceUrl;
        this.productServiceApiKey = productServiceApiKey;
    }

    public boolean isProductAvailable(Long productId) {
        try {
            JsonNode response = webClient.get()
                    .uri(productServiceUrl + "/" + productId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-API-KEY", productServiceApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            log.debug("Product {} found in product service", productId);
            return response != null && response.has("data");
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product {} not found in product service", productId);
            return false;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.error("Client error (4xx) calling product service for product {}: {}", productId, e.getMessage());
                throw new ProductServiceException("Client error for product " + productId + ": " + e.getMessage(), e.getStatusCode(), e);
            } else if (e.getStatusCode().is5xxServerError()) {
                log.error("Server error (5xx) from product service for product {}: {}", productId, e.getMessage());
                throw new ProductServiceException("Server error from product service", e.getStatusCode(), e);
            } else {
                log.error("Unexpected HTTP status from product service for product {}: {}", productId, e.getStatusCode());
                throw new ProductServiceException("Unexpected HTTP response from product service", HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        } catch (Exception e) {
            log.error("Unexpected error calling product service for product {}: {}", productId, e.getMessage(), e);
            throw new ProductServiceException("Unexpected error communicating with product service", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public String getProductName(Long productId) {
        try {
            JsonNode response = webClient.get()
                    .uri(productServiceUrl + "/" + productId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-API-KEY", productServiceApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("data")) {
                JsonNode dataNode = response.get("data");
                if (dataNode != null && dataNode.has("attributes")) {
                    JsonNode attributesNode = dataNode.get("attributes");
                    if (attributesNode != null && attributesNode.has("name")) {
                        return attributesNode.get("name").asText();
                    }
                }
            }
            return "Unknown Product";
        } catch (Exception e) {
            log.error("Error getting product name for product {}: {}", productId, e.getMessage());
            return "Unknown Product";
        }
    }
}