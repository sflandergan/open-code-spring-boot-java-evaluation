package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();
	}

	@Test
	void shouldSaveAndFindAssignmentByCompositeKey() {
		var sensorId = persistSensor();
		var device = deviceRepository.save(new Device("Device-001", "Main floor device"));

		deviceSensorRepository.save(new DeviceSensor(device, sensorId));

		var id = new DeviceSensorId(device.getId(), sensorId);
		Optional<DeviceSensor> savedAssignment = deviceSensorRepository.findById(id);

		assertThat(savedAssignment).isPresent();
		assertThat(savedAssignment.get().getSensorId()).isEqualTo(sensorId);
	}

	@Test
	void shouldCheckAssignmentExistsByDeviceIdAndSensorId() {
		var sensorId = persistSensor();
		var device = deviceRepository.save(new Device("Device-001", "Main floor device"));
		deviceSensorRepository.save(new DeviceSensor(device, sensorId));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId)).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId + 1)).isFalse();
	}

	@Test
	void shouldFindAllAssignmentsByDeviceId() {
		var sensorIdA = persistSensor();
		var sensorIdB = persistSensor();
		var device = deviceRepository.save(new Device("Device-001", "Main floor device"));

		deviceSensorRepository.save(new DeviceSensor(device, sensorIdA));
		deviceSensorRepository.save(new DeviceSensor(device, sensorIdB));

		var assignments = deviceSensorRepository.findAllByIdDeviceId(device.getId());

		assertThat(assignments).hasSize(2);
		assertThat(assignments)
			.extracting(DeviceSensor::getSensorId)
			.containsExactlyInAnyOrder(sensorIdA, sensorIdB);
	}

	@Test
	void shouldCheckSensorExistsById() {
		var sensorId = persistSensor();

		assertThat(deviceSensorRepository.existsSensorById(sensorId)).isTrue();
		assertThat(deviceSensorRepository.existsSensorById(sensorId + 99)).isFalse();
	}

	@Test
	void shouldEnforceForeignKeyConstraintWhenDeviceDoesNotExist() {
		var sensorId = persistSensor();
		var unknownDevice = createDevice(UUID.randomUUID(), "Device-unknown", "Unknown");

		assertThatThrownBy(() -> deviceSensorRepository.saveAndFlush(new DeviceSensor(unknownDevice, sensorId)))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Long persistSensor() {
		var sensor = new Sensor("Sensor-" + UUID.randomUUID(), "sensor", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor.getId();
	}

	private Device createDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
