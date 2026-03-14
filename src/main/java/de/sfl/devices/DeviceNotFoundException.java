package de.sfl.devices;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

	public DeviceNotFoundException(UUID id) {
		super("Device with id: '" + id + "' not found");
	}
}