package de.sfl.devices;

import de.sfl.sensors.Sensor;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "device_sensors")
@IdClass(DeviceSensorId.class)
public class DeviceSensor {

    // no hope to find primary key fields warning suppressed.
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", referencedColumnName = "id", nullable = false)
    private Device device;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", referencedColumnName = "id")
    private Sensor sensor;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    // JPA requires a no-arg constructor
    protected DeviceSensor() {
    }

    public DeviceSensor(Device device, Sensor sensor) {
        this.device = device;
        this.sensor = sensor;
        this.assignedAt = Instant.now();
    }
    
    public DeviceSensor(Device device, Sensor sensor, Instant assignedAt) {
        this.device = device;
        this.sensor = sensor;
        this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
    }

    public Device getDevice() {
        return device;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }

    // Override equals and hashCode for the composite PK
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceSensor that = (DeviceSensor) o;
        return Objects.equals(device, that.device) && Objects.equals(sensor, that.sensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, sensor);
    }
}