package de.sfl.devices;

import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorService;

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
	private final SensorService sensorService;

	public DeviceService(JpaDeviceRepository deviceRepository,
			JpaDeviceSensorRepository deviceSensorRepository,
			SensorService sensorService) {
		this.deviceRepository = deviceRepository;
		this.deviceSensorRepository = deviceSensorRepository;
		this.sensorService = sensorService;
	}

	public Device createDevice(Device device) {
		Device savedDevice = deviceRepository.save(device);
		logger.info("Device created successfully");
		return savedDevice;
	}

	public Device updateDevice(UUID id, UpdateDeviceDto dto) {
		Device device = findDeviceOrThrow(id);
		dto.applyToEntity(device);
		Device updatedDevice = deviceRepository.save(device);
		logger.info("Device updated successfully");
		return updatedDevice;
	}

	public void deleteDevice(UUID id) {
		findDeviceOrThrow(id);
		deviceRepository.deleteById(id);
		logger.info("Device deleted successfully");
	}

	public Device assignSensor(UUID deviceId, Long sensorId) {
		Device device = findDeviceOrThrow(deviceId);
		sensorService.getSensorById(sensorId);

		if (!deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			deviceSensorRepository.save(new DeviceSensor(deviceId, sensorId));
		}

		return device;
	}

	@Transactional(readOnly = true)
	public Device getDevice(UUID id) {
		logger.debug("Retrieving device");
		return findDeviceOrThrow(id);
	}

	@Transactional(readOnly = true)
	public List<Sensor> getDeviceSensors(UUID deviceId) {
		logger.debug("Retrieving sensors for device");
		findDeviceOrThrow(deviceId);
		return deviceSensorRepository.findAllByDeviceId(deviceId).stream()
				.map(ds -> sensorService.getSensorById(ds.getSensorId()))
				.toList();
	}

	private Device findDeviceOrThrow(UUID id) {
		return deviceRepository.findById(id)
				.orElseThrow(() -> new DeviceNotFoundException(id));
	}
}
