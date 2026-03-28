package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new CreateDeviceDto("Device-001", "Main floor device");

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Device-001\"");
		assertThat(json).contains("\"description\":\"Main floor device\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"name": "Device-001",
				"description": "Main floor device"
			}
			""";

		var dto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(dto.name()).isEqualTo("Device-001");
		assertThat(dto.description()).isEqualTo("Main floor device");
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var dto = new CreateDeviceDto("Device-001", "Main floor device");

		var device = dto.toEntity();

		assertThat(device.getName()).isEqualTo("Device-001");
		assertThat(device.getDescription()).isEqualTo("Main floor device");
	}
}
