package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import de.sfl.sensors.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

	@Mock
	private SensorService sensorService;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, sensorService);
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice() {
		var device = new Device("New Device", "A device description");
		var savedDevice = new TestDevice(UUID.randomUUID(), "New Device", "A device description");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		Device result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		assertThat(result.getName()).isEqualTo("New Device");
		verify(deviceRepository).save(device);
	}

	@Test
	void updateDevice_shouldUpdateAndReturnDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var existingDevice = new TestDevice(deviceId, "Old Name", "Old description");
		var updateDto = new UpdateDeviceDto("New Name", "New description");
		var updatedDevice = new TestDevice(deviceId, "New Name", "New description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceRepository.save(existingDevice)).thenReturn(updatedDevice);

		Device result = deviceService.updateDevice(deviceId, updateDto);

		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getDescription()).isEqualTo("New description");
		verify(deviceRepository).findById(deviceId);
		verify(deviceRepository).save(existingDevice);
	}

	@Test
	void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("New Name", "New description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.updateDevice(deviceId, updateDto))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(deviceId.toString());

		verify(deviceRepository).findById(deviceId);
		verify(deviceRepository, never()).save(any());
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).findById(deviceId);
		verify(deviceRepository).deleteById(deviceId);
	}

	@Test
	void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(deviceId.toString());

		verify(deviceRepository, never()).deleteById(any());
	}

	@Test
	void assignSensor_shouldAssignSensorToDevice_whenBothExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = new TestDevice(deviceId, "Device", "Description");
		var sensor = new TestSensor(sensorId, "Sensor", "temperature", Set.of());

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(sensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);
		when(deviceSensorRepository.save(any())).thenReturn(new DeviceSensor(deviceId, sensorId));

		Device result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = new TestDevice(deviceId, "Device", "Description");
		var sensor = new TestSensor(sensorId, "Sensor", "temperature", Set.of());

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(sensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);

		Device result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository, never()).save(any());
	}

	@Test
	void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(deviceId.toString());
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;
		var device = new TestDevice(deviceId, "Device", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(sensorId)).thenThrow(new SensorNotFoundException(sensorId));

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
				.isInstanceOf(SensorNotFoundException.class);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		Device result = deviceService.getDevice(deviceId);

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
	void getDeviceSensors_shouldReturnSensorsForDevice() {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = new TestDevice(deviceId, "Device", "Description");
		var sensor = new TestSensor(sensorId, "Sensor 1", "temperature", Set.of("read"));
		var assignment = new DeviceSensor(deviceId, sensorId);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.findAllByDeviceId(deviceId)).thenReturn(List.of(assignment));
		when(sensorService.getSensorById(sensorId)).thenReturn(sensor);

		List<Sensor> result = deviceService.getDeviceSensors(deviceId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Sensor 1");
	}

	@Test
	void getDeviceSensors_shouldReturnEmptyList_whenNoSensorsAssigned() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(deviceSensorRepository.findAllByDeviceId(deviceId)).thenReturn(List.of());

		List<Sensor> result = deviceService.getDeviceSensors(deviceId);

		assertThat(result).isEmpty();
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
