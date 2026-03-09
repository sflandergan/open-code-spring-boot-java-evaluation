package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldMarshallToJson() throws Exception {
        var createDeviceDto = new CreateDeviceDto("Test Device", "Test Description");

        String json = objectMapper.writeValueAsString(createDeviceDto);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"name\":\"Test Device\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
    }

    @Test
    void shouldUnmarshallFromJson() throws Exception {
        String json = """
            {
                "name": "Device Name",
                "description": "Device Description"
            }
            """;

        CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.name()).isEqualTo("Device Name");
        assertThat(dto.description()).isEqualTo("Device Description");
    }

    @Test
    void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
        var originalDto = new CreateDeviceDto("Symmetric Device", "Symmetric Description");

        String json = objectMapper.writeValueAsString(originalDto);
        CreateDeviceDto deserializedDto = objectMapper.readValue(json, CreateDeviceDto.class);

        assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
        assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
    }

    @Test
    void toEntity_shouldConvertToDeviceEntity() {
        var createDeviceDto = new CreateDeviceDto("Test Device", "Test Description");

        Device device = createDeviceDto.toEntity();

        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo("Test Device");
        assertThat(device.getDescription()).isEqualTo("Test Description");
    }
}
