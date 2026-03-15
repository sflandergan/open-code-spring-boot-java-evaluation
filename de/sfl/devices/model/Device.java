package de.sfl.devices.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String capabilities;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(id, device.id) &&
                Objects.equals(name, device.name) &&
                Objects.equals(type, device.type) &&
                Objects.equals(capabilities, device.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, capabilities);
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '"' +
                ", type='" + type + '"' +
                ", capabilities='" + capabilities + '"' +
                '}';
    }
}