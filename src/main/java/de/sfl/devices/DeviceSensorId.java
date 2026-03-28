package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DeviceSensorId implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "device_id", nullable = false)
	private UUID deviceId;

	@Column(name = "sensor_id", nullable = false)
	private Long sensorId;

	protected DeviceSensorId() {
	}

	public DeviceSensorId(UUID deviceId, Long sensorId) {
		this.deviceId = deviceId;
		this.sensorId = sensorId;
	}

	public UUID getDeviceId() {
		return deviceId;
	}

	public Long getSensorId() {
		return sensorId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var that = (DeviceSensorId) o;
		return Objects.equals(deviceId, that.deviceId) && Objects.equals(sensorId, that.sensorId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(deviceId, sensorId);
	}
}
