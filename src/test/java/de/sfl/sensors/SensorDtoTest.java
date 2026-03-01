package de.sfl.sensors;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.sfl.sensors.SensorDto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SensorDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		// Given
		var sensorDto = new SensorDto(
			1L,
			"Test Sensor",
			"sensor",
			Set.of("read", "write")
		);

		// When
		String json = objectMapper.writeValueAsString(sensorDto);

		// Then
		assertThat(json).isNotNull();
		assertThat(json).contains("\"id\":1");
		assertThat(json).contains("\"name\":\"Test Sensor\"");
		assertThat(json).contains("\"type\":\"sensor\"");
		assertThat(json).contains("\"capabilities\"");
		assertThat(json).contains("\"read\"");
		assertThat(json).contains("\"write\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		// Given
		String json = """
			{
				"id": 42,
				"name": "Sensor Name",
				"type": "actuator",
				"capabilities": ["read", "write", "execute"]
			}
			""";

		// When
		SensorDto sensorDto = objectMapper.readValue(json, SensorDto.class);

		// Then
		assertThat(sensorDto).isNotNull();
		assertThat(sensorDto.id()).isEqualTo(42L);
		assertThat(sensorDto.name()).isEqualTo("Sensor Name");
		assertThat(sensorDto.type()).isEqualTo("actuator");
		assertThat(sensorDto.capabilities()).containsExactlyInAnyOrder("read", "write", "execute");
	}

	@Test
	void shouldUnmarshallFromJsonWithEmptyCapabilities() throws Exception {
		// Given
		String json = """
			{
				"id": 1,
				"name": "Simple Sensor",
				"type": "sensor",
				"capabilities": []
			}
			""";

		// When
		SensorDto sensorDto = objectMapper.readValue(json, SensorDto.class);

		// Then
		assertThat(sensorDto).isNotNull();
		assertThat(sensorDto.id()).isEqualTo(1L);
		assertThat(sensorDto.name()).isEqualTo("Simple Sensor");
		assertThat(sensorDto.type()).isEqualTo("sensor");
		assertThat(sensorDto.capabilities()).isEmpty();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		// Given
		var originalDto = new SensorDto(
			123L,
			"Symmetric Sensor",
			"gateway",
			Set.of("capability1", "capability2", "capability3")
		);

		// When
		String json = objectMapper.writeValueAsString(originalDto);
		SensorDto deserializedDto = objectMapper.readValue(json, SensorDto.class);

		// Then
		assertThat(deserializedDto.id()).isEqualTo(originalDto.id());
		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.type()).isEqualTo(originalDto.type());
		assertThat(deserializedDto.capabilities()).containsExactlyInAnyOrderElementsOf(originalDto.capabilities());
	}
}
