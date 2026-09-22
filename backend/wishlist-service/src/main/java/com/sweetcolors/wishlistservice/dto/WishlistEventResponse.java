package com.sweetcolors.wishlistservice.dto;

import com.sweetcolors.wishlistservice.entity.WishlistEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WishlistEventResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String eventType;
    private LocalDateTime createdAt;

    public static WishlistEventResponse fromEntity(WishlistEvent event) {
        return new WishlistEventResponse(
                event.getId(),
                event.getUserId(),
                event.getProductId(),
                event.getProductName(),
                event.getEventType().name(),
                event.getCreatedAt()
        );
    }
}