package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import de.sfl.sensors.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

	private UUID testDeviceId;
	private Long testSensorId;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, sensorService);
		testDeviceId = UUID.randomUUID();
		testSensorId = 1L;
	}

	@Test
	void createDevice_shouldSaveAndReturnDevice() {
		var device = new Device("New Device", "Description");
		var savedDevice = createTestDevice(testDeviceId, "New Device", "Description");

		when(deviceRepository.save(device)).thenReturn(savedDevice);

		Device result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		assertThat(result.getId()).isEqualTo(testDeviceId);
		verify(deviceRepository).save(device);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var device = createTestDevice(testDeviceId, "Test Device", "Description");

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));

		Device result = deviceService.getDevice(testDeviceId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(testDeviceId);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(testDeviceId))
			.isInstanceOf(DeviceNotFoundException.class)
			.hasMessageContaining(testDeviceId.toString());

		verify(deviceRepository).findById(testDeviceId);
	}

	@Test
	void updateDevice_shouldUpdateAndReturnDevice() {
		var device = createTestDevice(testDeviceId, "Old Name", "Old Description");
		var updateDto = new UpdateDeviceDto("New Name", "New Description");

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));
		when(deviceRepository.save(device)).thenReturn(device);

		Device result = deviceService.updateDevice(testDeviceId, updateDto);

		assertThat(result).isEqualTo(device);
		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getDescription()).isEqualTo("New Description");
		verify(deviceRepository).findById(testDeviceId);
		verify(deviceRepository).save(device);
	}

	@Test
	void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var updateDto = new UpdateDeviceDto("Name", "Description");

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.updateDevice(testDeviceId, updateDto))
			.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(testDeviceId);
		verify(deviceRepository, never()).save(any());
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var device = createTestDevice(testDeviceId, "To Delete", "Description");

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(testDeviceId);

		verify(deviceRepository).findById(testDeviceId);
		verify(deviceRepository).delete(device);
	}

	@Test
	void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.deleteDevice(testDeviceId))
			.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(testDeviceId);
		verify(deviceRepository, never()).delete(any(Device.class));
	}

	@Test
	void assignSensor_shouldCreateAssignment_whenBothExistAndAssignmentNotExists() {
		var device = createTestDevice(testDeviceId, "Device", "Description");
		var sensor = createTestSensor(testSensorId, "Sensor", "type", Set.of("read"));

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(testSensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByDeviceIdAndSensorId(testDeviceId, testSensorId)).thenReturn(false);
		when(deviceRepository.save(device)).thenReturn(device);

		Device result = deviceService.assignSensor(testDeviceId, testSensorId);

		assertThat(result).isEqualTo(device);
		assertThat(result.getSensors()).isNotEmpty();
		verify(deviceRepository).findById(testDeviceId);
		verify(sensorService).getSensorById(testSensorId);
		verify(deviceSensorRepository).existsByDeviceIdAndSensorId(testDeviceId, testSensorId);
		verify(deviceRepository).save(device);
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAssignmentAlreadyExists() {
		var device = createTestDevice(testDeviceId, "Device", "Description");
		var sensor = createTestSensor(testSensorId, "Sensor", "type", Set.of("read"));

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(testSensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByDeviceIdAndSensorId(testDeviceId, testSensorId)).thenReturn(true);

		Device result = deviceService.assignSensor(testDeviceId, testSensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(testDeviceId);
		verify(sensorService).getSensorById(testSensorId);
		verify(deviceSensorRepository).existsByDeviceIdAndSensorId(testDeviceId, testSensorId);
		verify(deviceRepository, never()).save(any());
	}

	@Test
	void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.assignSensor(testDeviceId, testSensorId))
			.isInstanceOf(DeviceNotFoundException.class);

		verify(deviceRepository).findById(testDeviceId);
		verify(sensorService, never()).getSensorById(any());
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var device = createTestDevice(testDeviceId, "Device", "Description");

		when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(device));
		when(sensorService.getSensorById(testSensorId)).thenThrow(new SensorNotFoundException(testSensorId));

		assertThatThrownBy(() -> deviceService.assignSensor(testDeviceId, testSensorId))
			.isInstanceOf(SensorNotFoundException.class);

		verify(deviceRepository).findById(testDeviceId);
		verify(sensorService).getSensorById(testSensorId);
	}

	private Device createTestDevice(UUID id, String name, String description) {
		var device = new Device(name, description);
		device.setId(id);
		return device;
	}

	private Sensor createTestSensor(Long id, String name, String type, Set<String> capabilities) {
		return new TestSensor(id, name, type, capabilities);
	}

	static class TestSensor extends Sensor {
		private Long id;

		TestSensor(Long id, String name, String type, Set<String> capabilities) {
			super(name, type, capabilities);
			this.id = id;
		}

		public Long getId() {
			return id;
		}
	}
}