package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private TestEntityManager entityManager;

	@BeforeEach
	void setUp() {
		deviceRepository.deleteAll();
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Device-001", "First test device");

		Device savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Device-001");
		assertThat(savedDevice.getDescription()).isEqualTo("First test device");

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getName()).isEqualTo("Device-001");
	}

	@Test
	void shouldFindAllDevices() {
		deviceRepository.save(new Device("Device-001", "First device"));
		deviceRepository.save(new Device("Device-002", "Second device"));

		List<Device> devices = deviceRepository.findAll();

		assertThat(devices).hasSize(2);
		assertThat(devices).extracting(Device::getName)
				.containsExactlyInAnyOrder("Device-001", "Device-002");
	}

	@Test
	void shouldDeleteDevice() {
		Device saved = deviceRepository.save(new Device("ToDelete", "Will be deleted"));

		deviceRepository.deleteById(saved.getId());

		assertThat(deviceRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void shouldUpdateDevice() {
		Device saved = deviceRepository.save(new Device("Original", "Original description"));

		saved.setName("Updated");
		saved.setDescription("Updated description");
		Device updated = deviceRepository.save(saved);

		assertThat(updated.getName()).isEqualTo("Updated");
		assertThat(updated.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void shouldReturnEmptyOptionalWhenDeviceNotFound() {
		assertThat(deviceRepository.findById(UUID.randomUUID())).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenNoDevicesExist() {
		assertThat(deviceRepository.findAll()).isEmpty();
	}

	@Test
	void shouldSetCreatedAtAndUpdatedAtOnCreate() {
		var beforeSave = Instant.now();
		var device = new Device("TimestampTest", "Timestamp device");

		Device savedDevice = deviceRepository.save(device);
		var afterSave = Instant.now();

		assertThat(savedDevice.getCreatedAt()).isNotNull();
		assertThat(savedDevice.getUpdatedAt()).isNotNull();
		assertThat(savedDevice.getCreatedAt()).isBetween(beforeSave, afterSave);
		assertThat(savedDevice.getUpdatedAt()).isBetween(beforeSave, afterSave);
		assertThat(savedDevice.getCreatedAt()).isEqualTo(savedDevice.getUpdatedAt());
	}

	@Test
	void shouldUpdateUpdatedAtOnModification() throws InterruptedException {
		Device saved = deviceRepository.save(new Device("UpdateTimestampTest", "Device"));
		deviceRepository.flush();

		var originalCreatedAt = saved.getCreatedAt();
		var originalUpdatedAt = saved.getUpdatedAt();

		Thread.sleep(100);

		saved.setName("Modified");
		Device updated = deviceRepository.save(saved);
		deviceRepository.flush();

		Device refetched = deviceRepository.findById(updated.getId()).orElseThrow();

		assertThat(refetched.getCreatedAt()).isEqualTo(originalCreatedAt);
		assertThat(refetched.getUpdatedAt()).isAfter(originalUpdatedAt);
	}

	@Test
	void shouldCascadeDeleteSensorAssignmentsWhenDeviceIsDeleted() {
		Device device = deviceRepository.save(new Device("CascadeDevice", "Device with sensors"));
		Sensor sensor = entityManager.persist(new Sensor("CascadeSensor", "temperature", Set.of()));
		entityManager.flush();

		var assignment = new DeviceSensor(device.getId(), sensor.getId());
		entityManager.persist(assignment);
		entityManager.flush();

		deviceRepository.deleteById(device.getId());
		deviceRepository.flush();
		entityManager.flush();
		entityManager.clear();

		assertThat(deviceRepository.findById(device.getId())).isEmpty();
		DeviceSensor found = entityManager.find(DeviceSensor.class,
				new DeviceSensorId(device.getId(), sensor.getId()));
		assertThat(found).isNull();
	}
}
