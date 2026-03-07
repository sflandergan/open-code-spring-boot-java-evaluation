package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new UpdateDeviceDto("Updated Name", "Updated description");

		String json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Updated Name\"");
		assertThat(json).contains("\"description\":\"Updated description\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
				{
					"name": "Updated Name",
					"description": "Updated description"
				}
				""";

		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("Updated Name");
		assertThat(dto.description()).isEqualTo("Updated description");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var original = new UpdateDeviceDto("Symmetric Name", "Symmetric description");

		String json = objectMapper.writeValueAsString(original);
		UpdateDeviceDto deserialized = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(deserialized.name()).isEqualTo(original.name());
		assertThat(deserialized.description()).isEqualTo(original.description());
	}

	@Test
	void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
		String json = """
				{
					"description": "Updated description"
				}
				""";

		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(dto.name()).isNull();
		assertThat(dto.description()).isEqualTo("Updated description");
	}

	@Test
	void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
		String json = """
				{
					"name": "Updated Name"
				}
				""";

		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(dto.name()).isEqualTo("Updated Name");
		assertThat(dto.description()).isNull();
	}

	@Test
	void applyToEntity_shouldUpdateDeviceFields() {
		Device device = new Device("Original Name", "Original description");
		var dto = new UpdateDeviceDto("New Name", "New description");

		dto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("New description");
	}

	@Test
	void applyToEntity_shouldNotUpdateNullFields() {
		Device device = new Device("Original Name", "Original description");
		var dto = new UpdateDeviceDto(null, null);

		dto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Original Name");
		assertThat(device.getDescription()).isEqualTo("Original description");
	}
}
