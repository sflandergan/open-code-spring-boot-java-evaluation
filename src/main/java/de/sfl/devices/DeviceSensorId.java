package de.sfl.devices;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key for DeviceSensor.
 */
public class DeviceSensorId implements Serializable {

    private java.util.UUID deviceId;
    private java.util.UUID sensorId;

    // Default constructor
    public DeviceSensorId() {}

    public DeviceSensorId(UUID deviceId, UUID sensorId) {
        this.deviceId = deviceId;
        this.sensorId = sensorId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceSensorId that)) return false;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(sensorId, that.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, sensorId);
    }

    @Override
    public String toString() {
        return "DeviceSensorId(deviceId=" + deviceId + ", sensorId=" + sensorId + ")";
    }
}