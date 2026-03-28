package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;

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
		var device = createDevice("Gateway", "Factory gateway");
		var sensorId = createSensor("Temperature");
		var assignmentId = new DeviceSensorId(device.getId(), sensorId);

		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensorId));

		var foundAssignment = deviceSensorRepository.findById(assignmentId);

		assertThat(foundAssignment).isPresent();
		assertThat(foundAssignment.get().getDeviceId()).isEqualTo(device.getId());
		assertThat(foundAssignment.get().getSensorId()).isEqualTo(sensorId);
		assertThat(foundAssignment.get().getAssignedAt()).isNotNull();
	}

	@Test
	void shouldCheckIfAssignmentExistsByDeviceIdAndSensorId() {
		var device = createDevice("Gateway", "Factory gateway");
		var sensorId = createSensor("Temperature");
		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensorId));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensorId)).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), 999L)).isFalse();
	}

	@Test
	void shouldFindAllAssignmentsByDeviceId() {
		var device = createDevice("Gateway", "Factory gateway");
		var sensorOneId = createSensor("Temperature");
		var sensorTwoId = createSensor("Humidity");

		deviceSensorRepository.save(new DeviceSensor(device, sensorOneId));
		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensorTwoId));

		var assignments = deviceSensorRepository.findByIdDeviceId(device.getId());

		assertThat(assignments).hasSize(2);
		assertThat(assignments).extracting(DeviceSensor::getSensorId)
				.containsExactlyInAnyOrder(sensorOneId, sensorTwoId);
	}

	@Test
	void shouldDeleteAssignmentByCompositeKey() {
		var device = createDevice("Gateway", "Factory gateway");
		var sensorId = createSensor("Temperature");
		var assignmentId = new DeviceSensorId(device.getId(), sensorId);

		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensorId));
		deviceSensorRepository.deleteById(assignmentId);

		assertThat(deviceSensorRepository.findById(assignmentId)).isEmpty();
	}

	@Test
	void shouldFailToSaveAssignmentWhenSensorDoesNotExist() {
		var device = createDevice("Gateway", "Factory gateway");
		var missingSensorId = 999_999L;

		assertThatThrownBy(() -> deviceSensorRepository.saveAndFlush(new DeviceSensor(device, missingSensorId)))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Device createDevice(String name, String description) {
		return deviceRepository.save(new Device(name, description));
	}

	private Long createSensor(String name) {
		var sensor = new Sensor(name, "sensor", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor.getId();
	}
}
