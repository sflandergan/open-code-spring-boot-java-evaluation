package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@BeforeEach
	void setUp() {
		deviceRepository.deleteAll();
	}

	@Test
	void shouldSaveAndFindDeviceById() {
		var device = new Device("Device-001", "A test device");

		Device savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Device-001");
		assertThat(savedDevice.getDescription()).isEqualTo("A test device");

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getName()).isEqualTo("Device-001");
	}

	@Test
	void shouldFindAllDevices() {
		var device1 = new Device("Device-001", "First device");
		var device2 = new Device("Device-002", "Second device");

		deviceRepository.save(device1);
		deviceRepository.save(device2);

		var devices = deviceRepository.findAll();

		assertThat(devices).hasSize(2);
		assertThat(devices).extracting(Device::getName)
				.containsExactlyInAnyOrder("Device-001", "Device-002");
	}

	@Test
	void shouldDeleteDevice() {
		var device = new Device("ToDelete", "Will be deleted");
		Device savedDevice = deviceRepository.save(device);

		deviceRepository.deleteById(savedDevice.getId());

		assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
	}

	@Test
	void shouldUpdateDevice() {
		var device = new Device("Original", "Original description");
		Device savedDevice = deviceRepository.save(device);

		savedDevice.setName("Updated");
		savedDevice.setDescription("Updated description");

		Device updatedDevice = deviceRepository.save(savedDevice);

		assertThat(updatedDevice.getName()).isEqualTo("Updated");
		assertThat(updatedDevice.getDescription()).isEqualTo("Updated description");
	}

	@Test
	void shouldReturnEmptyListWhenNoDevicesExist() {
		assertThat(deviceRepository.findAll()).isEmpty();
	}

	@Test
	void shouldReturnEmptyOptionalWhenDeviceNotFound() {
		var nonExistentId = java.util.UUID.randomUUID();
		assertThat(deviceRepository.findById(nonExistentId)).isEmpty();
	}

	@Test
	void shouldSetCreatedAtAndUpdatedAtOnCreate() {
		var beforeSave = Instant.now();
		var device = new Device("TimestampTest", "Testing timestamps");

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
		var device = new Device("UpdateTimestampTest", "Testing timestamp update");
		Device savedDevice = deviceRepository.save(device);
		deviceRepository.flush();

		var originalCreatedAt = savedDevice.getCreatedAt();
		var originalUpdatedAt = savedDevice.getUpdatedAt();

		Thread.sleep(100);

		savedDevice.setName("Modified");
		Device updatedDevice = deviceRepository.save(savedDevice);
		deviceRepository.flush();

		Device refetchedDevice = deviceRepository.findById(updatedDevice.getId()).orElseThrow();

		assertThat(refetchedDevice.getCreatedAt()).isEqualTo(originalCreatedAt);
		assertThat(refetchedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
		assertThat(refetchedDevice.getUpdatedAt()).isNotEqualTo(refetchedDevice.getCreatedAt());
	}

	@Test
	void shouldPreserveTimestampsOnRead() {
		var device = new Device("ReadTimestampTest", "Testing read timestamps");
		Device savedDevice = deviceRepository.save(device);

		var savedCreatedAt = savedDevice.getCreatedAt();
		var savedUpdatedAt = savedDevice.getUpdatedAt();

		Device foundDevice = deviceRepository.findById(savedDevice.getId()).orElseThrow();

		assertThat(foundDevice.getCreatedAt()).isEqualTo(savedCreatedAt);
		assertThat(foundDevice.getUpdatedAt()).isEqualTo(savedUpdatedAt);
	}

	@Test
	void shouldHandleDeviceWithNullDescription() {
		var device = new Device("NoDescription", null);
		Device savedDevice = deviceRepository.save(device);

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getDescription()).isNull();
	}
}
