package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new CreateDeviceDto("My Device", "A test device");

		String json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"My Device\"");
		assertThat(json).contains("\"description\":\"A test device\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
				{
					"name": "My Device",
					"description": "A test device"
				}
				""";

		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("My Device");
		assertThat(dto.description()).isEqualTo("A test device");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var original = new CreateDeviceDto("Symmetric Device", "Symmetric description");

		String json = objectMapper.writeValueAsString(original);
		CreateDeviceDto deserialized = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(deserialized.name()).isEqualTo(original.name());
		assertThat(deserialized.description()).isEqualTo(original.description());
	}

	@Test
	void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
		String json = """
				{
					"description": "A test device"
				}
				""";

		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(dto.name()).isNull();
		assertThat(dto.description()).isEqualTo("A test device");
	}

	@Test
	void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
		String json = """
				{
					"name": "My Device"
				}
				""";

		CreateDeviceDto dto = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(dto.name()).isEqualTo("My Device");
		assertThat(dto.description()).isNull();
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var dto = new CreateDeviceDto("Test Device", "Test description");

		Device device = dto.toEntity();

		assertThat(device).isNotNull();
		assertThat(device.getName()).isEqualTo("Test Device");
		assertThat(device.getDescription()).isEqualTo("Test description");
	}
}
