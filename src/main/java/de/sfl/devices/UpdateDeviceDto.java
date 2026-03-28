package de.sfl.devices;

import jakarta.validation.constraints.Pattern;

public record UpdateDeviceDto(
	@Pattern(regexp = ".*\\S.*", message = "Device name must not be blank")
	String name,

	@Pattern(regexp = ".*\\S.*", message = "Device description must not be blank")
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
