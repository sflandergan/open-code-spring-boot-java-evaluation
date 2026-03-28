package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
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
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Edge Gateway", "Main building gateway");

		var savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Edge Gateway");
		assertThat(savedDevice.getDescription()).isEqualTo("Main building gateway");

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getName()).isEqualTo("Edge Gateway");
	}

	@Test
	void shouldFindAllDevices() {
		var deviceOne = new Device("Device-001", "Production line one");
		var deviceTwo = new Device("Device-002", "Production line two");

		deviceRepository.save(deviceOne);
		deviceRepository.save(deviceTwo);

		List<Device> devices = deviceRepository.findAll();

		assertThat(devices).hasSize(2);
		assertThat(devices).extracting(Device::getName)
				.containsExactlyInAnyOrder("Device-001", "Device-002");
	}

	@Test
	void shouldUpdateDevice() {
		var device = new Device("Original Name", "Original description");
		var savedDevice = deviceRepository.save(device);

		savedDevice.setName("Updated Name");
		savedDevice.setDescription("Updated description");

		var updatedDevice = deviceRepository.save(savedDevice);

		assertThat(updatedDevice.getName()).isEqualTo("Updated Name");
		assertThat(updatedDevice.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void shouldDeleteDeviceAndCascadeAssignments() {
		var device = deviceRepository.save(new Device("Gateway", "Factory gateway"));
		var deviceId = device.getId();
		var sensorId = createSensor("Temperature");

		deviceSensorRepository.saveAndFlush(new DeviceSensor(device, sensorId));
		entityManager.clear();

		deviceRepository.deleteById(deviceId);
		deviceRepository.flush();
		entityManager.clear();

		var remainingDevices = ((Number) entityManager
				.createNativeQuery("SELECT COUNT(*) FROM devices WHERE id = :id")
				.setParameter("id", deviceId)
				.getSingleResult()).longValue();

		var remainingAssignments = ((Number) entityManager
				.createNativeQuery("SELECT COUNT(*) FROM device_sensors WHERE device_id = :deviceId")
				.setParameter("deviceId", deviceId)
				.getSingleResult()).longValue();

		assertThat(remainingDevices).isZero();
		assertThat(remainingAssignments).isZero();
	}

	private Long createSensor(String name) {
		var sensor = new Sensor(name, "sensor", Set.of("read"));
		entityManager.persist(sensor);
		entityManager.flush();
		return sensor.getId();
	}
}
