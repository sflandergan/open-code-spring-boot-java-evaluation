package de.sfl.devices;

import de.sfl.sensors.SensorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

	private final JpaDeviceRepository deviceRepository;
	private final JpaDeviceSensorRepository deviceSensorRepository;
	private final SensorService sensorService;

	public DeviceService(
			JpaDeviceRepository deviceRepository,
			JpaDeviceSensorRepository deviceSensorRepository,
			SensorService sensorService) {
		this.deviceRepository = deviceRepository;
		this.deviceSensorRepository = deviceSensorRepository;
		this.sensorService = sensorService;
	}

	@Transactional(readOnly = true)
	public Device getDeviceById(Long id) {
		logger.debug("Retrieving device with id: {}", id);
		return findDeviceOrThrow(id);
	}

	@Transactional
	public Device createDevice(Device device) {
		Device savedDevice = deviceRepository.save(device);
		logger.info("Created device with id: {}", savedDevice.getId());
		return savedDevice;
	}

	@Transactional
	public Device updateDevice(Long id, UpdateDeviceDto dto) {
		Device device = findDeviceOrThrow(id);
		dto.applyToEntity(device);
		Device updatedDevice = deviceRepository.save(device);
		logger.info("Updated device with id: {}", updatedDevice.getId());
		return updatedDevice;
	}

	@Transactional
	public void deleteDevice(Long id) {
		Device device = findDeviceOrThrow(id);
		deviceRepository.delete(device);
		logger.info("Deleted device with id: {}", id);
	}

	@Transactional
	public Device assignSensor(Long deviceId, Long sensorId) {
		Device device = findDeviceOrThrow(deviceId);

		validateSensorExists(sensorId);

		if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			DeviceSensor deviceSensor = new DeviceSensor(device, sensorId);
			deviceSensorRepository.save(deviceSensor);
			logger.info("Assigned sensor {} to device {}", sensorId, deviceId);
		} else {
			logger.debug("Sensor {} already assigned to device {}", sensorId, deviceId);
		}

		return device;
	}

	@Transactional(readOnly = true)
	public List<Long> getDeviceSensorIds(Long deviceId) {
		logger.debug("Retrieving sensor IDs for device {}", deviceId);
		return deviceSensorRepository.findAll().stream()
				.filter(ds -> ds.getId().getDeviceId().equals(deviceId))
				.map(ds -> ds.getId().getSensorId())
				.toList();
	}

	private Device findDeviceOrThrow(Long id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException(id));
	}

	private void validateSensorExists(Long sensorId) {
		sensorService.getSensorById(sensorId);
	}
}
