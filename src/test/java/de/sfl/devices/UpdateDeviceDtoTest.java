package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDeviceDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldMarshallToJson() throws Exception {
		// Given
		var dto = new UpdateDeviceDto("Updated Name", "Updated Description");

		// When
		String json = objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"name\":\"Updated Name\"");
		assertThat(json).contains("\"description\":\"Updated Description\"");
	}

	@Test
	void shouldMarshallToJsonWithNullFieldsExcluded() throws Exception {
		// Given
		var dto = new UpdateDeviceDto("Updated Name", null);

		// When
		String json = objectMapper.writeValueAsString(dto);

		// Then
		assertThat(json).contains("\"name\":\"Updated Name\"");
		assertThat(json).doesNotContain("\"description\"");
	}

	@Test
	void shouldUnmarshallFromJson() throws Exception {
		// Given
		String json = """
			{
				"name": "New Name",
				"description": "New Description"
			}
			""";

		// When
		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("New Name");
		assertThat(dto.description()).isEqualTo("New Description");
	}

	@Test
	void shouldUnmarshallWithNullNameWhenNameIsMissing() throws Exception {
		// Given
		String json = """
			{
				"description": "Only Description"
			}
			""";

		// When
		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isNull();
		assertThat(dto.description()).isEqualTo("Only Description");
	}

	@Test
	void shouldUnmarshallWithNullDescriptionWhenDescriptionIsMissing() throws Exception {
		// Given
		String json = """
			{
				"name": "Only Name"
			}
			""";

		// When
		UpdateDeviceDto dto = objectMapper.readValue(json, UpdateDeviceDto.class);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.name()).isEqualTo("Only Name");
		assertThat(dto.description()).isNull();
	}

	@Test
	void shouldMarshallAndUnmarshallSymmetrically() throws Exception {
		// Given
		var originalDto = new UpdateDeviceDto("Symmetric Name", "Symmetric Description");

		// When
		String json = objectMapper.writeValueAsString(originalDto);
		UpdateDeviceDto deserializedDto = objectMapper.readValue(json, UpdateDeviceDto.class);

		// Then
		assertThat(deserializedDto.name()).isEqualTo(originalDto.name());
		assertThat(deserializedDto.description()).isEqualTo(originalDto.description());
	}

	@Test
	void applyTo_shouldUpdateBothFields() {
		// Given
		var device = new Device("Original Name", "Original Description");
		var dto = new UpdateDeviceDto("New Name", "New Description");

		// When
		dto.applyTo(device);

		// Then
		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("New Description");
	}

	@Test
	void applyTo_shouldUpdateOnlyNameWhenDescriptionIsNull() {
		// Given
		var device = new Device("Original Name", "Original Description");
		var dto = new UpdateDeviceDto("New Name", null);

		// When
		dto.applyTo(device);

		// Then
		assertThat(device.getName()).isEqualTo("New Name");
		assertThat(device.getDescription()).isEqualTo("Original Description");
	}

	@Test
	void applyTo_shouldUpdateOnlyDescriptionWhenNameIsNull() {
		// Given
		var device = new Device("Original Name", "Original Description");
		var dto = new UpdateDeviceDto(null, "New Description");

		// When
		dto.applyTo(device);

		// Then
		assertThat(device.getName()).isEqualTo("Original Name");
		assertThat(device.getDescription()).isEqualTo("New Description");
	}

	@Test
	void applyTo_shouldNotUpdateAnythingWhenBothFieldsAreNull() {
		// Given
		var device = new Device("Original Name", "Original Description");
		var dto = new UpdateDeviceDto(null, null);

		// When
		dto.applyTo(device);

		// Then
		assertThat(device.getName()).isEqualTo("Original Name");
		assertThat(device.getDescription()).isEqualTo("Original Description");
	}
}
