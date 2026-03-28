package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new UpdateDeviceDto("Device A", "Description A");

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Device A\"");
		assertThat(json).contains("\"description\":\"Description A\"");
	}

	@Test
	void shouldUnmarshallFromJsonWithOptionalFields() throws Exception {
		var json = """
			{
				"name": "Updated Device"
			}
			""";

		var dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(dto.name()).isEqualTo("Updated Device");
		assertThat(dto.description()).isNull();
	}

	@Test
	void hasValidName_shouldReturnFalseWhenBlank() {
		var dto = new UpdateDeviceDto("   ", "Description");

		assertThat(dto.hasValidName()).isFalse();
	}

	@Test
	void hasValidDescription_shouldReturnFalseWhenBlank() {
		var dto = new UpdateDeviceDto("Name", "   ");

		assertThat(dto.hasValidDescription()).isFalse();
	}
}
