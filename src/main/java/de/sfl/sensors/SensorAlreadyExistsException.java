package de.sfl.sensors;

public class SensorAlreadyExistsException extends RuntimeException {

	public SensorAlreadyExistsException(String name) {
		super("Sensor with name: '" + name + "' already exists");
	}
}
