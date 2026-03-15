package de.sfl.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
		// Given
		var device = new Device("Test Device", "Test Description");
		var savedDevice = new TestDevice(UUID.randomUUID(), "Test Device", "Test Description");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		// When
		Device result = deviceService.createDevice(device);

		// Then
		assertThat(result).isEqualTo(savedDevice);
		assertThat(result.getName()).isEqualTo("Test Device");
		assertThat(result.getDescription()).isEqualTo("Test Description");
		verify(deviceRepository).save(device);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		// Given
		var id = UUID.randomUUID();
		var device = new TestDevice(id, "Test Device", "Test Description");
		when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

		// When
		Device result = deviceService.getDevice(id);

		// Then
		assertThat(result).isEqualTo(device);
		assertThat(result.getId()).isEqualTo(id);
		verify(deviceRepository).findById(id);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		// Given
		var id = UUID.randomUUID();
		when(deviceRepository.findById(id)).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> deviceService.getDevice(id))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(id.toString());

		verify(deviceRepository).findById(id);
	}

	@Test
	void getDeviceSensors_shouldReturnSensorAssignments() {
		// Given
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Test Device", "Test Description");
		var assignment1 = new DeviceSensor(new DeviceSensorId(deviceId, 1L));
		var assignment2 = new DeviceSensor(new DeviceSensorId(deviceId, 2L));

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.findByIdDeviceId(deviceId)).thenReturn(List.of(assignment1, assignment2));

		// When
		List<DeviceSensor> result = deviceService.getDeviceSensors(deviceId);

		// Then
		assertThat(result).hasSize(2);
		verify(deviceRepository).findById(deviceId);
		verify(deviceSensorRepository).findByIdDeviceId(deviceId);
	}

	@Test
	void getDeviceSensors_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		// Given
		var deviceId = UUID.randomUUID();
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> deviceService.getDeviceSensors(deviceId))
				.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void updateDevice_shouldUpdateAndReturnDevice() {
		// Given
		var id = UUID.randomUUID();
		var device = new TestDevice(id, "Original Name", "Original Description");
		var updateDto = new UpdateDeviceDto("New Name", "New Description");

		when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);

		// When
		Device result = deviceService.updateDevice(id, updateDto);

		// Then
		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getDescription()).isEqualTo("New Description");
		verify(deviceRepository).findById(id);
		verify(deviceRepository).save(device);
	}

	@Test
	void updateDevice_shouldUpdateOnlyNameWhenDescriptionIsNull() {
		// Given
		var id = UUID.randomUUID();
		var device = new TestDevice(id, "Original Name", "Original Description");
		var updateDto = new UpdateDeviceDto("New Name", null);

		when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);

		// When
		Device result = deviceService.updateDevice(id, updateDto);

		// Then
		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getDescription()).isEqualTo("Original Description");
	}

	@Test
	void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		// Given
		var id = UUID.randomUUID();
		when(deviceRepository.findById(id)).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> deviceService.updateDevice(id, new UpdateDeviceDto("Name", "Desc")))
				.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void deleteDevice_shouldDeleteDevice() {
		// Given
		var id = UUID.randomUUID();
		var device = new TestDevice(id, "Test Device", "Test Description");
		when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

		// When
		deviceService.deleteDevice(id);

		// Then
		verify(deviceRepository).findById(id);
		verify(deviceRepository).deleteById(id);
	}

	@Test
	void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		// Given
		var id = UUID.randomUUID();
		when(deviceRepository.findById(id)).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> deviceService.deleteDevice(id))
				.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void assignSensor_shouldCreateNewAssignment() {
		// Given
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = new TestDevice(deviceId, "Test Device", "Test Description");
		var assignmentId = new DeviceSensorId(deviceId, sensorId);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsById(assignmentId)).thenReturn(false);
		when(deviceSensorRepository.save(any(DeviceSensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Device result = deviceService.assignSensor(deviceId, sensorId);

		// Then
		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAlreadyAssigned() {
		// Given
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = new TestDevice(deviceId, "Test Device", "Test Description");
		var assignmentId = new DeviceSensorId(deviceId, sensorId);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsById(assignmentId)).thenReturn(true);

		// When
		Device result = deviceService.assignSensor(deviceId, sensorId);

		// Then
		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		// Given
		var deviceId = UUID.randomUUID();
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		// When/Then
		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, 1L))
				.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		// Given
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;
		var device = new TestDevice(deviceId, "Test Device", "Test Description");
		var assignmentId = new DeviceSensorId(deviceId, sensorId);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.existsById(assignmentId)).thenReturn(false);
		when(deviceSensorRepository.save(any(DeviceSensor.class))).thenReturn(new DeviceSensor(assignmentId));
		// flush triggers the FK violation
		org.mockito.Mockito.doThrow(new DataIntegrityViolationException("FK violation"))
				.when(deviceSensorRepository).flush();

		// When/Then
		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
				.isInstanceOf(SensorNotFoundException.class)
				.hasMessageContaining("999");
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
