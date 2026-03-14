package de.sfl.devices;

import de.sfl.sensors.SensorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceConfiguration {

	@Bean
	public DeviceService deviceService(
			JpaDeviceRepository deviceRepository,
			JpaDeviceSensorRepository deviceSensorRepository,
			SensorService sensorService) {
		return new DeviceService(deviceRepository, deviceSensorRepository, sensorService);
	}

	@Bean
	public DeviceController deviceController(DeviceService deviceService, SensorService sensorService) {
		return new DeviceController(deviceService, sensorService);
	}
}
