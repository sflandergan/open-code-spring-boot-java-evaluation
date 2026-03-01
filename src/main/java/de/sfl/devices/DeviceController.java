package de.sfl.devices;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Create a new device", responses = {
            @ApiResponse(responseCode = "201", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")}
    ) 
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceDto dto) {
        Device device = deviceService.createDevice(dto);
        // Map entity to DTO (simplified)
        DeviceDto dtoOut = DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .sensors(java.util.Collections.emptyList())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoOut);
    }

    @PutMapping("/{deviceId}/sensors/{sensorId}")
    @Operation(summary = "Assign a sensor to a device", responses = {
            @ApiResponse(responseCode = "204", description = "Sensor assigned"),
            @ApiResponse(responseCode = "404", description = "Device or sensor not found")}
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignSensor(@PathVariable UUID deviceId, @PathVariable UUID sensorId) {
        deviceService.assignSensor(deviceId, sensorId);
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "Partially update a device", responses = {
            @ApiResponse(responseCode = "204", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request")}
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDevice(@PathVariable UUID deviceId, @Valid @RequestBody UpdateDeviceDto dto) {
        deviceService.updateDevice(deviceId, dto);
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Delete a device and cascade sensor assignments", responses = {
            @ApiResponse(responseCode = "204", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found")}
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable UUID deviceId) {
        deviceService.deleteDevice(deviceId);
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "Retrieve a device by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")}
    )
    public ResponseEntity<DeviceDto> getDevice(@PathVariable UUID deviceId) {
        Device device = deviceService.findDevice(deviceId);
        // Map entity to DTO (simplified)
        DeviceDto dtoOut = DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .sensors(java.util.Collections.emptyList())
                .build();
        return ResponseEntity.ok(dtoOut);
    }
}