package de.sfl.devices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceConfiguration {

	@Bean
	public DeviceService deviceService(
			JpaDeviceRepository deviceRepository,
			JpaDeviceSensorRepository deviceSensorRepository,
			JpaSensorLookupRepository sensorLookupRepository
	) {
		return new DeviceService(deviceRepository, deviceSensorRepository, sensorLookupRepository);
	}
}
