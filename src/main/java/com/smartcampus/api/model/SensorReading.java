package com.smartcampus.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single reading captured by a sensor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorReading {

    private String id;
    private long timestamp; // Epoch time (ms)
    private double value;

    public SensorReading() {}

    public SensorReading(String id, double value) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
