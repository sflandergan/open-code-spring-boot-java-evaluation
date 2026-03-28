package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
		entityManager.createNativeQuery("DELETE FROM sensors").executeUpdate();
		entityManager.flush();
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Gateway", "Main gateway");

		var savedDevice = deviceRepository.saveAndFlush(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getCreatedAt()).isNotNull();
		assertThat(savedDevice.getUpdatedAt()).isNotNull();

		var foundDevice = deviceRepository.findById(savedDevice.getId());

		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getName()).isEqualTo("Gateway");
		assertThat(foundDevice.get().getDescription()).isEqualTo("Main gateway");
	}

	@Test
	void shouldUpdateDevice() {
		var device = deviceRepository.saveAndFlush(new Device("Gateway", "Main gateway"));

		device.setName("Updated Gateway");
		device.setDescription("Updated description");
		var updatedDevice = deviceRepository.saveAndFlush(device);

		assertThat(updatedDevice.getName()).isEqualTo("Updated Gateway");
		assertThat(updatedDevice.getDescription()).isEqualTo("Updated description");
		assertThat(updatedDevice.getUpdatedAt()).isAfterOrEqualTo(updatedDevice.getCreatedAt());
	}

	@Test
	void shouldDeleteDeviceAndCascadeAssignments() {
		var sensor = persistSensor("Temperature Sensor", "temperature");
		var device = deviceRepository.saveAndFlush(new Device("Gateway", "Main gateway"));
		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensor));

		deviceRepository.deleteAllInBatch();
		entityManager.clear();

		assertThat(deviceRepository.findById(device.getId())).isEmpty();
		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(device.getId(), sensor.getId())).isFalse();
	}

	private Sensor persistSensor(String name, String type) {
		var sensor = new Sensor(name, type, java.util.Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor;
	}
}
