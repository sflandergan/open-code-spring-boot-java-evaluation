package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
		var device = new Device("Device-001", "Temperature Controller");

		Device savedDevice = deviceRepository.save(device);

		assertThat(savedDevice.getId()).isNotNull();
		assertThat(savedDevice.getName()).isEqualTo("Device-001");
		assertThat(savedDevice.getDescription()).isEqualTo("Temperature Controller");

		Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
		assertThat(foundDevice).isPresent();
		assertThat(foundDevice.get().getName()).isEqualTo("Device-001");
	}

	@Test
	void shouldFindAllDevices() {
		var device1 = new Device("Device-001", "Temperature Controller");
		var device2 = new Device("Device-002", "Humidity Sensor");

		deviceRepository.save(device1);
		deviceRepository.save(device2);

		List<Device> devices = deviceRepository.findAll();

		assertThat(devices).hasSize(2);
		assertThat(devices).extracting(Device::getName)
				.containsExactlyInAnyOrder("Device-001", "Device-002");
	}

	@Test
	void shouldCheckIfDeviceExistsByName() {
		var device = new Device("UniqueDevice", "Description");
		deviceRepository.save(device);

		assertThat(deviceRepository.existsByName(device.getName())).isTrue();
		assertThat(deviceRepository.existsByName("NonExistentDevice")).isFalse();
	}

	@Test
	void shouldFindFirstPageOrderedById() {
		var device1 = new Device("Device-001", "Controller");
		var device2 = new Device("Device-002", "Sensor");
		var device3 = new Device("Device-003", "Actuator");

		deviceRepository.save(device1);
		deviceRepository.save(device2);
		deviceRepository.save(device3);

		Pageable pageable = PageRequest.of(0, 2);
		List<Device> firstPage = deviceRepository.findFirstPage(pageable);

		assertThat(firstPage).hasSize(2);
	}

	@Test
	void shouldFindNextPageAfterLastId() {
		var device1 = new Device("Device-001", "Controller");
		var device2 = new Device("Device-002", "Sensor");
		var device3 = new Device("Device-003", "Actuator");

		var saved1 = deviceRepository.save(device1);
		var saved2 = deviceRepository.save(device2);
		var saved3 = deviceRepository.save(device3);

		Pageable pageable = PageRequest.of(0, 2);
		List<Device> nextPage = deviceRepository.findNextPage(saved1.getId(), pageable);

		assertThat(nextPage).hasSize(2);
	}

	@Test
	void shouldDeleteDevice() {
		var device = new Device("ToDelete", "Description");
		Device savedDevice = deviceRepository.save(device);

		deviceRepository.deleteById(savedDevice.getId());

		assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
	}

	@Test
	void shouldUpdateDevice() {
		var device = new Device("Original", "Description");
		Device savedDevice = deviceRepository.save(device);

		savedDevice.setName("Updated");
		savedDevice.setDescription("New Description");

		Device updatedDevice = deviceRepository.save(savedDevice);

		assertThat(updatedDevice.getName()).isEqualTo("Updated");
		assertThat(updatedDevice.getDescription()).isEqualTo("New Description");
	}

	@Test
	void shouldReturnEmptyListWhenNoDevicesExist() {
		assertThat(deviceRepository.findAll()).isEmpty();
	}

	@Test
	void shouldReturnEmptyOptionalWhenDeviceNotFound() {
		assertThat(deviceRepository.findById(java.util.UUID.randomUUID())).isEmpty();
	}

	@Test
	void shouldSetCreatedAtAndUpdatedAtOnCreate() {
		var beforeSave = Instant.now();
		var device = new Device("TimestampTest", "Description");

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
		var device = new Device("UpdateTimestampTest", "Description");
		Device savedDevice = deviceRepository.save(device);
		deviceRepository.flush();

		var originalCreatedAt = savedDevice.getCreatedAt();
		var originalUpdatedAt = savedDevice.getUpdatedAt();

		// Wait a bit to ensure timestamp difference
		Thread.sleep(100);

		savedDevice.setName("Modified");
		Device updatedDevice = deviceRepository.save(savedDevice);
		deviceRepository.flush();

		// Re-fetch to ensure we get the persisted state
		Device refetchedDevice = deviceRepository.findById(updatedDevice.getId()).orElseThrow();

		assertThat(refetchedDevice.getCreatedAt()).isEqualTo(originalCreatedAt);
		assertThat(refetchedDevice.getUpdatedAt()).isAfter(originalUpdatedAt);
		assertThat(refetchedDevice.getUpdatedAt()).isNotEqualTo(refetchedDevice.getCreatedAt());
	}

	@Test
	void shouldPreserveTimestampsOnRead() {
		var device = new Device("ReadTimestampTest", "Description");
		Device savedDevice = deviceRepository.save(device);

		var savedCreatedAt = savedDevice.getCreatedAt();
		var savedUpdatedAt = savedDevice.getUpdatedAt();

		Device foundDevice = deviceRepository.findById(savedDevice.getId()).orElseThrow();

		assertThat(foundDevice.getCreatedAt()).isEqualTo(savedCreatedAt);
		assertThat(foundDevice.getUpdatedAt()).isEqualTo(savedUpdatedAt);
	}
}