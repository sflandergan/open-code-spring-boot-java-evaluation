package de.sfl.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

	@Mock
	private JpaDeviceRepository deviceRepository;

	@Mock
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Mock
	private JpaSensorLookupRepository sensorLookupRepository;

	@Captor
	private ArgumentCaptor<DeviceSensor> deviceSensorCaptor;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, sensorLookupRepository);
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice_whenInputIsValid() {
		var device = new Device("Edge Gateway", "Main building gateway");
		var savedDevice = new TestDevice(UUID.randomUUID(), "Edge Gateway", "Main building gateway");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		var result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		verify(deviceRepository).save(device);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Boiler Controller", "Controller for boiler room");

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

		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void updateDevice_shouldUpdateOnlyProvidedFields_whenDescriptionIsNull() {
		var deviceId = UUID.randomUUID();
		var existingDevice = new TestDevice(deviceId, "Original Name", "Original Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);

		var result = deviceService.updateDevice(deviceId, "Updated Name", null);

		assertThat(result.getName()).isEqualTo("Updated Name");
		assertThat(result.getDescription()).isEqualTo("Original Description");
		verify(deviceRepository).save(existingDevice);
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var existingDevice = new TestDevice(deviceId, "To Delete", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).deleteById(deviceId);
	}

	@Test
	void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(deviceId.toString());

		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void assignSensor_shouldReturnDeviceWithoutSavingAssignment_whenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var sensorId = 42L;
		var existingDevice = new TestDevice(deviceId, "Edge Device", "Factory floor");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(existingDevice);
		verify(sensorLookupRepository, never()).existsById(anyLong());
		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 404L;
		var existingDevice = new TestDevice(deviceId, "Edge Device", "Factory floor");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);
		when(sensorLookupRepository.existsById(sensorId)).thenReturn(false);

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
				.isInstanceOf(SensorNotFoundException.class)
				.hasMessageContaining("404");

		verify(sensorLookupRepository).existsById(sensorId);
		verify(deviceSensorRepository, never()).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldSaveAssignment_whenDeviceAndSensorExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 7L;
		var existingDevice = new TestDevice(deviceId, "Edge Device", "Factory floor");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);
		when(sensorLookupRepository.existsById(sensorId)).thenReturn(true);

		deviceService.assignSensor(deviceId, sensorId);

		verify(deviceSensorRepository).save(deviceSensorCaptor.capture());
		var capturedAssignment = deviceSensorCaptor.getValue();
		assertThat(capturedAssignment.getDeviceId()).isEqualTo(deviceId);
		assertThat(capturedAssignment.getSensorId()).isEqualTo(sensorId);
	}

	@Test
	void getDeviceSensors_shouldReturnEmptyList_whenNoAssignmentsExist() {
		var deviceId = UUID.randomUUID();
		var existingDevice = new TestDevice(deviceId, "Edge Device", "Factory floor");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceSensorRepository.findByIdDeviceId(deviceId)).thenReturn(List.of());

		var result = deviceService.getDeviceSensors(deviceId);

		assertThat(result).isEmpty();
		verify(sensorLookupRepository, never()).findByIdIn(anyCollection());
	}

	@Test
	void getDeviceSensors_shouldReturnSensorsSortedBySensorId_whenAssignmentsExist() {
		var deviceId = UUID.randomUUID();
		var existingDevice = new TestDevice(deviceId, "Edge Device", "Factory floor");
		var assignmentOne = new DeviceSensor(existingDevice, 5L);
		var assignmentTwo = new DeviceSensor(existingDevice, 2L);
		var sensorTwo = new TestSensor(2L, "Temperature", "sensor", Set.of("read"));
		var sensorFive = new TestSensor(5L, "Actuator", "actuator", Set.of("write"));

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceSensorRepository.findByIdDeviceId(deviceId)).thenReturn(List.of(assignmentOne, assignmentTwo));
		when(sensorLookupRepository.findByIdIn(List.of(2L, 5L))).thenReturn(List.of(sensorFive, sensorTwo));

		var result = deviceService.getDeviceSensors(deviceId);

		assertThat(result).extracting(Sensor::getId).containsExactly(2L, 5L);
		verify(sensorLookupRepository).findByIdIn(List.of(2L, 5L));
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}

	static class TestSensor extends Sensor {
		TestSensor(Long id, String name, String type, Set<String> capabilities) {
			super(name, type, capabilities);
			this.id = id;
		}
	}
}
