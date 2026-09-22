package com.sweetcolors.historyservice.controller;

import com.sweetcolors.historyservice.dto.HistoryEventRequest;
import com.sweetcolors.historyservice.dto.HistoryEventResponse;
import com.sweetcolors.historyservice.service.HistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/events")
    public List<HistoryEventResponse> getEvents(@RequestParam(required = false) Long userId) {
        return historyService.list(userId);
    }

    @PostMapping("/events")
    public ResponseEntity<HistoryEventResponse> ingestEvent(@Valid @RequestBody HistoryEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.ingest(request));
    }

    @GetMapping("/sync")
    public Map<String, Object> sync() {
        long saved = historyService.syncFromWishlist();
        return Map.of("synchronized", saved, "message", saved + " evento(s) sincronizado(s) desde la wishlist");
    }
}