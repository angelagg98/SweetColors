package com.sweetcolors.wishlistservice.repository;

import com.sweetcolors.wishlistservice.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// REPOSITORIO: "interfaz que habla con la BD".
// NO hay implementación: Spring Data JPA lee el NOMBRE del método y genera el SQL automáticamente.
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // → SELECT * FROM wishlist_items WHERE user_id = ?
    List<WishlistItem> findByUserId(Long userId);

    // → SELECT * FROM wishlist_items WHERE id = ? AND user_id = ?
    // (permite operar solo ítems del MISMO usuario → seguridad básica).
    Optional<WishlistItem> findByIdAndUserId(Long id, Long userId);

    // → SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE user_id = ? AND product_id = ?)
    // Devuelve true/false; se usa para validar que el producto no esté repetido.
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}

// REGLA DEL EXAMEN: cada método del repositorio es una consulta declarada por convención de nombres:
//   find  = SELECT   ·  By...  = WHERE   ·  And / Or  = combinadores.
// Los métodos CRUD ya vienen gratis del JpaRepository (save, findById, findAll, delete).