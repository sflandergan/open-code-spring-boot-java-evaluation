package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceDto(
        @NotBlank(message = "Device name must not be blank", groups = NonBlankGroup.class)
        String name,

        @NotBlank(message = "Device description must not be blank", groups = NonBlankGroup.class)
        String description
) {
    public interface NonBlankGroup {
    }

    public void applyToEntity(Device device) {
        if (name != null && !name.isBlank()) {
            device.setName(name);
        }
        if (description != null && !description.isBlank()) {
            device.setDescription(description);
        }
    }
}
