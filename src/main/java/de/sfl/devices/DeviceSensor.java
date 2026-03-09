package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

    @EmbeddedId
    private DeviceSensorId id;

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "device_id", nullable = false, insertable = false, updatable = false)
    private Device device;

    @Column(name = "sensor_id", nullable = false, insertable = false, updatable = false)
    private Long sensorId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    protected DeviceSensor() {
    }

    public DeviceSensor(Device device, Long sensorId) {
        this.device = device;
        this.sensorId = sensorId;
        this.id = new DeviceSensorId(device.getId(), sensorId);
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }

    public DeviceSensorId getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Long getSensorId() {
        return sensorId;
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
        return Objects.hash(id);
    }
}
