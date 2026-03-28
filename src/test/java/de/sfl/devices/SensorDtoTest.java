package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SensorDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var sensorDto = new SensorDto(7L, "Humidity Sensor", "sensor", Set.of("read"));

		var json = objectMapper.writeValueAsString(sensorDto);

		var expectedJson = """
			{
				"id":7,
				"name":"Humidity Sensor",
				"type":"sensor",
				"capabilities":["read"]
			}
			""";

		assertThat(objectMapper.readTree(json)).isEqualTo(objectMapper.readTree(expectedJson));
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var json = """
			{
				"id":8,
				"name":"Relay",
				"type":"actuator",
				"capabilities":["write","toggle"]
			}
			""";

		var sensorDto = objectMapper.readValue(json, SensorDto.class);

		assertThat(sensorDto.id()).isEqualTo(8L);
		assertThat(sensorDto.name()).isEqualTo("Relay");
		assertThat(sensorDto.type()).isEqualTo("actuator");
		assertThat(sensorDto.capabilities()).containsExactlyInAnyOrder("write", "toggle");
	}
}
