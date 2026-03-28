package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new DeviceDto(
			UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
			"Device A",
			"Description A",
			List.of(new DeviceDto.AssignedSensorDto(1L, "Sensor A", "temperature", Set.of("read"))),
			Instant.parse("2026-03-28T12:00:00Z"),
			Instant.parse("2026-03-28T12:01:00Z")
		);

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"Device A\"");
		assertThat(json).contains("\"sensors\"");
		assertThat(json).contains("\"createdAt\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"id": "123e4567-e89b-12d3-a456-426614174000",
				"name": "Device A",
				"description": "Description A",
				"sensors": [
					{
						"id": 1,
						"name": "Sensor A",
						"type": "temperature",
						"capabilities": ["read"]
					}
				],
				"createdAt": "2026-03-28T12:00:00Z",
				"updatedAt": "2026-03-28T12:01:00Z"
			}
			""";

		var dto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(dto.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
		assertThat(dto.sensors()).hasSize(1);
		assertThat(dto.sensors().getFirst().name()).isEqualTo("Sensor A");
		assertThat(dto.createdAt()).isEqualTo(Instant.parse("2026-03-28T12:00:00Z"));
	}
}
