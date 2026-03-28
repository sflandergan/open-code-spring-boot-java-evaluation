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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
		var createDeviceDto = new CreateDeviceDto("Weather Station", "Outdoor monitoring unit");
		var createdDevice = new TestDevice(deviceId, "Weather Station", "Outdoor monitoring unit");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDeviceDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.name", is("Weather Station")))
				.andExpect(jsonPath("$.description", is("Outdoor monitoring unit")))
				.andExpect(jsonPath("$.sensors", hasSize(0)));

		verify(deviceService).createDevice(any(Device.class));
	}

	@Test
	void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var createDeviceDto = new CreateDeviceDto("", "Outdoor monitoring unit");

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDeviceDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getDevice_shouldReturnDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Weather Station", "Outdoor monitoring unit");
		device.assignSensor(new TestSensor(7L, "Humidity Sensor", "sensor", Set.of("read")));

		when(deviceService.getDevice(deviceId)).thenReturn(device);

		mockMvc.perform(get("/api/devices/{id}", deviceId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(deviceId.toString())))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].id", is(7)));

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void getDevice_shouldReturn404WhenDeviceDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

		mockMvc.perform(get("/api/devices/{id}", deviceId))
				.andExpect(status().isNotFound());

		verify(deviceService).getDevice(deviceId);
	}

	@Test
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Edge Gateway", "Updated description");
		var updatedDevice = new TestDevice(deviceId, "Edge Gateway", "Updated description");

		when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class))).thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/{id}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDeviceDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Edge Gateway")))
				.andExpect(jsonPath("$.description", is("Updated description")));

		verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
		var deviceId = UUID.randomUUID();
		var updateDeviceDto = new UpdateDeviceDto("Edge Gateway", "   ");

		mockMvc.perform(put("/api/devices/{id}", deviceId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDeviceDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void assignSensor_shouldReturnUpdatedDevice() throws Exception {
		var deviceId = UUID.randomUUID();
		var device = new TestDevice(deviceId, "Weather Station", "Outdoor monitoring unit");
		device.assignSensor(new TestSensor(7L, "Humidity Sensor", "sensor", Set.of("read")));

		when(deviceService.assignSensor(deviceId, 7L)).thenReturn(device);

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 7L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].name", is("Humidity Sensor")));

		verify(deviceService).assignSensor(deviceId, 7L);
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorDoesNotExist() throws Exception {
		var deviceId = UUID.randomUUID();

		when(deviceService.assignSensor(deviceId, 7L)).thenThrow(new SensorNotFoundException(7L));

		mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, 7L))
				.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(deviceId, 7L);
	}

	@Test
	void deleteDevice_shouldReturnNoContent() throws Exception {
		var deviceId = UUID.randomUUID();

		doNothing().when(deviceService).deleteDevice(deviceId);

		mockMvc.perform(delete("/api/devices/{id}", deviceId))
				.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(deviceId);
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
