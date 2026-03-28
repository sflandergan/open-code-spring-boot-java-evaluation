package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByIdDeviceIdAndIdSensorId(UUID deviceId, Long sensorId);

	List<DeviceSensor> findAllByIdDeviceId(UUID deviceId);

	@Query(value = "SELECT EXISTS (SELECT 1 FROM sensors WHERE id = :sensorId)", nativeQuery = true)
	boolean existsSensorById(@Param("sensorId") Long sensorId);
}
