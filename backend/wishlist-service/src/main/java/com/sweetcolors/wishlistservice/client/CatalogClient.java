package com.sweetcolors.wishlistservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${catalog.base-url}") String catalogBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogBaseUrl)
                .build();
    }

    public Optional<CatalogProduct> findProduct(Long productId) {
        try {
            CatalogProduct product = restClient.get()
                    .uri("/api/catalog/products/{id}", productId)
                    .retrieve()
                    .body(CatalogProduct.class);
            return Optional.ofNullable(product);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                return Optional.empty();
            }
            throw ex;
        } catch (RestClientException ex) {
            throw new CatalogUnavailableException("No se pudo contactar catalog-service: " + ex.getMessage());
        }
    }

    public CatalogStock checkStock(Long productId) {
        try {
            return restClient.get()
                    .uri("/api/catalog/products/{id}/stock", productId)
                    .retrieve()
                    .body(CatalogStock.class);
        } catch (HttpClientErrorException ex) {
            return null;
        } catch (RestClientException ex) {
            return null;
        }
    }

    public record CatalogProduct(Long id, String name, BigDecimal price, Integer stock, String category) {
    }

    public record CatalogStock(Long productId, Integer stock, Boolean inStock) {
    }
}