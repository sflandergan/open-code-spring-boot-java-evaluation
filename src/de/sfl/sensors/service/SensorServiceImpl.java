package de.sfl.sensors.service;

import de.sfl.sensors.model.Sensor;
import de.sfl.sensors.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorServiceImpl implements SensorService {
    @Autowired
    private SensorRepository sensorRepository;

    @Override
    public Sensor createSensor(Sensor sensor) {
        return sensorRepository.save(sensor);
    }

    @Override
    public Sensor updateSensor(Sensor sensor) {
        return sensorRepository.save(sensor);
    }

    @Override
    public void deleteSensor(Long id) {
        sensorRepository.deleteById(id);
    }

    @Override
    public Sensor assignSensorToDevice(Long sensorId, Long deviceId) {
        // Implementation for assigning a sensor to a device
        return null;
    }

    @Override
    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }

    @Override
    public Sensor getSensorById(Long id) {
        return sensorRepository.findById(id).orElse(null);
    }
}