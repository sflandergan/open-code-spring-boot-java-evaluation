package de.sfl.devices;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record DeviceDto(
	UUID id,
	String name,
	String description,
	List<SensorDto> sensors,
	Instant createdAt,
	Instant updatedAt
) {
	public static DeviceDto from(Device device) {
		var sensors = device.getDeviceSensors().stream()
				.map(DeviceSensor::getSensor)
				.sorted(Comparator.comparing(sensor -> sensor.getId()))
				.map(SensorDto::from)
				.toList();

		return new DeviceDto(
			device.getId(),
			device.getName(),
			device.getDescription(),
			sensors,
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}
}
