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

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device Management", description = "APIs for managing devices and sensor assignments")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @Operation(summary = "Create a new device", description = "Creates a new device with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Device created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceDto createDeviceDto) {
        Device createdDevice = deviceService.createDevice(createDeviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(createdDevice));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a device", description = "Updates the device with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable UUID id, @Valid @RequestBody UpdateDeviceDto updateDeviceDto) {
        Device updatedDevice = deviceService.updateDevice(id, updateDeviceDto);
        return ResponseEntity.ok(toDto(updatedDevice));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device", description = "Deletes the device and all its sensor assignments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deviceId}/sensors/{sensorId}")
    @Operation(summary = "Assign a sensor to a device", description = "Assigns a sensor to a device (idempotent)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sensor assigned successfully"),
        @ApiResponse(responseCode = "404", description = "Device or sensor not found")
    })
    public ResponseEntity<DeviceDto> assignSensor(@PathVariable UUID deviceId, @PathVariable Long sensorId) {
        Device device = deviceService.assignSensor(deviceId, sensorId);
        return ResponseEntity.ok(toDto(device));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves a single device by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<DeviceDto> getDevice(@PathVariable UUID id) {
        Device device = deviceService.getDevice(id);
        return ResponseEntity.ok(toDto(device));
    }

    private DeviceDto toDto(Device device) {
        return new DeviceDto(device);
    }
}
