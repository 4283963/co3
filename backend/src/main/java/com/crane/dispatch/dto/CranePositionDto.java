package com.crane.dispatch.dto;

public class CranePositionDto {
    private String craneId;
    private Double position;
    private Double speed;
    private String updatedAt;

    public CranePositionDto() {}

    public CranePositionDto(String craneId, Double position, Double speed, String updatedAt) {
        this.craneId = craneId;
        this.position = position;
        this.speed = speed;
        this.updatedAt = updatedAt;
    }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getPosition() { return position; }
    public void setPosition(Double position) { this.position = position; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
