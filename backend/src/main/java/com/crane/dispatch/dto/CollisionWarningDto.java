package com.crane.dispatch.dto;

public class CollisionWarningDto {
    private boolean safe;
    private Double craneAPosition;
    private Double craneBPosition;
    private Double distance;
    private String message;

    public CollisionWarningDto() {}

    public boolean isSafe() { return safe; }
    public void setSafe(boolean safe) { this.safe = safe; }

    public Double getCraneAPosition() { return craneAPosition; }
    public void setCraneAPosition(Double craneAPosition) { this.craneAPosition = craneAPosition; }

    public Double getCraneBPosition() { return craneBPosition; }
    public void setCraneBPosition(Double craneBPosition) { this.craneBPosition = craneBPosition; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
