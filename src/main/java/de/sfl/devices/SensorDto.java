package de.sfl.devices;

import de.sfl.sensors.Sensor;

import java.util.Set;

public record SensorDto(
	Long id,
	String name,
	String type,
	Set<String> capabilities
) {
	public static SensorDto from(Sensor sensor) {
		return new SensorDto(sensor.getId(), sensor.getName(), sensor.getType(), sensor.getCapabilities());
	}
}
