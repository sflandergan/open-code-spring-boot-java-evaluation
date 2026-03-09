package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface JpaDeviceRepository extends JpaRepository<Device, UUID> {
    // Inherited methods:
    // - save(Device device)
    // - findById(UUID id)
    // - findAll()
    // - delete(Device device)
    // - deleteById(UUID id)
    // - etc.
}