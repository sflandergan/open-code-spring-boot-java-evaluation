package de.sfl.devices;

import de.sfl.sensors.SensorDto;

import java.time.Instant;
import java.util.List;

public record DeviceDto(
	Long id,
	String name,
	String description,
	List<SensorDto> sensors,
	Instant createdAt,
	Instant updatedAt
) {
	public DeviceDto(Device device, List<SensorDto> sensors) {
		this(
			device.getId(),
			device.getName(),
			device.getDescription(),
			sensors,
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}

	public DeviceDto(Device device) {
		this(
			device.getId(),
			device.getName(),
			device.getDescription(),
			null,
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}
}
