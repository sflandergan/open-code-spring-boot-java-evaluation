package de.sfl.devices;

import de.sfl.sensors.Sensor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DeviceDto(
	UUID id,
	String name,
	String description,
	List<AssignedSensorDto> sensors,
	Instant createdAt,
	Instant updatedAt
) {
	public static DeviceDto fromEntity(Device device) {
		return new DeviceDto(
			device.getId(),
			device.getName(),
			device.getDescription(),
			device.getSensors().stream().map(AssignedSensorDto::fromEntity).toList(),
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}

	public record AssignedSensorDto(
		Long id,
		String name,
		String type,
		Set<String> capabilities
	) {
		static AssignedSensorDto fromEntity(Sensor sensor) {
			return new AssignedSensorDto(sensor.getId(), sensor.getName(), sensor.getType(), sensor.getCapabilities());
		}
	}
}
