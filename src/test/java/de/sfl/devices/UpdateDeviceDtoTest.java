package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		var updateDeviceDto = new UpdateDeviceDto(
				"Updated Gateway",
				"Updated building gateway"
		);

		String json = objectMapper.writeValueAsString(updateDeviceDto);

		assertThat(json).isNotNull();
		assertThat(json).contains("\"name\":\"Updated Gateway\"");
		assertThat(json).contains("\"description\":\"Updated building gateway\"");
	}

	@Test
	void shouldUnmarshallFromJsonWithAllFields() throws Exception {
		String json = """
			{
				"name": "Factory Controller",
				"description": "Controller for factory floor"
			}
			""";

		UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto).isNotNull();
		assertThat(updateDeviceDto.name()).isEqualTo("Factory Controller");
		assertThat(updateDeviceDto.description()).isEqualTo("Controller for factory floor");
	}

	@Test
	void shouldUnmarshallFromJsonWithOnlyName() throws Exception {
		String json = """
			{
				"name": "Factory Controller"
			}
			""";

		UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto).isNotNull();
		assertThat(updateDeviceDto.name()).isEqualTo("Factory Controller");
		assertThat(updateDeviceDto.description()).isNull();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var originalDto = new UpdateDeviceDto(
				"Boiler Controller",
				"Monitors and controls boiler systems"
		);

		String json = objectMapper.writeValueAsString(originalDto);
		UpdateDeviceDto deserializedDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(deserializedDto).isEqualTo(originalDto);
	}

	@Test
	void applyToEntity_shouldUpdateOnlyProvidedFields() {
		var device = new TestDevice(UUID.randomUUID(), "Original Name", "Original description");
		var updateDto = new UpdateDeviceDto("Updated Name", null);

		updateDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Updated Name");
		assertThat(device.getDescription()).isEqualTo("Original description");
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
