package com.sweetcolors.wishlistservice.repository;

import com.sweetcolors.wishlistservice.entity.WishlistEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistEventRepository extends JpaRepository<WishlistEvent, Long> {

    List<WishlistEvent> findAllByOrderByCreatedAtDesc();

    List<WishlistEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<WishlistEvent> findFirstByUserIdAndProductIdOrderByCreatedAtDesc(Long userId, Long productId);
}