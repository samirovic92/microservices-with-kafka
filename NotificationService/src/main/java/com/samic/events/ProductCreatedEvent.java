package com.samic.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreatedEvent {
    private  String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
