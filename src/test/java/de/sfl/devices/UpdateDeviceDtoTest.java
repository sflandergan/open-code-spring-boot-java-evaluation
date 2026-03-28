package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = JsonMapper.builder()
		.findAndAddModules()
		.build();

	@Test
	void marshalUpdateDeviceDto_shouldSerializeToJsonCorrectly() throws Exception {
		var dto = new UpdateDeviceDto("Gateway", "Updated description");

		var json = objectMapper.writeValueAsString(dto);

		var expectedJson = """
			{
				"name":"Gateway",
				"description":"Updated description"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void unmarshalJson_shouldDeserializeToUpdateDeviceDtoCorrectly() throws Exception {
		var json = """
			{
				"name":"Gateway",
				"description":"Updated description"
			}
			""";

		var result = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(result).isEqualTo(new UpdateDeviceDto("Gateway", "Updated description"));
	}

	@Test
	void applyToEntity_shouldUpdateOnlyProvidedFields() {
		var device = new TestDevice(UUID.randomUUID(), "Gateway", "Main gateway");
		var dto = new UpdateDeviceDto(null, "Updated description");

		dto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Gateway");
		assertThat(device.getDescription()).isEqualTo("Updated description");
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
