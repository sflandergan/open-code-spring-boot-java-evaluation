package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Long testDeviceId;
	private Long testSensorId;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();

		// Clean up sensors created via JDBC
		jdbcTemplate.update("DELETE FROM sensor_capabilities WHERE sensor_id IN (SELECT id FROM sensors WHERE name LIKE 'TestSensor%')");
		jdbcTemplate.update("DELETE FROM sensors WHERE name LIKE 'TestSensor%'");

		// Create test device
		Device device = deviceRepository.save(new Device("Test Device", "Test Description"));
		testDeviceId = device.getId();

		// Create test sensor via JDBC (since repository is package-protected)
		jdbcTemplate.update(
			"INSERT INTO sensors (name, type, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
			"TestSensor_" + System.currentTimeMillis(), "temperature"
		);
		testSensorId = jdbcTemplate.queryForObject(
			"SELECT id FROM sensors WHERE name LIKE 'TestSensor%' ORDER BY id DESC LIMIT 1",
			Long.class
		);
	}

	@Test
	void shouldSaveAndFindDeviceSensorById() {
		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		var deviceSensor = new DeviceSensor(device, testSensorId);

		DeviceSensor savedDeviceSensor = deviceSensorRepository.save(deviceSensor);

		assertThat(savedDeviceSensor.getId()).isNotNull();
		assertThat(savedDeviceSensor.getId().getDeviceId()).isEqualTo(testDeviceId);
		assertThat(savedDeviceSensor.getId().getSensorId()).isEqualTo(testSensorId);

		Optional<DeviceSensor> foundDeviceSensor = deviceSensorRepository.findById(savedDeviceSensor.getId());
		assertThat(foundDeviceSensor).isPresent();
	}

	@Test
	void shouldCheckIfDeviceSensorAssignmentExists() {
		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		var deviceSensor = new DeviceSensor(device, testSensorId);
		deviceSensorRepository.save(deviceSensor);

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(testDeviceId, testSensorId)).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(testDeviceId, 999L)).isFalse();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(999L, testSensorId)).isFalse();
	}

	@Test
	void shouldDeleteDeviceSensor() {
		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		var deviceSensor = new DeviceSensor(device, testSensorId);
		DeviceSensor savedDeviceSensor = deviceSensorRepository.save(deviceSensor);

		deviceSensorRepository.deleteById(savedDeviceSensor.getId());

		assertThat(deviceSensorRepository.findById(savedDeviceSensor.getId())).isEmpty();
	}

	@Test
	void shouldFindAllDeviceSensors() {
		// Create second sensor
		jdbcTemplate.update(
			"INSERT INTO sensors (name, type, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
			"TestSensor2_" + System.currentTimeMillis(), "humidity"
		);
		Long secondSensorId = jdbcTemplate.queryForObject(
			"SELECT id FROM sensors WHERE name LIKE 'TestSensor2%' ORDER BY id DESC LIMIT 1",
			Long.class
		);

		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		deviceSensorRepository.save(new DeviceSensor(device, testSensorId));
		deviceSensorRepository.save(new DeviceSensor(device, secondSensorId));

		List<DeviceSensor> deviceSensors = deviceSensorRepository.findAll();

		assertThat(deviceSensors).hasSize(2);
	}

	@Test
	void shouldReturnEmptyListWhenNoDeviceSensorsExist() {
		assertThat(deviceSensorRepository.findAll()).isEmpty();
	}

	@Test
	void shouldReturnEmptyOptionalWhenDeviceSensorNotFound() {
		var compositeId = new DeviceSensorId(999L, 999L);
		assertThat(deviceSensorRepository.findById(compositeId)).isEmpty();
	}

	@Test
	void shouldCascadeDeleteWhenDeviceIsDeleted() {
		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		deviceSensorRepository.save(new DeviceSensor(device, testSensorId));

		assertThat(deviceSensorRepository.findAll()).hasSize(1);

		deviceRepository.delete(device);

		assertThat(deviceSensorRepository.findAll()).isEmpty();
	}

	@Test
	void shouldCascadeDeleteWhenSensorIsDeleted() {
		var device = deviceRepository.findById(testDeviceId).orElseThrow();
		deviceSensorRepository.save(new DeviceSensor(device, testSensorId));

		assertThat(deviceSensorRepository.findAll()).hasSize(1);

		// Delete sensor via JDBC (cascade should delete the assignment)
		jdbcTemplate.update("DELETE FROM sensors WHERE id = ?", testSensorId);

		assertThat(deviceSensorRepository.findAll()).isEmpty();
	}
}
