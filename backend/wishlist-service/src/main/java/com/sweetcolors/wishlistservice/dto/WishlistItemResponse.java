package com.sweetcolors.wishlistservice.dto;

import com.sweetcolors.wishlistservice.entity.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WishlistItemResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Boolean inStock;
    private LocalDateTime updatedAt;

    public static WishlistItemResponse fromEntity(WishlistItem item, Boolean inStock) {
        return new WishlistItemResponse(
                item.getId(),
                item.getUserId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                inStock,
                item.getUpdatedAt()
        );
    }
}