package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

interface JpaDeviceRepository extends JpaRepository<Device, UUID> {
	boolean existsByName(String name);
	
	List<Device> findFirstPage(Pageable pageable);
	
	List<Device> findNextPage(UUID lastId, Pageable pageable);
}
