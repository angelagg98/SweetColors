package com.sweetcolors.wishlistservice.config;

import com.sweetcolors.wishlistservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // TODO: /events es publico temporalmente porque history-service lo
                // consume sin JWT de usuario (es una llamada servicio-a-servicio).
                // Pendiente: agregar autenticacion propia entre microservicios
                // (por ejemplo, una API key compartida) para cerrar este endpoint.
                .requestMatchers(HttpMethod.GET, "/api/wishlist/events").permitAll()
                // Todo lo demas de la wishlist es informacion personal del usuario:
                // requiere estar autenticado, sin excepciones.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}