package com.sweetcolors.authservice.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Convierte un refresh token en su version "hasheada" con SHA-256.
// Guardamos SOLO este hash en la base de datos, nunca el token real:
// asi, si alguien accede a la BD, no puede usar los tokens directamente.
@Component
public class TokenHasher {

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            // Codificamos el resultado en Base64 para que sea texto legible/guardable.
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en Java, esto practicamente nunca pasa,
            // pero Java nos obliga a manejar la excepcion.
            throw new RuntimeException("Error al hashear el token", e);
        }
    }
}