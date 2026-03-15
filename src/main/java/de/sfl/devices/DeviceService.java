package de.sfl.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
	public Device createDevice(Device device) {
		Device savedDevice = deviceRepository.save(device);
		logger.info("Device created successfully");
		return savedDevice;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(id);
	}

	@Transactional(readOnly = true)
	public List<DeviceSensor> getDeviceSensors(UUID deviceId) {
		logger.debug("Retrieving sensors for device");
		findDeviceOrThrow(deviceId);
		return deviceSensorRepository.findByIdDeviceId(deviceId);
	}

	@Transactional
	public Device updateDevice(UUID id, UpdateDeviceDto updateDto) {
		Device device = findDeviceOrThrow(id);
		updateDto.applyTo(device);
		Device savedDevice = deviceRepository.save(device);
		logger.info("Device updated successfully");
		return savedDevice;
	}

	@Transactional
	public void deleteDevice(UUID id) {
		findDeviceOrThrow(id);
		deviceRepository.deleteById(id);
		logger.info("Device deleted successfully");
	}

	@Transactional
	public Device assignSensor(UUID deviceId, Long sensorId) {
		Device device = findDeviceOrThrow(deviceId);

		var assignmentId = new DeviceSensorId(deviceId, sensorId);
		if (deviceSensorRepository.existsById(assignmentId)) {
			logger.debug("Sensor already assigned to device");
			return device;
		}

		try {
			var assignment = new DeviceSensor(assignmentId);
			deviceSensorRepository.save(assignment);
			deviceSensorRepository.flush();
			logger.info("Sensor assigned to device successfully");
		} catch (DataIntegrityViolationException ex) {
			throw new SensorNotFoundException(sensorId);
		}

		return device;
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException(id));
	}
}
