package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceDto(
	@NotBlank(message = "Device name is required")
	String name,

	@NotBlank(message = "Device description is required")
	String description
) {
	public void applyToEntity(Device device) {
		if (name != null) {
			device.setName(name);
		}
		if (description != null) {
			device.setDescription(description);
		}
	}
}