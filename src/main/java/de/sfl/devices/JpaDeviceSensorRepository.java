package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByIdDeviceIdAndIdSensorId(Long deviceId, Long sensorId);
}
