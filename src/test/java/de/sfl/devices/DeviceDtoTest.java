package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
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
		// Given
		var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
		var now = Instant.parse("2026-03-15T10:00:00Z");
		var sensors = List.of(
			new DeviceDto.AssignedSensorDto(1L, now),
			new DeviceDto.AssignedSensorDto(2L, now)
		);
		var dto = new DeviceDto(id, "Test Device", "A test device", sensors, now, now);

		// When
		String json = objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"id\":\"550e8400-e29b-41d4-a716-446655440000\"");
		assertThat(json).contains("\"name\":\"Test Device\"");
		assertThat(json).contains("\"description\":\"A test device\"");
		assertThat(json).contains("\"sensors\"");
		assertThat(json).contains("\"sensorId\":1");
		assertThat(json).contains("\"sensorId\":2");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		// Given
		String json = """
			{
				"id": "550e8400-e29b-41d4-a716-446655440000",
				"name": "Device Name",
				"description": "Device Description",
				"sensors": [
					{"sensorId": 1, "assignedAt": 1710500400.000000000},
					{"sensorId": 2, "assignedAt": 1710500400.000000000}
				],
				"createdAt": 1710500400.000000000,
				"updatedAt": 1710500400.000000000
			}
			""";

		// When
		DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.id()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
		assertThat(dto.name()).isEqualTo("Device Name");
		assertThat(dto.description()).isEqualTo("Device Description");
		assertThat(dto.sensors()).hasSize(2);
		assertThat(dto.sensors().get(0).sensorId()).isEqualTo(1L);
		assertThat(dto.sensors().get(1).sensorId()).isEqualTo(2L);
	}

	@Test
	void shouldUnmarshallFromJsonWithEmptySensors() throws Exception {
		// Given
		String json = """
			{
				"id": "550e8400-e29b-41d4-a716-446655440000",
				"name": "Device Name",
				"description": "Device Description",
				"sensors": [],
				"createdAt": 1710500400.000000000,
				"updatedAt": 1710500400.000000000
			}
			""";

		// When
		DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.sensors()).isEmpty();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		// Given
		var id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
		var now = Instant.parse("2026-03-15T10:00:00Z");
		var sensors = List.of(new DeviceDto.AssignedSensorDto(1L, now));
		var originalDto = new DeviceDto(id, "Symmetric Device", "Symmetric Desc", sensors, now, now);

		// When
		String json = objectMapper.writeValueAsString(originalDto);
		DeviceDto deserializedDto = objectMapper.readValue(json, DeviceDto.class);

		// Then
		assertThat(deserializedDto.id()).isEqualTo(originalDto.id());
		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
		assertThat(deserializedDto.sensors()).hasSize(1);
		assertThat(deserializedDto.sensors().get(0).sensorId()).isEqualTo(1L);
		assertThat(deserializedDto.createdAt()).isEqualTo(originalDto.createdAt());
		assertThat(deserializedDto.updatedAt()).isEqualTo(originalDto.updatedAt());
	}

	@Test
	void shouldUnmarshallWithNullSensors() throws Exception {
		// Given
		String json = """
			{
				"id": "550e8400-e29b-41d4-a716-446655440000",
				"name": "Device Name",
				"description": "Device Description",
				"sensors": null,
				"createdAt": 1710500400.000000000,
				"updatedAt": 1710500400.000000000
			}
			""";

		// When
		DeviceDto dto = objectMapper.readValue(json, DeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.sensors()).isNull();
	}
}
