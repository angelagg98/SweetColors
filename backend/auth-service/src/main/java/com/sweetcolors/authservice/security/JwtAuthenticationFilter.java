package com.sweetcolors.authservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter garantiza que este filtro se ejecute exactamente UNA vez
// por cada peticion HTTP que llega, antes de que llegue al controller.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Buscamos la cookie "access_token" entre todas las cookies de la peticion.
        String token = extractTokenFromCookies(request);

        // Si no hay token, o no es valido (vencido, firma incorrecta, etc.),
        // simplemente dejamos pasar la peticion SIN autenticar a nadie.
        // Spring Security, mas adelante en la cadena, se encarga de rechazarla
        // si la ruta requiere autenticacion (gracias a .anyRequest().authenticated()
        // que configuramos en SecurityConfig).
        if (token == null || !jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // El token es valido: extraemos los datos del usuario que vienen dentro.
        Claims claims = jwtService.extractAllClaims(token);
        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);

        // Creamos un "authority" (permiso) basado en el rol del usuario.
        // Spring Security espera el prefijo "ROLE_" por convencion para poder
        // usar despues cosas como @PreAuthorize("hasRole('ADMIN')").
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // Construimos el objeto de autenticacion que Spring Security entiende:
        // - principal: quien es (usamos el userId como identificador)
        // - credentials: null porque ya no necesitamos la contrasena aqui,
        //   el JWT ya demostro que el usuario es quien dice ser
        // - authorities: que permisos tiene
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Guardamos esta autenticacion en el "contexto de seguridad" de Spring,
        // que vive durante toda esta peticion. A partir de aqui, el controller
        // puede saber quien hizo la peticion (por ejemplo con @AuthenticationPrincipal).
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Dejamos que la peticion continue su camino normal hacia el controller.
        filterChain.doFilter(request, response);
    }

    // Metodo auxiliar: recorre las cookies de la peticion buscando "access_token".
    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}