package de.sfl.devices;

import de.sfl.sensors.SensorDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record DeviceDto(
	UUID id,
	String name,
	String description,
	List<SensorDto> sensors,
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Instant createdAt,
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	Instant updatedAt
) {
	public static DeviceDto from(Device device) {
		return new DeviceDto(
			device.getId(),
			device.getName(),
			device.getDescription(),
			toSensorDtos(device),
			device.getCreatedAt(),
			device.getUpdatedAt()
		);
	}

	private static List<SensorDto> toSensorDtos(Device device) {
		return device.getSensorAssignments().stream()
			.map(DeviceSensor::getSensor)
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(sensor -> sensor.getId() == null ? Long.MAX_VALUE : sensor.getId()))
			.map(sensor -> new SensorDto(sensor.getId(), sensor.getName(), sensor.getType(), sensor.getCapabilities()))
			.toList();
	}
}
