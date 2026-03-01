package de.sfl.sensors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.sfl.PageResult;

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "Sensor Management", description = "APIs for managing sensors")
public class SensorController {

	private final SensorService sensorService;

	public SensorController(SensorService sensorService) {
		this.sensorService = sensorService;
	}

	@GetMapping
	@Operation(summary = "Get all sensors", description = "Retrieves a paginated list of sensors using keyset pagination")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list of sensors")
	public ResponseEntity<PageResult<SensorDto>> getAllSensors(
		@RequestParam(required = false) Long lastId,
		@RequestParam(defaultValue = "10") int pageSize
	) {
		PageResult<Sensor> sensors = sensorService.getSensors(lastId, pageSize);
		PageResult<SensorDto> sensorDtos = new PageResult<>(
			sensors.items().stream().map(this::toDto).toList(),
			sensors.hasMore()
		);
		return ResponseEntity.ok(sensorDtos);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get sensor by ID", description = "Retrieves a single sensor by its ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved sensor"),
		@ApiResponse(responseCode = "404", description = "Sensor not found")
	})
	public ResponseEntity<SensorDto> getSensorById(@PathVariable Long id) {
		Sensor sensor = sensorService.getSensorById(id);
		return ResponseEntity.ok(toDto(sensor));
	}

	@PostMapping
	@Operation(summary = "Create a new sensor", description = "Creates a new sensor with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sensor created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input"),
		@ApiResponse(responseCode = "409", description = "Sensor with the same name already exists")
	})
	public ResponseEntity<SensorDto> createSensor(@Valid @RequestBody CreateSensorDto createSensorDto) {
		Sensor createdSensor = sensorService.createSensor(createSensorDto.toEntity());
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(createdSensor));
	}

	private SensorDto toDto(Sensor sensor) {
		return new SensorDto(
			sensor.getId(),
			sensor.getName(),
			sensor.getType(),
			sensor.getCapabilities()
		);
	}
}
