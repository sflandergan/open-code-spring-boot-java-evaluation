package de.sfl.sensors;

import de.sfl.PageResult;
import de.sfl.sensors.Sensor;
import de.sfl.sensors.SensorAlreadyExistsException;
import de.sfl.sensors.SensorNotFoundException;
import de.sfl.sensors.SensorService;
import de.sfl.sensors.JpaSensorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

	@Mock
	private JpaSensorRepository sensorRepository;

	@Captor
	private ArgumentCaptor<Pageable> pageableCaptor;

	private SensorService sensorService;

	@BeforeEach
	void setUp() {
		sensorService = new SensorService(sensorRepository);
	}

	@Test
	void getSensors_shouldReturnHasMoreTrueWhenMorePagesExist() {
		// Given: pageSize=2, but repository returns 3 sensors (pageSize + 1)
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read", "write"));

		when(sensorRepository.findFirstPage(any(Pageable.class)))
			.thenReturn(List.of(sensor1, sensor2, sensor3));

		// When
		PageResult<Sensor> result = sensorService.getSensors(null, 2);

		// Then
		assertThat(result.items()).hasSize(2);  // Only return pageSize items
		assertThat(result.hasMore()).isTrue();  // More pages exist
		assertThat(result.items())
			.containsExactly(sensor1, sensor2);

		verify(sensorRepository).findFirstPage(any(Pageable.class));
	}

	@Test
	void getSensors_shouldReturnHasMoreFalseWhenNoMorePagesExist() {
		// Given: pageSize=2, repository returns only 2 sensors (less than pageSize + 1)
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));

		when(sensorRepository.findFirstPage(any(Pageable.class)))
			.thenReturn(List.of(sensor1, sensor2));

		// When
		PageResult<Sensor> result = sensorService.getSensors(null, 2);

		// Then
		assertThat(result.items()).hasSize(2);
		assertThat(result.hasMore()).isFalse();  // No more pages
		assertThat(result.items())
			.containsExactly(sensor1, sensor2);
	}

	@Test
	void getSensors_shouldReturnHasMoreFalseWhenLastPageHasExactlyPageSizeElements() {
		// Given: Edge case - last page has exactly pageSize elements
		// pageSize=2, repository returns only 2 sensors (exactly pageSize, not pageSize + 1)
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read"));
		var sensor4 = new Sensor("Sensor-004", "actuator", Set.of("write"));

		when(sensorRepository.findNextPage(eq(2L), any(Pageable.class)))
			.thenReturn(List.of(sensor3, sensor4));

		// When
		PageResult<Sensor> result = sensorService.getSensors(2L, 2);

		// Then
		assertThat(result.items()).hasSize(2);
		assertThat(result.hasMore()).isFalse();  // No more pages (this is the fix!)
		assertThat(result.items())
			.containsExactly(sensor3, sensor4);

		verify(sensorRepository).findNextPage(eq(2L), any(Pageable.class));
	}

	@Test
	void getSensors_shouldReturnEmptyListWhenNoSensorsExist() {
		// Given
		when(sensorRepository.findFirstPage(any(Pageable.class)))
			.thenReturn(List.of());

		// When
		PageResult<Sensor> result = sensorService.getSensors(null, 10);

		// Then
		assertThat(result.items()).isEmpty();
		assertThat(result.hasMore()).isFalse();
	}

	@Test
	void getSensors_shouldReturnHasMoreFalseWhenFewerThanPageSizeElementsReturned() {
		// Given: pageSize=10, but only 3 sensors exist
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read", "write"));

		when(sensorRepository.findFirstPage(any(Pageable.class)))
			.thenReturn(List.of(sensor1, sensor2, sensor3));

		// When
		PageResult<Sensor> result = sensorService.getSensors(null, 10);

		// Then
		assertThat(result.items()).hasSize(3);
		assertThat(result.hasMore()).isFalse();
	}

	@Test
	void getSensors_shouldUseLastIdForNextPage() {
		// Given
		var sensor3 = new Sensor("Sensor-003", "sensor", Set.of("read"));
		var sensor4 = new Sensor("Sensor-004", "actuator", Set.of("write"));

		when(sensorRepository.findNextPage(eq(2L), any(Pageable.class)))
			.thenReturn(List.of(sensor3, sensor4));

		// When
		PageResult<Sensor> result = sensorService.getSensors(2L, 10);

		// Then
		assertThat(result.items()).hasSize(2);
		verify(sensorRepository).findNextPage(eq(2L), any(Pageable.class));
	}

	@Test
	void getSensors_shouldRequestPageSizePlusOneFromRepository() {
		// Given
		when(sensorRepository.findFirstPage(any(Pageable.class)))
			.thenReturn(List.of());

		// When
		int pageSize = 10;
		sensorService.getSensors(null, pageSize);

		// Then: Verify repository is called with pageSize + 1
		verify(sensorRepository).findFirstPage(pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(pageSize + 1);
	}

	@Test
	void getAllSensors_shouldReturnAllSensors() {
		// Given
		var sensor1 = new Sensor("Sensor-001", "sensor", Set.of("read"));
		var sensor2 = new Sensor("Sensor-002", "actuator", Set.of("write"));
		var sensor3 = new Sensor("Sensor-003", "gateway", Set.of("read", "write"));

		when(sensorRepository.findAll()).thenReturn(List.of(sensor1, sensor2, sensor3));

		// When
		List<Sensor> result = sensorService.getAllSensors();

		// Then
		assertThat(result).hasSize(3);
		assertThat(result).containsExactly(sensor1, sensor2, sensor3);
		verify(sensorRepository).findAll();
	}

	@Test
	void getAllSensors_shouldReturnEmptyListWhenNoSensorsExist() {
		// Given
		when(sensorRepository.findAll()).thenReturn(List.of());

		// When
		List<Sensor> result = sensorService.getAllSensors();

		// Then
		assertThat(result).isEmpty();
		verify(sensorRepository).findAll();
	}

	@Test
	void getSensorById_shouldReturnSensor_whenSensorExists() {
		// Given
		var sensor = new TestSensor(1L, "Test Sensor", "sensor", Set.of("read"));
		when(sensorRepository.findById(1L)).thenReturn(java.util.Optional.of(sensor));

		// When
		Sensor result = sensorService.getSensorById(1L);

		// Then
		assertThat(result).isEqualTo(sensor);
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Test Sensor");
		verify(sensorRepository).findById(1L);
	}

	@Test
	void getSensorById_shouldThrowSensorNotFoundException_whenSensorDoesNotExist() {
		// Given
		when(sensorRepository.findById(999L)).thenReturn(java.util.Optional.empty());

		// When/Then
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> sensorService.getSensorById(999L))
			.isInstanceOf(SensorNotFoundException.class)
			.hasMessageContaining("999");

		verify(sensorRepository).findById(999L);
	}

	@Test
	void createSensor_shouldSaveAndReturnSensor_whenSensorNameIsUnique() {
		// Given
		var sensor = new Sensor("New Sensor", "sensor", Set.of("read"));
		var savedSensor = new TestSensor(1L, "New Sensor", "sensor", Set.of("read"));

		when(sensorRepository.existsByName("New Sensor")).thenReturn(false);
		when(sensorRepository.save(sensor)).thenReturn(savedSensor);

		// When
		Sensor result = sensorService.createSensor(sensor);

		// Then
		assertThat(result).isEqualTo(savedSensor);
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("New Sensor");
		verify(sensorRepository).existsByName("New Sensor");
		verify(sensorRepository).save(sensor);
	}

	@Test
	void createSensor_shouldThrowSensorAlreadyExistsException_whenSensorNameExists() {
		// Given
		var sensor = new Sensor("Existing Sensor", "sensor", Set.of("read"));
		when(sensorRepository.existsByName("Existing Sensor")).thenReturn(true);

		// When/Then
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> sensorService.createSensor(sensor))
			.isInstanceOf(SensorAlreadyExistsException.class)
			.hasMessageContaining("Existing Sensor");

		verify(sensorRepository).existsByName("Existing Sensor");
	}

	static class TestSensor extends Sensor {
		TestSensor(Long id, String name, String type, Set<String> capabilities) {
			super(name, type, capabilities);
			this.id = id;
		}
	}
}
