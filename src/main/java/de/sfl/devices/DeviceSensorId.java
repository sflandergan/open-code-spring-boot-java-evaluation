package de.sfl.devices;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DeviceSensorId implements java.io.Serializable {
    private java.util.UUID deviceId;
    private Long sensorId;   // Assuming sensor ID is UUID

    public DeviceSensorId() {
    }

    public DeviceSensorId(UUID deviceId, Long sensorId) {
        this.deviceId = deviceId;
        this.sensorId = sensorId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceSensorId that = (DeviceSensorId) o;
        return Objects.equals(deviceId, that.deviceId) &&
               Objects.equals(sensorId, that.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, sensorId);
    }
}