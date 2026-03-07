package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record UpdateDeviceDto(
		@NotBlank String name,
		@NotBlank String description
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
