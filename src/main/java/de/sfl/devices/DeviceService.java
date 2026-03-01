package de.sfl.devices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for Device management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final JpaDeviceRepository deviceRepository;
    private final JpaDeviceSensorRepository deviceSensorRepository;

    /**
     * Creates a new device.
     *
     * @param dto CreateDeviceDto containing name and description
     * @return the persisted Device entity
     */
    @Transactional
    public Device createDevice(CreateDeviceDto dto) {
        Device device = Device.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return deviceRepository.save(device);
    }

    /**
     * Assigns a sensor to a device in an idempotent manner.
     *
     * @param deviceId the device identifier
     * @param sensorId the sensor identifier
     */
    @Transactional
    public void assignSensor(UUID deviceId, UUID sensorId) {
        // Check if assignment already exists
        boolean exists = deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId);
        if (!exists) {
            // Create a new DeviceSensor assignment
            DeviceSensor deviceSensor = new DeviceSensor();
            DeviceSensorId id = new DeviceSensorId(deviceId, sensorId);
            deviceSensor.setId(id);
            // Set only the foreign key to device; JPA will handle sensor mapping via @MapsId
            // We can set the device reference using a temporary instance:
            Device tmpDevice = new Device();
            tmpDevice.setId(deviceId);
            deviceSensor.setDevice(tmpDevice); // temporary reference for JPA
            deviceSensorRepository.save(deviceSensor);
        }
    }

    /**
     * Updates an existing device partially.
     *
     * @param deviceId the identifier
     * @param dto      UpdateDeviceDto with fields to update
     */
    @Transactional
    public void updateDevice(UUID deviceId, UpdateDeviceDto dto) {
        var device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            device.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            device.setDescription(dto.getDescription());
        }
        // entity is automatically updated due to merge semantics of JPA
    }

    /**
     * Deletes a device and cascades removal of its sensor assignments.
     *
     * @param deviceId the identifier
     */
    @Transactional
    public void deleteDevice(UUID deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new IllegalArgumentException("Device not found");
        }
        deviceRepository.deleteById(deviceId);
    }

    /**
     * Retrieves a device by its identifier.
     *
     * @param deviceId the identifier
     */
    public Device findDevice(UUID deviceId) {
        return deviceRepository.findById(deviceId).orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }
}