package de.sfl.sensors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensorConfiguration {

	@Bean
	public SensorService sensorService(JpaSensorRepository sensorRepository) {
		return new SensorService(sensorRepository);
	}
}
