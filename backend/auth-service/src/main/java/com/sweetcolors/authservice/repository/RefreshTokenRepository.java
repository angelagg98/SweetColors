package com.sweetcolors.authservice.repository;

import com.sweetcolors.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUserId(Long userId);

    // No necesitamos un metodo nuevo aqui realmente -- save() ya nos sirve
    // para actualizar el campo revoked. Dejamos el archivo igual que antes.
}