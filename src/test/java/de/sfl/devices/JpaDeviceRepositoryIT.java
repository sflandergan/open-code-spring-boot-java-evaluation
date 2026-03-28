package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

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
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Device A", "Description A");

		var saved = deviceRepository.save(device);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getName()).isEqualTo("Device A");
		assertThat(saved.getDescription()).isEqualTo("Description A");
		assertThat(deviceRepository.findById(saved.getId())).isPresent();
	}

	@Test
	void shouldDeleteDeviceAndCascadeAssignments() {
		var sensor = new Sensor("Sensor A", "temperature", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		var device = deviceRepository.save(new Device("Device A", "Description A"));
		device.assignSensor(sensor);
		var savedDevice = deviceRepository.save(device);

		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(savedDevice.getId(), sensor.getId())).isTrue();

		deviceRepository.delete(savedDevice);

		assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
		assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(savedDevice.getId(), sensor.getId())).isFalse();
	}
}
