package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

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
	void shouldSaveAndFindAssignmentByCompositeKey() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));
		var sensorId = insertSensor("Sensor A", "sensor");
		var assignmentId = new DeviceSensorId(device.getId(), sensorId);

		var savedAssignment = deviceSensorRepository.save(new DeviceSensor(assignmentId));

		assertThat(savedAssignment.getId()).isEqualTo(assignmentId);
		assertThat(savedAssignment.getAssignedAt()).isNotNull();
		assertThat(deviceSensorRepository.findById(assignmentId)).isPresent();
	}

	@Test
	void shouldCheckAssignmentExistenceByDeviceAndSensor() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));
		var sensorId = insertSensor("Sensor A", "sensor");

		deviceSensorRepository.save(new DeviceSensor(new DeviceSensorId(device.getId(), sensorId)));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId)).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId + 1)).isFalse();
	}

	@Test
	void shouldFindAssignmentsByDeviceId() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));
		var sensorId1 = insertSensor("Sensor A", "sensor");
		var sensorId2 = insertSensor("Sensor B", "actuator");

		deviceSensorRepository.save(new DeviceSensor(new DeviceSensorId(device.getId(), sensorId1)));
		deviceSensorRepository.save(new DeviceSensor(new DeviceSensorId(device.getId(), sensorId2)));

		var assignments = deviceSensorRepository.findByIdDeviceId(device.getId());

		assertThat(assignments).hasSize(2);
		assertThat(assignments)
			.extracting(assignment -> assignment.getId().getSensorId())
			.containsExactlyInAnyOrder(sensorId1, sensorId2);
	}

	@Test
	void shouldCheckSensorExists() {
		var sensorId = insertSensor("Sensor A", "sensor");

		assertThat(deviceSensorRepository.sensorExists(sensorId)).isTrue();
		assertThat(deviceSensorRepository.sensorExists(sensorId + 1)).isFalse();
	}

	@Test
	void shouldFailWhenSensorDoesNotExist() {
		var device = deviceRepository.save(new Device("Device A", "Primary device"));

		assertThatThrownBy(() -> deviceSensorRepository.saveAndFlush(
			new DeviceSensor(new DeviceSensorId(device.getId(), 99999L))
		))
			.isInstanceOf(DataIntegrityViolationException.class);
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
