package com.sweetcolors.wishlistservice.controller;

import com.sweetcolors.wishlistservice.dto.UpdateQuantityRequest;
import com.sweetcolors.wishlistservice.dto.WishlistEventResponse;
import com.sweetcolors.wishlistservice.dto.WishlistItemRequest;
import com.sweetcolors.wishlistservice.dto.WishlistItemResponse;
import com.sweetcolors.wishlistservice.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CAPA DE PRESENTACIÓN HTTP: recibe la petición, valida y responde JSON.
// NO tiene lógica de negocio: todo se delega al WishlistService.
@RestController                                  // Expone endpoints REST (cada método se serializa a JSON).
@RequestMapping("/api/wishlist")                 // Prefijo base de TODOS los endpoints de esta clase.
@RequiredArgsConstructor                        // Inyección de dependencias: constructor con los "final".
public class WishlistController {

    private final WishlistService wishlistService; // Con este servicio habla este controller.

    // GET /api/wishlist/items?userId=1
    @GetMapping("/items")
    public List<WishlistItemResponse> list(@RequestParam Long userId) { // @RequestParam = "?userId=1".
        return wishlistService.list(userId);                            // Delegar y devolver.
    }

    // POST /api/wishlist/items  →  body: {"userId":1,"productId":1,"quantity":1}
    @PostMapping("/items")
    public ResponseEntity<WishlistItemResponse> add(@Valid @RequestBody WishlistItemRequest request) {
        // @RequestBody: convierte el JSON del cuerpo de la petición en el DTO.
        // @Valid: valida las reglas del DTO (@NotNull, @Min) ANTES de llegar al service.
        WishlistItemResponse response = wishlistService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created + el objeto creado.
    }

    // PUT /api/wishlist/items/1?userId=1
    @PutMapping("/items/{id}")
    public WishlistItemResponse update(@PathVariable Long id,            // @PathVariable: toma el "1" de la URL.
                                       @RequestParam Long userId,
                                       @Valid @RequestBody UpdateQuantityRequest request) {
        return wishlistService.updateQuantity(id, userId, request);
    }

    // DELETE /api/wishlist/items/1?userId=1
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id, @RequestParam Long userId) {
        wishlistService.remove(id, userId);
        return ResponseEntity.noContent().build(); // 204 No Content: eliminado sin cuerpo en la respuesta.
    }

    // GET /api/wishlist/events → lo consume history-service (userId opcional)
    @GetMapping("/events")
    public List<WishlistEventResponse> events(@RequestParam(required = false) Long userId) { // Parámetro OPCIONAL.
        return wishlistService.listEvents(userId);
    }
}

// RECORDAR: la URL completa = puerto (8083) + @RequestMapping de clase + ruta del método.
// Ej.: GET http://localhost:8083/api/wishlist/items?userId=1