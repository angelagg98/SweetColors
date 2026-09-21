package com.sweetcolors.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Lo que el cliente manda al hacer login: solo email y password.
@Data
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
}