package de.sfl.devices;

import de.sfl.RepositoryIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaDeviceRepositoryIT extends RepositoryIT {

    @Autowired
    private JpaDeviceRepository deviceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindDevice() {
        // Given
        var device = new Device("Test Device", "Test Description");
        
        // When
        var savedDevice = deviceRepository.save(device);
        
        var foundDevice = deviceRepository.findById(savedDevice.getId());
        
        // Then
        assertThat(foundDevice).isPresent();
        assertThat(foundDevice.get().getId()).isEqualTo(savedDevice.getId());
        assertThat(foundDevice.get().getName()).isEqualTo("Test Device");
        assertThat(foundDevice.get().getDescription()).isEqualTo("Test Description");
    }

    @Test
    void shouldDeleteDevice() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        // When
        deviceRepository.deleteById(savedDevice.getId());
        
        // Then
        var deletedDevice = deviceRepository.findById(savedDevice.getId());
        assertThat(deletedDevice).isEmpty();
    }

    @Test
    void shouldUpdateDevice() {
        // Given
        var device = new Device("Test Device", "Test Description");
        var savedDevice = deviceRepository.save(device);
        
        // When
        var deviceToUpdate = deviceRepository.findById(savedDevice.getId()).orElseThrow();
        deviceToUpdate.setName("Updated Device");
        deviceToUpdate.setDescription("Updated Description");
        var updatedDevice = deviceRepository.save(deviceToUpdate);
        
        // Then
        var foundDevice = deviceRepository.findById(updatedDevice.getId());
        assertThat(foundDevice).isPresent();
        assertThat(foundDevice.get().getName()).isEqualTo("Updated Device");
        assertThat(foundDevice.get().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void shouldFindAllDevices() {
        // Given
        var device1 = new Device("Device 1", "Description 1");
        var device2 = new Device("Device 2", "Description 2");
        deviceRepository.save(device1);
        deviceRepository.save(device2);
        deviceRepository.flush();
        
        // When
        var allDevices = deviceRepository.findAll();
        
        // Then
        assertThat(allDevices).hasSize(2);
    }
}