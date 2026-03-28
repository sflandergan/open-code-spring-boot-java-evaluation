package de.sfl.devices;

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
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device Management", description = "APIs for managing devices")
public class DeviceController {

	private final DeviceService deviceService;

	public DeviceController(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@PostMapping
	@Operation(summary = "Create a device", description = "Creates a new device")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Device created successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request")
	})
	public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceDto createDeviceDto) {
		var createdDevice = deviceService.createDevice(createDeviceDto.toEntity());
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(createdDevice, List.of()));
	}

	@PutMapping("/{deviceId}")
	@Operation(summary = "Update a device", description = "Updates fields of an existing device")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Device updated successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<DeviceDto> updateDevice(
		@PathVariable UUID deviceId,
		@Valid @RequestBody UpdateDeviceDto updateDeviceDto
	) {
		var updatedDevice = deviceService.updateDevice(deviceId, updateDeviceDto.name(), updateDeviceDto.description());
		var sensorIds = deviceService.getDeviceSensorIds(deviceId);
		return ResponseEntity.ok(toDto(updatedDevice, sensorIds));
	}

	@DeleteMapping("/{deviceId}")
	@Operation(summary = "Delete a device", description = "Deletes a device and its sensor assignments")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Device deleted successfully"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<Void> deleteDevice(@PathVariable UUID deviceId) {
		deviceService.deleteDevice(deviceId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{deviceId}/sensors/{sensorId}")
	@Operation(summary = "Assign sensor", description = "Assigns a sensor to a device idempotently")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Sensor assigned successfully"),
		@ApiResponse(responseCode = "404", description = "Device or sensor not found")
	})
	public ResponseEntity<DeviceDto> assignSensor(@PathVariable UUID deviceId, @PathVariable Long sensorId) {
		var device = deviceService.assignSensor(deviceId, sensorId);
		var sensorIds = deviceService.getDeviceSensorIds(deviceId);
		return ResponseEntity.ok(toDto(device, sensorIds));
	}

	@GetMapping("/{deviceId}")
	@Operation(summary = "Get a device", description = "Gets a device by id")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Device retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "Device not found")
	})
	public ResponseEntity<DeviceDto> getDevice(@PathVariable UUID deviceId) {
		var device = deviceService.getDevice(deviceId);
		var sensorIds = deviceService.getDeviceSensorIds(deviceId);
		return ResponseEntity.ok(toDto(device, sensorIds));
	}

	private DeviceDto toDto(Device device, List<Long> sensorIds) {
		return DeviceDto.from(device, sensorIds);
	}
}
