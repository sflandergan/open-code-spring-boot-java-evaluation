package de.sfl.devices;

import de.sfl.sensors.Sensor;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("deviceId")
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("sensorId")
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(Device device, Sensor sensor) {
		this.id = new DeviceSensorId(device.getId(), sensor.getId());
		this.device = device;
		this.sensor = sensor;
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

	public Sensor getSensor() {
		return sensor;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	public boolean hasSensor(Long sensorId) {
		return Objects.equals(id.getSensorId(), sensorId);
	}
}
