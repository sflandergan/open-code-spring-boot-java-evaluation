package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	boolean existsByIdDeviceIdAndIdSensorId(UUID deviceId, Long sensorId);

	@Query("SELECT ds FROM DeviceSensor ds WHERE ds.id.deviceId = :deviceId")
	List<DeviceSensor> findAllByDeviceId(@Param("deviceId") UUID deviceId);

	void deleteByIdDeviceId(UUID deviceId);
}
