package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

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
		entityManager.createNativeQuery("DELETE FROM sensors").executeUpdate();
		entityManager.flush();
	}

	@Test
	void shouldSaveAndFindAssignmentById() {
		var sensor = persistSensor("Temperature Sensor", "temperature");
		var device = deviceRepository.saveAndFlush(new Device("Gateway", "Main gateway"));
		var assignment = deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensor));

		var foundAssignment = deviceSensorRepository.findById(assignment.getId());

		assertThat(foundAssignment).isPresent();
		assertThat(foundAssignment.get().getId().getDeviceId()).isEqualTo(device.getId());
		assertThat(foundAssignment.get().getId().getSensorId()).isEqualTo(sensor.getId());
		assertThat(foundAssignment.get().getAssignedAt()).isNotNull();
	}

	@Test
	void shouldCheckWhetherAssignmentExists() {
		var sensor = persistSensor("Temperature Sensor", "temperature");
		var device = deviceRepository.saveAndFlush(new Device("Gateway", "Main gateway"));
		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensor));

		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(device.getId(), sensor.getId())).isTrue();
		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(device.getId(), sensor.getId() + 1)).isFalse();
	}

	@Test
	void shouldEnforceForeignKeyConstraintForSensor() {
		var device = deviceRepository.saveAndFlush(new Device("Gateway", "Main gateway"));
		var missingSensor = entityManager.getReference(Sensor.class, 999L);
		var invalidAssignment = new DeviceSensor(device, missingSensor);

		assertThatThrownBy(() -> deviceSensorRepository.saveAndFlush(invalidAssignment))
			.isInstanceOfAny(
				DataIntegrityViolationException.class,
				JpaObjectRetrievalFailureException.class,
				EntityNotFoundException.class
			);
	}

	private Sensor persistSensor(String name, String type) {
		var sensor = new Sensor(name, type, java.util.Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor;
	}
}
