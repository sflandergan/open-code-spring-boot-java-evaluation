package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import de.sfl.sensors.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

	@MockBean
	private SensorService sensorService;

	@Test
	void getDeviceById_shouldReturnDeviceWithSensors() throws Exception {
		var device = createDevice(1L, "Test Device", "Test Description");
		var sensor = createSensor(10L, "Temperature Sensor", "temperature", Set.of("read"));

		when(deviceService.getDeviceById(1L)).thenReturn(device);
		when(deviceService.getDeviceSensorIds(1L)).thenReturn(List.of(10L));
		when(sensorService.getSensorById(10L)).thenReturn(sensor);

		mockMvc.perform(get("/api/devices/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Test Device")))
				.andExpect(jsonPath("$.description", is("Test Description")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].id", is(10)))
				.andExpect(jsonPath("$.sensors[0].name", is("Temperature Sensor")));

		verify(deviceService).getDeviceById(1L);
		verify(deviceService).getDeviceSensorIds(1L);
		verify(sensorService).getSensorById(10L);
	}

	@Test
	void getDeviceById_shouldReturn404WhenNotFound() throws Exception {
		when(deviceService.getDeviceById(999L)).thenThrow(new DeviceNotFoundException(999L));

		mockMvc.perform(get("/api/devices/999"))
				.andExpect(status().isNotFound());

		verify(deviceService).getDeviceById(999L);
	}

	@Test
	void createDevice_shouldReturnCreatedDevice() throws Exception {
		var createDto = new CreateDeviceDto("New Device", "New Description");
		var createdDevice = createDevice(1L, "New Device", "New Description");

		when(deviceService.createDevice(any(Device.class))).thenReturn(createdDevice);

		mockMvc.perform(post("/api/devices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("New Device")))
				.andExpect(jsonPath("$.description", is("New Description")));

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
	void updateDevice_shouldReturnUpdatedDevice() throws Exception {
		var updateDto = new UpdateDeviceDto("Updated Device", "Updated Description");
		var updatedDevice = createDevice(1L, "Updated Device", "Updated Description");

		when(deviceService.updateDevice(eq(1L), any(UpdateDeviceDto.class))).thenReturn(updatedDevice);

		mockMvc.perform(put("/api/devices/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Updated Device")))
				.andExpect(jsonPath("$.description", is("Updated Description")));

		verify(deviceService).updateDevice(eq(1L), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		var updateDto = new UpdateDeviceDto("Updated Device", "Updated Description");

		when(deviceService.updateDevice(eq(999L), any(UpdateDeviceDto.class)))
				.thenThrow(new DeviceNotFoundException(999L));

		mockMvc.perform(put("/api/devices/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());

		verify(deviceService).updateDevice(eq(999L), any(UpdateDeviceDto.class));
	}

	@Test
	void updateDevice_shouldReturn400WhenNameIsBlank() throws Exception {
		var invalidDto = new UpdateDeviceDto("", "Description");

		mockMvc.perform(put("/api/devices/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteDevice_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/api/devices/1"))
				.andExpect(status().isNoContent());

		verify(deviceService).deleteDevice(1L);
	}

	@Test
	void deleteDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
		org.mockito.Mockito.doThrow(new DeviceNotFoundException(999L))
				.when(deviceService).deleteDevice(999L);

		mockMvc.perform(delete("/api/devices/999"))
				.andExpect(status().isNotFound());

		verify(deviceService).deleteDevice(999L);
	}

	@Test
	void assignSensor_shouldReturnDeviceWithSensors() throws Exception {
		var device = createDevice(1L, "Test Device", "Test Description");
		var sensor = createSensor(10L, "Temperature Sensor", "temperature", Set.of("read"));

		when(deviceService.assignSensor(1L, 10L)).thenReturn(device);
		when(deviceService.getDeviceSensorIds(1L)).thenReturn(List.of(10L));
		when(sensorService.getSensorById(10L)).thenReturn(sensor);

		mockMvc.perform(put("/api/devices/1/sensors/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Test Device")))
				.andExpect(jsonPath("$.sensors", hasSize(1)))
				.andExpect(jsonPath("$.sensors[0].id", is(10)))
				.andExpect(jsonPath("$.sensors[0].name", is("Temperature Sensor")));

		verify(deviceService).assignSensor(1L, 10L);
		verify(deviceService).getDeviceSensorIds(1L);
		verify(sensorService).getSensorById(10L);
	}

	@Test
	void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
		when(deviceService.assignSensor(999L, 1L)).thenThrow(new DeviceNotFoundException(999L));

		mockMvc.perform(put("/api/devices/999/sensors/1"))
				.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(999L, 1L);
	}

	@Test
	void assignSensor_shouldReturn404WhenSensorNotFound() throws Exception {
		when(deviceService.assignSensor(1L, 999L)).thenThrow(new SensorNotFoundException(999L));

		mockMvc.perform(put("/api/devices/1/sensors/999"))
				.andExpect(status().isNotFound());

		verify(deviceService).assignSensor(1L, 999L);
	}

	private Device createDevice(Long id, String name, String description) {
		var device = new Device(name, description);
		return new TestDevice(id, name, description, device.getCreatedAt(), device.getUpdatedAt());
	}

	private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
		var sensor = new Sensor(name, type, capabilities);
		return new TestSensor(id, name, type, capabilities);
	}

	static class TestDevice extends Device {
		TestDevice(Long id, String name, String description, Instant createdAt, Instant updatedAt) {
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
