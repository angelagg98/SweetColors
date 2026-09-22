package com.sweetcolors.historyservice.service;

import com.sweetcolors.historyservice.client.WishlistClient;
import com.sweetcolors.historyservice.dto.HistoryEventRequest;
import com.sweetcolors.historyservice.dto.HistoryEventResponse;
import com.sweetcolors.historyservice.entity.HistoryEvent;
import com.sweetcolors.historyservice.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryEventRepository eventRepository;
    private final WishlistClient wishlistClient;

    @Transactional
    public long syncFromWishlist() {
        List<WishlistClient.WishlistEvent> events = wishlistClient.listEvents();
        long saved = 0;

        for (WishlistClient.WishlistEvent event : events) {
            if (eventRepository.existsBySourceId(event.id())) {
                continue;
            }
            eventRepository.save(HistoryEvent.builder()
                    .sourceId(event.id())
                    .userId(event.userId())
                    .productId(event.productId())
                    .productName(event.productName())
                    .eventType(HistoryEvent.EventType.valueOf(event.eventType()))
                    .createdAt(event.createdAt())
                    .build());
            saved++;
        }
        return saved;
    }

    @Transactional
    public HistoryEventResponse ingest(HistoryEventRequest request) {
        if (eventRepository.existsBySourceId(request.getSourceId())) {
            throw new IllegalArgumentException("El evento con sourceId " + request.getSourceId()
                    + " ya existe en el historico (evita duplicados)");
        }

        HistoryEvent event = HistoryEvent.builder()
                .sourceId(request.getSourceId())
                .userId(request.getUserId())
                .productId(request.getProductId())
                .productName(request.getProductName())
                .eventType(HistoryEvent.EventType.valueOf(request.getEventType()))
                .createdAt(request.getCreatedAt())
                .build();
        return HistoryEventResponse.fromEntity(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<HistoryEventResponse> list(Long userId) {
        if (userId == null) {
            return eventRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(HistoryEventResponse::fromEntity)
                    .toList();
        }
        return eventRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(HistoryEventResponse::fromEntity)
                .toList();
    }
}