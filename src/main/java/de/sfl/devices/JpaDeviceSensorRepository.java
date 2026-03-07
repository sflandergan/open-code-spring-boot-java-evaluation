package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {
    boolean existsById_DeviceIdAndId_SensorId(UUID deviceId, Long sensorId);
}
