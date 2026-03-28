package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var createDeviceDto = new CreateDeviceDto("Device A", "Primary device");

		var json = objectMapper.writeValueAsString(createDeviceDto);

		assertThat(json).contains("\"name\":\"Device A\"");
		assertThat(json).contains("\"description\":\"Primary device\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"name": "Device A",
				"description": "Primary device"
			}
			""";

		var createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(createDeviceDto.name()).isEqualTo("Device A");
		assertThat(createDeviceDto.description()).isEqualTo("Primary device");
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var createDeviceDto = new CreateDeviceDto("Device A", "Primary device");

		var device = createDeviceDto.toEntity();

		assertThat(device.getName()).isEqualTo("Device A");
		assertThat(device.getDescription()).isEqualTo("Primary device");
	}
}
