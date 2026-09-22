package com.sweetcolors.wishlistservice.service;

import com.sweetcolors.wishlistservice.client.CatalogClient;
import com.sweetcolors.wishlistservice.dto.UpdateQuantityRequest;
import com.sweetcolors.wishlistservice.dto.WishlistEventResponse;
import com.sweetcolors.wishlistservice.dto.WishlistItemRequest;
import com.sweetcolors.wishlistservice.dto.WishlistItemResponse;
import com.sweetcolors.wishlistservice.entity.WishlistEvent;
import com.sweetcolors.wishlistservice.entity.WishlistItem;
import com.sweetcolors.wishlistservice.exception.WishlistItemNotFoundException;
import com.sweetcolors.wishlistservice.repository.WishlistEventRepository;
import com.sweetcolors.wishlistservice.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// CLASE PRINCIPAL DE LA WISHLIST: contiene TODA la lógica de negocio.
// El controller solo recibe el pedido HTTP y delega todo aquí.
@Service                                    // Marca la clase como servicio: Spring crea e inyecta una instancia única.
@RequiredArgsConstructor                    // Lombok genera el constructor con los campos "private final" (inyección de dependencias).
public class WishlistService {

    private final WishlistItemRepository itemRepository;    // Habla con la tabla wishlist_items.
    private final WishlistEventRepository eventRepository;  // Habla con la tabla wishlist_events.
    private final CatalogClient catalogClient;              // Cliente HTTP que llama AL CATALOG (servicio 8082).

    // ===== LISTAR LA WISHLIST DE UN USUARIO (GET /items?userId=X) =====
    @Transactional                          // Todo o nada: si algo falla la operación se revierte (rollback).
    public List<WishlistItemResponse> list(Long userId) {
        List<WishlistItem> items = itemRepository.findByUserId(userId);  // Trae los ítems del usuario desde la BD.
        List<WishlistItemResponse> responses = new ArrayList<>();

        for (WishlistItem item : items) {                       // Recorre cada ítem...
            Boolean inStock = resolveStock(item.getProductId()); // ...pregunta el stock al CATALOG por HTTP.
            responses.add(WishlistItemResponse.fromEntity(item, inStock)); // Convierte entidad → DTO para el JSON.
            if (Boolean.FALSE.equals(inStock)) {                // Si NOS dijeron que está agotado...
                notifyOutOfStock(item);                         // ...registra el evento OUT_OF_STOCK (si no está repetido).
            }
        }
        return responses;                                       // Devuelve la lista lista para convertir a JSON.
    }

    // ===== AGREGAR UN PRODUCTO A LA WISHLIST (POST /items) =====
    @Transactional
    public WishlistItemResponse add(WishlistItemRequest request) {
        Long userId = request.getUserId();

        if (itemRepository.existsByUserIdAndProductId(userId, request.getProductId())) { // ¿Ya lo tiene?
            throw new IllegalArgumentException("El producto ya esta en la wishlist del usuario"); // → 400 (regla de negocio)
        }

        CatalogClient.CatalogProduct product = catalogClient.findProduct(request.getProductId()) // Pide el producto al catalog.
                .orElseThrow(() -> new IllegalArgumentException(          // Si devuelve vacío (404) → 400.
                        "El producto con id " + request.getProductId() + " no existe en el catalogo"));

        WishlistItem item = WishlistItem.builder()          // Builder de Lombok para crear la entidad.
                .userId(userId)
                .productId(request.getProductId())
                .productName(product.name())                // IMPORTANTE: el nombre lo trae del CATALOG, no lo escribe el cliente.
                .quantity(request.getQuantity())
                .build();
        WishlistItem saved = itemRepository.save(item);     // INSERT en wishlist_items (la fecha la pone @PrePersist).

        recordEvent(userId, saved.getProductId(), saved.getProductName(), WishlistEvent.EventType.ADDED); // Registra evento.
        return WishlistItemResponse.fromEntity(saved, resolveStock(saved.getProductId())); // Responde con el stock actual.
    }

