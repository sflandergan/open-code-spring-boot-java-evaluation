package de.sfl.devices.test-data;

import de.sfl.devices.model.Device;

public class DeviceTestData {

	public static Device createDevice() {
		return new Device("Test Device", "TypeA", "1234567890");
	}

	public static Device createDeviceWithId(Long id) {
		Device device = createDevice();
		device.setId(id);
		return device;
	}
}
