package de.sfl.devices;

import de.sfl.RepositoryIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

	@Autowired
	private JpaDeviceSensorRepository deviceSensorRepository;

	@Autowired
	private JpaDeviceRepository deviceRepository;

	@Autowired
	private EntityManager entityManager;

	private Device savedDevice;
	private Long savedSensorId1;
	private Long savedSensorId2;

	@BeforeEach
	void setUp() {
		deviceSensorRepository.deleteAll();
		deviceRepository.deleteAll();
		entityManager.createNativeQuery("DELETE FROM sensor_capabilities").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM sensors").executeUpdate();
		entityManager.flush();

		savedDevice = deviceRepository.save(new Device("Test Device", "Test Description"));

		entityManager.createNativeQuery("INSERT INTO sensors (name, type, created_at, updated_at) VALUES (:name, :type, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
				.setParameter("name", "Sensor-001")
				.setParameter("type", "temperature")
				.executeUpdate();
		savedSensorId1 = ((Number) entityManager.createNativeQuery("SELECT id FROM sensors WHERE name = 'Sensor-001'")
				.getSingleResult()).longValue();

		entityManager.createNativeQuery("INSERT INTO sensors (name, type, created_at, updated_at) VALUES (:name, :type, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
				.setParameter("name", "Sensor-002")
				.setParameter("type", "humidity")
				.executeUpdate();
		savedSensorId2 = ((Number) entityManager.createNativeQuery("SELECT id FROM sensors WHERE name = 'Sensor-002'")
				.getSingleResult()).longValue();

		entityManager.flush();
	}

	@Test
	void shouldSaveAndFindAssignment() {
		var assignmentId = new DeviceSensorId(savedDevice.getId(), savedSensorId1);
		var assignment = new DeviceSensor(assignmentId);

		DeviceSensor savedAssignment = deviceSensorRepository.save(assignment);

		assertThat(savedAssignment.getId()).isNotNull();
		assertThat(savedAssignment.getId().getDeviceId()).isEqualTo(savedDevice.getId());
		assertThat(savedAssignment.getId().getSensorId()).isEqualTo(savedSensorId1);
	}

	@Test
	void shouldFindAssignmentsByDeviceId() {
		var assignment1 = new DeviceSensor(new DeviceSensorId(savedDevice.getId(), savedSensorId1));
		var assignment2 = new DeviceSensor(new DeviceSensorId(savedDevice.getId(), savedSensorId2));

		deviceSensorRepository.save(assignment1);
		deviceSensorRepository.save(assignment2);

		List<DeviceSensor> assignments = deviceSensorRepository.findByIdDeviceId(savedDevice.getId());

		assertThat(assignments).hasSize(2);
	}

	@Test
	void shouldReturnEmptyListWhenNoAssignmentsExist() {
		List<DeviceSensor> assignments = deviceSensorRepository.findByIdDeviceId(savedDevice.getId());

		assertThat(assignments).isEmpty();
	}

	@Test
	void shouldCheckExistenceByCompositeKey() {
		var assignmentId = new DeviceSensorId(savedDevice.getId(), savedSensorId1);
		var assignment = new DeviceSensor(assignmentId);
		deviceSensorRepository.save(assignment);

		assertThat(deviceSensorRepository.existsById(assignmentId)).isTrue();
		assertThat(deviceSensorRepository.existsById(new DeviceSensorId(savedDevice.getId(), savedSensorId2))).isFalse();
	}

	@Test
	void shouldDeleteAssignment() {
		var assignmentId = new DeviceSensorId(savedDevice.getId(), savedSensorId1);
		var assignment = new DeviceSensor(assignmentId);
		deviceSensorRepository.save(assignment);

		deviceSensorRepository.deleteById(assignmentId);

		assertThat(deviceSensorRepository.existsById(assignmentId)).isFalse();
	}

	@Test
	void shouldCascadeDeleteWhenDeviceIsDeleted() {
		var assignment = new DeviceSensor(new DeviceSensorId(savedDevice.getId(), savedSensorId1));
		deviceSensorRepository.save(assignment);
		deviceSensorRepository.flush();

		deviceRepository.delete(savedDevice);
		deviceRepository.flush();

		assertThat(deviceSensorRepository.findByIdDeviceId(savedDevice.getId())).isEmpty();
	}

	@Test
	void shouldCascadeDeleteWhenSensorIsDeleted() {
		var assignment = new DeviceSensor(new DeviceSensorId(savedDevice.getId(), savedSensorId1));
		deviceSensorRepository.save(assignment);
		deviceSensorRepository.flush();

		entityManager.createNativeQuery("DELETE FROM device_sensors WHERE sensor_id = :sensorId")
				.setParameter("sensorId", savedSensorId1)
				.executeUpdate();
		entityManager.createNativeQuery("DELETE FROM sensor_capabilities WHERE sensor_id = :sensorId")
				.setParameter("sensorId", savedSensorId1)
				.executeUpdate();
		entityManager.createNativeQuery("DELETE FROM sensors WHERE id = :sensorId")
				.setParameter("sensorId", savedSensorId1)
				.executeUpdate();
		entityManager.flush();
		entityManager.clear();

		var assignmentId = new DeviceSensorId(savedDevice.getId(), savedSensorId1);
		assertThat(deviceSensorRepository.existsById(assignmentId)).isFalse();
	}

	@Test
	void shouldSetAssignedAtOnCreate() {
		var assignmentId = new DeviceSensorId(savedDevice.getId(), savedSensorId1);
		var assignment = new DeviceSensor(assignmentId);

		DeviceSensor savedAssignment = deviceSensorRepository.save(assignment);

		assertThat(savedAssignment.getAssignedAt()).isNotNull();
	}

	@Test
	void shouldRejectAssignmentWithNonExistentDevice() {
		var nonExistentDeviceId = UUID.randomUUID();
		var assignmentId = new DeviceSensorId(nonExistentDeviceId, savedSensorId1);
		var assignment = new DeviceSensor(assignmentId);

		assertThatThrownBy(() -> {
			deviceSensorRepository.save(assignment);
			deviceSensorRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void shouldRejectAssignmentWithNonExistentSensor() {
		var assignmentId = new DeviceSensorId(savedDevice.getId(), 999999L);
		var assignment = new DeviceSensor(assignmentId);

		assertThatThrownBy(() -> {
			deviceSensorRepository.save(assignment);
			deviceSensorRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}
}
