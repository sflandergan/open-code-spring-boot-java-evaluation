package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.PersistenceException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class DeviceEntityTest extends RepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveDevice() {
        // Given
        var device = new Device("Test Device", "Test Description");
        
        // When
        var savedDevice = entityManager.persistAndFlush(device);
        entityManager.clear();
        
        var foundDevice = entityManager.find(Device.class, savedDevice.getId());
        
        // Then
        assertThat(foundDevice).isNotNull();
        assertThat(foundDevice.getId()).isEqualTo(savedDevice.getId());
        assertThat(foundDevice.getName()).isEqualTo("Test Device");
        assertThat(foundDevice.getDescription()).isEqualTo("Test Description");
        assertThat(foundDevice.getCreatedAt()).isNotNull();
        assertThat(foundDevice.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldValidateDeviceNameNotNull() {
        // Given
        var device = new Device();
        device.setDescription("Test Description");
        
        // When
        var thrown = catchThrowable(() -> {
            entityManager.persistAndFlush(device);
            entityManager.clear();
        });
        
        // Then
        assertThat(thrown).isInstanceOf(PersistenceException.class);
    }

    @Test
    void shouldCreateDeviceSensorAssociation() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = entityManager.persistAndFlush(device);
        
        // Create a sensor (we need to check if sensors exist first)
        var sensor = new de.sfl.sensors.Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        
        // When
        var savedAssociation = entityManager.persistAndFlush(deviceSensor);
        entityManager.clear();
        
        var foundAssociation = entityManager.find(DeviceSensor.class, savedAssociation.getId());
        
        // Then
        assertThat(foundAssociation).isNotNull();
        assertThat(foundAssociation.getDeviceId()).isEqualTo(savedDevice.getId());
        assertThat(foundAssociation.getSensorId()).isEqualTo(savedSensor.getId());
        assertThat(foundAssociation.getAssignedAt()).isNotNull();
    }

    @Test
    void shouldCascadeDeleteDeviceSensorsWhenDeviceDeleted() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = entityManager.persistAndFlush(device);
        
        var sensor = new de.sfl.sensors.Sensor("Test Sensor", "TEMPERATURE", java.util.Set.of("read", "write"));
        var savedSensor = entityManager.persistAndFlush(sensor);
        
        var deviceSensor = new DeviceSensor(savedDevice.getId(), savedSensor.getId());
        entityManager.persistAndFlush(deviceSensor);
        entityManager.clear();
        
        // When
        var deviceToDelete = entityManager.find(Device.class, savedDevice.getId());
        entityManager.remove(deviceToDelete);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        var deletedDevice = entityManager.find(Device.class, savedDevice.getId());
        assertThat(deletedDevice).isNull();
        
        var deletedAssociation = entityManager.find(DeviceSensor.class, new DeviceSensorId(savedDevice.getId(), savedSensor.getId()));
        assertThat(deletedAssociation).isNull();
    }
}