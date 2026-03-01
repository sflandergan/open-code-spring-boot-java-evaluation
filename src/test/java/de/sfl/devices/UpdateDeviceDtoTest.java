package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        // Given
        var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated description");

        // When
        String json = objectMapper.writeValueAsString(updateDeviceDto);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Updated Device\"");
        assertThat(json).contains("\"description\":\"Updated description\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        // Given
        String json = """
            {
                "name": "Updated Device",
                "description": "Updated description"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isEqualTo("Updated Device");
        assertThat(updateDeviceDto.description()).isEqualTo("Updated description");
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        // Given
        var originalDto = new UpdateDeviceDto("Device", "Description");

        // When
        String json = objectMapper.writeValueAsString(originalDto);
        UpdateDeviceDto deserializedDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
        assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
    }

    @Test
    void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
        // Given
        String json = """
            {
                "description": "Updated description"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isNull();
        assertThat(updateDeviceDto.description()).isEqualTo("Updated description");
    }

    @Test
    void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
        // Given
        String json = """
            {
                "name": "Updated Device"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isEqualTo("Updated Device");
        assertThat(updateDeviceDto.description()).isNull();
    }

    @Test
    void applyToEntity_shouldUpdateName() {
        // Given
        var device = new Device("Original", "Original description");
        var updateDeviceDto = new UpdateDeviceDto("Updated", null);

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Updated");
        assertThat(device.getDescription()).isEqualTo("Original description");
    }

    @Test
    void applyToEntity_shouldUpdateDescription() {
        // Given
        var device = new Device("Original", "Original description");
        var updateDeviceDto = new UpdateDeviceDto(null, "Updated description");

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Original");
        assertThat(device.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void applyToEntity_shouldUpdateBothFields() {
        // Given
        var device = new Device("Original", "Original description");
        var updateDeviceDto = new UpdateDeviceDto("Updated", "Updated description");

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Updated");
        assertThat(device.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void applyToEntity_shouldNotUpdateFieldsWhenBothNull() {
        // Given
        var device = new Device("Original", "Original description");
        var updateDeviceDto = new UpdateDeviceDto(null, null);

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Original");
        assertThat(device.getDescription()).isEqualTo("Original description");
    }
}
