package com.admincore.microservice.inventory.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long productId;
    private String productName;
    private Integer purchasedQuantity;
    private String message;
}