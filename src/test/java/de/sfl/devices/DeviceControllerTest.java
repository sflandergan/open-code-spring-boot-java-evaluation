package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
		var createDto = new CreateDeviceDto("New Device", "Device description");
		var createdDevice = createTestDevice(deviceId, "New Device", "Device description");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(deviceId.toString()))
			.andExpect(jsonPath("$.name").value("New Device"))
			.andExpect(jsonPath("$.description").value("Device description"));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("", "Description");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void createDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("Name", "");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void getDevice_shouldReturnDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createTestDevice(deviceId, "Test Device", "Description");

		when(deviceService.getDevice(deviceId)).thenReturn(device);

		mockMvc.perform(get("/api/devices/" + deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(deviceId.toString()))
			.andExpect(jsonPath("$.name").value("Test Device"))
			.andExpect(jsonPath("$.description").value("Description"));

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void getDevice_shouldReturn404WhenNotFound() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/" + deviceId))
			.andExpect(status().isNotFound());

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
		var device = createTestDevice(deviceId, "Updated Name", "Updated Description");

		when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class))).thenReturn(device);

		mockMvc.perform(put("/api/devices/" + deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(deviceId.toString()))
			.andExpect(jsonPath("$.name").value("Updated Name"))
			.andExpect(jsonPath("$.description").value("Updated Description"));

		verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var invalidDto = new UpdateDeviceDto("", "Description");

		mockMvc.perform(put("/api/devices/" + deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void updateDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var invalidDto = new UpdateDeviceDto("Name", "");

		mockMvc.perform(put("/api/devices/" + deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void updateDevice_shouldReturn404WhenNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("Name", "Description");

		when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class)))
			.thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(put("/api/devices/" + deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
			.andExpect(status().isNotFound());

		verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
	}

	@Test
	void deleteDevice_shouldReturn204() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/" + deviceId))
			.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void deleteDevice_shouldReturn404WhenNotFound() throws Exception {
		var deviceId = UUID.randomUUID();

		doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/" + deviceId))
			.andExpect(status().isNotFound());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void assignSensor_shouldReturnDeviceWithSensor() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = createTestDevice(deviceId, "Device", "Description");

		when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);

		mockMvc.perform(put("/api/devices/" + deviceId + "/sensors/" + sensorId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(deviceId.toString()))
			.andExpect(jsonPath("$.name").value("Device"));

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;

		when(deviceService.assignSensor(deviceId, sensorId))
			.thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(put("/api/devices/" + deviceId + "/sensors/" + sensorId))
			.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;

		when(deviceService.assignSensor(deviceId, sensorId))
			.thenThrow(new SensorNotFoundException(sensorId));

		mockMvc.perform(put("/api/devices/" + deviceId + "/sensors/" + sensorId))
			.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	private Device createTestDevice(UUID id, String name, String description) {
		var device = new TestDevice(id, name, description);
		return device;
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			setId(id);
		}
	}
}