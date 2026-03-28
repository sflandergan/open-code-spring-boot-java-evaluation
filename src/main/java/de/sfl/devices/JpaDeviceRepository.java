package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface JpaDeviceRepository extends JpaRepository<Device, UUID> {

	@Query("""
		SELECT DISTINCT d FROM Device d
		LEFT JOIN FETCH d.sensorAssignments assignment
		LEFT JOIN FETCH assignment.sensor
		WHERE d.id = :id
		""")
	Optional<Device> findDetailedById(@Param("id") UUID id);
}
