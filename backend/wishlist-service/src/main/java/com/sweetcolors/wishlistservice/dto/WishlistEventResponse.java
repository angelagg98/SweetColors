package com.sweetcolors.wishlistservice.dto;

import com.sweetcolors.wishlistservice.entity.WishlistEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// DTO DE SALIDA para los eventos (GET /events → lo consume history-service).
@Data
@AllArgsConstructor
public class WishlistEventResponse {

    private Long id;                        // id del evento EN LA WISHLIST (será "sourceId" en history).
    private Long userId;
    private Long productId;
    private String productName;
    private String eventType;               // Se expone como String ("ADDED") y no como enum.
    private LocalDateTime createdAt;

    public static WishlistEventResponse fromEntity(WishlistEvent event) {
        return new WishlistEventResponse(
                event.getId(),
                event.getUserId(),
                event.getProductId(),
                event.getProductName(),
                event.getEventType().name(),  // .name() → convierte el enum a su texto.
                event.getCreatedAt()
        );
    }
}