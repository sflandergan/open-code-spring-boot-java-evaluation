package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var createDeviceDto = new CreateDeviceDto(
			"New Device",
			"Device description"
		);

		String json = objectMapper.writeValueAsString(createDeviceDto);

		assertThat(json).isNotNull();
		assertThat(json).contains("\"name\":\"New Device\"");
		assertThat(json).contains("\"description\":\"Device description\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"name": "Device Name",
				"description": "Device description text"
			}
			""";

		CreateDeviceDto createDeviceDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(createDeviceDto).isNotNull();
		assertThat(createDeviceDto.name()).isEqualTo("Device Name");
		assertThat(createDeviceDto.description()).isEqualTo("Device description text");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var originalDto = new CreateDeviceDto(
			"Symmetric Device",
			"Description for symmetric testing"
		);

		String json = objectMapper.writeValueAsString(originalDto);
		CreateDeviceDto deserializedDto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var createDeviceDto = new CreateDeviceDto(
			"Test Device",
			"Test device description"
		);

		Device device = createDeviceDto.toEntity();

		assertThat(device).isNotNull();
		assertThat(device.getName()).isEqualTo("Test Device");
		assertThat(device.getDescription()).isEqualTo("Test device description");
		assertThat(device.getId()).isNotNull();
	}

	@Test
	void toEntity_shouldCreateNewIdEachTime() {
		var createDeviceDto = new CreateDeviceDto("Device 1", "Description");

		Device device1 = createDeviceDto.toEntity();
		Device device2 = createDeviceDto.toEntity();

		assertThat(device1.getId()).isNotNull();
		assertThat(device2.getId()).isNotNull();
		assertThat(device1.getId()).isNotEqualTo(device2.getId());
	}
}