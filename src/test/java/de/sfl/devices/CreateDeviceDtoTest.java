package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var createDeviceDto = new CreateDeviceDto(
				"Edge Gateway",
				"Main building gateway"
		);

		String json = objectMapper.writeValueAsString(createDeviceDto);

		assertThat(json).isNotNull();
		assertThat(json).contains("\"name\":\"Edge Gateway\"");
		assertThat(json).contains("\"description\":\"Main building gateway\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"name": "Factory Controller",
				"description": "Controller for factory floor"
			}
			""";

		CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(createDeviceDto).isNotNull();
		assertThat(createDeviceDto.name()).isEqualTo("Factory Controller");
		assertThat(createDeviceDto.description()).isEqualTo("Controller for factory floor");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var originalDto = new CreateDeviceDto(
				"Boiler Controller",
				"Monitors and controls boiler systems"
		);

		String json = objectMapper.writeValueAsString(originalDto);
		CreateDeviceDto deserializedDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(deserializedDto).isEqualTo(originalDto);
	}

	@Test
	void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
		String json = """
			{
				"description": "Controller for factory floor"
			}
			""";

		CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(createDeviceDto).isNotNull();
		assertThat(createDeviceDto.name()).isNull();
		assertThat(createDeviceDto.description()).isEqualTo("Controller for factory floor");
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var createDeviceDto = new CreateDeviceDto(
				"Edge Gateway",
				"Main building gateway"
		);

		Device device = createDeviceDto.toEntity();

		assertThat(device).isNotNull();
		assertThat(device.getName()).isEqualTo("Edge Gateway");
		assertThat(device.getDescription()).isEqualTo("Main building gateway");
	}
}
