package de.sfl.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import de.sfl.sensors.Sensor;

import java.util.Collection;
import java.util.List;

interface JpaSensorLookupRepository extends JpaRepository<Sensor, Long> {

	List<Sensor> findByIdIn(Collection<Long> ids);
}
