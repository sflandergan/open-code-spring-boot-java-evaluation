package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByIdDeviceIdAndIdSensorId(UUID deviceId, Long sensorId);

	List<DeviceSensor> findByIdDeviceId(UUID deviceId);

	@Query("SELECT COUNT(s) > 0 FROM Sensor s WHERE s.id = :sensorId")
	boolean sensorExists(@Param("sensorId") Long sensorId);
}
