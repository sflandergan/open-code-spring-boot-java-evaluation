package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

    @EmbeddedId
    private DeviceSensorId id;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected DeviceSensor() {
    }

    public DeviceSensor(DeviceSensorId id) {
        this.id = id;
        this.assignedAt = Instant.now();
    }

    public DeviceSensor(UUID deviceId, Long sensorId) {
        this.id = new DeviceSensorId(deviceId, sensorId);
        this.assignedAt = Instant.now();
    }

    public DeviceSensorId getId() {
        return id;
    }

    public void setId(DeviceSensorId id) {
        this.id = id;
    }

    public UUID getDeviceId() {
        return id.getDeviceId();
    }

    public Long getSensorId() {
        return id.getSensorId();
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceSensor that = (DeviceSensor) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DeviceSensor{" +
                "id=" + id +
                ", assignedAt=" + assignedAt +
                '}';
    }
}