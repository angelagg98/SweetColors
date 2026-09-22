package com.sweetcolors.wishlistservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO DE ENTRADA (POST /items): lo que el cliente debe enviar por JSON.
// Aquí se VALIDAN los datos antes de llegar al service (gracias a @Valid del controller).
@Data
public class WishlistItemRequest {

    @NotNull(message = "El userId es obligatorio")  // Si falta → 400 con ese mensaje.
    private Long userId;

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad minima es 1")  // Debe ser ≥ 1.
    private Integer quantity;
}

// NOTA: el DTO de entrada no trae "productName" a propósito:
// el nombre lo baja el servicio desde el CATALOG, no se confía en el cliente.