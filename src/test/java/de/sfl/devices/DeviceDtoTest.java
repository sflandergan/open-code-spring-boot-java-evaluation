package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.sfl.sensors.SensorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
	}

	@Test
	void shouldMarshallToJson() throws Exception {
		var id = UUID.randomUUID();
		var now = Instant.now();
		var sensor = new SensorDto(1L, "Sensor 1", "temperature", Set.of("read"));
		var dto = new DeviceDto(id, "My Device", "A test device", List.of(sensor), now, now);

		String json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"name\":\"My Device\"");
		assertThat(json).contains("\"description\":\"A test device\"");
		assertThat(json).contains("\"sensors\"");
		assertThat(json).contains("\"Sensor 1\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var id = UUID.randomUUID();
		String json = """
				{
					"id": "%s",
					"name": "My Device",
					"description": "A test device",
					"sensors": [
						{
							"id": 1,
							"name": "Sensor 1",
							"type": "temperature",
							"capabilities": ["read"]
						}
					],
					"createdAt": "2025-01-01T00:00:00Z",
					"updatedAt": "2025-01-01T00:00:00Z"
				}
				""".formatted(id);

		DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(dto).isNotNull();
		assertThat(dto.id()).isEqualTo(id);
		assertThat(dto.name()).isEqualTo("My Device");
		assertThat(dto.description()).isEqualTo("A test device");
		assertThat(dto.sensors()).hasSize(1);
		assertThat(dto.sensors().get(0).name()).isEqualTo("Sensor 1");
	}

	@Test
	void shouldUnmarshallWithEmptySensorsList() throws Exception {
		var id = UUID.randomUUID();
		String json = """
				{
					"id": "%s",
					"name": "My Device",
					"description": "A test device",
					"sensors": [],
					"createdAt": "2025-01-01T00:00:00Z",
					"updatedAt": "2025-01-01T00:00:00Z"
				}
				""".formatted(id);

		DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(dto.sensors()).isEmpty();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var id = UUID.randomUUID();
		var now = Instant.parse("2025-01-01T00:00:00Z");
		var sensor = new SensorDto(1L, "Sensor 1", "temperature", Set.of("read"));
		var original = new DeviceDto(id, "Symmetric Device", "Symmetric description", List.of(sensor), now, now);

		String json = objectMapper.writeValueAsString(original);
		DeviceDto deserialized = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deserialized.id()).isEqualTo(original.id());
		assertThat(deserialized.name()).isEqualTo(original.name());
		assertThat(deserialized.description()).isEqualTo(original.description());
		assertThat(deserialized.sensors()).hasSize(1);
		assertThat(deserialized.sensors().get(0).name()).isEqualTo("Sensor 1");
	}
}
