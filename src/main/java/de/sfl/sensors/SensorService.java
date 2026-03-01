package de.sfl.sensors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import de.sfl.PageResult;

import java.util.List;

public class SensorService {

	private static final Logger logger = LoggerFactory.getLogger(SensorService.class);

	private final JpaSensorRepository sensorRepository;

	public SensorService(JpaSensorRepository sensorRepository) {
		this.sensorRepository = sensorRepository;
	}

	@Transactional(readOnly = true)
	public List<Sensor> getAllSensors() {
		logger.debug("Retrieving all sensors");
		return sensorRepository.findAll();
	}

	@Transactional(readOnly = true)
	public PageResult<Sensor> getSensors(Long lastId, int pageSize) {
		logger.debug("Retrieving sensors with pagination - lastId: {}, pageSize: {}", lastId, pageSize);
		Pageable pageable = PageRequest.of(0, pageSize + 1);

		List<Sensor> sensors = (lastId == null)
				? sensorRepository.findFirstPage(pageable)
				: sensorRepository.findNextPage(lastId, pageable);

		boolean hasMore = sensors.size() > pageSize;
		List<Sensor> resultSensors = sensors.stream()
				.limit(pageSize)
				.toList();

		return new PageResult<>(resultSensors, hasMore);
	}

	@Transactional(readOnly = true)
	public Sensor getSensorById(Long id) {
		logger.debug("Retrieving sensor with id: {}", id);
		return findSensorOrThrow(id);
	}

	@Transactional
	public Sensor createSensor(Sensor sensor) {
		validateSensoreNameUnique(sensor.getName());

		Sensor savedSensor = sensorRepository.save(sensor);
		logger.info("Created sensor with id: {}, name: {}", savedSensor.getId(), savedSensor.getName());

		return savedSensor;
	}

	private Sensor findSensorOrThrow(Long id) {
		return sensorRepository.findById(id)
				.orElseThrow(() -> new SensorNotFoundException(id));
	}

	private void validateSensoreNameUnique(String name) {
		if (sensorRepository.existsByName(name)) {
			throw new SensorAlreadyExistsException(name);
		}
	}
}
