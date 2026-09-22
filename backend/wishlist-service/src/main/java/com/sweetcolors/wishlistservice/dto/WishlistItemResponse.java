package com.sweetcolors.wishlistservice.dto;

import com.sweetcolors.wishlistservice.entity.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// DTO DE SALIDA: lo que la API devuelve (nunca se expone la entidad de la BD directamente).
@Data
@AllArgsConstructor
public class WishlistItemResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Boolean inStock;            // ← ESTE campo lo trae el CATALOG, no la tabla. Puede ser null.
    private LocalDateTime updatedAt;

    // Método estático que "convierte" entidad → DTO.
    public static WishlistItemResponse fromEntity(WishlistItem item, Boolean inStock) {
        return new WishlistItemResponse(
                item.getId(),
                item.getUserId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                inStock,                // Viene como parámetro (calculado por el service con el catalog).
                item.getUpdatedAt()
        );
    }
}

// POR QUÉ EXISTE EL DTO: controlar qué datos salen por el API y no exponer la entidad completa.