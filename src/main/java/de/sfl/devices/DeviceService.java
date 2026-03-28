package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

	private final JpaDeviceRepository deviceRepository;
	private final JpaDeviceSensorRepository deviceSensorRepository;
	private final EntityManager entityManager;

	public DeviceService(
		JpaDeviceRepository deviceRepository,
		JpaDeviceSensorRepository deviceSensorRepository,
		EntityManager entityManager
	) {
		this.deviceRepository = deviceRepository;
		this.deviceSensorRepository = deviceSensorRepository;
		this.entityManager = entityManager;
	}

	@Transactional
	public Device createDevice(Device device) {
		var createdDevice = deviceRepository.save(device);
		logger.info("Created device successfully");
		return getDevice(createdDevice.getId());
	}

	@Transactional
	public Device updateDevice(UUID id, UpdateDeviceDto updateDeviceDto) {
		var device = findDeviceOrThrow(id);
		updateDeviceDto.applyToEntity(device);
		logger.info("Updated device successfully");
		return device;
	}

	@Transactional
	public void deleteDevice(UUID id) {
		var device = findDeviceOrThrow(id);
		deviceRepository.delete(device);
		logger.info("Deleted device successfully");
	}

	@Transactional
	public Device assignSensor(UUID deviceId, Long sensorId) {
		var device = findDeviceOrThrow(deviceId);
		var sensor = findSensorOrThrow(sensorId);

		if (!deviceSensorRepository.existsAssignment(deviceId, sensorId)) {
			device.assignSensor(sensor);
			logger.info("Assigned sensor to device successfully");
		}

		return device;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(id);
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findDetailedById(id)
				.orElseThrow(() -> new DeviceNotFoundException(id));
	}

	private Sensor findSensorOrThrow(Long sensorId) {
		var sensor = entityManager.find(Sensor.class, sensorId);
		if (sensor == null) {
			throw new SensorNotFoundException(sensorId);
		}
		return sensor;
	}
}
