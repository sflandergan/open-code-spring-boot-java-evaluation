package de.sfl.devices;

import de.sfl.sensors.SensorDto;
import de.sfl.sensors.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device Management", description = "APIs for managing devices and sensor assignments")
public class DeviceController {

	private final DeviceService deviceService;
	private final SensorService sensorService;

	public DeviceController(DeviceService deviceService, SensorService sensorService) {
		this.deviceService = deviceService;
		this.sensorService = sensorService;
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get device by ID", description = "Retrieves a single device by its ID with assigned sensors")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved device"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
		Device device = deviceService.getDeviceById(id);
		List<SensorDto> sensors = deviceService.getDeviceSensorIds(id).stream()
				.map(sensorService::getSensorById)
				.map(this::toSensorDto)
				.toList();
		return ResponseEntity.ok(new DeviceDto(device, sensors));
	}

	@PostMapping
	@Operation(summary = "Create a new device", description = "Creates a new device with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Device created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input")
	})
	public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceDto createDeviceDto) {
		Device createdDevice = deviceService.createDevice(createDeviceDto.toEntity());
		return ResponseEntity.status(HttpStatus.CREATED).body(new DeviceDto(createdDevice));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update device", description = "Updates an existing device with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Device updated successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid input"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<DeviceDto> updateDevice(
			@PathVariable Long id,
			@Valid @RequestBody UpdateDeviceDto updateDeviceDto) {
		Device updatedDevice = deviceService.updateDevice(id, updateDeviceDto);
		return ResponseEntity.ok(new DeviceDto(updatedDevice));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete device", description = "Deletes a device and all its sensor assignments")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Device deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
		deviceService.deleteDevice(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{deviceId}/sensors/{sensorId}")
	@Operation(summary = "Assign sensor to device", description = "Assigns a sensor to a device. Idempotent - repeated calls have the same effect.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sensor assigned successfully"),
		@ApiResponse(responseCode = "404", description = "Device or sensor not found")
	})
	public ResponseEntity<DeviceDto> assignSensor(
			@PathVariable Long deviceId,
			@PathVariable Long sensorId) {
		Device device = deviceService.assignSensor(deviceId, sensorId);
		List<SensorDto> sensors = deviceService.getDeviceSensorIds(deviceId).stream()
				.map(sensorService::getSensorById)
				.map(this::toSensorDto)
				.toList();
		return ResponseEntity.ok(new DeviceDto(device, sensors));
	}

	private SensorDto toSensorDto(de.sfl.sensors.Sensor sensor) {
		return new SensorDto(
			sensor.getId(),
			sensor.getName(),
			sensor.getType(),
			sensor.getCapabilities()
		);
	}
}
