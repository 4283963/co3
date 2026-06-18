package com.crane.dispatch.repository;

import com.crane.dispatch.entity.CraneTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CraneTaskRepository extends JpaRepository<CraneTask, Long> {
    List<CraneTask> findByCraneIdOrderByCreatedAtDesc(String craneId);
    List<CraneTask> findByStatusOrderByCreatedAtDesc(String status);
}
