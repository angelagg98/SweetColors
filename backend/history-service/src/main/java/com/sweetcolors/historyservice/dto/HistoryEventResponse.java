package com.sweetcolors.historyservice.dto;

import com.sweetcolors.historyservice.entity.HistoryEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistoryEventResponse {

    private Long id;
    private Long sourceId;
    private Long userId;
    private Long productId;
    private String productName;
    private String eventType;
    private LocalDateTime createdAt;

    public static HistoryEventResponse fromEntity(HistoryEvent event) {
        return new HistoryEventResponse(
                event.getId(),
                event.getSourceId(),
                event.getUserId(),
                event.getProductId(),
                event.getProductName(),
                event.getEventType().name(),
                event.getCreatedAt()
        );
    }
}