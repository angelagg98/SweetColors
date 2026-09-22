package com.sweetcolors.authservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Este controller es TEMPORAL, solo para confirmar que el filtro JWT funciona.
// Mas adelante lo podemos borrar o convertirlo en algo real (como "mi perfil").
@RestController
public class TestController {

    // Como esta ruta NO empieza con /api/auth/, Spring Security exige
    // autenticacion aqui -- solo entra quien tenga un access_token valido.
    @GetMapping("/api/test/protected")
    public String protectedEndpoint(Authentication authentication) {
        // authentication.getName() nos devuelve el "principal" que guardamos
        // en el filtro -- en nuestro caso, el userId.
        return "Hola, estas autenticado como el usuario con id: " + authentication.getName();
    }
}