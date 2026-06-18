package com.crane.dispatch.dto;

public class DispatchTaskRequest {
    private String craneId;
    private Double targetPosition;
    private String taskType;
    private String description;

    public DispatchTaskRequest() {}

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getTargetPosition() { return targetPosition; }
    public void setTargetPosition(Double targetPosition) { this.targetPosition = targetPosition; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
