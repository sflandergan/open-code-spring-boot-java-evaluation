package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeviceDtoTest {

	private final ObjectMapper objectMapper = JsonMapper.builder()
		.findAndAddModules()
		.build();

	@Test
	void marshalCreateDeviceDto_shouldSerializeToJsonCorrectly() throws Exception {
		var dto = new CreateDeviceDto("Gateway", "Main gateway");

		var json = objectMapper.writeValueAsString(dto);

		var expectedJson = """
			{
				"name":"Gateway",
				"description":"Main gateway"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void unmarshalJson_shouldDeserializeToCreateDeviceDtoCorrectly() throws Exception {
		var json = """
			{
				"name":"Gateway",
				"description":"Main gateway"
			}
			""";

		var result = objectMapper.readValue(json, CreateDeviceDto.class);

		assertThat(result).isEqualTo(new CreateDeviceDto("Gateway", "Main gateway"));
	}

	@Test
	void toEntity_shouldConvertToDeviceEntity() {
		var dto = new CreateDeviceDto("Gateway", "Main gateway");

		var device = dto.toEntity();

		assertThat(device.getName()).isEqualTo("Gateway");
		assertThat(device.getDescription()).isEqualTo("Main gateway");
	}
}
