package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.sfl.sensors.SensorDto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Test
	void shouldMarshallToJson() throws Exception {
		var deviceDto = new DeviceDto(
				UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
				"Edge Gateway",
				"Main building gateway",
				List.of(new SensorDto(1L, "Temperature", "sensor", Set.of("read"))),
				Instant.parse("2026-03-28T10:15:30Z"),
				Instant.parse("2026-03-28T10:16:30Z")
		);

		String json = objectMapper.writeValueAsString(deviceDto);

		String expectedJson = """
			{
				"id":"123e4567-e89b-12d3-a456-426614174000",
				"name":"Edge Gateway",
				"description":"Main building gateway",
				"sensors":[
					{
						"id":1,
						"name":"Temperature",
						"type":"sensor",
						"capabilities":["read"]
					}
				],
				"createdAt":"2026-03-28T10:15:30Z",
				"updatedAt":"2026-03-28T10:16:30Z"
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"id":"123e4567-e89b-12d3-a456-426614174000",
				"name":"Factory Controller",
				"description":"Controller for factory floor",
				"sensors":[
					{
						"id":42,
						"name":"Humidity",
						"type":"sensor",
						"capabilities":["read"]
					}
				],
				"createdAt":"2026-03-28T10:15:30Z",
				"updatedAt":"2026-03-28T10:16:30Z"
			}
			""";

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
		assertThat(deviceDto.name()).isEqualTo("Factory Controller");
		assertThat(deviceDto.description()).isEqualTo("Controller for factory floor");
		assertThat(deviceDto.sensors()).hasSize(1);
		assertThat(deviceDto.sensors().get(0).id()).isEqualTo(42L);
		assertThat(deviceDto.createdAt()).isEqualTo(Instant.parse("2026-03-28T10:15:30Z"));
		assertThat(deviceDto.updatedAt()).isEqualTo(Instant.parse("2026-03-28T10:16:30Z"));
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var originalDto = new DeviceDto(
				UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
				"Boiler Controller",
				"Controls boiler pressure",
				List.of(
						new SensorDto(1L, "Temperature", "sensor", Set.of("read")),
						new SensorDto(2L, "Pressure", "sensor", Set.of("read"))
				),
				Instant.parse("2026-03-28T10:15:30Z"),
				Instant.parse("2026-03-28T10:16:30Z")
		);

		String json = objectMapper.writeValueAsString(originalDto);
		DeviceDto deserializedDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deserializedDto).isEqualTo(originalDto);
	}
}