    // ===== CAMBIAR LA CANTIDAD (PUT /items/{id}) =====
    @Transactional
    public WishlistItemResponse updateQuantity(Long itemId, Long userId, UpdateQuantityRequest request) {
        WishlistItem item = itemRepository.findByIdAndUserId(itemId, userId)   // Busca por id Y del MISMO usuario.
                .orElseThrow(() -> new WishlistItemNotFoundException(itemId)); // Si no existe → excepción propia → 404.

        item.setQuantity(request.getQuantity());            // Cambia la cantidad en memoria.
        WishlistItem saved = itemRepository.save(item);     // UPDATE en la BD.

        recordEvent(userId, saved.getProductId(), saved.getProductName(), WishlistEvent.EventType.UPDATED);
        return WishlistItemResponse.fromEntity(saved, resolveStock(saved.getProductId()));
    }

    // ===== ELIMINAR UN ÍTEM (DELETE /items/{id}) =====
    @Transactional
    public void remove(Long itemId, Long userId) {
        WishlistItem item = itemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new WishlistItemNotFoundException(itemId));

        itemRepository.delete(item);                        // DELETE en la BD.
        recordEvent(userId, item.getProductId(), item.getProductName(), WishlistEvent.EventType.REMOVED); // Registra evento.
    }

    // ===== LISTAR EVENTOS (GET /events) — lo consume HISTORY-SERVICE =====
    @Transactional(readOnly = true)                         // Solo lectura: Optimiza (no hace "dirty checking").
    public List<WishlistEventResponse> listEvents(Long userId) {
        if (userId == null) {                               // Sin filtro → todos los eventos, más reciente primero.
            return eventRepository.findAllByOrderByCreatedAtDesc().stream() // Stream sobre la lista...
                    .map(WishlistEventResponse::fromEntity)  // ...entidad → DTO (method reference: ficticio "::").
                    .toList();                               // Java 16+: coleccionar en una lista inmutable.
        }
        return eventRepository.findByUserIdOrderByCreatedAtDesc(userId).stream() // Filtrado por usuario.
                .map(WishlistEventResponse::fromEntity)
                .toList();
    }

    // ===== ANTI-DUPLICADO DE LA NOTIFICACIÓN DE SIN-STOCK =====
    private void notifyOutOfStock(WishlistItem item) {
        Optional<WishlistEvent> last =
                eventRepository.findFirstByUserIdAndProductIdOrderByCreatedAtDesc(item.getUserId(), item.getProductId());
                // Busca el evento MÁS RECIENTE (OrderBy...Desc + findFirst) de ese usuario+producto.

        if (last.isPresent() && last.get().getEventType() == WishlistEvent.EventType.OUT_OF_STOCK) {
            return;   // Si el último ya fue OUT_OF_STOCK, NO vuelve a guardar (evita spam/duplicados).
        }

        eventRepository.save(WishlistEvent.builder()         // Si no → INSERT del evento OUT_OF_STOCK.
                .userId(item.getUserId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .eventType(WishlistEvent.EventType.OUT_OF_STOCK)
                .build());
    }

    // ===== MÉTODO PRIVADO: registrar cualquier tipo de evento =====
    private void recordEvent(Long userId, Long productId, String productName, WishlistEvent.EventType type) {
        eventRepository.save(WishlistEvent.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .eventType(type)
                .build());
    }

    // ===== PREGUNTAR EL STOCK AL CATALOG =====
    private Boolean resolveStock(Long productId) {
        CatalogClient.CatalogStock stock = catalogClient.checkStock(productId); // GET /api/catalog/products/{id}/stock
        if (stock == null) {            // El catalog no respondió → "no verificable".
            return null;                // inStock queda null (no inventa true ni false).
        }
        return Boolean.TRUE.equals(stock.inStock()); // Convierte el Boolean del catalog en true/false seguro.
    }
}