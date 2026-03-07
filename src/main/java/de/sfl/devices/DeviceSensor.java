package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

	@EmbeddedId
	private DeviceSensorId id;

	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(UUID deviceId, Long sensorId) {
		this.id = new DeviceSensorId(deviceId, sensorId);
	}

	@PrePersist
	protected void onCreate() {
		assignedAt = Instant.now();
	}

	public UUID getDeviceId() {
		return id.getDeviceId();
	}

	public Long getSensorId() {
		return id.getSensorId();
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}
}
