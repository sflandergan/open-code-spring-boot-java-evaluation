package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Test
    void shouldMarshallToJson() throws Exception {
        var deviceId = UUID.randomUUID();
        var now = Instant.now();
        var device = createTestDevice(deviceId, "Test Device", "Test Description", now, now);

        DeviceDto dto = DeviceDto.fromEntity(device);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"id\":\"" + deviceId + "\"");
        assertThat(json).contains("\"name\":\"Test Device\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        String json = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "name": "Device Name",
                "description": "Device Description"
            }
            """;

        DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertThat(dto.name()).isEqualTo("Device Name");
        assertThat(dto.description()).isEqualTo("Device Description");
    }

    @Test
    void fromEntity_shouldConvertFromEntity() {
        var deviceId = UUID.randomUUID();
        var now = Instant.now();
        var device = createTestDevice(deviceId, "Test Device", "Test Description", now, now);

        DeviceDto dto = DeviceDto.fromEntity(device);

        assertThat(dto.id()).isEqualTo(deviceId);
        assertThat(dto.name()).isEqualTo("Test Device");
        assertThat(dto.description()).isEqualTo("Test Description");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
        assertThat(dto.sensors()).isNull();
    }

    @Test
    void fromEntityWithSensors_shouldIncludeSensors() {
        var deviceId = UUID.randomUUID();
        var now = Instant.now();
        var device = createTestDevice(deviceId, "Test Device", "Test Description", now, now);

        var sensors = List.of(
                new de.sfl.sensors.SensorDto(1L, "Sensor 1", "type", null)
        );

        DeviceDto dto = DeviceDto.fromEntityWithSensors(device, sensors);

        assertThat(dto.id()).isEqualTo(deviceId);
        assertThat(dto.sensors()).isNotNull();
        assertThat(dto.sensors()).hasSize(1);
        assertThat(dto.sensors().get(0).name()).isEqualTo("Sensor 1");
    }

    private Device createTestDevice(UUID id, String name, String description, Instant createdAt, Instant updatedAt) {
        return new TestDevice(id, name, description, createdAt, updatedAt);
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description, Instant createdAt, Instant updatedAt) {
            super(name, description);
            try {
                var idField = Device.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(this, id);

                var createdAtField = Device.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(this, createdAt);

                var updatedAtField = Device.class.getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(this, updatedAt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
