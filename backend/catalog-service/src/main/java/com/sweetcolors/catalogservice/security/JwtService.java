package com.sweetcolors.catalogservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// A diferencia de auth-service, este JwtService NO genera tokens -- solo los LEE
// y VALIDA. catalog-service confia en que auth-service ya genero el token
// correctamente; aqui solo confirmamos que la firma es valida y no esta vencido.
@Component
public class JwtService {

    // Debe ser EXACTAMENTE el mismo secreto que usa auth-service para firmar,
    // si no, ningun token se validaria aqui (la firma no coincidiria).
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}