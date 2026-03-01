package de.sfl.devices;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

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
        var createDto = new CreateDeviceDto("Device-001", "Test device");
        var expectedDevice = new TestDevice(UUID.randomUUID(), "Device-001", "Test device");

        when(deviceRepository.save(any(Device.class))).thenReturn(expectedDevice);

        // When
        Device result = deviceService.createDevice(createDto);

        // Then
        assertThat(result).isEqualTo(expectedDevice);
        assertThat(result.getName()).isEqualTo("Device-001");
        assertThat(result.getDescription()).isEqualTo("Test device");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateDevice_shouldUpdateAndReturnDevice() {
        // Given
        var deviceId = UUID.randomUUID();
        var existingDevice = new TestDevice(deviceId, "Original", "Original description");
        var updateDto = new UpdateDeviceDto("Updated", "Updated description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);

        // When
        Device result = deviceService.updateDevice(deviceId, updateDto);

        // Then
        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository).save(existingDevice);
    }

    @Test
    void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("Updated", "Updated description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, updateDto))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void deleteDevice_shouldDeleteDevice() {
        // Given
        var deviceId = UUID.randomUUID();
        var existingDevice = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

        // When
        deviceService.deleteDevice(deviceId);

        // Then
        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository).delete(existingDevice);
    }

    @Test
    void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        // Given
        var deviceId = UUID.randomUUID();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void assignSensor_shouldAssignSensorWhenNotAlreadyAssigned() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var existingDevice = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);

        // When
        Device result = deviceService.assignSensor(deviceId, sensorId);

        // Then
        assertThat(result.getId()).isEqualTo(deviceId);
        verify(deviceRepository).findById(deviceId);
        verify(deviceSensorRepository).existsByIdDeviceIdAndIdSensorId(deviceId, sensorId);
        verify(deviceSensorRepository).save(any(DeviceSensor.class));
    }

    @Test
    void assignSensor_shouldBeIdempotentWhenSensorAlreadyAssigned() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var existingDevice = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);

        // When
        Device result = deviceService.assignSensor(deviceId, sensorId);

        // Then
        assertThat(result.getId()).isEqualTo(deviceId);
        verify(deviceRepository).findById(deviceId);
        verify(deviceSensorRepository).existsByIdDeviceIdAndIdSensorId(deviceId, sensorId);
        verify(deviceSensorRepository, never()).save(any());
    }

    @Test
    void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository).findById(deviceId);
        verify(deviceSensorRepository, never()).save(any());
    }

    @Test
    void getDevice_shouldReturnDevice_whenDeviceExists() {
        // Given
        var deviceId = UUID.randomUUID();
        var existingDevice = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));

        // When
        Device result = deviceService.getDevice(deviceId);

        // Then
        assertThat(result).isEqualTo(existingDevice);
        assertThat(result.getId()).isEqualTo(deviceId);
        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void getDevice_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        // Given
        var deviceId = UUID.randomUUID();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.getDevice(deviceId))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void getAllDevices_shouldReturnAllDevices() {
        // Given
        var device1 = new TestDevice(UUID.randomUUID(), "Device-001", "First device");
        var device2 = new TestDevice(UUID.randomUUID(), "Device-002", "Second device");

        when(deviceRepository.findAll()).thenReturn(List.of(device1, device2));

        // When
        List<Device> result = deviceService.getAllDevices();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(device1, device2);
        verify(deviceRepository).findAll();
    }

    @Test
    void getAllDevices_shouldReturnEmptyListWhenNoDevicesExist() {
        // Given
        when(deviceRepository.findAll()).thenReturn(List.of());

        // When
        List<Device> result = deviceService.getAllDevices();

        // Then
        assertThat(result).isEmpty();
        verify(deviceRepository).findAll();
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description) {
            super(name, description);
            this.id = id;
        }
    }
}
