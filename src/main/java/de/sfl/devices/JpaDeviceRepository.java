package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface JpaDeviceRepository extends JpaRepository<Device, UUID> {
}