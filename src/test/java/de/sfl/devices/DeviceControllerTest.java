package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DeviceService deviceService;

	@Test
	void createDevice_shouldReturnCreatedDevice() throws Exception {
		var dto = new CreateDeviceDto("Device A", "Description");
		var created = new TestDevice(UUID.randomUUID(), "Device A", "Description");

		when(deviceService.createDevice(any(Device.class))).thenReturn(created);

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name", is("Device A")))
			.andExpect(jsonPath("$.description", is("Description")));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameBlank() throws Exception {
		var dto = new CreateDeviceDto("", "Description");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void getDevice_shouldReturnDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Device A", "Description");
		when(deviceService.getDevice(deviceId)).thenReturn(device);

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Device A")));
	}

	@Test
	void getDevice_shouldReturn404WhenMissing() throws Exception {
		var deviceId = UUID.randomUUID();
		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNotFound());
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var dto = new UpdateDeviceDto("New Name", "New Description");
		var updated = new TestDevice(deviceId, "New Name", "New Description");

		when(deviceService.updateDevice(deviceId, "New Name", "New Description")).thenReturn(updated);

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("New Name")));
	}

	@Test
	void updateDevice_shouldReturn400WhenProvidedNameBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var dto = new UpdateDeviceDto("   ", "New Description");

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void deleteDevice_shouldReturn204() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void assignSensor_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updated = new TestDevice(deviceId, "Device A", "Description");
		when(deviceService.assignSensor(deviceId, 99L)).thenReturn(updated);

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 99L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())));
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorMissing() throws Exception {
		var deviceId = UUID.randomUUID();
		when(deviceService.assignSensor(deviceId, 99L)).thenThrow(new AssignedSensorNotFoundException(99L));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 99L))
			.andExpect(status().isNotFound());
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
			this.setName(name);
			this.setDescription(description);
		}

		@Override
		public Instant getCreatedAt() {
			return Instant.parse("2026-03-28T12:00:00Z");
		}

		@Override
		public Instant getUpdatedAt() {
			return Instant.parse("2026-03-28T12:00:00Z");
		}
	}
}
