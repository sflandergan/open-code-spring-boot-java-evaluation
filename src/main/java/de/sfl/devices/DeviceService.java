package de.sfl.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
public class DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

	private final JpaDeviceRepository deviceRepository;
	private final JpaDeviceSensorRepository deviceSensorRepository;

	public DeviceService(JpaDeviceRepository deviceRepository, JpaDeviceSensorRepository deviceSensorRepository) {
		this.deviceRepository = deviceRepository;
		this.deviceSensorRepository = deviceSensorRepository;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID deviceId) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(deviceId);
	}

	@Transactional(readOnly = true)
	public List<Long> getDeviceSensors(UUID deviceId) {
		findDeviceOrThrow(deviceId);
		logger.debug("Retrieving assigned sensors for device");

		return deviceSensorRepository.findAllByIdDeviceId(deviceId).stream()
			.map(DeviceSensor::getSensorId)
			.sorted()
			.toList();
	}

	public Device createDevice(Device device) {
		var savedDevice = deviceRepository.save(device);
		logger.info("Device created successfully");
		return savedDevice;
	}

	public Device updateDevice(UUID deviceId, String name, String description) {
		var device = findDeviceOrThrow(deviceId);
		updateNameIfProvided(device, name);
		updateDescriptionIfProvided(device, description);

		var updatedDevice = deviceRepository.save(device);
		logger.info("Device updated successfully");
		return updatedDevice;
	}

	public Device assignSensor(UUID deviceId, Long sensorId) {
		var device = findDeviceOrThrow(deviceId);
		validateSensorExists(sensorId);

		if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			deviceSensorRepository.save(new DeviceSensor(device, sensorId));
			logger.info("Sensor assigned to device successfully");
		}

		return device;
	}

	public void deleteDevice(UUID deviceId) {
		var device = findDeviceOrThrow(deviceId);
		deviceRepository.delete(device);
		logger.info("Device deleted successfully");
	}

	private void validateSensorExists(Long sensorId) {
		if (!deviceSensorRepository.existsSensorById(sensorId)) {
			throw new SensorNotFoundException(sensorId);
		}
	}

	private Device findDeviceOrThrow(UUID deviceId) {
		return deviceRepository.findById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException(deviceId));
	}

	private void updateNameIfProvided(Device device, String name) {
		if (name != null) {
			device.setName(name);
		}
	}

	private void updateDescriptionIfProvided(Device device, String description) {
		if (description != null) {
			device.setDescription(description);
		}
	}
}
