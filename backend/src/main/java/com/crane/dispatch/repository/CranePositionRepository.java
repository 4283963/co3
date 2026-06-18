package com.crane.dispatch.repository;

import com.crane.dispatch.entity.CranePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CranePositionRepository extends JpaRepository<CranePosition, Long> {
    Optional<CranePosition> findByCraneId(String craneId);
}
