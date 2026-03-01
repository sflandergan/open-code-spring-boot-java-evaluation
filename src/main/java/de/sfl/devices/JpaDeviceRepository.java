package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring Data JPA repository for Device entity.
 */
public interface JpaDeviceRepository extends JpaRepository<Device, UUID> {
    // Spring Data provides default CRUD methods.
}