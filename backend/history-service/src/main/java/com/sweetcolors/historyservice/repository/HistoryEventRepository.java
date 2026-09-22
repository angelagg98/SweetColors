package com.sweetcolors.historyservice.repository;

import com.sweetcolors.historyservice.entity.HistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryEventRepository extends JpaRepository<HistoryEvent, Long> {

    List<HistoryEvent> findAllByOrderByCreatedAtDesc();

    List<HistoryEvent> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsBySourceId(Long sourceId);
}