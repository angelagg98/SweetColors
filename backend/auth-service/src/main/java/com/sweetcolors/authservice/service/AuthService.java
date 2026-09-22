package com.sweetcolors.authservice.service;

import com.sweetcolors.authservice.dto.LoginRequest;
import com.sweetcolors.authservice.dto.RegisterRequest;
import com.sweetcolors.authservice.dto.UserResponse;
import com.sweetcolors.authservice.entity.RefreshToken;
import com.sweetcolors.authservice.entity.User;
import com.sweetcolors.authservice.repository.RefreshTokenRepository;
import com.sweetcolors.authservice.repository.UserRepository;
import com.sweetcolors.authservice.security.JwtService;
import com.sweetcolors.authservice.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;

    // Leemos la duracion del refresh token desde application.yml, igual que
    // hicimos con el access token dentro de JwtService.
    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CLIENTE)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    // Contiene el resultado del login: el usuario, el access token (para la cookie)
    // y el refresh token EN TEXTO PLANO (solo existe este momento, nunca se guarda asi).
    public record LoginResult(UserResponse user, String accessToken, String rawRefreshToken) {}

    public LoginResult login(LoginRequest request) {
        // Busca el usuario por email; si no existe, lanzamos error generico
        // (nunca decimos "el email no existe" por separado de "la clave es incorrecta",
        // para no darle pistas a quien intenta adivinar cuentas validas).
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email o contrasena incorrectos"));

        // matches() compara la contrasena en texto plano que llego contra el hash
        // guardado en la BD, usando el mismo algoritmo BCrypt.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email o contrasena incorrectos");
        }

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Esta cuenta esta deshabilitada");
        }

        // Genera el access token (JWT) con los datos del usuario.
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );

        // Genera el refresh token: un identificador aleatorio y unico, NO un JWT.
        // No necesita ser JWT porque nunca lo decodificamos, solo lo buscamos
        // en la BD por su hash para confirmar que existe y no esta vencido/revocado.
        String rawRefreshToken = UUID.randomUUID().toString();

        // Guardamos SOLO el hash del refresh token en la base de datos.
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(tokenHasher.hash(rawRefreshToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new LoginResult(UserResponse.fromEntity(user), accessToken, rawRefreshToken);
    }
}