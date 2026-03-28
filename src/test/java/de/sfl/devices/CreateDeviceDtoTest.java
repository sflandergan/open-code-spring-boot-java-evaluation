package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new CreateDeviceDto("Device A", "Description A");

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Device A\"");
		assertThat(json).contains("\"description\":\"Description A\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"name": "Device A",
				"description": "Description A"
			}
			""";

		var dto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(dto.name()).isEqualTo("Device A");
		assertThat(dto.description()).isEqualTo("Description A");
	}

	@Test
	void toEntity_shouldConvertDtoToDevice() {
		var dto = new CreateDeviceDto("Device A", "Description A");

		var device = dto.toEntity();

		assertThat(device.getName()).isEqualTo("Device A");
		assertThat(device.getDescription()).isEqualTo("Description A");
	}
}
