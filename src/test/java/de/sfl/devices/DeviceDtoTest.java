package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build();

	@Test
	void shouldMarshallToJson() throws Exception {
		var deviceDto = new DeviceDto(
			UUID.fromString("a8dcc9d0-845b-4f6d-ad02-7fd547f26fd0"),
			"Weather Station",
			"Outdoor monitoring unit",
			List.of(new SensorDto(7L, "Humidity Sensor", "sensor", java.util.Set.of("read"))),
			Instant.parse("2026-03-28T10:15:30Z"),
			Instant.parse("2026-03-28T11:15:30Z")
		);

		var json = objectMapper.writeValueAsString(deviceDto);

		var expectedJson = """
			{
				"id":"a8dcc9d0-845b-4f6d-ad02-7fd547f26fd0",
				"name":"Weather Station",
				"description":"Outdoor monitoring unit",
				"sensors":[
					{
						"id":7,
						"name":"Humidity Sensor",
						"type":"sensor",
						"capabilities":["read"]
					}
				],
				"createdAt":"2026-03-28T10:15:30Z",
				"updatedAt":"2026-03-28T11:15:30Z"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"id":"a8dcc9d0-845b-4f6d-ad02-7fd547f26fd0",
				"name":"Weather Station",
				"description":"Outdoor monitoring unit",
				"sensors":[
					{
						"id":7,
						"name":"Humidity Sensor",
						"type":"sensor",
						"capabilities":["read"]
					}
				],
				"createdAt":"2026-03-28T10:15:30Z",
				"updatedAt":"2026-03-28T11:15:30Z"
			}
			""";

		var deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto.id()).isEqualTo(UUID.fromString("a8dcc9d0-845b-4f6d-ad02-7fd547f26fd0"));
		assertThat(deviceDto.name()).isEqualTo("Weather Station");
		assertThat(deviceDto.description()).isEqualTo("Outdoor monitoring unit");
		assertThat(deviceDto.sensors()).hasSize(1);
		assertThat(deviceDto.createdAt()).isEqualTo(Instant.parse("2026-03-28T10:15:30Z"));
		assertThat(deviceDto.updatedAt()).isEqualTo(Instant.parse("2026-03-28T11:15:30Z"));
	}
}
