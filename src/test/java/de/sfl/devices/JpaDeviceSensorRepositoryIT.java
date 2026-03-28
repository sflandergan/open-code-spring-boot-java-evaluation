package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
		entityManager.createQuery("DELETE FROM Sensor").executeUpdate();
	}

	@Test
	void shouldPersistDeviceSensorAssignmentWithCompositeKey() {
		var sensor = new Sensor("Sensor A", "temperature", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();

		var device = deviceRepository.save(new Device("Device A", "Description"));
		var assignment = new DeviceSensor(device, sensor);
		var savedAssignment = deviceSensorRepository.save(assignment);

		assertThat(savedAssignment.getId()).isNotNull();
		assertThat(savedAssignment.getId().getDeviceId()).isEqualTo(device.getId());
		assertThat(savedAssignment.getId().getSensorId()).isEqualTo(sensor.getId());
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensor.getId())).isTrue();
	}
}
