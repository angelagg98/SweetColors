package com.sweetcolors.wishlistservice.exception;

import com.sweetcolors.wishlistservice.client.CatalogUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// MANEJADOR GLOBAL DE EXCEPCIONES:
// centro único donde se convierten los errores en respuestas HTTP limpias (JSON),
// en lugar de que Spring devuelva un error genérico feo.
@RestControllerAdvice          // Intercepta las excepciones de TODOS los controllers del servicio.
public class GlobalExceptionHandler {

    @ExceptionHandler(WishlistItemNotFoundException.class)         // Excepción propia (404).
    public ResponseEntity<Map<String, String>> handleNotFound(WishlistItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)        // 404 Not Found.
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)              // Reglas de negocio (producto repetido, no existe...).
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)      // 400 Bad Request.
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CatalogUnavailableException.class)           // El catalog no respondió (client).
    public ResponseEntity<Map<String, String>> handleCatalogUnavailable(CatalogUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)  // 503 Service Unavailable.
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)       // La lanza @Valid cuando falla la validación.
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();       // LinkedHashMap: conserva el orden.
        ex.getBindingResult().getFieldErrors()                     // Todos los campos que fallaron...
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage())); // ...campo → mensaje.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);  // 400 con el detalle campo a campo.
    }
}

// RESUMEN DE CÓDIGOS DE ESTE SERVICIO:
//   404 = ítem no encontrado · 400 = validación o regla de negocio · 503 = catalog caído.