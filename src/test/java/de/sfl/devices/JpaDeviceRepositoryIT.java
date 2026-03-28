package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Device-001", "Main floor device");

		var savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Device-001");

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getDescription()).isEqualTo("Main floor device");
	}

	@Test
	void shouldUpdateDevice() {
		var device = new Device("Device-001", "Main floor device");
		var savedDevice = deviceRepository.save(device);

		savedDevice.setName("Device-002");
		savedDevice.setDescription("Second floor device");

		var updatedDevice = deviceRepository.save(savedDevice);

		assertThat(updatedDevice.getName()).isEqualTo("Device-002");
		assertThat(updatedDevice.getDescription()).isEqualTo("Second floor device");
	}

	@Test
	void shouldDeleteDevice() {
		var device = new Device("Device-001", "Main floor device");
		var savedDevice = deviceRepository.save(device);

		deviceRepository.deleteById(savedDevice.getId());

		assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
	}

	@Test
	void shouldCascadeDeleteDeviceAssignmentsWhenDeviceIsDeleted() {
		var sensorId = persistSensor();
		var savedDevice = deviceRepository.save(new Device("Device-001", "Main floor device"));

		deviceSensorRepository.save(new DeviceSensor(savedDevice, sensorId));
		assertThat(deviceSensorRepository.findAllByIdDeviceId(savedDevice.getId())).hasSize(1);

		entityManager.createNativeQuery("DELETE FROM devices WHERE id = :deviceId")
			.setParameter("deviceId", savedDevice.getId())
			.executeUpdate();
		entityManager.clear();

		assertThat(deviceSensorRepository.findAllByIdDeviceId(savedDevice.getId())).isEmpty();
	}

	@Test
	void shouldSetCreatedAtAndUpdatedAtOnCreate() {
		var savedDevice = deviceRepository.save(new Device("Device-001", "Main floor device"));

		assertThat(savedDevice.getCreatedAt()).isNotNull();
		assertThat(savedDevice.getUpdatedAt()).isNotNull();
		assertThat(savedDevice.getCreatedAt()).isEqualTo(savedDevice.getUpdatedAt());
	}

	private Long persistSensor() {
		var sensor = new Sensor("Sensor-" + UUID.randomUUID(), "sensor", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor.getId();
	}
}
