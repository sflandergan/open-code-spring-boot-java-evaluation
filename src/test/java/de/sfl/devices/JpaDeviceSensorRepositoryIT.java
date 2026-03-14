package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private JpaDeviceRepository deviceRepository;

	private Device testDevice;

	@BeforeEach
	void setUp() {
		deviceRepository.deleteAll();
		deviceSensorRepository.deleteAll();

		testDevice = deviceRepository.save(new Device("Test Device", "Test description"));
	}

	@Test
	void shouldReturnEmptyListWhenNoAssignmentsExist() {
		assertThat(deviceSensorRepository.findAll()).isEmpty();
	}

	@Test
	void shouldReturnEmptyOptionalWhenAssignmentNotFound() {
		var nonExistentId = new DeviceSensorId(UUID.randomUUID(), 999L);
		assertThat(deviceSensorRepository.findById(nonExistentId)).isEmpty();
	}

	@Test
	void shouldCheckIfExistsByDeviceIdAndSensorId_whenAssignmentNotFound() {
		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(testDevice.getId(), 1L)).isFalse();
		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(testDevice.getId(), 999L)).isFalse();
		assertThat(deviceSensorRepository.existsByDeviceIdAndSensorId(UUID.randomUUID(), 1L)).isFalse();
	}

	@Test
	void shouldCreateAndUseCompositeKey() {
		var deviceId = testDevice.getId();
		var sensorId = 1L;

		var compositeKey = new DeviceSensorId(deviceId, sensorId);
		assertThat(compositeKey.getDeviceId()).isEqualTo(deviceId);
		assertThat(compositeKey.getSensorId()).isEqualTo(sensorId);

		var sensorId2 = 2L;
		var compositeKey2 = new DeviceSensorId(deviceId, sensorId2);
		assertThat(compositeKey.equals(compositeKey2)).isFalse();
	}

	@Test
	void shouldAllowMultipleDevicesForCompositeKeyTests() {
		var device1 = deviceRepository.save(new Device("Device 1", "First device"));
		var device2 = deviceRepository.save(new Device("Device 2", "Second device"));

		var compositeKey1 = new DeviceSensorId(device1.getId(), 1L);
		var compositeKey2 = new DeviceSensorId(device2.getId(), 1L);

		assertThat(compositeKey1.equals(compositeKey2)).isFalse();
		assertThat(compositeKey1.hashCode()).isNotEqualTo(compositeKey2.hashCode());
	}

	@Test
	void shouldHaveCorrectEqualsAndHashCodeForCompositeKey() {
		var deviceId = testDevice.getId();
		var sensorId = 1L;

		var key1 = new DeviceSensorId(deviceId, sensorId);
		var key2 = new DeviceSensorId(deviceId, sensorId);
		var key3 = new DeviceSensorId(UUID.randomUUID(), sensorId);
		var key4 = new DeviceSensorId(deviceId, 2L);

		assertThat(key1.equals(key2)).isTrue();
		assertThat(key1.equals(key3)).isFalse();
		assertThat(key1.equals(key4)).isFalse();
		assertThat(key1.equals(null)).isFalse();
		assertThat(key1.equals(new Object())).isFalse();

		assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
	}
}