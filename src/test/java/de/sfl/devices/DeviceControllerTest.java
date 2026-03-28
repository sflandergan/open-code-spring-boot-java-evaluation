package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sfl.sensors.SensorNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
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
		var deviceId = UUID.randomUUID();
		var createDeviceDto = new CreateDeviceDto("Device A", "Primary device");
		var createdDevice = createDevice(deviceId, "Device A", "Primary device");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Device A")))
			.andExpect(jsonPath("$.description", is("Primary device")))
			.andExpect(jsonPath("$.sensorIds", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var createDeviceDto = new CreateDeviceDto("", "Primary device");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated description");
		var updatedDevice = createDevice(deviceId, "Updated Device", "Updated description");

		when(deviceService.updateDevice(deviceId, updateDeviceDto.name(), updateDeviceDto.description())).thenReturn(updatedDevice);
		when(deviceService.getDeviceSensorIds(deviceId)).thenReturn(List.of(1L, 2L));

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Updated Device")))
			.andExpect(jsonPath("$.sensorIds", hasSize(2)));

		verify(deviceService).updateDevice(deviceId, updateDeviceDto.name(), updateDeviceDto.description());
	}

	@Test
	void updateDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto(" ", "Updated description");

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void deleteDevice_shouldReturnNoContent() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void assignSensor_shouldReturnDeviceWithSensorIds() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 10L;
		var device = createDevice(deviceId, "Device A", "Primary device");

		when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);
		when(deviceService.getDeviceSensorIds(deviceId)).thenReturn(List.of(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.sensorIds[0]", is(10)));
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;

		when(deviceService.assignSensor(deviceId, sensorId)).thenThrow(new SensorNotFoundException(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
			.andExpect(status().isNotFound());
	}

	@Test
	void getDevice_shouldReturnDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Device A", "Primary device");

		when(deviceService.getDevice(deviceId)).thenReturn(device);
		when(deviceService.getDeviceSensorIds(deviceId)).thenReturn(List.of(1L, 2L));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Device A")))
			.andExpect(jsonPath("$.sensorIds", hasSize(2)));
	}

	@Test
	void getDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNotFound());
	}

	private TestDevice createDevice(UUID id, String name, String description) {
		return new TestDevice(
			id,
			name,
			description,
			Instant.parse("2026-03-28T10:00:00Z"),
			Instant.parse("2026-03-28T10:05:00Z")
		);
	}

	static class TestDevice extends Device {
		private final Instant createdAt;
		private final Instant updatedAt;

		TestDevice(UUID id, String name, String description, Instant createdAt, Instant updatedAt) {
			super(name, description);
			this.id = id;
			this.createdAt = createdAt;
			this.updatedAt = updatedAt;
		}

		@Override
		public Instant getCreatedAt() {
			return createdAt;
		}

		@Override
		public Instant getUpdatedAt() {
			return updatedAt;
		}
	}
}
