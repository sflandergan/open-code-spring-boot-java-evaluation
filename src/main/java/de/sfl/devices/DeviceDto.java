package de.sfl.devices;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeviceDto(
	UUID id,
	String name,
	String description,
	List<AssignedSensorDto> sensors,
	Instant createdAt,
	Instant updatedAt
) {

	public record AssignedSensorDto(
		Long sensorId,
		Instant assignedAt
	) {
	}
}
