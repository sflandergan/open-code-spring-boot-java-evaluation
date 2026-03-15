package de.sfl.devices;

public class SensorNotFoundException extends RuntimeException {

	public SensorNotFoundException(Long id) {
		super("Sensor with id: '" + id + "' not found");
	}
}
