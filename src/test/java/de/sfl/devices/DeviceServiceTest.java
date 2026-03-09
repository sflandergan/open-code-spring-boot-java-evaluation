package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorService;
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
        var createDto = new CreateDeviceDto("Test Device", "Test Description");
        var device = createDto.toEntity();
        var savedDevice = new TestDevice(device.getId(), "Test Device", "Test Description");

        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        Device result = deviceService.createDevice(createDto);

        assertThat(result.getId()).isEqualTo(savedDevice.getId());
        assertThat(result.getName()).isEqualTo("Test Device");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void getDevice_shouldReturnDevice_whenDeviceExists() {
        var deviceId = UUID.randomUUID();
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

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

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void updateDevice_shouldUpdateAndReturnDevice() {
        var deviceId = UUID.randomUUID();
        var existingDevice = new TestDevice(deviceId, "Old Name", "Old Description");
        var updateDto = new UpdateDeviceDto("New Name", "New Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(existingDevice);

        Device result = deviceService.updateDevice(deviceId, updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Description");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateDevice_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("New Name", "New Description");
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.updateDevice(deviceId, updateDto))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    void deleteDevice_shouldDeleteDevice_whenDeviceExists() {
        var deviceId = UUID.randomUUID();
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.deleteDevice(deviceId);

        verify(deviceRepository).delete(device);
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
    void assignSensor_shouldAssignSensor_whenAssignmentDoesNotExist() {
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)).thenReturn(false);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        Device result = deviceService.assignSensor(deviceId, sensorId);

        assertThat(result).isEqualTo(device);
        verify(sensorService).getSensorById(sensorId);
        verify(deviceSensorRepository).save(any(DeviceSensor.class));
    }

    @Test
    void assignSensor_shouldNotAssignAgain_whenAssignmentAlreadyExists() {
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var device = new TestDevice(deviceId, "Test Device", "Test Description");

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)).thenReturn(true);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        Device result = deviceService.assignSensor(deviceId, sensorId);

        assertThat(result).isEqualTo(device);
        verify(sensorService).getSensorById(sensorId);
    }

    @Test
    void assignSensor_shouldThrowDeviceNotFoundException_whenDeviceDoesNotExist() {
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.assignSensor(deviceId, sensorId))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());

        verify(deviceRepository).findById(deviceId);
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description) {
            super(name, description);
            setId(id);
        }

        private void setId(UUID id) {
            try {
                var field = Device.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(this, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
