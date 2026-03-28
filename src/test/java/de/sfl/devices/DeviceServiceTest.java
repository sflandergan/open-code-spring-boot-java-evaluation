package de.sfl.devices;

import de.sfl.sensors.Sensor;
import jakarta.persistence.EntityManager;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

	@Mock
	private JpaDeviceRepository deviceRepository;

	@Mock
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Mock
	private EntityManager entityManager;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, entityManager);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.getDevice(deviceId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findById(deviceId);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceMissing() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(deviceId))
			.isInstanceOf(DeviceNotFoundException.class);
	}

	@Test
	void createDevice_shouldPersistAndReturnDevice() {
		var device = new Device("Device A", "Description");
		var saved = new TestDevice(UUID.randomUUID(), "Device A", "Description");

		when(deviceRepository.save(device)).thenReturn(saved);

		var result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(saved);
		verify(deviceRepository).save(device);
	}

	@Test
	void updateDevice_shouldUpdateOnlyProvidedFields() {
		var deviceId = UUID.randomUUID();
		var existing = new TestDevice(deviceId, "Old Name", "Old Description");
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existing));
		when(deviceRepository.save(existing)).thenReturn(existing);

		var result = deviceService.updateDevice(deviceId, "New Name", null);

		assertThat(result.getName()).isEqualTo("New Name");
		assertThat(result.getDescription()).isEqualTo("Old Description");
	}

	@Test
	void deleteDevice_shouldDeleteExistingDevice() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).delete(device);
	}

	@Test
	void assignSensor_shouldCreateAssignmentWhenMissing() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");
		var sensor = new TestSensor(42L, "Sensor A", "temperature", Set.of("read"));

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, 42L)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, 42L)).thenReturn(false);
		when(deviceRepository.save(device)).thenReturn(device);

		var result = deviceService.assignSensor(deviceId, 42L);

		assertThat(result.getSensors()).hasSize(1);
		assertThat(result.getSensors().getFirst().getId()).isEqualTo(42L);
		verify(deviceRepository).save(device);
	}

	@Test
	void assignSensor_shouldBeIdempotentWhenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");
		var sensor = new TestSensor(7L, "Sensor B", "humidity", Set.of("read"));

		device.assignSensor(sensor);
		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, 7L)).thenReturn(sensor);
		when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, 7L)).thenReturn(true);

		var result = deviceService.assignSensor(deviceId, 7L);

		assertThat(result.getSensors()).hasSize(1);
	}

	@Test
	void assignSensor_shouldThrowWhenSensorMissing() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, 55L)).thenReturn(null);

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, 55L))
			.isInstanceOf(AssignedSensorNotFoundException.class);
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
