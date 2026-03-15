package de.sfl.devices;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateDeviceDto(
	@Size(min = 1, message = "Device name must not be blank")
	String name,

	@Size(min = 1, message = "Device description must not be blank")
	String description
) {
	public void applyTo(Device device) {
		if (name != null) {
			device.setName(name);
		}
		if (description != null) {
			device.setDescription(description);
		}
	}
}
