package de.sfl.devices;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {

	@Id
	private DeviceSensorId id;

	@ManyToOne
	@MapsId("deviceId")
	@JoinColumn(name = "device_id")
	private Device device;

	@ManyToOne
	@MapsId("sensorId")
	@JoinColumn(name = "sensor_id")
	private de.sfl.sensors.Sensor sensor;

	@Column(name = "assigned_at", nullable = false)
	private Instant assignedAt;

	protected DeviceSensor() {
	}

	public DeviceSensor(Device device, de.sfl.sensors.Sensor sensor) {
		this.device = device;
		this.sensor = sensor;
		this.id = new DeviceSensorId(device.getId(), sensor.getId());
		this.assignedAt = Instant.now();
	}

	public DeviceSensorId getId() {
		return id;
	}

	public void setId(DeviceSensorId id) {
		this.id = id;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public de.sfl.sensors.Sensor getSensor() {
		return sensor;
	}

	public void setSensor(de.sfl.sensors.Sensor sensor) {
		this.sensor = sensor;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(Instant assignedAt) {
		this.assignedAt = assignedAt;
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