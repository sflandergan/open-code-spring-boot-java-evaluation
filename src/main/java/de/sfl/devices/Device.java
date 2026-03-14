package de.sfl.devices;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DeviceSensor> sensors = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Device() {
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

	public Device(String name, String description) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.description = description;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public List<DeviceSensor> getSensors() {
		return new ArrayList<>(sensors);
	}

	public void setSensors(List<DeviceSensor> sensors) {
		this.sensors = sensors != null ? new ArrayList<>(sensors) : new ArrayList<>();
	}

	protected void addSensor(DeviceSensor deviceSensor) {
		this.sensors.add(deviceSensor);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Device device = (Device) o;
		return Objects.equals(id, device.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Device{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", sensors=" + sensors +
				'}';
	}
}