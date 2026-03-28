package de.sfl.devices;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceConfiguration {

	@Bean
	public DeviceService deviceService(
		JpaDeviceRepository deviceRepository,
		JpaDeviceSensorRepository deviceSensorRepository,
		EntityManager entityManager
	) {
		return new DeviceService(deviceRepository, deviceSensorRepository, entityManager);
	}
}
