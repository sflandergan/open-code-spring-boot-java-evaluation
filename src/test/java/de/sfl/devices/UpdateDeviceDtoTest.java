package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated description");

		var json = objectMapper.writeValueAsString(updateDeviceDto);

		assertThat(json).contains("\"name\":\"Updated Device\"");
		assertThat(json).contains("\"description\":\"Updated description\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"name": "Updated Device",
				"description": "Updated description"
			}
			""";

		var updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto.name()).isEqualTo("Updated Device");
		assertThat(updateDeviceDto.description()).isEqualTo("Updated description");
	}

	@Test
	void shouldUnmarshallWithNullFieldsWhenJsonIsEmpty() throws Exception {
		String json = "{}";

		var updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto.name()).isNull();
		assertThat(updateDeviceDto.description()).isNull();
	}

	@Test
	void applyToEntity_shouldUpdateOnlyProvidedFields() {
		var device = new Device("Device A", "Initial description");
		var updateDeviceDto = new UpdateDeviceDto("Updated Device", null);

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Updated Device");
		assertThat(device.getDescription()).isEqualTo("Initial description");
	}
}
