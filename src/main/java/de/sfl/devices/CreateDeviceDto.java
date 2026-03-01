package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceDto(
    @NotBlank(message = "Device name is required")
    String name,

    @NotBlank(message = "Device description is required")
    String description
) {
    public Device toEntity() {
        return new Device(name, description);
    }
}
