package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
	private EntityManager entityManager;

	@Captor
	private ArgumentCaptor<DeviceSensor> deviceSensorCaptor;

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, entityManager);
	}

	@Test
	void getDevice_shouldReturnDetailedDevice_whenDeviceExists() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Thermostat", "Living room thermostat");

		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.getDevice(deviceId);

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findDetailedById(deviceId);
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(deviceId))
			.isInstanceOf(DeviceNotFoundException.class)
			.hasMessageContaining(deviceId.toString());
	}

	@Test
	void createDevice_shouldSaveAndReturnDetailedDevice() {
		var savedDevice = createDevice(UUID.randomUUID(), "Gateway", "Main gateway");

		when(deviceRepository.save(savedDevice)).thenReturn(savedDevice);
		when(deviceRepository.findDetailedById(savedDevice.getId())).thenReturn(Optional.of(savedDevice));

		var result = deviceService.createDevice(savedDevice);

		assertThat(result).isEqualTo(savedDevice);
		verify(deviceRepository).save(savedDevice);
		verify(deviceRepository).findDetailedById(savedDevice.getId());
	}

	@Test
	void updateDevice_shouldUpdateOnlyProvidedFields() {
		var deviceId = UUID.randomUUID();
		var existingDevice = createDevice(deviceId, "Gateway", "Main gateway");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
		when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);
		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.of(existingDevice));

		var result = deviceService.updateDevice(deviceId, null, "Updated gateway description");

		assertThat(result.getName()).isEqualTo("Gateway");
		assertThat(result.getDescription()).isEqualTo("Updated gateway description");
		verify(deviceRepository).save(existingDevice);
	}

	@Test
	void deleteDevice_shouldDeleteExistingDevice() {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Hub", "Control hub");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

		deviceService.deleteDevice(deviceId);

		verify(deviceRepository).delete(device);
	}

	@Test
	void assignSensor_shouldCreateAssignment_whenAssignmentDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 15L;
		var device = createDevice(deviceId, "Hub", "Control hub");
		var sensor = createSensor(sensorId, "Temperature Sensor", "temperature", Set.of("read"));
		device.addSensorAssignment(sensor);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(createDevice(deviceId, "Hub", "Control hub")));
		when(entityManager.find(Sensor.class, sensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)).thenReturn(false);
		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result.getSensorAssignments()).hasSize(1);
		verify(deviceSensorRepository).save(deviceSensorCaptor.capture());
		assertThat(deviceSensorCaptor.getValue().getId().getDeviceId()).isEqualTo(deviceId);
		assertThat(deviceSensorCaptor.getValue().getId().getSensorId()).isEqualTo(sensorId);
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAssignmentAlreadyExists() {
		var deviceId = UUID.randomUUID();
		var sensorId = 15L;
		var device = createDevice(deviceId, "Hub", "Control hub");
		var sensor = createSensor(sensorId, "Temperature Sensor", "temperature", Set.of("read"));
		device.addSensorAssignment(sensor);

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, sensorId)).thenReturn(sensor);
		when(deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)).thenReturn(true);
		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.of(device));

		var result = deviceService.assignSensor(deviceId, sensorId);

		assertThat(result).isEqualTo(device);
		verify(deviceSensorRepository, never()).save(org.mockito.ArgumentMatchers.any(DeviceSensor.class));
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var deviceId = UUID.randomUUID();
		var sensorId = 15L;
		var device = createDevice(deviceId, "Hub", "Control hub");

		when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, sensorId)).thenReturn(null);

		assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
			.isInstanceOf(SensorNotFoundException.class)
			.hasMessageContaining("15");
	}

	private Device createDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
		return new TestSensor(id, name, type, capabilities);
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
