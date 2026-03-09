package de.sfl.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void getDevice_shouldReturnDevice_whenDeviceExists() {
        // Given
        var deviceId = UUID.randomUUID();
        var device = new TestDevice(deviceId, "Test Device", "Test Description");
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Device result = deviceService.getDevice(deviceId);

        // Then
        assertThat(result).isEqualTo(device);
        assertThat(result.getId()).isEqualTo(deviceId);
        assertThat(result.getName()).isEqualTo("Test Device");
        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void getDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        // Given
        var deviceId = UUID.randomUUID();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.getDevice(deviceId))
            .isInstanceOf(DeviceNotFoundException.class)
            .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void createDevice_shouldSaveAndReturnDevice() {
        // Given
        var createDeviceDto = new CreateDeviceDto("New Device", "Device Description");
        var device = createDeviceDto.toEntity();
        var deviceId = UUID.randomUUID();
        var savedDevice = new TestDevice(deviceId, "New Device", "Device Description");

        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        // When
        Device result = deviceService.createDevice(createDeviceDto);

        // Then
        assertThat(result).isEqualTo(savedDevice);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Device");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateDevice_shouldUpdateAndReturnDevice_whenDeviceExists() {
        // Given
        var deviceId = UUID.randomUUID();
        var existingDevice = new TestDevice(deviceId, "Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(existingDevice)).thenReturn(existingDevice);

        // When
        Device result = deviceService.updateDevice(deviceId, updateDeviceDto);

        // Then
        assertThat(result).isEqualTo(existingDevice);
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository).save(existingDevice);
    }

    @Test
    void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated Description");
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, updateDeviceDto))
            .isInstanceOf(DeviceNotFoundException.class)
            .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void assignSensor_shouldCreateAssociation_whenDeviceExistsAndSensorNotAssigned() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 123L;
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceSensorRepository.existsById_DeviceIdAndId_SensorId(deviceId, sensorId)).thenReturn(false);

        // When
        Device result = deviceService.assignSensor(deviceId, sensorId);

        // Then
        assertThat(result).isEqualTo(device);
        verify(deviceRepository).findById(deviceId);
        verify(deviceSensorRepository).existsById_DeviceIdAndId_SensorId(deviceId, sensorId);
        verify(deviceSensorRepository).save(any(DeviceSensor.class));
    }

    @Test
    void assignSensor_shouldNotCreateAssociation_whenDeviceExistsAndSensorAlreadyAssigned() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 123L;
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceSensorRepository.existsById_DeviceIdAndId_SensorId(deviceId, sensorId)).thenReturn(true);

        // When
        Device result = deviceService.assignSensor(deviceId, sensorId);

        // Then
        assertThat(result).isEqualTo(device);
        verify(deviceRepository).findById(deviceId);
        verify(deviceSensorRepository).existsById_DeviceIdAndId_SensorId(deviceId, sensorId);
        verify(deviceSensorRepository, org.mockito.Mockito.never()).save(any(DeviceSensor.class));
    }

    @Test
    void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 123L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
            .isInstanceOf(DeviceNotFoundException.class)
            .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
        // Given
        var deviceId = UUID.randomUUID();
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deviceService.deleteDevice(deviceId);

        // Then
        verify(deviceRepository).findById(deviceId);
        verify(deviceRepository).delete(device);
    }

    @Test
    void deleteDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        // Given
        var deviceId = UUID.randomUUID();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deviceService.deleteDevice(deviceId))
            .isInstanceOf(DeviceNotFoundException.class)
            .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description) {
            super(name, description);
            this.id = id;
        }
    }
}