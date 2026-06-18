package com.crane.dispatch.dto;

public class HistoryPointDto {
    private String craneId;
    private Double position;
    private Double speed;
    private String timestamp;

    public HistoryPointDto() {}

    public HistoryPointDto(String craneId, Double position, Double speed, String timestamp) {
        this.craneId = craneId;
        this.position = position;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public String getCraneId() { return craneId; }
    public void setCraneId(String craneId) { this.craneId = craneId; }

    public Double getPosition() { return position; }
    public void setPosition(Double position) { this.position = position; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
