package de.sfl.devices;

import de.sfl.sensors.SensorNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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

	public Device createDevice(Device device) {
		var savedDevice = deviceRepository.save(device);
		logger.info("Device created successfully");
		return savedDevice;
	}

	public Device updateDevice(UUID deviceId, String name, String description) {
		var device = findDeviceOrThrow(deviceId);
		applyUpdates(device, name, description);
		var updatedDevice = deviceRepository.save(device);
		logger.info("Device updated successfully");
		return updatedDevice;
	}

	public void deleteDevice(UUID deviceId) {
		var device = findDeviceOrThrow(deviceId);
		deviceRepository.delete(device);
		logger.info("Device deleted successfully");
	}

	public Device assignSensor(UUID deviceId, Long sensorId) {
		var device = findDeviceOrThrow(deviceId);
		validateSensorExists(sensorId);

		if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			var assignment = new DeviceSensor(new DeviceSensorId(deviceId, sensorId));
			deviceSensorRepository.save(assignment);
		}

		logger.info("Sensor assigned to device successfully");
		return device;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID deviceId) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(deviceId);
	}

	@Transactional(readOnly = true)
	public List<Long> getDeviceSensorIds(UUID deviceId) {
		findDeviceOrThrow(deviceId);

		return deviceSensorRepository.findByIdDeviceId(deviceId).stream()
			.map(assignment -> assignment.getId().getSensorId())
			.sorted(Comparator.naturalOrder())
			.toList();
	}

	private Device findDeviceOrThrow(UUID deviceId) {
		return deviceRepository.findById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException(deviceId));
	}

	private void validateSensorExists(Long sensorId) {
		if (!deviceSensorRepository.sensorExists(sensorId)) {
			throw new SensorNotFoundException(sensorId);
		}
	}

	private void applyUpdates(Device device, String name, String description) {
		if (name != null) {
			device.setName(name);
		}

		if (description != null) {
			device.setDescription(description);
		}
	}
}
