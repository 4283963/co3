package com.crane.dispatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crane_tasks")
public class CraneTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String craneId;

    @Column(nullable = false)
    private Double targetPosition;

    @Column(nullable = false, length = 50)
    private String taskType;

    @Column(nullable = false, length = 20)
    private String status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public CraneTask() {}

    public CraneTask(String craneId, Double targetPosition, String taskType, String description) {
        this.craneId = craneId;
        this.targetPosition = targetPosition;
        this.taskType = taskType;
        this.description = description;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getTargetPosition() { return targetPosition; }
    public void setTargetPosition(Double targetPosition) { this.targetPosition = targetPosition; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
