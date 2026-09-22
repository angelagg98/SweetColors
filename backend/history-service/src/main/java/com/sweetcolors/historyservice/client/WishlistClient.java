package com.sweetcolors.historyservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WishlistClient {

    private final RestClient restClient;

    public WishlistClient(@Value("${wishlist.base-url}") String wishlistBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(wishlistBaseUrl)
                .build();
    }

    public List<WishlistEvent> listEvents() {
        try {
            WishlistEvent[] events = restClient.get()
                    .uri("/api/wishlist/events")
                    .retrieve()
                    .body(WishlistEvent[].class);
            return events == null ? List.of() : List.of(events);
        } catch (RestClientException ex) {
            throw new WishlistUnavailableException("No se pudo contactar wishlist-service: " + ex.getMessage());
        }
    }

    public record WishlistEvent(Long id, Long userId, Long productId, String productName,
                                String eventType, LocalDateTime createdAt) {
    }
}