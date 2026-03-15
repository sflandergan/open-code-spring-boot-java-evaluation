package de.sfl.devices;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String model;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DeviceSensor> sensors;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<DeviceSensor> getSensors() {
        return sensors;
    }

    public void setSensors(Set<DeviceSensor> sensors) {
        this.sensors = sensors;
    }

    // Default constructor
    public Device() {
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public Device(String serialNumber, String model) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.createdAt = LocalDateTime.now();
    }

    // Utility method to add a sensor
    public void addSensor(DeviceSensor sensor) {
        sensors.add(sensor);
        sensor.setDevice(this);
    }

    // Utility method to remove a sensor
    public void removeSensor(DeviceSensor sensor) {
        sensors.remove(sensor);
        sensor.setDevice(null);
    }
}