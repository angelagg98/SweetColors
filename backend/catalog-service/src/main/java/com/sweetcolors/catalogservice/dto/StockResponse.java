package com.sweetcolors.catalogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockResponse {

    private Long productId;
    private Integer stock;
    private Boolean inStock;

    public static StockResponse fromEntity(Long id, Integer stock) {
        return new StockResponse(id, stock, stock > 0);
    }
}