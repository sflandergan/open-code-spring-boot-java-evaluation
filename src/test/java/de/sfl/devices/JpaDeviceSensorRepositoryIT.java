package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Device savedDevice;
	private Sensor savedSensor;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();

		savedDevice = deviceRepository.save(new Device("Test Device", "A device for testing"));
		savedSensor = entityManager.persist(new Sensor("Test Sensor", "temperature", Set.of("read")));
		entityManager.flush();
	}

	@Test
	void shouldSaveDeviceSensorAssignment() {
		var assignment = new DeviceSensor(savedDevice.getId(), savedSensor.getId());

		DeviceSensor saved = deviceSensorRepository.save(assignment);

		assertThat(saved.getDeviceId()).isEqualTo(savedDevice.getId());
		assertThat(saved.getSensorId()).isEqualTo(savedSensor.getId());
		assertThat(saved.getAssignedAt()).isNotNull();
	}

	@Test
	void shouldCheckIfAssignmentExists() {
		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), savedSensor.getId()));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				savedDevice.getId(), savedSensor.getId())).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				savedDevice.getId(), 99999L)).isFalse();
	}

	@Test
	void shouldFindAllAssignmentsByDeviceId() {
		Sensor anotherSensor = entityManager.persist(new Sensor("Another Sensor", "humidity", Set.of("read")));
		entityManager.flush();

		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), savedSensor.getId()));
		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), anotherSensor.getId()));

		List<DeviceSensor> assignments = deviceSensorRepository.findAllByDeviceId(savedDevice.getId());

		assertThat(assignments).hasSize(2);
		assertThat(assignments).extracting(DeviceSensor::getSensorId)
				.containsExactlyInAnyOrder(savedSensor.getId(), anotherSensor.getId());
	}

	@Test
	void shouldReturnEmptyListWhenNoAssignmentsExist() {
		List<DeviceSensor> assignments = deviceSensorRepository.findAllByDeviceId(savedDevice.getId());

		assertThat(assignments).isEmpty();
	}

	@Test
	void shouldDeleteAssignment() {
		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), savedSensor.getId()));

		deviceSensorRepository.deleteById(new DeviceSensorId(savedDevice.getId(), savedSensor.getId()));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				savedDevice.getId(), savedSensor.getId())).isFalse();
	}

	@Test
	void shouldSupportIdempotentAssignment() {
		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), savedSensor.getId()));

		boolean alreadyAssigned = deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				savedDevice.getId(), savedSensor.getId());

		assertThat(alreadyAssigned).isTrue();

		List<DeviceSensor> assignments = deviceSensorRepository.findAllByDeviceId(savedDevice.getId());
		assertThat(assignments).hasSize(1);
	}

	@Test
	void shouldSupportMultipleDevicesSharingSameSensor() {
		Device anotherDevice = deviceRepository.save(new Device("Another Device", "Another device for testing"));

		deviceSensorRepository.save(new DeviceSensor(savedDevice.getId(), savedSensor.getId()));
		deviceSensorRepository.save(new DeviceSensor(anotherDevice.getId(), savedSensor.getId()));

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				savedDevice.getId(), savedSensor.getId())).isTrue();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(
				anotherDevice.getId(), savedSensor.getId())).isTrue();
	}
}
