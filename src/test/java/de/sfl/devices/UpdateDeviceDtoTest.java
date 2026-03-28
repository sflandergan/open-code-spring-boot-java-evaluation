package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated description");

		var json = objectMapper.writeValueAsString(updateDeviceDto);

		var expectedJson = """
			{
				"name":"Updated Device",
				"description":"Updated description"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void shouldUnmarshallFromJsonWithMissingFields() throws Exception {
		var json = """
			{
				"name":"Updated Device"
			}
			""";

		var updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto.name()).isEqualTo("Updated Device");
		assertThat(updateDeviceDto.description()).isNull();
	}

	@Test
	void applyToEntity_shouldUpdateOnlyProvidedFields() {
		var device = new TestDevice(UUID.randomUUID(), "Original Device", "Original description");
		var updateDeviceDto = new UpdateDeviceDto(null, "Updated description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Original Device");
		assertThat(device.getDescription()).isEqualTo("Updated description");
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
