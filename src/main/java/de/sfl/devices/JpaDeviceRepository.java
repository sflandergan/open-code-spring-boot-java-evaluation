package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

interface JpaDeviceRepository extends JpaRepository<Device, UUID> {
    boolean existsByName(String name);
    Device findByName(String name);
}