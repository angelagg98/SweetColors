package com.sweetcolors.wishlistservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ENTIDAD JPA: esta clase = la tabla wishlist_events en MySQL.
// Es el "LOG" de todo lo que pasa en la wishlist (lo consume history-service).
@Entity
@Table(name = "wishlist_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;                // A qué usuario le pasó el evento.

    @Column(nullable = false)
    private Long productId;             // A qué producto se refiere.

    @Column(nullable = false, length = 150)
    private String productName;

    @Enumerated(EnumType.STRING)        // Guarda el enum como TEXTO ("ADDED") y no como número (más legible).
    @Column(nullable = false, length = 30)
    private EventType eventType;        // Qué pasó: ADDED, REMOVED, UPDATED, OUT_OF_STOCK.

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // Cuándo pasó.

    @PrePersist                         // Callback JPA: fecha automática antes de insertar.
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Enumeración propia de la entidad: los 4 tipos de evento posibles.
    public enum EventType {
        ADDED,          // Se agregó a la wishlist.
        REMOVED,        // Se quitó de la wishlist.
        UPDATED,        // Se cambió la cantidad.
        OUT_OF_STOCK    // Notificación: el producto quedó sin stock.
    }
}