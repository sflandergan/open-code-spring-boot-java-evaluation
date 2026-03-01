package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructorFromDevice_shouldCreateDtoWithoutSensors() {
        // Given
        var device = new Device("Device-001", "Test device");

        // When
        var deviceDto = new DeviceDto(device);

        // Then
        assertThat(deviceDto.name()).isEqualTo("Device-001");
        assertThat(deviceDto.description()).isEqualTo("Test device");
        assertThat(deviceDto.sensors()).isEmpty();
    }

    @Test
    void constructorFromDeviceWithSensors_shouldCreateDtoWithSensors() {
        // Given
        var device = new Device("Device-001", "Test device");
        var sensors = List.of(new SensorSummaryDto(1L, "Sensor-001", "temperature"));

        // When
        var deviceDto = new DeviceDto(device, sensors);

        // Then
        assertThat(deviceDto.name()).isEqualTo("Device-001");
        assertThat(deviceDto.description()).isEqualTo("Test device");
        assertThat(deviceDto.sensors()).hasSize(1);
    }
}
