package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.sfl.sensors.SensorDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Test
	void shouldMarshallToJson() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorDtos = List.of(
			new SensorDto(1L, "Sensor 1", "temperature", Set.of("read")),
			new SensorDto(2L, "Sensor 2", "humidity", Set.of("read", "write"))
		);

		var deviceDto = new DeviceDto(
			deviceId,
			"Test Device",
			"Device description",
			sensorDtos,
			Instant.parse("2024-01-01T00:00:00Z"),
			Instant.parse("2024-01-02T00:00:00Z")
		);

		String json = objectMapper.writeValueAsString(deviceDto);

		assertThat(json).isNotNull();
		assertThat(json).contains("\"id\":\"" + deviceId + "\"");
		assertThat(json).contains("\"name\":\"Test Device\"");
		assertThat(json).contains("\"description\":\"Device description\"");
		assertThat(json).contains("\"sensors\"");
		assertThat(json).contains("\"createdAt\":\"2024-01-01T00:00:00Z\"");
		assertThat(json).contains("\"updatedAt\":\"2024-01-02T00:00:00Z\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		var deviceId = UUID.randomUUID();
		String json = """
			{
				"id": "%s",
				"name": "Device Name",
				"description": "Device description text",
				"sensors": [
					{
						"id": 1,
						"name": "Sensor 1",
						"type": "temperature",
						"capabilities": ["read"]
					}
				],
				"createdAt": "2024-01-01T12:00:00Z",
				"updatedAt": "2024-01-02T12:00:00Z"
			}
			""".formatted(deviceId);

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.id()).isEqualTo(deviceId);
		assertThat(deviceDto.name()).isEqualTo("Device Name");
		assertThat(deviceDto.description()).isEqualTo("Device description text");
		assertThat(deviceDto.sensors()).hasSize(1);
		assertThat(deviceDto.sensors().get(0).id()).isEqualTo(1L);
		assertThat(deviceDto.createdAt()).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
		assertThat(deviceDto.updatedAt()).isEqualTo(Instant.parse("2024-01-02T12:00:00Z"));
	}

	@Test
	void shouldUnmarshallFromJsonWithEmptySensors() throws Exception {
		var deviceId = UUID.randomUUID();
		String json = """
			{
				"id": "%s",
				"name": "Simple Device",
				"description": "Description",
				"sensors": [],
				"createdAt": "2024-01-01T00:00:00Z",
				"updatedAt": "2024-01-01T00:00:00Z"
			}
			""".formatted(deviceId);

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.id()).isEqualTo(deviceId);
		assertThat(deviceDto.name()).isEqualTo("Simple Device");
		assertThat(deviceDto.sensors()).isEmpty();
	}

	@Test
	void shouldUnmarshallFromJsonWithMultipleSensors() throws Exception {
		var deviceId = UUID.randomUUID();
		String json = """
			{
				"id": "%s",
				"name": "Multi-Sensor Device",
				"description": "Multiple sensors",
				"sensors": [
					{
						"id": 1,
						"name": "Temp Sensor",
						"type": "temperature",
						"capabilities": ["read"]
					},
					{
						"id": 2,
						"name": "Humidity Sensor",
						"type": "humidity",
						"capabilities": ["read", "write"]
					},
					{
						"id": 3,
						"name": "Pressure Sensor",
						"type": "pressure",
						"capabilities": ["read"]
					}
				],
				"createdAt": "2024-01-01T00:00:00Z",
				"updatedAt": "2024-01-02T00:00:00Z"
			}
			""".formatted(deviceId);

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.sensors()).hasSize(3);
		assertThat(deviceDto.sensors()).extracting(SensorDto::id).containsExactly(1L, 2L, 3L);
		assertThat(deviceDto.sensors()).extracting(SensorDto::name)
			.containsExactly("Temp Sensor", "Humidity Sensor", "Pressure Sensor");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorDtos = List.of(
			new SensorDto(1L, "Sensor 1", "type1", Set.of("cap1")),
			new SensorDto(2L, "Sensor 2", "type2", Set.of("cap2"))
		);

		var originalDto = new DeviceDto(
			deviceId,
			"Symmetric Device",
			"Symmetric description",
			sensorDtos,
			Instant.parse("2024-01-01T00:00:00Z"),
			Instant.parse("2024-01-02T00:00:00Z")
		);

		String json = objectMapper.writeValueAsString(originalDto);
		DeviceDto deserializedDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deserializedDto.id()).isEqualTo(originalDto.id());
		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
		assertThat(deserializedDto.sensors()).hasSize(originalDto.sensors().size());
		assertThat(deserializedDto.createdAt()).isEqualTo(originalDto.createdAt());
		assertThat(deserializedDto.updatedAt()).isEqualTo(originalDto.updatedAt());
	}

	@Test
	void shouldHandleNullDescription() throws Exception {
		var deviceId = UUID.randomUUID();
		String json = """
			{
				"id": "%s",
				"name": "Device Without Description",
				"sensors": [],
				"createdAt": "2024-01-01T00:00:00Z",
				"updatedAt": "2024-01-01T00:00:00Z"
			}
			""".formatted(deviceId);

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.description()).isNull();
	}

	@Test
	void shouldHandleNullSensors() throws Exception {
		var deviceId = UUID.randomUUID();
		String json = """
			{
				"id": "%s",
				"name": "Device With Null Sensors",
				"createdAt": "2024-01-01T00:00:00Z",
				"updatedAt": "2024-01-01T00:00:00Z"
			}
			""".formatted(deviceId);

		DeviceDto deviceDto = objectMapper.readValue(json, DeviceDto.class);

		assertThat(deviceDto).isNotNull();
		assertThat(deviceDto.sensors()).isNull();
	}
}