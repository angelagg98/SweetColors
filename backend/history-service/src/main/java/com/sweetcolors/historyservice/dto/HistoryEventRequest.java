package com.sweetcolors.historyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryEventRequest {

    @NotNull
    private Long sourceId;

    @NotNull
    private Long userId;

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String eventType;

    private LocalDateTime createdAt;
}