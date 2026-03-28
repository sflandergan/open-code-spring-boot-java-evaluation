package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByIdDeviceIdAndIdSensorId(UUID deviceId, Long sensorId);

	List<DeviceSensor> findByIdDeviceId(UUID deviceId);
}
