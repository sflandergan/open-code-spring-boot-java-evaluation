package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        // Given
        var createDeviceDto = new CreateDeviceDto(
            "New Device",
            "Device Description"
        );

        // When
        String json = objectMapper.writeValueAsString(createDeviceDto);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"New Device\"");
        assertThat(json).contains("\"description\":\"Device Description\"");
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
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isEqualTo("Device Name");
        assertThat(createDeviceDto.description()).isEqualTo("Device Description");
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        // Given
        var originalDto = new CreateDeviceDto(
            "Symmetric Device",
            "Symmetric Description"
        );

        // When
        String json = objectMapper.writeValueAsString(originalDto);
        CreateDeviceDto deserializedDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
        assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
    }

    @Test
    void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
        // Given
        String json = """
            {
                "description": "Device Description"
            }
            """;

        // When
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isNull();
        assertThat(createDeviceDto.description()).isEqualTo("Device Description");
    }

    @Test
    void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
        // Given
        String json = """
            {
                "name": "Device Name"
            }
            """;

        // When
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isEqualTo("Device Name");
        assertThat(createDeviceDto.description()).isNull();
    }

    @Test
    void toEntity_shouldConvertToDeviceEntity() {
        // Given
        var createDeviceDto = new CreateDeviceDto(
            "Test Device",
            "Test Description"
        );

        // When
        Device device = createDeviceDto.toEntity();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo("Test Device");
        assertThat(device.getDescription()).isEqualTo("Test Description");
    }
}