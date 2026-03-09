package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.sfl.sensors.SensorDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void shouldMarshallToJson() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorDto = new SensorDto(1L, "Sensor Name", "sensor", java.util.Set.of("read", "write"));
        var deviceDto = new DeviceDto(
            deviceId,
            "Device Name",
            "Device Description",
            List.of(sensorDto),
            Instant.now(),
            Instant.now()
        );

        // When
        String json = objectMapper.writeValueAsString(deviceDto);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"id\":\"" + deviceId + "\"");
        assertThat(json).contains("\"name\":\"Device Name\"");
        assertThat(json).contains("\"description\":\"Device Description\"");
        assertThat(json).contains("\"sensors\"");
        assertThat(json).contains("\"createdAt\"");
        assertThat(json).contains("\"updatedAt\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        String json = """
            {
                "id": "%s",
                "name": "Device Name",
                "description": "Device Description",
                "sensors": [
                    {
                        "id": 1,
                        "name": "Sensor Name",
                        "type": "sensor",
                        "capabilities": ["read", "write"]
                    }
                ],
                "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-01T00:00:00Z"
            }
            """.formatted(deviceId);

        // When
        DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

        // Then
        assertThat(deviceDto).isNotNull();
        assertThat(deviceDto.id()).isEqualTo(deviceId);
        assertThat(deviceDto.name()).isEqualTo("Device Name");
        assertThat(deviceDto.description()).isEqualTo("Device Description");
        assertThat(deviceDto.sensors()).hasSize(1);
        assertThat(deviceDto.sensors().get(0).name()).isEqualTo("Sensor Name");
        assertThat(deviceDto.createdAt()).isNotNull();
        assertThat(deviceDto.updatedAt()).isNotNull();
    }

    @Test
    void shouldUnmarshallFromJsonWithoutSensors() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        String json = """
            {
                "id": "%s",
                "name": "Device Name",
                "description": "Device Description",
                "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-01T00:00:00Z"
            }
            """.formatted(deviceId);

        // When
        DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

        // Then
        assertThat(deviceDto).isNotNull();
        assertThat(deviceDto.id()).isEqualTo(deviceId);
        assertThat(deviceDto.name()).isEqualTo("Device Name");
        assertThat(deviceDto.description()).isEqualTo("Device Description");
        assertThat(deviceDto.sensors()).isNull();
        assertThat(deviceDto.createdAt()).isNotNull();
        assertThat(deviceDto.updatedAt()).isNotNull();
    }

    @Test
    void shouldUnmarshallFromJsonWithEmptySensorsList() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        String json = """
            {
                "id": "%s",
                "name": "Device Name",
                "description": "Device Description",
                "sensors": [],
                "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-01T00:00:00Z"
            }
            """.formatted(deviceId);

        // When
        DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

        // Then
        assertThat(deviceDto).isNotNull();
        assertThat(deviceDto.id()).isEqualTo(deviceId);
        assertThat(deviceDto.sensors()).isEmpty();
    }

    @Test
    void fromEntity_shouldConvertDeviceEntity() {
        // Given
        var device = new Device("Test Device", "Test Description");

        // When
        DeviceDto deviceDto = DeviceDto.fromEntityWithoutSensors(device);

        // Then
        assertThat(deviceDto).isNotNull();
        assertThat(deviceDto.id()).isEqualTo(device.getId());
        assertThat(deviceDto.name()).isEqualTo(device.getName());
        assertThat(deviceDto.description()).isEqualTo(device.getDescription());
        assertThat(deviceDto.sensors()).isNull();
        assertThat(deviceDto.createdAt()).isEqualTo(device.getCreatedAt());
        assertThat(deviceDto.updatedAt()).isEqualTo(device.getUpdatedAt());
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorDto = new SensorDto(1L, "Sensor Name", "sensor", java.util.Set.of("read"));
        var originalDto = new DeviceDto(
            deviceId,
            "Symmetric Device",
            "Symmetric Description",
            List.of(sensorDto),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z")
        );

        // When
        String json = objectMapper.writeValueAsString(originalDto);
        DeviceDto deserializedDto = objectMapper.readValue(json, DeviceDto.class);

        // Then
        assertThat(deserializedDto.id()).isEqualTo(originalDto.id());
        assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
        assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
        assertThat(deserializedDto.sensors()).hasSameSizeAs(originalDto.sensors());
        assertThat(deserializedDto.createdAt()).isEqualTo(originalDto.createdAt());
        assertThat(deserializedDto.updatedAt()).isEqualTo(originalDto.updatedAt());
    }
}