package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
	void createDevice_shouldReturnCreatedDeviceWhenInputIsValid() throws Exception {
		var deviceId = UUID.randomUUID();
		var createDeviceDto = new CreateDeviceDto("Device-001", "Main floor device");
		var createdDevice = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of());

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Device-001")))
			.andExpect(jsonPath("$.description", is("Main floor device")))
			.andExpect(jsonPath("$.sensorIds", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
		verify(deviceService).getDeviceSensors(deviceId);
	}

	@Test
	void createDevice_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
		var createDeviceDto = new CreateDeviceDto("", "Main floor device");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isBadRequest());

		verify(deviceService, never()).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
		var createDeviceDto = new CreateDeviceDto("Device-001", "");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void getDevice_shouldReturnDeviceWhenDeviceExists() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceService.getDevice(deviceId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(1L, 2L));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.sensorIds", hasSize(2)));

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void getDevice_shouldReturnNotFoundWhenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNotFound());
	}

	@Test
	void updateDevice_shouldReturnUpdatedDeviceWhenInputIsValid() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated description");
		var updatedDevice = createDevice(deviceId, "Updated Name", "Updated description");

		when(deviceService.updateDevice(deviceId, "Updated Name", "Updated description")).thenReturn(updatedDevice);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(5L));

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("Updated Name")))
			.andExpect(jsonPath("$.sensorIds", hasSize(1)));

		verify(deviceService).updateDevice(deviceId, "Updated Name", "Updated description");
	}

	@Test
	void updateDevice_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("", null);

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void updateDevice_shouldReturnNotFoundWhenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Updated Name", "Updated description");

		when(deviceService.updateDevice(deviceId, "Updated Name", "Updated description"))
			.thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturnUpdatedDeviceWhenBothResourcesExist() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 10L;
		var device = createDevice(deviceId, "Device-001", "Main floor device");

		when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sensorIds", hasSize(1)))
			.andExpect(jsonPath("$.sensorIds[0]", is(10)));

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void assignSensor_shouldReturnNotFoundWhenSensorDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;

		when(deviceService.assignSensor(deviceId, sensorId)).thenThrow(new SensorNotFoundException(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
			.andExpect(status().isNotFound());
	}

	@Test
	void deleteDevice_shouldReturnNoContentWhenDeviceExists() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void deleteDevice_shouldReturnNotFoundWhenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
			.andExpect(status().isNotFound());
	}

	private Device createDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}
}
