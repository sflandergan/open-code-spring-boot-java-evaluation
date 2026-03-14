package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByDeviceIdAndSensorId(UUID deviceId, Long sensorId);
}