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

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/items")
    public List<WishlistItemResponse> list(@RequestParam Long userId) {
        return wishlistService.list(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistItemResponse> add(@Valid @RequestBody WishlistItemRequest request) {
        WishlistItemResponse response = wishlistService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{id}")
    public WishlistItemResponse update(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @Valid @RequestBody UpdateQuantityRequest request) {
        return wishlistService.updateQuantity(id, userId, request);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id, @RequestParam Long userId) {
        wishlistService.remove(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public List<WishlistEventResponse> events(@RequestParam(required = false) Long userId) {
        return wishlistService.listEvents(userId);
    }
}