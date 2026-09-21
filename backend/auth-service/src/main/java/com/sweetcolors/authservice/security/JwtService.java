package com.sweetcolors.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// @Component: le dice a Spring que cree una unica instancia de esta clase (singleton)
// y la inyecte donde la necesitemos (por ejemplo en AuthService).
@Component
public class JwtService {

    // @Value lee el valor desde application.yml (que a su vez lo saca del .env).
    // Este es el "secreto" con el que se firman los tokens -- si alguien lo conoce,
    // puede falsificar tokens validos, por eso NUNCA va hardcodeado ni en el repo.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    // Convierte el secreto (texto plano) en una SecretKey que la libreria jjwt
    // puede usar para firmar/verificar tokens con el algoritmo HMAC-SHA256.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Genera el ACCESS TOKEN: el JWT de corta duracion (15 min) que el cliente
    // manda en cada peticion para demostrar quien es.
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                // "subject" = a quien pertenece el token (usamos el id del usuario)
                .subject(String.valueOf(userId))
                // Datos extra que guardamos dentro del token, para no tener que
                // consultar la base de datos cada vez que alguien lo use.
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                // Firma el token con nuestra clave secreta -- esto es lo que hace
                // que nadie pueda modificar el contenido sin ser detectado.
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrae todos los datos (claims) de un token, validando su firma de paso.
    // Si el token fue modificado o esta vencido, esto lanza una excepcion.
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Saca el id del usuario (subject) de un token ya validado.
    public Long extractUserId(String token) {
        return Long.valueOf(extractAllClaims(token).getSubject());
    }

    // Revisa si el token sigue siendo valido (firma correcta y no vencido).
    // Lo usaremos en el filtro que protege las rutas privadas mas adelante.
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Si algo falla al leer el token (firma invalida, formato raro, etc.)
            // lo tratamos simplemente como "no valido", sin tumbar la app.
            return false;
        }
    }
}