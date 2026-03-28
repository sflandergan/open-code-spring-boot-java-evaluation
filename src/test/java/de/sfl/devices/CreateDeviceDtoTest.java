package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var createDeviceDto = new CreateDeviceDto("Weather Station", "Outdoor monitoring unit");

		var json = objectMapper.writeValueAsString(createDeviceDto);

		var expectedJson = """
			{
				"name":"Weather Station",
				"description":"Outdoor monitoring unit"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"name":"Edge Gateway",
				"description":"Collects sensor data"
			}
			""";

		var createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(createDeviceDto.name()).isEqualTo("Edge Gateway");
		assertThat(createDeviceDto.description()).isEqualTo("Collects sensor data");
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var createDeviceDto = new CreateDeviceDto("Edge Gateway", "Collects sensor data");

		var device = createDeviceDto.toEntity();

		assertThat(device.getName()).isEqualTo("Edge Gateway");
		assertThat(device.getDescription()).isEqualTo("Collects sensor data");
	}
}
