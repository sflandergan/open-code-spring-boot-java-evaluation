package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaDeviceRepository extends JpaRepository<Device, Long> {
}
