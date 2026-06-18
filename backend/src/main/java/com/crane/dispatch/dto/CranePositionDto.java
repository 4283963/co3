package com.crane.dispatch.dto;

public class CranePositionDto {
    private String craneId;
    private Double position;
    private Double targetPosition;
    private Double speed;
    private String updatedAt;

    public CranePositionDto() {}

    public CranePositionDto(String craneId, Double position, Double targetPosition, Double speed, String updatedAt) {
        this.craneId = craneId;
        this.position = position;
        this.targetPosition = targetPosition;
        this.speed = speed;
        this.updatedAt = updatedAt;
    }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getPosition() { return position; }
    public void setPosition(Double position) { this.position = position; }

    public Double getTargetPosition() { return targetPosition; }
    public void setTargetPosition(Double targetPosition) { this.targetPosition = targetPosition; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
