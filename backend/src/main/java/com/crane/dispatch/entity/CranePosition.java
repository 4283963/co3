package com.crane.dispatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crane_positions")
public class CranePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String craneId;

    @Column(nullable = false)
    private Double position;

    @Column(nullable = false)
    private Double speed;

    private Double targetPosition;

    private LocalDateTime updatedAt;

    public CranePosition() {}

    public CranePosition(String craneId, Double position, Double speed) {
        this.craneId = craneId;
        this.position = position;
        this.speed = speed;
        this.targetPosition = position;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getPosition() { return position; }
    public void setPosition(Double position) { this.position = position; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getTargetPosition() { return targetPosition; }
    public void setTargetPosition(Double targetPosition) { this.targetPosition = targetPosition; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
