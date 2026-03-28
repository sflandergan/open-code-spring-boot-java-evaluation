package de.sfl.devices;

import jakarta.validation.constraints.AssertTrue;

public record UpdateDeviceDto(
	String name,
	String description
) {
	@AssertTrue(message = "Device name must be non-blank when provided")
	public boolean hasValidName() {
		return name == null || !name.isBlank();
	}

	@AssertTrue(message = "Device description must be non-blank when provided")
	public boolean hasValidDescription() {
		return description == null || !description.isBlank();
	}
}
