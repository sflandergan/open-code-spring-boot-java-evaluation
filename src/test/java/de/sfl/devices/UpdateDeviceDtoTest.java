package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        // Given
        var updateDeviceDto = new UpdateDeviceDto(
            "Updated Device",
            "Updated Description"
        );

        // When
        String json = objectMapper.writeValueAsString(updateDeviceDto);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Updated Device\"");
        assertThat(json).contains("\"description\":\"Updated Description\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        // Given
        String json = """
            {
                "name": "Device Name",
                "description": "Device Description"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isEqualTo("Device Name");
        assertThat(updateDeviceDto.description()).isEqualTo("Device Description");
    }

    @Test
    void shouldUnmarshallFromJsonWithOnlyName() throws Exception {
        // Given
        String json = """
            {
                "name": "Device Name"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isEqualTo("Device Name");
        assertThat(updateDeviceDto.description()).isNull();
    }

    @Test
    void shouldUnmarshallFromJsonWithOnlyDescription() throws Exception {
        // Given
        String json = """
            {
                "description": "Device Description"
            }
            """;

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isNull();
        assertThat(updateDeviceDto.description()).isEqualTo("Device Description");
    }

    @Test
    void shouldUnmarshallFromJsonWithEmptyObject() throws Exception {
        // Given
        String json = "{}";

        // When
        UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

        // Then
        assertThat(updateDeviceDto).isNotNull();
        assertThat(updateDeviceDto.name()).isNull();
        assertThat(updateDeviceDto.description()).isNull();
    }

    @Test
    void applyToEntity_shouldUpdateDeviceFields() {
        // Given
        var device = new Device("Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated Description");

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Updated Name");
        assertThat(device.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void applyToEntity_shouldUpdateOnlyNameWhenDescriptionIsNull() {
        // Given
        var device = new Device("Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto("Updated Name", null);

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Updated Name");
        assertThat(device.getDescription()).isEqualTo("Original Description");
    }

    @Test
    void applyToEntity_shouldUpdateOnlyDescriptionWhenNameIsNull() {
        // Given
        var device = new Device("Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto(null, "Updated Description");

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Original Name");
        assertThat(device.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void applyToEntity_shouldNotUpdateWhenBothFieldsAreNull() {
        // Given
        var device = new Device("Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto(null, null);

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Original Name");
        assertThat(device.getDescription()).isEqualTo("Original Description");
    }

    @Test
    void applyToEntity_shouldNotUpdateWhenFieldsAreBlank() {
        // Given
        var device = new Device("Original Name", "Original Description");
        var updateDeviceDto = new UpdateDeviceDto("  ", "  ");

        // When
        updateDeviceDto.applyToEntity(device);

        // Then
        assertThat(device.getName()).isEqualTo("Original Name");
        assertThat(device.getDescription()).isEqualTo("Original Description");
    }
}