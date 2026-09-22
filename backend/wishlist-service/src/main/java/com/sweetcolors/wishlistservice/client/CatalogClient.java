package com.sweetcolors.wishlistservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;

// COMUNICACIÓN ENTRE MICROSERVICIOS:
// este "cliente" le habla al catalog-service (8082) por HTTP, igual que una app externa.
// IMPORTANTE: la wishlist NO consulta la BD del catalog, le pregunta a su API.
@Component                                          // Componente de Spring: se puede inyectar en el servicio.
public class CatalogClient {

    private final RestClient restClient;            // Cliente HTTP moderno de Spring (sustituye a RestTemplate).

    public CatalogClient(@Value("${catalog.base-url}") String catalogBaseUrl) {
        // @Value: lee la propiedad "catalog.base-url" del application.yaml (http://localhost:8082).
        this.restClient = RestClient.builder()      // Construye el cliente con la URL base del catalog.
                .baseUrl(catalogBaseUrl)
                .build();
    }

    // ===== BUSCAR UN PRODUCTO (se usa en "add") =====
    public Optional<CatalogProduct> findProduct(Long productId) {
        try {
            CatalogProduct product = restClient.get()                      // Ejecuta un GET al catalog.
                    .uri("/api/catalog/products/{id}", productId)         // Rellena {id} con el productId.
                    .retrieve()                                           // Ejecuta y recupera la respuesta.
                    .body(CatalogProduct.class);                          // Convierte el JSON de respuesta en objeto.
            return Optional.ofNullable(product);                          // Envuelve en Optional para evitar "null mágico".
        } catch (HttpClientErrorException ex) {                           // El catalog respondió con un código 4xx.
            if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                return Optional.empty();                                  // 404 = el producto NO existe → Optional vacío.
            }
            throw ex;                                                     // Otros errores HTTP: los deja pasar.
        } catch (RestClientException ex) {                                // Error de RED: el catalog está caído o inaccesible.
            throw new CatalogUnavailableException("No se pudo contactar catalog-service: " + ex.getMessage());
                // Excepción propia → GlobalExceptionHandler la convierte en HTTP 503.
        }
    }

    // ===== CONSULTAR SOLO EL STOCK (se usa en "list" y "resolveStock") =====
    public CatalogStock checkStock(Long productId) {
        try {
            return restClient.get()
                    .uri("/api/catalog/products/{id}/stock", productId)  // Endpoint especial del catalog.
                    .retrieve()
                    .body(CatalogStock.class);
        } catch (HttpClientErrorException ex) { return null; }  // Error HTTP → null (no tumbar el servicio).
        catch (RestClientException ex)     { return null; }     // Red caída → null (no tumbar el servicio).
    }

    // ===== RECORDS: clases inmutables que "mapean" el JSON que responde el catalog =====
    // (Java 15+). Con el mismo nombre de campos que el JSON, Jackson deserializa solo.
    public record CatalogProduct(Long id, String name, BigDecimal price, Integer stock, String category) {
    }

    public record CatalogStock(Long productId, Integer stock, Boolean inStock) {
    }
}

// DIFERENCIA CLAVE PARA EL EXAMEN:
//   - findProduct: si el catalog está caído LANZA excepción → 503. Se usa en "add" porque el dato es imprescindible.
//   - checkStock:  si el catalog falla devuelve null → no se inventa estado. Se usa en "list" porque es informativo.