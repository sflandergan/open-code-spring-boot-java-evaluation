package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	@Query("""
		SELECT COUNT(ds) > 0 FROM DeviceSensor ds
		WHERE ds.id.deviceId = :deviceId AND ds.id.sensorId = :sensorId
		""")
	boolean existsByDeviceIdAndSensorId(@Param("deviceId") UUID deviceId, @Param("sensorId") Long sensorId);
}
