package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.AopTestUtils;
import jakarta.persistence.EntityManager;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

    @Autowired
    private JpaDeviceSensorRepository deviceSensorRepository;

    @Autowired
    private JpaDeviceRepository deviceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Device device;
    private Long sensor1Id;
    private Long sensor2Id;

    @BeforeEach
    void setUp() {
        deviceSensorRepository.deleteAll();
        deviceRepository.deleteAll();

        // Create test sensors directly via SQL to avoid package-private repository
        jdbcTemplate.update("INSERT INTO sensors (id, name, type, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                1L, "Sensor-001", "temperature");
        jdbcTemplate.update("INSERT INTO sensors (id, name, type, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                2L, "Sensor-002", "humidity");

        sensor1Id = 1L;
        sensor2Id = 2L;

        // Create test device
        device = new Device("Device-001", "Test device");
        deviceRepository.save(device);
    }

    @Test
    void shouldAssignSensorToDevice() {
        var deviceSensorId = new DeviceSensorId(device.getId(), sensor1Id);
        var deviceSensor = new DeviceSensor(device, deviceSensorId);

        var savedAssignment = deviceSensorRepository.save(deviceSensor);

        assertThat(savedAssignment.getId()).isNotNull();
        assertThat(savedAssignment.getId().getDeviceId()).isEqualTo(device.getId());
        assertThat(savedAssignment.getId().getSensorId()).isEqualTo(sensor1Id);
        assertThat(savedAssignment.getAssignedAt()).isNotNull();
    }

    @Test
    void shouldCheckIfAssignmentExists() {
        var deviceSensorId = new DeviceSensorId(device.getId(), sensor1Id);
        var deviceSensor = new DeviceSensor(device, deviceSensorId);
        deviceSensorRepository.save(deviceSensor);

        assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensor1Id)).isTrue();
        assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensor2Id)).isFalse();
    }

    @Test
    void shouldAssignMultipleSensorsToOneDevice() {
        var deviceSensorId1 = new DeviceSensorId(device.getId(), sensor1Id);
        var deviceSensorId2 = new DeviceSensorId(device.getId(), sensor2Id);

        deviceSensorRepository.save(new DeviceSensor(device, deviceSensorId1));
        deviceSensorRepository.save(new DeviceSensor(device, deviceSensorId2));

        var allAssignments = deviceSensorRepository.findAll();

        assertThat(allAssignments).hasSize(2);
    }

    @Test
    void shouldRemoveAssignment() {
        var deviceSensorId = new DeviceSensorId(device.getId(), sensor1Id);
        var deviceSensor = new DeviceSensor(device, deviceSensorId);
        deviceSensorRepository.save(deviceSensor);

        deviceSensorRepository.deleteById(deviceSensorId);

        assertThat(deviceSensorRepository.existsByIdDeviceIdAndIdSensorId(device.getId(), sensor1Id)).isFalse();
    }

    @Test
    void shouldCascadeDeleteAssignmentsWhenDeviceIsDeleted() {
        // Create and maintain bidirectional relationship
        var deviceSensorId1 = new DeviceSensorId(device.getId(), sensor1Id);
        var deviceSensorId2 = new DeviceSensorId(device.getId(), sensor2Id);

        var assignment1 = new DeviceSensor(device, deviceSensorId1);
        var assignment2 = new DeviceSensor(device, deviceSensorId2);
        
        // Add to device's collection to maintain bidirectional relationship
        var assignmentSet = device.getDeviceSensors();
        assignmentSet.add(assignment1);
        assignmentSet.add(assignment2);
        device.setDeviceSensors(assignmentSet);
        
        // Save assignments
        deviceSensorRepository.save(assignment1);
        deviceSensorRepository.save(assignment2);
        
        // Save device with relationship
        deviceRepository.save(device);
        entityManager.flush();

        // Verify assignments exist before deletion
        assertThat(deviceSensorRepository.findAll()).hasSize(2);
        
        // Delete device (JPA will cascade delete assignments)
        deviceRepository.delete(device);
        entityManager.flush();
        
        // Clear cache to force fresh load
        entityManager.clear();
        
        // Verify cascade deletion worked
        assertThat(deviceSensorRepository.findAll()).isEmpty();
    }

    @Test
    void shouldHandleCompositeKeyEquality() {
        var id1 = new DeviceSensorId(device.getId(), sensor1Id);
        var id2 = new DeviceSensorId(device.getId(), sensor1Id);
        var id3 = new DeviceSensorId(device.getId(), sensor2Id);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
    }

    @Test
    void shouldReturnEmptyListWhenNoAssignmentsExist() {
        assertThat(deviceSensorRepository.findAll()).isEmpty();
    }
}
