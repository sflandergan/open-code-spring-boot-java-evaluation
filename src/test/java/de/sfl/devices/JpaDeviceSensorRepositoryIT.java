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
	void shouldSaveAssignmentWithCompositeKey() {
		var device = deviceRepository.save(new Device("Weather Station", "Outdoor monitoring unit"));
		var sensor = persistSensor("Humidity Sensor");

		var assignment = deviceSensorRepository.save(new DeviceSensor(device, sensor));

		assertThat(assignment.getId().getDeviceId()).isEqualTo(device.getId());
		assertThat(assignment.getId().getSensorId()).isEqualTo(sensor.getId());
		assertThat(assignment.getAssignedAt()).isNotNull();
	}

	@Test
	void shouldCheckIfAssignmentExists() {
		var device = deviceRepository.save(new Device("Weather Station", "Outdoor monitoring unit"));
		var sensor = persistSensor("Humidity Sensor");
		deviceSensorRepository.save(new DeviceSensor(device, sensor));

		assertThat(deviceSensorRepository.existsAssignment(device.getId(), sensor.getId())).isTrue();
		assertThat(deviceSensorRepository.existsAssignment(device.getId(), 999L)).isFalse();
	}

	@Test
	void shouldCascadeDeleteAssignmentsWhenDeviceIsDeleted() {
		var device = deviceRepository.save(new Device("Weather Station", "Outdoor monitoring unit"));
		var sensor = persistSensor("Humidity Sensor");
		deviceSensorRepository.save(new DeviceSensor(device, sensor));

		entityManager.createNativeQuery("DELETE FROM devices WHERE id = :id")
				.setParameter("id", device.getId())
				.executeUpdate();
		entityManager.clear();

		assertThat(deviceSensorRepository.existsAssignment(device.getId(), sensor.getId())).isFalse();
	}

	private Sensor persistSensor(String name) {
		var sensor = new Sensor(name, "sensor", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor;
	}
}
