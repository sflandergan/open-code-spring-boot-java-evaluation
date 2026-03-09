package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaDeviceRepository deviceRepository;

    @Autowired
    private JpaDeviceSensorRepository deviceSensorRepository;

    @BeforeEach
    void setUp() {
        deviceSensorRepository.deleteAll();
        deviceRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM sensors CASCADE");
    }

    @Test
    void shouldSaveAndFindDeviceById() {
        var device = new Device("Device-001", "Test Description");

        Device savedDevice = deviceRepository.save(device);

        assertThat(savedDevice.getId()).isNotNull();
        assertThat(savedDevice.getName()).isEqualTo("Device-001");
        assertThat(savedDevice.getDescription()).isEqualTo("Test Description");

        Optional<Device> foundDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(foundDevice).isPresent();
        assertThat(foundDevice.get().getName()).isEqualTo("Device-001");
    }

    @Test
    void shouldFindAllDevices() {
        var device1 = new Device("Device-001", "Description 1");
        var device2 = new Device("Device-002", "Description 2");

        deviceRepository.save(device1);
        deviceRepository.save(device2);

        var devices = deviceRepository.findAll();

        assertThat(devices).hasSize(2);
    }

    @Test
    void shouldDeleteDevice() {
        var device = new Device("ToDelete", "Description");
        Device savedDevice = deviceRepository.save(device);

        deviceRepository.deleteById(savedDevice.getId());

        assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
    }

    @Test
    void shouldUpdateDevice() {
        var device = new Device("Original", "Original Description");
        Device savedDevice = deviceRepository.save(device);

        savedDevice.setName("Updated");
        savedDevice.setDescription("Updated Description");
        Device updatedDevice = deviceRepository.save(savedDevice);

        assertThat(updatedDevice.getName()).isEqualTo("Updated");
        assertThat(updatedDevice.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var device = new Device("TimestampTest", "Description");

        Device savedDevice = deviceRepository.save(device);

        assertThat(savedDevice.getCreatedAt()).isNotNull();
        assertThat(savedDevice.getUpdatedAt()).isNotNull();
        assertThat(savedDevice.getCreatedAt()).isEqualTo(savedDevice.getUpdatedAt());
    }

    @Test
    void shouldCascadeDeleteDeviceSensors() {
        jdbcTemplate.execute("INSERT INTO sensors (name, type) VALUES ('Test Sensor', 'type')");

        Long sensorId = jdbcTemplate.queryForObject("SELECT id FROM sensors WHERE name = 'Test Sensor'", Long.class);

        var device = new Device("Device-001", "Description");
        Device savedDevice = deviceRepository.save(device);

        var deviceSensor = new DeviceSensor(savedDevice, sensorId);
        savedDevice.addDeviceSensor(deviceSensor);
        deviceRepository.save(savedDevice);
        deviceRepository.flush();

        deviceRepository.deleteById(savedDevice.getId());
        deviceRepository.flush();

        assertThat(deviceRepository.findById(savedDevice.getId())).isEmpty();
        assertThat(deviceSensorRepository.findAll()).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveDeviceSensorAssignment() {
        jdbcTemplate.execute("INSERT INTO sensors (name, type) VALUES ('Sensor-001', 'type')");

        Long sensorId = jdbcTemplate.queryForObject("SELECT id FROM sensors WHERE name = 'Sensor-001'", Long.class);

        var device = new Device("Device-001", "Description");
        Device savedDevice = deviceRepository.save(device);

        var deviceSensor = new DeviceSensor(savedDevice, sensorId);
        deviceSensorRepository.save(deviceSensor);

        boolean exists = deviceSensorRepository.existsByDeviceIdAndSensorId(savedDevice.getId(), sensorId);

        assertThat(exists).isTrue();
    }
}
