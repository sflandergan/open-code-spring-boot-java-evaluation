package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring Data JPA repository for DeviceSensor entity.
 */
public interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {
    boolean existsByDeviceIdAndSensorId(UUID deviceId, UUID sensorId);
}