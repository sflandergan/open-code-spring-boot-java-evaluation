package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Test
	void shouldMarshallToJson() throws Exception {
		var dto = new DeviceDto(
			UUID.fromString("34e04c7c-91c8-4c8b-b7f1-c6f831eeb8ae"),
			"Device-001",
			"Main floor device",
			List.of(1L, 2L),
			Instant.parse("2026-03-28T10:00:00Z"),
			Instant.parse("2026-03-28T10:05:00Z")
		);

		var json = objectMapper.writeValueAsString(dto);

		assertThat(json).contains("\"id\":\"34e04c7c-91c8-4c8b-b7f1-c6f831eeb8ae\"");
		assertThat(json).contains("\"sensorIds\":[1,2]");
		assertThat(json).contains("\"createdAt\":\"2026-03-28T10:00:00Z\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"id": "34e04c7c-91c8-4c8b-b7f1-c6f831eeb8ae",
				"name": "Device-001",
				"description": "Main floor device",
				"sensorIds": [1, 2, 3],
				"createdAt": "2026-03-28T10:00:00Z",
				"updatedAt": "2026-03-28T10:05:00Z"
			}
			""";

		var dto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(dto.id()).isEqualTo(UUID.fromString("34e04c7c-91c8-4c8b-b7f1-c6f831eeb8ae"));
		assertThat(dto.name()).isEqualTo("Device-001");
		assertThat(dto.sensorIds()).containsExactly(1L, 2L, 3L);
		assertThat(dto.createdAt()).isEqualTo(Instant.parse("2026-03-28T10:00:00Z"));
	}

	@Test
	void from_shouldConvertEntityAndSensorIdsToDto() {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device-001", "Main floor device");

		var dto = DeviceDto.from(device, List.of(10L, 20L));

		assertThat(dto.id()).isEqualTo(deviceId);
		assertThat(dto.name()).isEqualTo("Device-001");
		assertThat(dto.sensorIds()).containsExactly(10L, 20L);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
