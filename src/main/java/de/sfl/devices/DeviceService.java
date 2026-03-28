package de.sfl.devices;

import de.sfl.sensors.Sensor;
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

	@Transactional(readOnly = true)
	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(id);
	}

	@Transactional
	public Device createDevice(Device device) {
		var created = deviceRepository.save(device);
		logger.info("Created device");
		return created;
	}

	@Transactional
	public Device updateDevice(UUID deviceId, String name, String description) {
		var device = findDeviceOrThrow(deviceId);

		if (name != null) {
			device.setName(name);
		}
		if (description != null) {
			device.setDescription(description);
		}

		var updated = deviceRepository.save(device);
		logger.info("Updated device");
		return updated;
	}

	@Transactional
	public void deleteDevice(UUID deviceId) {
		var device = findDeviceOrThrow(deviceId);
		deviceRepository.delete(device);
		logger.info("Deleted device");
	}

	@Transactional
	public Device assignSensor(UUID deviceId, Long sensorId) {
		var device = findDeviceOrThrow(deviceId);
		var sensor = findSensorOrThrow(sensorId);

		if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			device.assignSensor(sensor);
			deviceRepository.save(device);
			logger.info("Assigned sensor to device");
		}

		return findDeviceOrThrow(deviceId);
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
	}

	private Sensor findSensorOrThrow(Long sensorId) {
		var sensor = entityManager.find(Sensor.class, sensorId);
		if (sensor == null) {
			throw new AssignedSensorNotFoundException(sensorId);
		}
		return sensor;
	}
}
