package de.sfl.sensors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface JpaSensorRepository extends JpaRepository<Sensor, Long> {

	boolean existsByName(String name);

	@Query("""
		SELECT d FROM Sensor d
		WHERE d.id > :lastId
		ORDER BY d.id ASC
		""")
	List<Sensor> findNextPage(@Param("lastId") Long lastId, Pageable pageable);

	@Query("""
		SELECT d FROM Sensor d
		ORDER BY d.id ASC
		""")
	List<Sensor> findFirstPage(Pageable pageable);
}
