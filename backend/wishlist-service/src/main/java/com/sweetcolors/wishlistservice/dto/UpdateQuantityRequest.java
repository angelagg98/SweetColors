package com.sweetcolors.wishlistservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO de entrada para ACTUALIZAR cantidad (PUT /items/{id}).
@Data
public class UpdateQuantityRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer quantity;   // Solo la nueva cantidad (no hace falta reenviar el resto).
}