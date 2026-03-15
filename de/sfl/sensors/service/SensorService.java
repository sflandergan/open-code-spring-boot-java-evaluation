package de.sfl.sensors.service;

import de.sfl.sensors.model.Sensor;
import java.util.List;

public interface SensorService {
    Sensor createSensor(Sensor sensor);
    Sensor updateSensor(Sensor sensor);
    void deleteSensor(Long id);
    Sensor assignSensorToDevice(Long sensorId, Long deviceId);
    List<Sensor> getAllSensors();
    Sensor getSensorById(Long id);
}