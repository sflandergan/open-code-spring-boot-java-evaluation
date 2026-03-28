package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;

import java.util.List;
import java.util.Set;
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
	void createDevice_shouldReturnCreatedDevice_whenInputIsValid() throws Exception {
		var deviceId = UUID.randomUUID();
		var createDeviceDto = new CreateDeviceDto("Edge Gateway", "Main building gateway");
		var createdDevice = createDevice(deviceId, "Edge Gateway", "Main building gateway");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of());

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDeviceDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.name", is("Edge Gateway")))
				.andExpect(jsonPath("$.description", is("Main building gateway")))
				.andExpect(jsonPath("$.sensors", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400_whenNameIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("", "Main building gateway");

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(deviceService, never()).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400_whenDescriptionIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("Edge Gateway", "   ");

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(deviceService, never()).createDevice(any(Device.class));
	}

	@Test
	void getDevice_shouldReturnDeviceWithSensors_whenDeviceExists() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Factory Controller", "Factory floor controller");
		var sensor = createSensor(7L, "Temperature", "sensor", Set.of("read"));

		when(deviceService.getDevice(deviceId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(sensor));

		mockMvc.perform(get("/api/devices/{id}", deviceId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.name", is("Factory Controller")))
				.andExpect(jsonPath("$.description", is("Factory floor controller")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].id", is(7)))
				.andExpect(jsonPath("$.sensors[0].name", is("Temperature")));

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void getDevice_shouldReturn404_whenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();
		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{id}", deviceId))
				.andExpect(status().isNotFound());

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice_whenInputIsValid() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDto = new UpdateDeviceDto("Updated Device", "Updated description");
		var updatedDevice = createDevice(deviceId, "Updated Device", "Updated description");

		when(deviceService.updateDevice(deviceId, "Updated Device", "Updated description")).thenReturn(updatedDevice);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of());

		mockMvc.perform(put("/api/devices/{id}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.name", is("Updated Device")))
				.andExpect(jsonPath("$.description", is("Updated description")));

		verify(deviceService).updateDevice(deviceId, "Updated Device", "Updated description");
	}

	@Test
	void updateDevice_shouldReturn400_whenNameIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var invalidDto = new UpdateDeviceDto("   ", "Updated description");

		mockMvc.perform(put("/api/devices/{id}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(deviceService, never()).updateDevice(deviceId, "   ", "Updated description");
	}

	@Test
	void assignSensor_shouldReturnUpdatedDevice_whenDeviceAndSensorExist() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 9L;
		var device = createDevice(deviceId, "Edge Gateway", "Main building gateway");
		var sensor = createSensor(sensorId, "Humidity", "sensor", Set.of("read"));

		when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);
		when(deviceService.getDeviceSensors(deviceId)).thenReturn(List.of(sensor));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].id", is(9)));

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void assignSensor_shouldReturn404_whenSensorDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();
		var sensorId = 999L;

		when(deviceService.assignSensor(deviceId, sensorId)).thenThrow(new SensorNotFoundException(sensorId));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
				.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(deviceId, sensorId);
	}

	@Test
	void deleteDevice_shouldReturnNoContent_whenDeviceExists() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/{id}", deviceId))
				.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void deleteDevice_shouldReturn404_whenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();
		doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/{id}", deviceId))
				.andExpect(status().isNotFound());

		verify(deviceService).deleteDevice(deviceId);
	}

	private Device createDevice(UUID id, String name, String description) {
		return new TestDevice(id, name, description);
	}

	private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
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
