package de.sfl.sensors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSensorDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		// Given
		var createSensorDto = new CreateSensorDto(
			"New Sensor",
			"sensor",
			Set.of("read", "write")
		);

		// When
		String json = objectMapper.writeValueAsString(createSensorDto);

		// Then
		assertThat(json).isNotNull();
		assertThat(json).contains("\"name\":\"New Sensor\"");
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
				"name": "Sensor Name",
				"type": "actuator",
				"capabilities": ["read", "write", "execute"]
			}
			""";

		// When
		CreateSensorDto createSensorDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(createSensorDto).isNotNull();
		assertThat(createSensorDto.name()).isEqualTo("Sensor Name");
		assertThat(createSensorDto.type()).isEqualTo("actuator");
		assertThat(createSensorDto.capabilities()).containsExactlyInAnyOrder("read", "write", "execute");
	}

	@Test
	void shouldUnmarshallFromJsonWithEmptyCapabilities() throws Exception {
		// Given
		String json = """
			{
				"name": "Simple Sensor",
				"type": "sensor",
				"capabilities": []
			}
			""";

		// When
		CreateSensorDto createSensorDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(createSensorDto).isNotNull();
		assertThat(createSensorDto.name()).isEqualTo("Simple Sensor");
		assertThat(createSensorDto.type()).isEqualTo("sensor");
		assertThat(createSensorDto.capabilities()).isEmpty();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		// Given
		var originalDto = new CreateSensorDto(
			"Symmetric Sensor",
			"gateway",
			Set.of("capability1", "capability2", "capability3")
		);

		// When
		String json = objectMapper.writeValueAsString(originalDto);
		CreateSensorDto deserializedDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.type()).isEqualTo(originalDto.type());
		assertThat(deserializedDto.capabilities()).containsExactlyInAnyOrderElementsOf(originalDto.capabilities());
	}

	@Test
	void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
		// Given
		String json = """
			{
				"type": "sensor",
				"capabilities": ["read"]
			}
			""";

		// When
		CreateSensorDto createSensorDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(createSensorDto).isNotNull();
		assertThat(createSensorDto.name()).isNull();
		assertThat(createSensorDto.type()).isEqualTo("sensor");
		assertThat(createSensorDto.capabilities()).containsExactly("read");
	}

	@Test
	void shouldUnmarshallWithNullTypeWhenTypeIsMissing() throws Exception {
		// Given
		String json = """
			{
				"name": "Sensor Name",
				"capabilities": ["read"]
			}
			""";

		// When
		CreateSensorDto createSensorDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(createSensorDto).isNotNull();
		assertThat(createSensorDto.name()).isEqualTo("Sensor Name");
		assertThat(createSensorDto.type()).isNull();
		assertThat(createSensorDto.capabilities()).containsExactly("read");
	}

	@Test
	void shouldUnmarshallWithNullCapabilitiesWhenCapabilitiesIsMissing() throws Exception {
		// Given
		String json = """
			{
				"name": "Sensor Name",
				"type": "sensor"
			}
			""";

		// When
		CreateSensorDto createSensorDto = objectMapper.readValue(json, CreateSensorDto.class);

		// Then
		assertThat(createSensorDto).isNotNull();
		assertThat(createSensorDto.name()).isEqualTo("Sensor Name");
		assertThat(createSensorDto.type()).isEqualTo("sensor");
		assertThat(createSensorDto.capabilities()).isNull();
	}

	@Test
	void toEntity_shouldConvertToSensorEntity() {
		// Given
		var createSensorDto = new CreateSensorDto(
			"Test Sensor",
			"sensor",
			Set.of("read", "write")
		);

		// When
		Sensor sensor = createSensorDto.toEntity();

		// Then
		assertThat(sensor).isNotNull();
		assertThat(sensor.getName()).isEqualTo("Test Sensor");
		assertThat(sensor.getType()).isEqualTo("sensor");
		assertThat(sensor.getCapabilities()).containsExactlyInAnyOrder("read", "write");
	}

	@Test
	void toEntity_shouldHandleEmptyCapabilities() {
		// Given
		var createSensorDto = new CreateSensorDto(
			"Simple Sensor",
			"sensor",
			Set.of()
		);

		// When
		Sensor sensor = createSensorDto.toEntity();

		// Then
		assertThat(sensor).isNotNull();
		assertThat(sensor.getName()).isEqualTo("Simple Sensor");
		assertThat(sensor.getType()).isEqualTo("sensor");
		assertThat(sensor.getCapabilities()).isEmpty();
	}
}
