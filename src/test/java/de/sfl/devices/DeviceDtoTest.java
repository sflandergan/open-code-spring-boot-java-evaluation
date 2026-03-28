package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.sfl.sensors.SensorDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceDtoTest {

	private final ObjectMapper objectMapper = JsonMapper.builder()
		.findAndAddModules()
		.build();

	@Test
	void marshalDeviceDto_shouldSerializeToJsonCorrectly() throws Exception {
		var dto = new DeviceDto(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"Gateway",
			"Main gateway",
			List.of(new SensorDto(7L, "Sensor A", "temperature", Set.of("read"))),
			Instant.parse("2026-03-28T10:15:30Z"),
			Instant.parse("2026-03-28T11:15:30Z")
		);

		var json = objectMapper.writeValueAsString(dto);

		var expectedJson = """
			{
				"id":"11111111-1111-1111-1111-111111111111",
				"name":"Gateway",
				"description":"Main gateway",
				"sensors":[
					{
						"id":7,
						"name":"Sensor A",
						"type":"temperature",
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
	void unmarshalJson_shouldDeserializeToDeviceDtoCorrectly() throws Exception {
		var json = """
			{
				"id":"11111111-1111-1111-1111-111111111111",
				"name":"Gateway",
				"description":"Main gateway",
				"sensors":[
					{
						"id":7,
						"name":"Sensor A",
						"type":"temperature",
						"capabilities":["read"]
					}
				],
				"createdAt":"2026-03-28T10:15:30Z",
				"updatedAt":"2026-03-28T11:15:30Z"
			}
			""";

		var result = objectMapper.readValue(json, DeviceDto.class);

		assertThat(result).isEqualTo(new DeviceDto(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"Gateway",
			"Main gateway",
			List.of(new SensorDto(7L, "Sensor A", "temperature", Set.of("read"))),
			Instant.parse("2026-03-28T10:15:30Z"),
			Instant.parse("2026-03-28T11:15:30Z")
		));
	}
}
