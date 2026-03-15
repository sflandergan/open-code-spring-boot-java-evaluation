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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

	private static final UUID DEVICE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

	@Test
	void createDevice_shouldReturnCreatedDevice() throws Exception {
		var createDto = new CreateDeviceDto("Test Device", "Test Description");
		var createdDevice = createDevice(DEVICE_ID, "Test Device", "Test Description");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(DEVICE_ID.toString())))
				.andExpect(jsonPath("$.name", is("Test Device")))
				.andExpect(jsonPath("$.description", is("Test Description")))
				.andExpect(jsonPath("$.sensors", hasSize(0)));

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
		var invalidDto = new CreateDeviceDto("Device Name", "");

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsNull() throws Exception {
		String invalidJson = """
				{
					"description": "Some description"
				}
				""";

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getDevice_shouldReturnDeviceWithSensors() throws Exception {
		var device = createDevice(DEVICE_ID, "Test Device", "Test Description");
		var assignment = new DeviceSensor(new DeviceSensorId(DEVICE_ID, 1L));

		when(deviceService.getDevice(DEVICE_ID)).thenReturn(device);
		when(deviceService.getDeviceSensors(DEVICE_ID)).thenReturn(List.of(assignment));

		mockMvc.perform(get("/api/devices/{id}", DEVICE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(DEVICE_ID.toString())))
				.andExpect(jsonPath("$.name", is("Test Device")))
				.andExpect(jsonPath("$.description", is("Test Description")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].sensorId", is(1)));

		verify(deviceService).getDevice(DEVICE_ID);
		verify(deviceService).getDeviceSensors(DEVICE_ID);
	}

	@Test
	void getDevice_shouldReturn404WhenNotFound() throws Exception {
		when(deviceService.getDevice(DEVICE_ID)).thenThrow(new DeviceNotFoundException(DEVICE_ID));

		mockMvc.perform(get("/api/devices/{id}", DEVICE_ID))
				.andExpect(status().isNotFound());

		verify(deviceService).getDevice(DEVICE_ID);
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
		var updatedDevice = createDevice(DEVICE_ID, "Updated Name", "Updated Description");

		when(deviceService.updateDevice(eq(DEVICE_ID), any(UpdateDeviceDto.class))).thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/{id}", DEVICE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(DEVICE_ID.toString())))
				.andExpect(jsonPath("$.name", is("Updated Name")))
				.andExpect(jsonPath("$.description", is("Updated Description")));

		verify(deviceService).updateDevice(eq(DEVICE_ID), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn404WhenNotFound() throws Exception {
		var updateDto = new UpdateDeviceDto("Name", "Description");

		when(deviceService.updateDevice(eq(DEVICE_ID), any(UpdateDeviceDto.class)))
				.thenThrow(new DeviceNotFoundException(DEVICE_ID));

		mockMvc.perform(put("/api/devices/{id}", DEVICE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		String invalidJson = """
				{
					"name": "",
					"description": "Valid Description"
				}
				""";

		mockMvc.perform(put("/api/devices/{id}", DEVICE_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteDevice_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/api/devices/{id}", DEVICE_ID))
				.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(DEVICE_ID);
	}

	@Test
	void deleteDevice_shouldReturn404WhenNotFound() throws Exception {
		doThrow(new DeviceNotFoundException(DEVICE_ID)).when(deviceService).deleteDevice(DEVICE_ID);

		mockMvc.perform(delete("/api/devices/{id}", DEVICE_ID))
				.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturnDeviceWithSensors() throws Exception {
		var device = createDevice(DEVICE_ID, "Test Device", "Test Description");
		var assignment = new DeviceSensor(new DeviceSensorId(DEVICE_ID, 1L));

		when(deviceService.assignSensor(DEVICE_ID, 1L)).thenReturn(device);
		when(deviceService.getDeviceSensors(DEVICE_ID)).thenReturn(List.of(assignment));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", DEVICE_ID, 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(DEVICE_ID.toString())))
				.andExpect(jsonPath("$.name", is("Test Device")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].sensorId", is(1)));

		verify(deviceService).assignSensor(DEVICE_ID, 1L);
		verify(deviceService).getDeviceSensors(DEVICE_ID);
	}

	@Test
	void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
		when(deviceService.assignSensor(DEVICE_ID, 1L)).thenThrow(new DeviceNotFoundException(DEVICE_ID));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", DEVICE_ID, 1L))
				.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorNotFound() throws Exception {
		when(deviceService.assignSensor(DEVICE_ID, 999L)).thenThrow(new SensorNotFoundException(999L));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", DEVICE_ID, 999L))
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
