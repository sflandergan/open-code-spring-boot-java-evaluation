package de.sfl.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

	@Mock
	private JpaDeviceRepository deviceRepository;

	@Mock
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Captor
	private ArgumentCaptor<DeviceSensor> deviceSensorCaptor;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository);
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice() {
		var device = new Device("Device-001", "Main floor device");
		var savedDevice = createDevice(UUID.randomUUID(), "Device-001", "Main floor device");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		var result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		verify(deviceRepository).save(device);
	}

	@Test
	void getDevice_shouldReturnDeviceWhenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.getDevice(deviceId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundExceptionWhenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(deviceId))
			.isInstanceOf(DeviceNotFoundException.class)
			.hasMessageContaining(deviceId.toString());

		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void updateDevice_shouldUpdateOnlyProvidedFields() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Original", "Original description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);

		var updatedDevice = deviceService.updateDevice(deviceId, "Updated", null);

		assertThat(updatedDevice.getName()).isEqualTo("Updated");
		assertThat(updatedDevice.getDescription()).isEqualTo("Original description");
		verify(deviceRepository).save(device);
	}

	@Test
	void deleteDevice_shouldDeleteDeviceWhenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).delete(device);
	}

	@Test
	void assignSensor_shouldCreateAssignmentWhenNotAlreadyAssigned() {
		var deviceId = UUID.randomUUID();
		var sensorId = 42L;
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsSensorById(sensorId)).thenReturn(true);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository).save(deviceSensorCaptor.capture());
		assertThat(deviceSensorCaptor.getValue().getDevice()).isEqualTo(device);
		assertThat(deviceSensorCaptor.getValue().getSensorId()).isEqualTo(sensorId);
	}

	@Test
	void assignSensor_shouldRemainIdempotentWhenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var sensorId = 42L;
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsSensorById(sensorId)).thenReturn(true);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);

		deviceService.assignSensor(deviceId, sensorId);

		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundExceptionWhenSensorDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsSensorById(sensorId)).thenReturn(false);

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
			.isInstanceOf(SensorNotFoundException.class)
			.hasMessageContaining("999");

		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void getDeviceSensors_shouldReturnSortedSensorIds() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Device-001", "Main floor device");
		var assignmentA = new DeviceSensor(device, 30L);
		var assignmentB = new DeviceSensor(device, 10L);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.findAllByIdDeviceId(eq(deviceId))).thenReturn(List.of(assignmentA, assignmentB));

		var result = deviceService.getDeviceSensors(deviceId);

		assertThat(result).containsExactly(10L, 30L);
		verify(deviceSensorRepository).findAllByIdDeviceId(deviceId);
	}

	private Device createDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
