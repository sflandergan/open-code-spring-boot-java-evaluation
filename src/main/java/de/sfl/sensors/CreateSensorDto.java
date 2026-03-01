package de.sfl.sensors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateSensorDto(
	@NotBlank(message = "Sensor name is required")
	String name,

	@NotBlank(message = "Sensor type is required")
	String type,

	@NotNull(message = "Capabilities must not be null")
	Set<String> capabilities
) {
	public Sensor toEntity() {
		return new Sensor(name, type, capabilities);
	}
}
