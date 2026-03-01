package de.sfl.devices;

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

    @Transactional
    public Device createDevice(CreateDeviceDto dto) {
        var device = dto.toEntity();
        Device savedDevice = deviceRepository.save(device);
        logger.info("Created device with id: {}, name: {}", savedDevice.getId(), savedDevice.getName());
        return savedDevice;
    }

    @Transactional
    public Device updateDevice(UUID id, UpdateDeviceDto dto) {
        Device device = findDeviceOrThrow(id);
        dto.applyToEntity(device);
        Device updatedDevice = deviceRepository.save(device);
        logger.info("Updated device with id: {}", id);
        return updatedDevice;
    }

    @Transactional
    public void deleteDevice(UUID id) {
        Device device = findDeviceOrThrow(id);
        deviceRepository.delete(device);
        logger.info("Deleted device with id: {}", id);
    }

    @Transactional
    public Device assignSensor(UUID deviceId, Long sensorId) {
        Device device = findDeviceOrThrow(deviceId);

        var deviceSensorId = new DeviceSensorId(deviceId, sensorId);
        
        // Idempotent: only assign if not already assigned
        if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
            var deviceSensor = new DeviceSensor(device, deviceSensorId);
            deviceSensorRepository.save(deviceSensor);
            logger.info("Assigned sensor with id: {} to device with id: {}", sensorId, deviceId);
        }

        return device;
    }

    @Transactional(readOnly = true)
    public Device getDevice(UUID id) {
        logger.debug("Retrieving device with id: {}", id);
        return findDeviceOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        logger.debug("Retrieving all devices");
        return deviceRepository.findAll();
    }

    private Device findDeviceOrThrow(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}
