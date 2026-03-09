package de.sfl.devices;

import de.sfl.sensors.Sensor;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
  
    @Column
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DeviceSensor> deviceSensors = new HashSet<>();

    protected Device() {
        // JPA
    }

    public Device(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Indicates if this device is currently assigned a given sensor
    public boolean hasSensor(Sensor sensor) {
        return deviceSensors.stream()
            .anyMatch(ds -> ds.getSensor().equals(sensor));
    }

    public void assignSensor(Sensor sensor) {
        if (!hasSensor(sensor)) {
            DeviceSensor assignment = new DeviceSensor(this, sensor);
            // bidirectional
            deviceSensors.add(assignment);
        }
    }

    public void removeSensor(Sensor sensor) {
        deviceSensors.removeIf(ds -> ds.getSensor().equals(sensor));
    }

    // getters & setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    public Set<Sensor> getSensors() {
        Set<Sensor> sensors = new HashSet<>();
        for (DeviceSensor ds : deviceSensors) {
            sensors.add(ds.getSensor());
        }
        return sensors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id != null && id.equals(device.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}