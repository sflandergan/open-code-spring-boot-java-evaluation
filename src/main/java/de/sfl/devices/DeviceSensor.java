package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

	@EmbeddedId
	private DeviceSensorId id;

	@Column(name = "assigned_at", nullable = false, updatable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(DeviceSensorId id) {
		this.id = id;
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

	public Instant getAssignedAt() {
		return assignedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var that = (DeviceSensor) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
