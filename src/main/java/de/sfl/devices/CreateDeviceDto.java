package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceDto(
		@NotBlank String name,
		@NotBlank String description
) {
	public Device toEntity() {
		return new Device(name, description);
	}
}
