package com.crane.dispatch.repository;

import com.crane.dispatch.entity.CranePositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CranePositionHistoryRepository extends JpaRepository<CranePositionHistory, Long> {
    List<CranePositionHistory> findByCraneIdAndTimestampBetweenOrderByTimestampAsc(String craneId, LocalDateTime start, LocalDateTime end);
    List<CranePositionHistory> findByTimestampBetweenOrderByTimestampAsc(LocalDateTime start, LocalDateTime end);
}
