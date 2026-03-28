package de.sfl.devices;

import de.sfl.sensors.SensorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

	@Mock
	private JpaDeviceRepository deviceRepository;

	@Mock
	private JpaDeviceSensorRepository deviceSensorRepository;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository);
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice() {
		var device = new Device("Device A", "Primary device");
		var savedDevice = new TestDevice(UUID.randomUUID(), "Device A", "Primary device");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		var result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		verify(deviceRepository).save(device);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.getDevice(deviceId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(deviceId))
			.isInstanceOf(DeviceNotFoundException.class)
			.hasMessageContaining(deviceId.toString());
	}

	@Test
	void updateDevice_shouldApplyUpdateAndSaveDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);

		var result = deviceService.updateDevice(deviceId, "Updated Device", "Updated description");

		assertThat(result.getName()).isEqualTo("Updated Device");
		assertThat(result.getDescription()).isEqualTo("Updated description");
		verify(deviceRepository).save(device);
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).delete(device);
	}

	@Test
	void assignSensor_shouldCreateAssignment_whenAssignmentDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 10L;
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.sensorExists(sensorId)).thenReturn(true);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var sensorId = 10L;
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.sensorExists(sensorId)).thenReturn(true);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.sensorExists(sensorId)).thenReturn(false);

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
			.isInstanceOf(SensorNotFoundException.class)
			.hasMessageContaining("999");
	}

	@Test
	void getDeviceSensorIds_shouldReturnSortedSensorIds() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Primary device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.findByIdDeviceId(deviceId)).thenReturn(List.of(
			new DeviceSensor(new DeviceSensorId(deviceId, 3L)),
			new DeviceSensor(new DeviceSensorId(deviceId, 1L)),
			new DeviceSensor(new DeviceSensorId(deviceId, 2L))
		));

		var sensorIds = deviceService.getDeviceSensorIds(deviceId);

		assertThat(sensorIds).containsExactly(1L, 2L, 3L);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
