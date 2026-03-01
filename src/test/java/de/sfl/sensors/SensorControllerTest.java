package de.sfl.sensors;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.sfl.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorController.class)
class SensorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SensorService sensorService;

	@Test
	void getAllSensors_shouldReturnFirstPage() throws Exception {
		var sensor1 = createSensor(1L, "Sensor 1", "Type A", Set.of("capability1"));
		var sensor2 = createSensor(2L, "Sensor 2", "Type B", Set.of("capability2"));
		var pageResult = new PageResult<>(List.of(sensor1, sensor2), true);

		when(sensorService.getSensors(null, 10)).thenReturn(pageResult);

		mockMvc.perform(get("/api/sensors")
						.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[0].id", is(1)))
				.andExpect(jsonPath("$.items[0].name", is("Sensor 1")))
				.andExpect(jsonPath("$.items[0].type", is("Type A")))
				.andExpect(jsonPath("$.items[1].id", is(2)))
				.andExpect(jsonPath("$.items[1].name", is("Sensor 2")))
				.andExpect(jsonPath("$.hasMore", is(true)));

		verify(sensorService).getSensors(null, 10);
	}

	@Test
	void getAllSensors_shouldReturnNextPage() throws Exception {
		var sensor3 = createSensor(3L, "Sensor 3", "Type C", Set.of("capability3"));
		var pageResult = new PageResult<>(List.of(sensor3), false);

		when(sensorService.getSensors(2L, 10)).thenReturn(pageResult);

		mockMvc.perform(get("/api/sensors")
						.param("lastId", "2")
						.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].id", is(3)))
				.andExpect(jsonPath("$.hasMore", is(false)));

		verify(sensorService).getSensors(2L, 10);
	}

	@Test
	void getAllSensors_shouldUseDefaultPageSize() throws Exception {
		var pageResult = new PageResult<>(List.<Sensor>of(), false);

		when(sensorService.getSensors(null, 10)).thenReturn(pageResult);

		mockMvc.perform(get("/api/sensors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(0)))
				.andExpect(jsonPath("$.hasMore", is(false)));

		verify(sensorService).getSensors(null, 10);
	}

	@Test
	void getSensorById_shouldReturnSensor() throws Exception {
		var sensor = createSensor(1L, "Test Sensor", "Type A", Set.of("capability1", "capability2"));

		when(sensorService.getSensorById(1L)).thenReturn(sensor);

		mockMvc.perform(get("/api/sensors/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Test Sensor")))
				.andExpect(jsonPath("$.type", is("Type A")))
				.andExpect(jsonPath("$.capabilities", hasSize(2)));

		verify(sensorService).getSensorById(1L);
	}

	@Test
	void getSensorById_shouldReturn404WhenNotFound() throws Exception {
		when(sensorService.getSensorById(999L)).thenThrow(new SensorNotFoundException(999L));

		mockMvc.perform(get("/api/sensors/999"))
				.andExpect(status().isNotFound());

		verify(sensorService).getSensorById(999L);
	}

	@Test
	void createSensor_shouldReturnCreatedSensor() throws Exception {
		var createDto = new CreateSensorDto("New Sensor", "Type A", Set.of("capability1"));
		var createdSensor = createSensor(1L, "New Sensor", "Type A", Set.of("capability1"));

		when(sensorService.createSensor(any(Sensor.class))).thenReturn(createdSensor);

		mockMvc.perform(post("/api/sensors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("New Sensor")))
				.andExpect(jsonPath("$.type", is("Type A")));

		verify(sensorService).createSensor(any(Sensor.class));
	}

	@Test
	void createSensor_shouldReturn400WhenNameIsBlank() throws Exception {
		var invalidDto = new CreateSensorDto("", "Type A", Set.of("capability1"));

		mockMvc.perform(post("/api/sensors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createSensor_shouldReturn400WhenTypeIsBlank() throws Exception {
		var invalidDto = new CreateSensorDto("Sensor Name", "", Set.of("capability1"));

		mockMvc.perform(post("/api/sensors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createSensor_shouldReturn400WhenCapabilitiesIsNull() throws Exception {
		String invalidJson = """
				{
					"name": "Sensor Name",
					"type": "Type A",
					"capabilities": null
				}
				""";

		mockMvc.perform(post("/api/sensors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createSensor_shouldReturn409WhenSensorAlreadyExists() throws Exception {
		var createDto = new CreateSensorDto("Existing Sensor", "Type A", Set.of("capability1"));

		when(sensorService.createSensor(any(Sensor.class)))
				.thenThrow(new SensorAlreadyExistsException("Existing Sensor"));

		mockMvc.perform(post("/api/sensors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createDto)))
				.andExpect(status().isConflict());

		verify(sensorService).createSensor(any(Sensor.class));
	}

	private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
		return new TestSensor(id, name, type, capabilities);
	}

	static class TestSensor extends Sensor {
		TestSensor(Long id, String name, String type, Set<String> capabilities) {
			super(name, type, capabilities);
			this.id = id;
		}
	}
}
