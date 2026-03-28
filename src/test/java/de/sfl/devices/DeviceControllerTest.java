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

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
		var createDeviceDto = new CreateDeviceDto("Gateway", "Main gateway");
		var createdDevice = createDevice(deviceId, "Gateway", "Main gateway");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDeviceDto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.name", is("Gateway")))
			.andExpect(jsonPath("$.description", is("Main gateway")))
			.andExpect(jsonPath("$.sensors", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var invalidDto = new CreateDeviceDto("", "Main gateway");

		mockMvc.perform(post("/api/devices")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(deviceService);
	}

	@Test
	void getDevice_shouldReturnDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = createDevice(deviceId, "Gateway", "Main gateway");
		device.addSensorAssignment(createSensor(10L, "Sensor A", "temperature", Set.of("read")));

		when(deviceService.getDevice(deviceId)).thenReturn(device);

		mockMvc.perform(get("/api/devices/{id}", deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(deviceId.toString())))
			.andExpect(jsonPath("$.sensors", hasSize(1)))
			.andExpect(jsonPath("$.sensors[0].id", is(10)))
			.andExpect(jsonPath("$.sensors[0].name", is("Sensor A")));

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Updated Gateway", "Updated description");
		var updatedDevice = createDevice(deviceId, "Updated Gateway", "Updated description");

		when(deviceService.updateDevice(deviceId, "Updated Gateway", "Updated description"))
			.thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/{id}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDeviceDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("Updated Gateway")))
			.andExpect(jsonPath("$.description", is("Updated description")));

		verify(deviceService).updateDevice(deviceId, "Updated Gateway", "Updated description");
	}

	@Test
	void updateDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var invalidDto = new UpdateDeviceDto("Updated Gateway", "   ");

		mockMvc.perform(put("/api/devices/{id}", deviceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(deviceService);
	}

	@Test
	void deleteDevice_shouldReturnNoContent() throws Exception {
		var deviceId = UUID.randomUUID();

		mockMvc.perform(delete("/api/devices/{id}", deviceId))
			.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
	}

	@Test
	void deleteDevice_shouldReturn404WhenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/{id}", deviceId))
			.andExpect(status().isNotFound());
	}

	@Test
	void assignSensor_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updatedDevice = createDevice(deviceId, "Gateway", "Main gateway");
		updatedDevice.addSensorAssignment(createSensor(12L, "Sensor B", "humidity", Set.of("read")));

		when(deviceService.assignSensor(deviceId, 12L)).thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 12L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sensors", hasSize(1)))
			.andExpect(jsonPath("$.sensors[0].id", is(12)));

		verify(deviceService).assignSensor(deviceId, 12L);
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.assignSensor(deviceId, 12L)).thenThrow(new SensorNotFoundException(12L));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 12L))
			.andExpect(status().isNotFound());
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
