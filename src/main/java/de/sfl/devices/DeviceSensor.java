package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

    @EmbeddedId
    private DeviceSensorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deviceId")
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected DeviceSensor() {
    }

    public DeviceSensor(Device device, DeviceSensorId id) {
        this.device = device;
        this.id = id;
        this.assignedAt = Instant.now();
    }

    public DeviceSensorId getId() {
        return id;
    }

    public void setId(DeviceSensorId id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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
