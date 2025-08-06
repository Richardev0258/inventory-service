package com.admincore.microservice.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true)
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Column(name = "quantity")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity = 0;
}