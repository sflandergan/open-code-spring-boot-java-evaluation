package de.sfl.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
public class DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

	private final JpaDeviceRepository deviceRepository;
	private final JpaDeviceSensorRepository deviceSensorRepository;
	private final JpaSensorLookupRepository sensorLookupRepository;

	public DeviceService(
			JpaDeviceRepository deviceRepository,
			JpaDeviceSensorRepository deviceSensorRepository,
			JpaSensorLookupRepository sensorLookupRepository
	) {
		this.deviceRepository = deviceRepository;
		this.deviceSensorRepository = deviceSensorRepository;
		this.sensorLookupRepository = sensorLookupRepository;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(id);
	}

	@Transactional(readOnly = true)
	public List<Sensor> getDeviceSensors(UUID deviceId) {
		findDeviceOrThrow(deviceId);

		var assignedSensorIds = deviceSensorRepository.findByIdDeviceId(deviceId).stream()
				.map(DeviceSensor::getSensorId)
				.distinct()
				.sorted()
				.toList();

		if (assignedSensorIds.isEmpty()) {
			return List.of();
		}

		var sensorsById = sensorLookupRepository.findByIdIn(assignedSensorIds).stream()
				.collect(Collectors.toMap(Sensor::getId, Function.identity()));

		return assignedSensorIds.stream()
				.map(sensorsById::get)
				.filter(Objects::nonNull)
				.toList();
	}

	public Device createDevice(Device device) {
		var savedDevice = deviceRepository.save(device);
		logger.info("Device created successfully");
		return savedDevice;
	}

	public Device updateDevice(UUID id, String name, String description) {
		var existingDevice = findDeviceOrThrow(id);
		applyDeviceUpdates(existingDevice, name, description);
		var updatedDevice = deviceRepository.save(existingDevice);
		logger.info("Device updated successfully");
		return updatedDevice;
	}

	public void deleteDevice(UUID id) {
		findDeviceOrThrow(id);
		deviceRepository.deleteById(id);
		logger.info("Device deleted successfully");
	}

	public Device assignSensor(UUID deviceId, Long sensorId) {
		var existingDevice = findDeviceOrThrow(deviceId);

		if (deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			logger.debug("Sensor assignment already exists for device");
			return existingDevice;
		}

		validateSensorExists(sensorId);
		deviceSensorRepository.save(new DeviceSensor(existingDevice, sensorId));
		logger.info("Sensor assigned to device successfully");

		return existingDevice;
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException(id));
	}

	private void validateSensorExists(Long sensorId) {
		if (!sensorLookupRepository.existsById(sensorId)) {
			throw new SensorNotFoundException(sensorId);
		}
	}

	private void applyDeviceUpdates(Device device, String name, String description) {
		if (name != null) {
			device.setName(name);
		}

		if (description != null) {
			device.setDescription(description);
		}
	}
}
