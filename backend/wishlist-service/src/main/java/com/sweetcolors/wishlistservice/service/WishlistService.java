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

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository itemRepository;
    private final WishlistEventRepository eventRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public List<WishlistItemResponse> list(Long userId) {
        List<WishlistItem> items = itemRepository.findByUserId(userId);
        List<WishlistItemResponse> responses = new ArrayList<>();

        for (WishlistItem item : items) {
            Boolean inStock = resolveStock(item.getProductId());
            responses.add(WishlistItemResponse.fromEntity(item, inStock));
            if (Boolean.FALSE.equals(inStock)) {
                notifyOutOfStock(item);
            }
        }
        return responses;
    }

    @Transactional
    public WishlistItemResponse add(WishlistItemRequest request) {
        Long userId = request.getUserId();

        if (itemRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("El producto ya esta en la wishlist del usuario");
        }

        CatalogClient.CatalogProduct product = catalogClient.findProduct(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto con id " + request.getProductId() + " no existe en el catalogo"));

        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .productId(request.getProductId())
                .productName(product.name())
                .quantity(request.getQuantity())
                .build();
        WishlistItem saved = itemRepository.save(item);

        recordEvent(userId, saved.getProductId(), saved.getProductName(), WishlistEvent.EventType.ADDED);
        return WishlistItemResponse.fromEntity(saved, resolveStock(saved.getProductId()));
    }

    @Transactional
    public WishlistItemResponse updateQuantity(Long itemId, Long userId, UpdateQuantityRequest request) {
        WishlistItem item = itemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new WishlistItemNotFoundException(itemId));

        item.setQuantity(request.getQuantity());
        WishlistItem saved = itemRepository.save(item);

        recordEvent(userId, saved.getProductId(), saved.getProductName(), WishlistEvent.EventType.UPDATED);
        return WishlistItemResponse.fromEntity(saved, resolveStock(saved.getProductId()));
    }

    @Transactional
    public void remove(Long itemId, Long userId) {
        WishlistItem item = itemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new WishlistItemNotFoundException(itemId));

        itemRepository.delete(item);
        recordEvent(userId, item.getProductId(), item.getProductName(), WishlistEvent.EventType.REMOVED);
    }

    @Transactional(readOnly = true)
    public List<WishlistEventResponse> listEvents(Long userId) {
        if (userId == null) {
            return eventRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(WishlistEventResponse::fromEntity)
                    .toList();
        }
        return eventRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistEventResponse::fromEntity)
                .toList();
    }

    private void notifyOutOfStock(WishlistItem item) {
        Optional<WishlistEvent> last =
                eventRepository.findFirstByUserIdAndProductIdOrderByCreatedAtDesc(item.getUserId(), item.getProductId());

        if (last.isPresent() && last.get().getEventType() == WishlistEvent.EventType.OUT_OF_STOCK) {
            return;
        }

        eventRepository.save(WishlistEvent.builder()
                .userId(item.getUserId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .eventType(WishlistEvent.EventType.OUT_OF_STOCK)
                .build());
    }

    private void recordEvent(Long userId, Long productId, String productName, WishlistEvent.EventType type) {
        eventRepository.save(WishlistEvent.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .eventType(type)
                .build());
    }

    private Boolean resolveStock(Long productId) {
        CatalogClient.CatalogStock stock = catalogClient.checkStock(productId);
        if (stock == null) {
            return null;
        }
        return Boolean.TRUE.equals(stock.inStock());
    }
}