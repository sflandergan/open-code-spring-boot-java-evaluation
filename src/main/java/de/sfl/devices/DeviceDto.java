package de.sfl.devices;

import de.sfl.sensors.SensorDto;

import java.time.Instant;
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
    public static DeviceDto fromEntity(Device device) {
        return new DeviceDto(
                device.getId(),
                device.getName(),
                device.getDescription(),
                null,
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    public static DeviceDto fromEntityWithSensors(Device device, List<SensorDto> sensors) {
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
