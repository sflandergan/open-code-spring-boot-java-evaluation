package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated Description");

        String json = objectMapper.writeValueAsString(updateDeviceDto);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Updated Name\"");
        assertThat(json).contains("\"description\":\"Updated Description\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        String json = """
            {
                "name": "New Name",
                "description": "New Description"
            }
            """;

        UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.name()).isEqualTo("New Name");
        assertThat(dto.description()).isEqualTo("New Description");
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        var originalDto = new UpdateDeviceDto("Symmetric Update", "Symmetric Description");

        String json = objectMapper.writeValueAsString(originalDto);
        UpdateDeviceDto deserializedDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
        assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
    }

    @Test
    void applyToEntity_shouldUpdateName() {
        var device = new Device("Old Name", "Description");
        var updateDto = new UpdateDeviceDto("New Name", null);

        updateDto.applyToEntity(device);

        assertThat(device.getName()).isEqualTo("New Name");
        assertThat(device.getDescription()).isEqualTo("Description");
    }

    @Test
    void applyToEntity_shouldUpdateDescription() {
        var device = new Device("Name", "Old Description");
        var updateDto = new UpdateDeviceDto(null, "New Description");

        updateDto.applyToEntity(device);

        assertThat(device.getName()).isEqualTo("Name");
        assertThat(device.getDescription()).isEqualTo("New Description");
    }

    @Test
    void applyToEntity_shouldUpdateBothFields() {
        var device = new Device("Old Name", "Old Description");
        var updateDto = new UpdateDeviceDto("New Name", "New Description");

        updateDto.applyToEntity(device);

        assertThat(device.getName()).isEqualTo("New Name");
        assertThat(device.getDescription()).isEqualTo("New Description");
    }

    @Test
    void applyToEntity_shouldNotUpdateWhenValueIsBlank() {
        var device = new Device("Original Name", "Original Description");
        var updateDto = new UpdateDeviceDto("", "New Description");

        updateDto.applyToEntity(device);

        assertThat(device.getName()).isEqualTo("Original Name");
        assertThat(device.getDescription()).isEqualTo("New Description");
    }
}
