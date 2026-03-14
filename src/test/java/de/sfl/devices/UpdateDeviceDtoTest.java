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
			"Updated Device",
			"Updated description"
		);

		String json = objectMapper.writeValueAsString(updateDeviceDto);

		assertThat(json).isNotNull();
		assertThat(json).contains("\"name\":\"Updated Device\"");
		assertThat(json).contains("\"description\":\"Updated description\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		String json = """
			{
				"name": "Device Name",
				"description": "Device description text"
			}
			""";

		UpdateDeviceDto updateDeviceDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(updateDeviceDto).isNotNull();
		assertThat(updateDeviceDto.name()).isEqualTo("Device Name");
		assertThat(updateDeviceDto.description()).isEqualTo("Device description text");
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		var originalDto = new UpdateDeviceDto(
			"Symmetric Device",
			"Description for symmetric testing"
		);

		String json = objectMapper.writeValueAsString(originalDto);
		UpdateDeviceDto deserializedDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
	}

	@Test
	void applyToEntity_shouldUpdateDeviceName() {
		var updateDeviceDto = new UpdateDeviceDto("New Name", "Old Description");
		var device = new Device("Old Name", "Old Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("Old Description");
	}

	@Test
	void applyToEntity_shouldUpdateDeviceDescription() {
		var updateDeviceDto = new UpdateDeviceDto("Old Name", "New Description");
		var device = new Device("Old Name", "Old Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Old Name");
		assertThat(device.getDescription()).isEqualTo("New Description");
	}

	@Test
	void applyToEntity_shouldUpdateBothFields() {
		var updateDeviceDto = new UpdateDeviceDto("New Name", "New Description");
		var device = new Device("Old Name", "Old Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("New Description");
	}

	@Test
	void applyToEntity_shouldNotModifyDeviceWhenFieldsAreNull() {
		var updateDeviceDto = new UpdateDeviceDto(null, null);
		var device = new Device("Original Name", "Original Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Original Name");
		assertThat(device.getDescription()).isEqualTo("Original Description");
	}

	@Test
	void applyToEntity_shouldNotModifyDeviceWhenNameIsNull() {
		var updateDeviceDto = new UpdateDeviceDto(null, "New Description");
		var device = new Device("Original Name", "Original Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("Original Name");
		assertThat(device.getDescription()).isEqualTo("New Description");
	}

	@Test
	void applyToEntity_shouldNotModifyDeviceWhenDescriptionIsNull() {
		var updateDeviceDto = new UpdateDeviceDto("New Name", null);
		var device = new Device("Original Name", "Original Description");

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("Original Description");
	}

	@Test
	void applyToEntity_shouldUpdateDeviceWithExistingId() {
		var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated Description");
		var deviceId = UUID.randomUUID();
		var device = new Device("Original Name", "Original Description");
		device.setId(deviceId);

		updateDeviceDto.applyToEntity(device);

		assertThat(device.getId()).isEqualTo(deviceId);
		assertThat(device.getName()).isEqualTo("Updated Name");
		assertThat(device.getDescription()).isEqualTo("Updated Description");
	}
}