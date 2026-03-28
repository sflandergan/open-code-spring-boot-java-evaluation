package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();
		jdbcTemplate.update("DELETE FROM sensor_capabilities");
		jdbcTemplate.update("DELETE FROM sensors");
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Device A", "Primary device");

		var savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Device A");
		assertThat(savedDevice.getDescription()).isEqualTo("Primary device");
		assertThat(savedDevice.getCreatedAt()).isNotNull();
		assertThat(savedDevice.getUpdatedAt()).isNotNull();

		var foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.orElseThrow().getName()).isEqualTo("Device A");
	}

	@Test
	void shouldUpdateDevice() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));

		device.setName("Updated Device");
		device.setDescription("Updated description");

		var updatedDevice = deviceRepository.save(device);

		assertThat(updatedDevice.getName()).isEqualTo("Updated Device");
		assertThat(updatedDevice.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void shouldDeleteDeviceAndCascadeAssignments() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));
		var sensorId = insertSensor("Sensor A", "sensor");

		deviceSensorRepository.save(new DeviceSensor(new DeviceSensorId(device.getId(), sensorId)));
		deviceSensorRepository.flush();

		deviceRepository.deleteById(device.getId());
		deviceRepository.flush();

		assertThat(deviceRepository.findById(device.getId())).isEmpty();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId)).isFalse();
	}

	@Test
	void shouldReturnEmptyWhenDeviceDoesNotExist() {
		assertThat(deviceRepository.findById(UUID.randomUUID())).isEmpty();
	}

	private Long insertSensor(String name, String type) {
		return jdbcTemplate.queryForObject(
			"INSERT INTO sensors(name, type) VALUES (?, ?) RETURNING id",
			Long.class,
			name,
			type
		);
	}
}
