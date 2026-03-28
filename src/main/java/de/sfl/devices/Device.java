package de.sfl.devices;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	protected UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DeviceSensor> assignments = new LinkedHashSet<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Device() {
	}

	public Device(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@PrePersist
	protected void onCreate() {
		var now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Set<DeviceSensor> getAssignments() {
		return Set.copyOf(assignments);
	}

	public boolean assignSensor(Long sensorId) {
		if (hasSensorAssigned(sensorId)) {
			return false;
		}

		assignments.add(new DeviceSensor(this, sensorId));
		return true;
	}

	private boolean hasSensorAssigned(Long sensorId) {
		return assignments.stream()
			.map(DeviceSensor::getSensorId)
			.anyMatch(sensorId::equals);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var device = (Device) o;
		return Objects.equals(id, device.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
