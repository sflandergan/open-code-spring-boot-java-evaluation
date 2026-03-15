package de.sfl.devices;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_sensors")
public class DeviceSensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorType;

    @Column(name = "installed_at", nullable = false)
    private LocalDateTime installedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    // Default constructor
    public DeviceSensor() {
        this.installedAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public DeviceSensor(String sensorType) {
        this.sensorType = sensorType;
        this.installedAt = LocalDateTime.now();
    }
}