package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		// Given
		var dto = new CreateDeviceDto("Test Device", "A test device");

		// When
		String json = objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"name\":\"Test Device\"");
		assertThat(json).contains("\"description\":\"A test device\"");
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
		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("Device Name");
		assertThat(dto.description()).isEqualTo("Device Description");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		// Given
		var originalDto = new CreateDeviceDto("Symmetric Device", "Symmetric Description");

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
				"description": "Some description"
			}
			""";

		// When
		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isNull();
		assertThat(dto.description()).isEqualTo("Some description");
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
		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("Device Name");
		assertThat(dto.description()).isNull();
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		// Given
		var dto = new CreateDeviceDto("Test Device", "A test device");

		// When
		Device device = dto.toEntity();

		// Then
		assertThat(device).isNotNull();
		assertThat(device.getName()).isEqualTo("Test Device");
		assertThat(device.getDescription()).isEqualTo("A test device");
		assertThat(device.getId()).isNull();
	}
}
