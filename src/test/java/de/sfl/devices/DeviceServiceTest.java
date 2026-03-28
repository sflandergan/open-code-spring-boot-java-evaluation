package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
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

	private DeviceService deviceService;

	@BeforeEach
	void setUp() {
		deviceService = new DeviceService(deviceRepository, deviceSensorRepository, entityManager);
	}

	@Test
	void createDevice_shouldSaveAndReturnDetailedDevice_whenInputIsValid() {
		var deviceId = UUID.randomUUID();
		var device = new Device("Weather Station", "Outdoor monitoring unit");
		var savedDevice = new TestDevice(deviceId, "Weather Station", "Outdoor monitoring unit");

		when(deviceRepository.save(device)).thenReturn(savedDevice);
		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.of(savedDevice));

		var result = deviceService.createDevice(device);

		assertThat(result).isEqualTo(savedDevice);
		verify(deviceRepository).save(device);
		verify(deviceRepository).findDetailedById(deviceId);
	}

	@Test
	void getDevice_shouldReturnDevice_whenDeviceExists() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));

		var result = deviceService.getDevice(device.getId());

		assertThat(result).isEqualTo(device);
		verify(deviceRepository).findDetailedById(device.getId());
	}

	@Test
	void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
		var deviceId = UUID.randomUUID();

		when(deviceRepository.findDetailedById(deviceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deviceService.getDevice(deviceId))
				.isInstanceOf(DeviceNotFoundException.class)
				.hasMessageContaining(deviceId.toString());

		verify(deviceRepository).findDetailedById(deviceId);
	}

	@Test
	void updateDevice_shouldApplyProvidedFields_whenDeviceExists() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");
		var updateDeviceDto = new UpdateDeviceDto("Edge Gateway", null);

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));

		var result = deviceService.updateDevice(device.getId(), updateDeviceDto);

		assertThat(result.getName()).isEqualTo("Edge Gateway");
		assertThat(result.getDescription()).isEqualTo("Outdoor monitoring unit");
	}

	@Test
	void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));

		deviceService.deleteDevice(device.getId());

		verify(deviceRepository).delete(device);
	}

	@Test
	void assignSensor_shouldCreateAssignment_whenAssignmentDoesNotExist() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");
		var sensor = new TestSensor(11L, "Humidity Sensor", "sensor", Set.of("read"));

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, sensor.getId())).thenReturn(sensor);
		when(deviceSensorRepository.existsAssignment(device.getId(), sensor.getId())).thenReturn(false);

		var result = deviceService.assignSensor(device.getId(), sensor.getId());

		assertThat(result.getDeviceSensors()).hasSize(1);
		assertThat(result.hasSensor(sensor.getId())).isTrue();
		verify(deviceSensorRepository).existsAssignment(device.getId(), sensor.getId());
	}

	@Test
	void assignSensor_shouldBeIdempotent_whenAssignmentAlreadyExists() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");
		var sensor = new TestSensor(11L, "Humidity Sensor", "sensor", Set.of("read"));
		device.assignSensor(sensor);

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, sensor.getId())).thenReturn(sensor);
		when(deviceSensorRepository.existsAssignment(device.getId(), sensor.getId())).thenReturn(true);

		var result = deviceService.assignSensor(device.getId(), sensor.getId());

		assertThat(result.getDeviceSensors()).hasSize(1);
		verify(deviceSensorRepository).existsAssignment(device.getId(), sensor.getId());
	}

	@Test
	void assignSensor_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		var device = new TestDevice(UUID.randomUUID(), "Weather Station", "Outdoor monitoring unit");

		when(deviceRepository.findDetailedById(device.getId())).thenReturn(Optional.of(device));
		when(entityManager.find(Sensor.class, 11L)).thenReturn(null);

		assertThatThrownBy(() -> deviceService.assignSensor(device.getId(), 11L))
				.isInstanceOf(SensorNotFoundException.class)
				.hasMessageContaining("11");

		verify(deviceSensorRepository, never()).existsAssignment(device.getId(), 11L);
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
