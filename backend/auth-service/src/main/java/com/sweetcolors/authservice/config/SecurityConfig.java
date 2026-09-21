package com.sweetcolors.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration le dice a Spring que esta clase define beans (objetos administrados por Spring),
// no es una entidad ni un controlador, es configuracion pura.
@Configuration
// @EnableWebSecurity activa el modulo de Spring Security para toda la app,
// permitiendo que definamos nuestras propias reglas de acceso en vez de usar las de por defecto.
@EnableWebSecurity
public class SecurityConfig {

    // Este metodo define el "filtro de seguridad": las reglas que se aplican
    // a CADA peticion HTTP que llega al servicio, antes de que llegue al controller.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF (Cross-Site Request Forgery) protege apps que usan sesiones con cookies
            // tradicionales del navegador. Nosotros usamos JWT sin sesiones de servidor,
            // asi que no aplica y lo desactivamos.
            .csrf(csrf -> csrf.disable())

            // STATELESS = el servidor NUNCA guarda sesion de ningun usuario.
            // Cada peticion se autentica sola con su JWT, sin depender de que el servidor
            // "recuerde" quien eres. Esto es clave para microservicios: cualquier instancia
            // puede atender cualquier peticion sin compartir estado con otras instancias.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Aqui definimos que rutas son publicas y cuales requieren autenticacion.
            .authorizeHttpRequests(auth -> auth
                // Todo lo que empiece con /api/auth/ (login, registro) es publico:
                // nadie puede loguearse si loguearse ya requiere estar logueado.
                .requestMatchers("/api/auth/**").permitAll()

                // Cualquier otra ruta que no hayamos mencionado arriba, requiere
                // autenticacion (mas adelante, via JWT).
                .anyRequest().authenticated()
            );

        // Construye y devuelve la cadena de filtros ya configurada.
        return http.build();
    }
}