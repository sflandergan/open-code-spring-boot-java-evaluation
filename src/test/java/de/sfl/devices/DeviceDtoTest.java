package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Test
	void shouldMarshallToJson() throws Exception {
		var deviceDto = new DeviceDto(
			UUID.fromString("f4fd6078-5448-42cd-89b5-69b2d6aa92cd"),
			"Device A",
			"Primary device",
			List.of(1L, 2L),
			Instant.parse("2026-03-28T10:00:00Z"),
			Instant.parse("2026-03-28T10:05:00Z")
		);

		var json = objectMapper.writeValueAsString(deviceDto);

		assertThat(json).contains("\"id\":\"f4fd6078-5448-42cd-89b5-69b2d6aa92cd\"");
		assertThat(json).contains("\"name\":\"Device A\"");
		assertThat(json).contains("\"description\":\"Primary device\"");
		assertThat(json).contains("\"sensorIds\":[1,2]");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"id": "f4fd6078-5448-42cd-89b5-69b2d6aa92cd",
				"name": "Device A",
				"description": "Primary device",
				"sensorIds": [1, 2],
				"createdAt": "2026-03-28T10:00:00Z",
				"updatedAt": "2026-03-28T10:05:00Z"
			}
			""";

		var deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto.id()).isEqualTo(UUID.fromString("f4fd6078-5448-42cd-89b5-69b2d6aa92cd"));
		assertThat(deviceDto.name()).isEqualTo("Device A");
		assertThat(deviceDto.description()).isEqualTo("Primary device");
		assertThat(deviceDto.sensorIds()).containsExactly(1L, 2L);
		assertThat(deviceDto.createdAt()).isEqualTo(Instant.parse("2026-03-28T10:00:00Z"));
		assertThat(deviceDto.updatedAt()).isEqualTo(Instant.parse("2026-03-28T10:05:00Z"));
	}
}
