package de.sfl.sensors;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "sensors")
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@ElementCollection
	@CollectionTable(name = "sensor_capabilities", joinColumns = @JoinColumn(name = "sensor_id"))
	@Column(name = "capability")
	private Set<String> capabilities = new HashSet<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Sensor() {
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

	public Sensor(String name, String type, Set<String> capabilities) {
		this.name = name;
		this.type = type;
		this.capabilities = capabilities != null ? new HashSet<>(capabilities) : new HashSet<>();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getCapabilities() {
		return new HashSet<>(capabilities);
	}

	public void setCapabilities(Set<String> capabilities) {
		this.capabilities = capabilities != null ? new HashSet<>(capabilities) : new HashSet<>();
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
		Sensor sensor = (Sensor) o;
		return Objects.equals(id, sensor.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Sensor{" +
				"id=" + id +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				", capabilities=" + capabilities +
				'}';
	}
}
