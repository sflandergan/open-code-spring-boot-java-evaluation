package de.sfl.devices;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeviceSensorId implements Serializable {

	private Long deviceId;
	private Long sensorId;

	protected DeviceSensorId() {
	}

	public DeviceSensorId(Long deviceId, Long sensorId) {
		this.deviceId = deviceId;
		this.sensorId = sensorId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public Long getSensorId() {
		return sensorId;
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
