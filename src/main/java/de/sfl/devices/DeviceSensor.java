package de.sfl.devices;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import de.sfl.sensors.Sensor;

/**
 * JPA entity representing the junction table for Device-Sensor assignment.
 */
@Entity
@Table(name = "device_sensors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSensor {

    /**
     * Composite identifier.
     */
    @EmbeddedId
    private DeviceSensorId id;

    /**
     * Back reference to the owning device.
     */
    @ManyToOne
    @MapsId("deviceId")
    private Device device;

    /**
     * Back reference to the sensor.
     */
    @ManyToOne
    @MapsId("sensorId")
    private Sensor sensor;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void prePersist() {
        assignedAt = LocalDateTime.now();
    }
}