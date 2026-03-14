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
	void getDeviceById_shouldReturnDevice_whenDeviceExists() {
		var device = new TestDevice(1L, "Test Device", "Test Description");
		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

		Device result = deviceService.getDeviceById(1L);

		assertThat(result).isEqualTo(device);
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Test Device");
		verify(deviceRepository).findById(1L);
	}

	@Test
	void getDeviceById_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDeviceById(999L))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining("999");

		verify(deviceRepository).findById(999L);
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice() {
		var device = new Device("New Device", "New Description");
		var savedDevice = new TestDevice(1L, "New Device", "New Description");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		Device result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		assertThat(result.getId()).isEqualTo(1L);
		verify(deviceRepository).save(device);
	}

	@Test
	void updateDevice_shouldUpdateAndReturnDevice_whenDeviceExists() {
		var existingDevice = new TestDevice(1L, "Original Name", "Original Description");
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
		var updatedDevice = new TestDevice(1L, "Updated Name", "Updated Description");

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(existingDevice));
		when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);

		Device result = deviceService.updateDevice(1L, updateDto);

		assertThat(result.getName()).isEqualTo("Updated Name");
		assertThat(result.getDescription()).isEqualTo("Updated Description");
		verify(deviceRepository).findById(1L);
		verify(deviceRepository).save(existingDevice);
	}

	@Test
	void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
		when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.updateDevice(999L, updateDto))
				.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(999L);
		verify(deviceRepository, never()).save(any());
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var device = new TestDevice(1L, "Test Device", "Test Description");
		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(1L);

		verify(deviceRepository).findById(1L);
		verify(deviceRepository).delete(device);
	}

	@Test
	void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.deleteDevice(999L))
				.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(999L);
		verify(deviceRepository, never()).delete(any());
	}

	@Test
	void assignSensor_shouldAssignSensor_whenDeviceAndSensorExist() {
		var device = new TestDevice(1L, "Test Device", "Test Description");
		var sensor = new Sensor("Test Sensor", "temperature", Set.of("read"));
		var savedDeviceSensor = new DeviceSensor(device, 2L);

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(2L)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(1L, 2L)).thenReturn(false);
		when(deviceSensorRepository.save(any(DeviceSensor.class))).thenReturn(savedDeviceSensor);

		Device result = deviceService.assignSensor(1L, 2L);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(1L);
		verify(sensorService).getSensorById(2L);
		verify(deviceSensorRepository).existsByIdDeviceIdAndIdSensorId(1L, 2L);
		verify(deviceSensorRepository).save(any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenSensorAlreadyAssigned() {
		var device = new TestDevice(1L, "Test Device", "Test Description");
		var sensor = new Sensor("Test Sensor", "temperature", Set.of("read"));

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(2L)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(1L, 2L)).thenReturn(true);

		Device result = deviceService.assignSensor(1L, 2L);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(1L);
		verify(sensorService).getSensorById(2L);
		verify(deviceSensorRepository).existsByIdDeviceIdAndIdSensorId(1L, 2L);
		verify(deviceSensorRepository, never()).save(any());
	}

	@Test
	void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.assignSensor(999L, 1L))
				.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(999L);
		verify(sensorService, never()).getSensorById(any());
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var device = new TestDevice(1L, "Test Device", "Test Description");

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(999L)).thenThrow(new SensorNotFoundException(999L));

		assertThatThrownBy(() -> deviceService.assignSensor(1L, 999L))
				.isInstanceOf(SensorNotFoundException.class);

		verify(deviceRepository).findById(1L);
		verify(sensorService).getSensorById(999L);
		verify(deviceSensorRepository, never()).existsByIdDeviceIdAndIdSensorId(any(), any());
	}

	@Test
	void getDeviceSensorIds_shouldReturnSensorIds() {
		var device = new TestDevice(1L, "Test Device", "Test Description");
		var deviceSensor1 = new DeviceSensor(device, 10L);
		var deviceSensor2 = new DeviceSensor(device, 20L);

		when(deviceSensorRepository.findAll()).thenReturn(List.of(deviceSensor1, deviceSensor2));

		List<Long> result = deviceService.getDeviceSensorIds(1L);

		assertThat(result).containsExactlyInAnyOrder(10L, 20L);
	}

	@Test
	void getDeviceSensorIds_shouldReturnEmptyList_whenNoSensorsAssigned() {
		when(deviceSensorRepository.findAll()).thenReturn(List.of());

		List<Long> result = deviceService.getDeviceSensorIds(1L);

		assertThat(result).isEmpty();
	}

	static class TestDevice extends Device {
		TestDevice(Long id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
