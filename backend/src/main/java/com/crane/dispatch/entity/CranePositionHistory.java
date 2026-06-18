package com.crane.dispatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crane_position_history", indexes = {
    @Index(name = "idx_crane_id_timestamp", columnList = "craneId, timestamp")
})
public class CranePositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String craneId;

    @Column(nullable = false)
    private Double position;

    @Column(nullable = false)
    private Double speed;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public CranePositionHistory() {}

    public CranePositionHistory(String craneId, Double position, Double speed, LocalDateTime timestamp) {
        this.craneId = craneId;
        this.position = position;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getPosition() { return position; }
    public void setPosition(Double position) { this.position = position; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
