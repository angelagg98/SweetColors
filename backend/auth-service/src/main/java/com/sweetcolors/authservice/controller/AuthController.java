package com.sweetcolors.authservice.controller;

import com.sweetcolors.authservice.dto.LoginRequest;
import com.sweetcolors.authservice.dto.RegisterRequest;
import com.sweetcolors.authservice.dto.UserResponse;
import com.sweetcolors.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            // HttpServletResponse nos deja agregar headers/cookies directo a la respuesta,
            // algo que no se puede hacer solo con el valor de retorno del metodo.
            HttpServletResponse response
    ) {
        AuthService.LoginResult result = authService.login(request);

        // Cookie del ACCESS TOKEN: dura poco (15 min, igual que el JWT).
        ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
                .httpOnly(true)   // JavaScript del navegador NO puede leerla (protege contra XSS)
                .secure(true)     // solo se envia por HTTPS (en desarrollo local con http puede
                                  // dar problemas -- si te pasa, lo ajustamos para dev)
                .sameSite("Strict") // el navegador no la manda en peticiones desde otros sitios
                .path("/")
                .maxAge(15 * 60) // 15 minutos, en segundos
                .build();

        // Cookie del REFRESH TOKEN: dura mas (7 dias), y restringimos su path para
        // que el navegador SOLO la envie cuando se llame al endpoint de refresh,
        // reduciendo la exposicion del token en el resto de peticiones.
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.rawRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60) // 7 dias, en segundos
                .build();

        // Agregamos ambas cookies a la respuesta HTTP.
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // El body de la respuesta NUNCA incluye los tokens -- viajan solo en las cookies.
        return ResponseEntity.ok(result.user());
    }
}