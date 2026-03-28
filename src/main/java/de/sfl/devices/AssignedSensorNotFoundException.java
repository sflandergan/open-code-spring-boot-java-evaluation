package de.sfl.devices;

public class AssignedSensorNotFoundException extends RuntimeException {

	public AssignedSensorNotFoundException(Long sensorId) {
		super("Sensor with id " + sensorId + " not found");
	}
}
