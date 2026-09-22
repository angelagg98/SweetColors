package com.sweetcolors.wishlistservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ENTIDAD JPA: esta clase = la tabla wishlist_items en MySQL.
// Hibernate crea la tabla automáticamente al arrancar (ddl-auto: update).
@Entity                                              // "Soy una entidad persistente (tabla)".
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_product", columnNames = {"user_id", "product_id"})
})                                                   // CLAVE ÚNICA (user+product): un usuario NO puede repetir el mismo producto.
@Data                                               // Lombok: getters + setters automáticos.
@Builder                                            // Lombok: permite construir con WishlistItem.builder()...build().
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

    @Id                                             // Llave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrementada por MySQL.
    private Long id;

    @Column(nullable = false)                       // NOT NULL en la BD.
    private Long userId;                            // A quién pertenece el ítem.

    @Column(nullable = false)
    private Long productId;                         // Qué producto (su id en el catalog).

    @Column(nullable = false, length = 150)
    private String productName;                     // Nombre del producto (denormalizado a propósito).

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;                   // Cantidad; por defecto 1 y el @Builder.Default lo conserva.

    @Column(nullable = false, updatable = false)    // createdAt NO se podrá actualizar (solo insert).
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist                                     // Callback de JPA: se ejecuta ANTES del INSERT.
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;                       // Rellena las fechas automáticamente.
        this.updatedAt = now;
    }

    @PreUpdate                                      // Callback de JPA: se ejecuta ANTES del UPDATE.
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();       // Actualiza SOLO updatedAt (createdAt queda intacto).
    }
}

// NOTA PARA EL EXAMEN: @PrePersist / @PreUpdate son callbacks que JPA ejecuta en el ciclo de vida
// de la entidad para que no tengas que setear las fechas a mano en el service.