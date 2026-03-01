package de.sfl.devices;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeviceDto(
    UUID id,
    String name,
    String description,
    List<SensorSummaryDto> sensors,
    Instant createdAt,
    Instant updatedAt
) {
    public DeviceDto(Device device, List<SensorSummaryDto> sensors) {
        this(device.getId(), device.getName(), device.getDescription(), sensors, device.getCreatedAt(), device.getUpdatedAt());
    }

    public DeviceDto(Device device) {
        this(device, List.of());
    }
}
