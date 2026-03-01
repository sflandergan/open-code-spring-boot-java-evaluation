package de.sfl.sensors;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JpaSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaSensorRepository sensorRepository;

	@BeforeEach
	void setUp() {
		sensorRepository.deleteAll();
	}

	@Test
	void shouldSaveAndFindSensorById() {
		var sensor = new Sensor("Sensor-001", "temperature", Set.of("read", "write"));

		Sensor savedSensor = sensorRepository.save(sensor);

		assertThat(savedSensor.getId()).isNotNull();
		assertThat(savedSensor.getName()).isEqualTo("Sensor-001");
		assertThat(savedSensor.getType()).isEqualTo("temperature");
		assertThat(savedSensor.getCapabilities()).containsExactlyInAnyOrder("read", "write");

		Optional<Sensor> foundSensor = sensorRepository.findById(savedSensor.getId());
		assertThat(foundSensor).isPresent();
		assertThat(foundSensor.get().getName()).isEqualTo("Sensor-001");
	}

	@Test
	void shouldFindAllSensors() {
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));

		sensorRepository.save(sensor1);
		sensorRepository.save(sensor2);

		List<Sensor> sensors = sensorRepository.findAll();

		assertThat(sensors).hasSize(2);
		assertThat(sensors).extracting(Sensor::getName)
				.containsExactlyInAnyOrder("Sensor-001", "Sensor-002");
	}

	@Test
	void shouldCheckIfSensorExistsByName() {
		var sensor = new Sensor("UniqueSensor", "sensor", Set.of("read"));
		sensorRepository.save(sensor);

		assertThat(sensorRepository.existsByName(sensor.getName())).isTrue();
		assertThat(sensorRepository.existsByName("NonExistentSensor")).isFalse();
	}

	@Test
	void shouldFindFirstPageOrderedById() {
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read", "write"));

		sensorRepository.save(sensor1);
		sensorRepository.save(sensor2);
		sensorRepository.save(sensor3);

		Pageable pageable = PageRequest.of(0, 2);
		List<Sensor> firstPage = sensorRepository.findFirstPage(pageable);

		assertThat(firstPage).hasSize(2);
		assertThat(firstPage.get(0).getId()).isLessThan(firstPage.get(1).getId());
	}

	@Test
	void shouldFindNextPageAfterLastId() {
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read", "write"));

		var saved1 = sensorRepository.save(sensor1);
		var saved2 = sensorRepository.save(sensor2);
		var saved3 = sensorRepository.save(sensor3);

		Pageable pageable = PageRequest.of(0, 2);
		List<Sensor> nextPage = sensorRepository.findNextPage(saved1.getId(), pageable);

		assertThat(nextPage).hasSize(2);
		assertThat(nextPage).extracting(Sensor::getId)
				.containsExactly(saved2.getId(), saved3.getId());
	}

	@Test
	void shouldDeleteSensor() {
		var sensor = new Sensor("ToDelete", "sensor", Set.of("read"));
		Sensor savedSensor = sensorRepository.save(sensor);

		sensorRepository.deleteById(savedSensor.getId());

		assertThat(sensorRepository.findById(savedSensor.getId())).isEmpty();
	}

	@Test
	void shouldUpdateSensor() {
		var sensor = new Sensor("Original", "sensor", Set.of("read"));
		Sensor savedSensor = sensorRepository.save(sensor);

		savedSensor.setName("Updated");
		savedSensor.setType("actuator");
		savedSensor.setCapabilities(Set.of("write", "execute"));

		Sensor updatedSensor = sensorRepository.save(savedSensor);

		assertThat(updatedSensor.getName()).isEqualTo("Updated");
		assertThat(updatedSensor.getType()).isEqualTo("actuator");
		assertThat(updatedSensor.getCapabilities()).containsExactlyInAnyOrder("write", "execute");
	}

	@Test
	void shouldHandleSensorWithEmptyCapabilities() {
		var sensor = new Sensor("NoCapabilities", "sensor", Set.of());
		Sensor savedSensor = sensorRepository.save(sensor);

		Optional<Sensor> foundSensor = sensorRepository.findById(savedSensor.getId());
		assertThat(foundSensor).isPresent();
		assertThat(foundSensor.get().getCapabilities()).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenNoSensorsExist() {
		assertThat(sensorRepository.findAll()).isEmpty();
	}

	@Test
	void shouldReturnEmptyOptionalWhenSensorNotFound() {
		assertThat(sensorRepository.findById(999L)).isEmpty();
	}

	@Test
	void shouldSetCreatedAtAndUpdatedAtOnCreate() {
		var beforeSave = Instant.now();
		var sensor = new Sensor("TimestampTest", "sensor", Set.of("read"));

		Sensor savedSensor = sensorRepository.save(sensor);
		var afterSave = Instant.now();

		assertThat(savedSensor.getCreatedAt()).isNotNull();
		assertThat(savedSensor.getUpdatedAt()).isNotNull();
		assertThat(savedSensor.getCreatedAt()).isBetween(beforeSave, afterSave);
		assertThat(savedSensor.getUpdatedAt()).isBetween(beforeSave, afterSave);
		assertThat(savedSensor.getCreatedAt()).isEqualTo(savedSensor.getUpdatedAt());
	}

	@Test
	void shouldUpdateUpdatedAtOnModification() throws InterruptedException {
		var sensor = new Sensor("UpdateTimestampTest", "sensor", Set.of("read"));
		Sensor savedSensor = sensorRepository.save(sensor);
		sensorRepository.flush();

		var originalCreatedAt = savedSensor.getCreatedAt();
		var originalUpdatedAt = savedSensor.getUpdatedAt();

		// Wait a bit to ensure timestamp difference
		Thread.sleep(100);

		savedSensor.setName("Modified");
		Sensor updatedSensor = sensorRepository.save(savedSensor);
		sensorRepository.flush();

		// Re-fetch to ensure we get the persisted state
		Sensor refetchedSensor = sensorRepository.findById(updatedSensor.getId()).orElseThrow();

		assertThat(refetchedSensor.getCreatedAt()).isEqualTo(originalCreatedAt);
		assertThat(refetchedSensor.getUpdatedAt()).isAfter(originalUpdatedAt);
		assertThat(refetchedSensor.getUpdatedAt()).isNotEqualTo(refetchedSensor.getCreatedAt());
	}

	@Test
	void shouldPreserveTimestampsOnRead() {
		var sensor = new Sensor("ReadTimestampTest", "sensor", Set.of("read"));
		Sensor savedSensor = sensorRepository.save(sensor);

		var savedCreatedAt = savedSensor.getCreatedAt();
		var savedUpdatedAt = savedSensor.getUpdatedAt();

		Sensor foundSensor = sensorRepository.findById(savedSensor.getId()).orElseThrow();

		assertThat(foundSensor.getCreatedAt()).isEqualTo(savedCreatedAt);
		assertThat(foundSensor.getUpdatedAt()).isEqualTo(savedUpdatedAt);
	}
}
