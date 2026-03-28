package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Weather Station", "Outdoor monitoring unit");

		var savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(deviceRepository.findById(savedDevice.getId())).isPresent();
		assertThat(deviceRepository.findById(savedDevice.getId()).orElseThrow().getName()).isEqualTo("Weather Station");
	}

	@Test
	void shouldUpdateDevice() {
		var device = deviceRepository.save(new Device("Weather Station", "Outdoor monitoring unit"));

		device.setName("Edge Gateway");
		device.setDescription("Updated description");

		var updatedDevice = deviceRepository.save(device);

		assertThat(updatedDevice.getName()).isEqualTo("Edge Gateway");
		assertThat(updatedDevice.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void shouldDeleteDevice() {
		var device = deviceRepository.save(new Device("Weather Station", "Outdoor monitoring unit"));

		deviceRepository.deleteById(device.getId());

		assertThat(deviceRepository.findById(device.getId())).isEmpty();
	}

	@Test
	void shouldReturnEmptyWhenDeviceDoesNotExist() {
		assertThat(deviceRepository.findById(UUID.randomUUID())).isEmpty();
	}
}
