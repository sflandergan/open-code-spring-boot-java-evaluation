package de.sfl.sensors;

import java.util.Set;

public record SensorDto(
		Long id,
		String name,
		String type,
		Set<String> capabilities
) {
}
