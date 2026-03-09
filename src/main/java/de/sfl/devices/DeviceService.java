package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final JpaDeviceRepository deviceRepository;
    private final JpaDeviceSensorRepository deviceSensorRepository;

    public DeviceService(JpaDeviceRepository deviceRepository, JpaDeviceSensorRepository deviceSensorRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceSensorRepository = deviceSensorRepository;
    }

    @Transactional(readOnly = true)
    public Device getDevice(UUID id) {
        logger.debug("Retrieving device with id: {}", id);
        return findDeviceOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Sensor> getDeviceSensors(UUID deviceId) {
        logger.debug("Retrieving sensors for device with id: {}", deviceId);
        Device device = findDeviceOrThrow(deviceId);
        
        // This would need to fetch the actual sensor entities from a sensor service
        // For now, we'll return an empty list as a placeholder
        return List.of();
    }

    @Transactional
    public Device createDevice(CreateDeviceDto dto) {
        Device device = dto.toEntity();
        Device savedDevice = deviceRepository.save(device);
        logger.info("Created device with id: {}, name: {}", savedDevice.getId(), savedDevice.getName());
        
        return savedDevice;
    }

    @Transactional
    public Device updateDevice(UUID id, UpdateDeviceDto dto) {
        Device device = findDeviceOrThrow(id);
        dto.applyToEntity(device);
        
        Device updatedDevice = deviceRepository.save(device);
        logger.info("Updated device with id: {}, name: {}", updatedDevice.getId(), updatedDevice.getName());
        
        return updatedDevice;
    }

    @Transactional
    public Device assignSensor(UUID deviceId, Long sensorId) {
        Device device = findDeviceOrThrow(deviceId);
        
        // Check if sensor exists (this would need a sensor service in a real implementation)
        // For now, we'll assume the sensor exists
        
        // Check if assignment already exists (idempotent operation)
        boolean exists = deviceSensorRepository.existsById_DeviceIdAndId_SensorId(deviceId, sensorId);
        
        if (!exists) {
            DeviceSensor deviceSensor = new DeviceSensor(deviceId, sensorId);
            deviceSensorRepository.save(deviceSensor);
            logger.info("Assigned sensor with id: {} to device with id: {}", sensorId, deviceId);
        }
        
        return device;
    }

    @Transactional
    public void deleteDevice(UUID id) {
        Device device = findDeviceOrThrow(id);
        deviceRepository.delete(device);
        logger.info("Deleted device with id: {}", id);
    }

    private Device findDeviceOrThrow(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}