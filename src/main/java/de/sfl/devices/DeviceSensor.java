package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

    @EmbeddedId
    private DeviceSensorId id;

    @ManyToOne
    @MapsId("deviceId")
    private Device device;

    @ManyToOne
    @MapsId("sensorId")
    private de.sfl.sensors.Sensor sensor;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    protected DeviceSensor() {
    }

    public DeviceSensor(Device device, de.sfl.sensors.Sensor sensor) {
        this.id = new DeviceSensorId(device.getId(), sensor.getId());
        this.device = device;
        this.sensor = sensor;
    }

    public DeviceSensorId getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public de.sfl.sensors.Sensor getSensor() {
        return sensor;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
