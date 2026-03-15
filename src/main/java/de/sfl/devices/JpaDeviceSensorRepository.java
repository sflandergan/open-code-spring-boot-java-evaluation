package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaDeviceSensorRepository extends JpaRepository<DeviceSensor, DeviceSensorId> {

	List<DeviceSensor> findByIdDeviceId(UUID deviceId);
}
