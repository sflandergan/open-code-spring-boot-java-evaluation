package de.sfl.devices;

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
import java.util.UUID;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

	@EmbeddedId
	private DeviceSensorId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("deviceId")
	@JoinColumn(name = "device_id", nullable = false)
	private Device device;

	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(Device device, Long sensorId) {
		this.device = device;
		this.id = new DeviceSensorId(device.getId(), sensorId);
	}

	@PrePersist
	protected void onCreate() {
		assignedAt = Instant.now();
	}

	public DeviceSensorId getId() {
		return id;
	}

	public UUID getDeviceId() {
		return id.getDeviceId();
	}

	public Long getSensorId() {
		return id.getSensorId();
	}

	public Device getDevice() {
		return device;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DeviceSensor that = (DeviceSensor) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "DeviceSensor{" +
				"id=" + id +
				", assignedAt=" + assignedAt +
				'}';
	}
}
