package de.sfl.devices;

import de.sfl.RepositoryIT;
import de.sfl.sensors.Sensor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceSensorRepositoryIT extends RepositoryIT {

    @Autowired
    private JpaDeviceSensorRepository deviceSensorRepository;

    @Autowired
    private JpaDeviceRepository deviceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        deviceSensorRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindDeviceSensorAssociation() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        var sensor = new Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        
        // When
        var savedAssociation = deviceSensorRepository.save(deviceSensor);
        
        var foundAssociation = deviceSensorRepository.findById(savedAssociation.getId());
        
        // Then
        assertThat(foundAssociation).isPresent();
        assertThat(foundAssociation.get().getDeviceId()).isEqualTo(savedDevice.getId());
        assertThat(foundAssociation.get().getSensorId()).isEqualTo(savedSensor.getId());
    }

    @Test
    void shouldCheckIfAssociationExists() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        var sensor = new Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        deviceSensorRepository.save(deviceSensor);
        
        // When
        boolean exists = deviceSensorRepository.existsById_DeviceIdAndId_SensorId(savedDevice.getId(), savedSensor.getId());
        
        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldNotFindNonExistentAssociation() {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 99999L;
        
        // When
        boolean exists = deviceSensorRepository.existsById_DeviceIdAndId_SensorId(deviceId, sensorId);
        
        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldDeleteDeviceSensorAssociation() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        var sensor = new Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        var savedAssociation = deviceSensorRepository.save(deviceSensor);
        
        // When
        deviceSensorRepository.deleteById(savedAssociation.getId());
        
        // Then
        var deletedAssociation = deviceSensorRepository.findById(savedAssociation.getId());
        assertThat(deletedAssociation).isEmpty();
    }

    @Test
    void shouldCascadeDeleteAssociationsWhenDeviceDeleted() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        var sensor = new Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        deviceSensorRepository.save(deviceSensor);
        entityManager.flush();
        
        // When
        deviceRepository.deleteById(savedDevice.getId());
        deviceRepository.flush();
        entityManager.clear();
        
        // Then
        var deletedDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(deletedDevice).isEmpty();
        
        var deletedAssociation = deviceSensorRepository.findById(new DeviceSensorId(savedDevice.getId(), savedSensor.getId()));
        assertThat(deletedAssociation).isEmpty();
    }
}