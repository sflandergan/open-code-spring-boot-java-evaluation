package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import de.sfl.sensors.SensorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final JpaDeviceRepository deviceRepository;
    private final JpaDeviceSensorRepository deviceSensorRepository;
    private final SensorService sensorService;

    public DeviceService(JpaDeviceRepository deviceRepository,
                         JpaDeviceSensorRepository deviceSensorRepository,
                         SensorService sensorService) {
        this.deviceRepository = deviceRepository;
        this.deviceSensorRepository = deviceSensorRepository;
        this.sensorService = sensorService;
    }

    @Transactional
    public Device createDevice(CreateDeviceDto dto) {
        Device device = dto.toEntity();
        Device savedDevice = deviceRepository.save(device);
        logger.info("Created device with id: {}, name: {}", savedDevice.getId(), savedDevice.getName());
        return savedDevice;
    }

    @Transactional(readOnly = true)
    public Device getDevice(UUID id) {
        logger.debug("Retrieving device with id: {}", id);
        return findDeviceOrThrow(id);
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

        sensorService.getSensorById(sensorId);

        if (!deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)) {
            DeviceSensor deviceSensor = new DeviceSensor(device, sensorId);
            deviceSensorRepository.save(deviceSensor);
            logger.info("Assigned sensor {} to device {}", sensorId, deviceId);
        } else {
            logger.debug("Sensor {} already assigned to device {}", sensorId, deviceId);
        }

        return deviceRepository.findById(deviceId).orElseThrow();
    }

    private Device findDeviceOrThrow(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}
