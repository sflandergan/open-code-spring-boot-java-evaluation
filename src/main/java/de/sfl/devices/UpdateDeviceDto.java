package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceDto(
	@NotBlank(message = "Device name cannot be blank")
	String name,

	@NotBlank(message = "Device description cannot be blank")
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
