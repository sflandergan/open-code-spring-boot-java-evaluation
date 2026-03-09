package de.sfl.devices;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface DeviceRepository /*remove class*/ extends JpaRepository<Device, UUID> {

    boolean existsByName(String name);
    //find by name
    Device findByName(String name);
    
    // Additional custom queries can be added here
}

interface DeviceSensorRepository extends JpaRepository<DeviceSensor, Long> {
    // Add any custom methods here if needed
    Optional<DeviceSensor> findByDeviceIdAndSensorId(UUID deviceId, Long sensorId);
    void deleteByDeviceIdAndSensorId(UUID deviceId, Long sensorId);
}