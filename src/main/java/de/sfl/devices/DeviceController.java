package de.sfl.devices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceDto createDevice(@RequestBody CreateDeviceDto createDeviceDto) {
        Device device = deviceService.createDevice(createDeviceDto);
        return DeviceDto.fromEntityWithoutSensors(device);
    }

    @PutMapping("/{deviceId}")
    @ResponseStatus(HttpStatus.OK)
    public DeviceDto updateDevice(@PathVariable UUID deviceId, @RequestBody UpdateDeviceDto updateDeviceDto) {
        Device device = deviceService.updateDevice(deviceId, updateDeviceDto);
        return DeviceDto.fromEntityWithoutSensors(device);
    }

    @DeleteMapping("/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable UUID deviceId) {
        deviceService.deleteDevice(deviceId);
    }

    @PutMapping("/{deviceId}/sensors/{sensorId}")
    @ResponseStatus(HttpStatus.OK)
    public DeviceDto assignSensor(@PathVariable UUID deviceId, @PathVariable Long sensorId) {
        Device device = deviceService.assignSensor(deviceId, sensorId);
        return DeviceDto.fromEntityWithoutSensors(device);
    }

    @GetMapping("/{deviceId}")
    @ResponseStatus(HttpStatus.OK)
    public DeviceDto getDevice(@PathVariable UUID deviceId) {
        Device device = deviceService.getDevice(deviceId);
        return DeviceDto.fromEntityWithoutSensors(device);
    }
}