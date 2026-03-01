package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        // Given
        var createDeviceDto = new CreateDeviceDto("Device-001", "Test device");

        // When
        String json = objectMapper.writeValueAsString(createDeviceDto);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Device-001\"");
        assertThat(json).contains("\"description\":\"Test device\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        // Given
        String json = """
            {
                "name": "Device-001",
                "description": "Test device"
            }
            """;

        // When
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isEqualTo("Device-001");
        assertThat(createDeviceDto.description()).isEqualTo("Test device");
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        // Given
        var originalDto = new CreateDeviceDto("Device-001", "Test device");

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
                "description": "Test device"
            }
            """;

        // When
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isNull();
        assertThat(createDeviceDto.description()).isEqualTo("Test device");
    }

    @Test
    void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
        // Given
        String json = """
            {
                "name": "Device-001"
            }
            """;

        // When
        CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

        // Then
        assertThat(createDeviceDto).isNotNull();
        assertThat(createDeviceDto.name()).isEqualTo("Device-001");
        assertThat(createDeviceDto.description()).isNull();
    }

    @Test
    void toEntity_shouldConvertToDeviceEntity() {
        // Given
        var createDeviceDto = new CreateDeviceDto("Device-001", "Test device");

        // When
        Device device = createDeviceDto.toEntity();

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo("Device-001");
        assertThat(device.getDescription()).isEqualTo("Test device");
    }
}
