package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new UpdateDeviceDto("Updated Name", "Updated description");

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Updated Name\"");
		assertThat(json).contains("\"description\":\"Updated description\"");
	}

	@Test
	void shouldUnmarshallFromJsonWithOptionalFields() throws Exception {
		var json = """
			{
				"description": "Updated description"
			}
			""";

		var dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(dto.name()).isNull();
		assertThat(dto.description()).isEqualTo("Updated description");
	}

	@Test
	void applyToEntity_shouldUpdateOnlyProvidedFields() {
		var dto = new UpdateDeviceDto(null, "Updated description");
		var device = new TestDevice(UUID.randomUUID(), "Original", "Original description");

		dto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Original");
		assertThat(device.getDescription()).isEqualTo("Updated description");
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
