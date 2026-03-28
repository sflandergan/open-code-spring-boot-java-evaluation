package de.sfl.devices;

public class SensorNotFoundException extends RuntimeException {

	public SensorNotFoundException(Long sensorId) {
		super("Sensor with id: '" + sensorId + "' not found");
	}
}
