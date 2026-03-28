package de.sfl.devices;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeviceDto(
	UUID id,
	String name,
	String description,
	List<Long> sensorIds,
	Instant createdAt,
	Instant updatedAt
) {
	public static DeviceDto from(Device device, List<Long> sensorIds) {
		return new DeviceDto(
			device.getId(),
			device.getName(),
			device.getDescription(),
			List.copyOf(sensorIds),
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}
}
