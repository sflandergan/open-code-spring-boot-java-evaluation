package de.sfl.devices;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeviceSensorId implements Serializable {
    private java.util.UUID deviceId;
    private Long sensorId;

    protected DeviceSensorId() {
    }

    public DeviceSensorId(java.util.UUID deviceId, Long sensorId) {
        this.deviceId = deviceId;
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
