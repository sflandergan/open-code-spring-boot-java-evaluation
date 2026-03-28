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

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

	@EmbeddedId
	private DeviceSensorId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("deviceId")
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("sensorId")
	@JoinColumn(name = "sensor_id", nullable = false)
	private Sensor sensor;

	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(Device device, Sensor sensor) {
		this.device = device;
		this.sensor = sensor;
		this.id = new DeviceSensorId(device.getId(), sensor.getId());
	}

	@PrePersist
	protected void onCreate() {
		assignedAt = Instant.now();
	}

	public DeviceSensorId getId() {
		return id;
	}

	public Sensor getSensor() {
		return sensor;
	}
}
