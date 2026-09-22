package com.sweetcolors.wishlistservice.repository;

import com.sweetcolors.wishlistservice.entity.WishlistEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repositorio de EVENTOS: idéntico concepto que los demás.
public interface WishlistEventRepository extends JpaRepository<WishlistEvent, Long> {

    // → SELECT * FROM wishlist_events ORDER BY created_at DESC
    List<WishlistEvent> findAllByOrderByCreatedAtDesc();

    // → SELECT * FROM wishlist_events WHERE user_id = ? ORDER BY created_at DESC
    List<WishlistEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    // → SELECT * FROM wishlist_events WHERE user_id = ? AND product_id = ? ORDER BY created_at DESC LIMIT 1
    // Devuelve el evento MÁS RECIENTE de ese usuario+producto.
    // CLAVE del anti-duplicado de OUT_OF_STOCK en WishlistService.
    Optional<WishlistEvent> findFirstByUserIdAndProductIdOrderByCreatedAtDesc(Long userId, Long productId);
}

// "findFirstBy..." = trae el PRIMERO (LIMIT 1). "OrderByXDesc" = ordena descendente por X.