package com.smartcampus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents an IoT sensor installed in a room.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sensor {

    public enum Status {
        ACTIVE, MAINTENANCE, OFFLINE
    }

    private String id;
    private String type;
    private Status status;
    private double currentValue;
    private String roomId;

    public Sensor() {
    }

    public Sensor(String id, String type, Status status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
