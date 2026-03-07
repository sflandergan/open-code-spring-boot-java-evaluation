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

	@Test
	void createDevice_shouldReturn201WithDeviceDto() throws Exception {
		var createDto = new CreateDeviceDto("New Device", "A device description");
		var savedDevice = createTestDevice(UUID.randomUUID(), "New Device", "A device description");

		when(deviceService.createDevice(any(Device.class))).thenReturn(savedDevice);

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("New Device")))
				.andExpect(jsonPath("$.description", is("A device description")))
				.andExpect(jsonPath("$.sensors", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("", "A device description");

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
	void updateDevice_shouldReturn200WithUpdatedDeviceDto() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated description");
		var updatedDevice = createTestDevice(deviceId, "Updated Name", "Updated description");

		when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class))).thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Updated Name")))
				.andExpect(jsonPath("$.description", is("Updated description")));

		verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("Updated Name", "Updated description");

		when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class)))
				.thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var invalidDto = new UpdateDeviceDto("", "Updated description");

		mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
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
	void deleteDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();

		doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
				.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturn200WithDeviceDtoIncludingSensor() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;
		var device = createTestDevice(deviceId, "My Device", "Description");
		var sensor = createTestSensor(sensorId, "My Sensor", "temperature", Set.of("read"));

		when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(sensor));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("My Device")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].name", is("My Sensor")));

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 1L;

		when(deviceService.assignSensor(deviceId, sensorId))
				.thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
				.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorNotFound() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;

		when(deviceService.assignSensor(deviceId, sensorId))
				.thenThrow(new SensorNotFoundException(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
				.andExpect(status().isNotFound());
	}

	@Test
	void getDevice_shouldReturn200WithDeviceDtoAndSensors() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createTestDevice(deviceId, "My Device", "Description");
		var sensor = createTestSensor(1L, "My Sensor", "temperature", Set.of("read"));

		when(deviceService.getDevice(deviceId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(sensor));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("My Device")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].name", is("My Sensor")));

		verify(deviceService).getDevice(deviceId);
		verify(deviceService).getDeviceSensors(deviceId);
	}

	@Test
	void getDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
				.andExpect(status().isNotFound());
	}

	private Device createTestDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	private Sensor createTestSensor(Long id, String name, String type, Set<String> capabilities) {
		return new TestSensor(id, name, type, capabilities);
	}

	static class TestDevice extends Device {
		TestDevice(UUID id, String name, String description) {
			super(name, description);
			this.id = id;
		}
	}

	static class TestSensor extends Sensor {
		TestSensor(Long id, String name, String type, Set<String> capabilities) {
			super(name, type, capabilities);
			this.id = id;
		}
	}
}
