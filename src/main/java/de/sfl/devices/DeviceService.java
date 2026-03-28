package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
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

	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDetailedDeviceOrThrow(id);
	}

	@Transactional
	public Device createDevice(Device device) {
		var savedDevice = deviceRepository.save(device);
		logger.info("Created device successfully");
		return findDetailedDeviceOrThrow(savedDevice.getId());
	}

	@Transactional
	public Device updateDevice(UUID id, String name, String description) {
		var device = findDeviceOrThrow(id);
		applyChanges(device, name, description);
		deviceRepository.save(device);
		logger.info("Updated device successfully");
		return findDetailedDeviceOrThrow(id);
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

		if (!deviceSensorRepository.existsByDeviceIdAndSensorId(deviceId, sensorId)) {
			deviceSensorRepository.save(new DeviceSensor(device, sensor));
			logger.info("Assigned sensor to device successfully");
		}

		return findDetailedDeviceOrThrow(deviceId);
	}

	private void applyChanges(Device device, String name, String description) {
		if (name != null) {
			device.setName(name);
		}

		if (description != null) {
			device.setDescription(description);
		}
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findById(id)
			.orElseThrow(() -> new DeviceNotFoundException(id));
	}

	private Device findDetailedDeviceOrThrow(UUID id) {
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
