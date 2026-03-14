package de.sfl.devices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceConfiguration {

	@Bean
	public DeviceService deviceService(JpaDeviceRepository jpaDeviceRepository, JpaDeviceSensorRepository jpaDeviceSensorRepository, de.sfl.sensors.SensorService sensorService) {
		return new DeviceService(jpaDeviceRepository, jpaDeviceSensorRepository, sensorService);
	}
}