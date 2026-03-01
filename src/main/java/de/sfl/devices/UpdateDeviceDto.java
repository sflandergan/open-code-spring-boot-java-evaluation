package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceDto(
    @NotBlank(message = "Device name must not be blank if provided")
    String name,

    @NotBlank(message = "Device description must not be blank if provided")
    String description
) {
    public void applyToEntity(Device device) {
        if (name != null && !name.isBlank()) {
            device.setName(name);
        }
        if (description != null && !description.isBlank()) {
            device.setDescription(description);
        }
    }
}
