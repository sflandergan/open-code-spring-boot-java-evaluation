# Repository Integration Testing Pattern

## Overview
Every repository must have an integration test that verifies database operations against a real PostgreSQL instance using Testcontainers.

## Naming Convention
- Test class name: `<RepositoryName>IT`
- Example: `JpaSensorRepository` → `JpaSensorRepositoryIT`

## Base Class
All repository integration tests must extend the `RepositoryIT` abstract base class.

### What RepositoryIT Provides
- **Shared PostgreSQL container**: Uses Testcontainers with PostgreSQL 17 Alpine
- **Separate from docker-maven-plugin**: Independent container lifecycle from application integration tests
- **@DataJpaTest configuration**: Configures only the JPA layer for focused testing
- **@ServiceConnection**: Spring Boot 3.1+ feature that automatically configures datasource properties from the container
- **Manual lifecycle management**: Container is started in static block and reused across all test classes
- **Flyway migrations**: Database schema is created using production Flyway migrations

### Container Sharing and Lifecycle
The PostgreSQL container is managed manually with `.withReuse(true)`:
- One container instance is shared across all repository integration test classes
- Container starts once in the static initializer block when `RepositoryIT` is first loaded
- Container is reused across all test classes (via Testcontainers reuse feature)
- Improves test performance by avoiding repeated container startup
- Each test class still gets a clean database state via `@BeforeEach` cleanup
- **@ServiceConnection** eliminates the need for manual `@DynamicPropertySource` configuration
- **Note**: The container is NOT managed by `@Testcontainers` annotation to avoid lifecycle conflicts when multiple test classes run in parallel

## Test Structure

### Basic Template
```java
class JpaSensorRepositoryIT extends RepositoryIT {
    
    @Autowired
    private JpaSensorRepository sensorRepository;
    
    @BeforeEach
    void setUp() {
        sensorRepository.deleteAll();
    }
    
    @Test
    void shouldSaveAndFindSensorById() {
        Sensor sensor = new Sensor("Sensor-001", "temperature", Set.of("read"));
        Sensor savedSensor = sensorRepository.save(sensor);
        
        Optional<Sensor> foundSensor = sensorRepository.findById(savedSensor.getId());
        assertThat(foundSensor).isPresent();
        assertThat(foundSensor.get().getName()).isEqualTo("Sensor-001");
    }
}
```

### Required Elements

#### 1. Extend RepositoryIT
```java
class JpaSensorRepositoryIT extends RepositoryIT {
```

#### 2. Inject Repository
```java
@Autowired
private JpaSensorRepository sensorRepository;
```

#### 3. Clean Up Between Tests
```java
@BeforeEach
void setUp() {
    sensorRepository.deleteAll();
}
```

## What to Test

### CRUD Operations
- **Create**: Save new entities and verify they're persisted
- **Read**: Find by ID, find all, custom queries
- **Update**: Modify entities and verify changes are saved
- **Delete**: Remove entities and verify they're gone

### Custom Query Methods
Test all custom `@Query` methods, especially:
- Pagination queries (first page, next page)
- Existence checks (`existsByXxx`)
- Complex filters and joins
- Sorting behavior

### Edge Cases
- Empty results (no data found)
- Empty collections (e.g., empty capabilities)
- Non-existent IDs
- Boundary conditions for pagination

### Example: Comprehensive Test Coverage
```java
class JpaSensorRepositoryIT extends RepositoryIT {
    
    @Autowired
    private JpaSensorRepository sensorRepository;
    
    @BeforeEach
    void setUp() {
        sensorRepository.deleteAll();
    }
    
    @Test
    void shouldSaveAndFindSensorById() {
        Sensor sensor = new Sensor("Sensor-001", "temperature", Set.of("read", "write"));
        Sensor savedSensor = sensorRepository.save(sensor);
		
		assertThat(savedSensor.getId()).isNotNull();
		assertThat(sensorRepository.findById(savedSensor.getId())).isPresent();
    }
    
    @Test
    void shouldFindAllSensors() {
        sensorRepository.save(new Sensor("Sensor-001", "sensor", Set.of("read")));
        sensorRepository.save(new Sensor("Sensor-002", "actuator", Set.of("write")));

		assertThat(sensorRepository.findAll()).hasSize(2);
    }
    
    @Test
    void shouldCheckIfSensorExistsByName() {
		var sensor = new Sensor("UniqueSensor", "sensor", Set.of("read"));
		sensorRepository.save(sensor);

		assertThat(sensorRepository.existsByName(sensor.getName())).isTrue();
		assertThat(sensorRepository.existsByName("NonExistentSensor")).isFalse();
    }
    
    @Test
    void shouldFindFirstPageOrderedById() {
        sensorRepository.save(new Sensor("Sensor-001", "sensor", Set.of("read")));
        sensorRepository.save(new Sensor("Sensor-002", "actuator", Set.of("write")));
        sensorRepository.save(new Sensor("Sensor-003", "sensor", Set.of("read", "write")));
        
        Pageable pageable = PageRequest.of(0, 2);
        List<Sensor> firstPage = sensorRepository.findFirstPage(pageable);
        
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getId()).isLessThan(firstPage.get(1).getId());
    }
    
    @Test
    void shouldFindNextPageAfterLastId() {
        Sensor saved1 = sensorRepository.save(new Sensor("Sensor-001", "sensor", Set.of("read")));
        Sensor saved2 = sensorRepository.save(new Sensor("Sensor-002", "actuator", Set.of("write")));
        Sensor saved3 = sensorRepository.save(new Sensor("Sensor-003", "sensor", Set.of("read", "write")));
        
        Pageable pageable = PageRequest.of(0, 2);
        List<Sensor> nextPage = sensorRepository.findNextPage(saved1.getId(), pageable);
        
        assertThat(nextPage).hasSize(2);
        assertThat(nextPage).extracting(Sensor::getId)
                .containsExactly(saved2.getId(), saved3.getId());
    }
    
    @Test
    void shouldDeleteSensor() {
        Sensor savedSensor = sensorRepository.save(new Sensor("ToDelete", "sensor", Set.of("read")));
        
        sensorRepository.deleteById(savedSensor.getId());
        
        assertThat(sensorRepository.findById(savedSensor.getId())).isEmpty();
    }
    
    @Test
    void shouldUpdateSensor() {
        Sensor sensor = sensorRepository.save(new Sensor("Original", "sensor", Set.of("read")));
        
        sensor.setName("Updated");
        sensor.setType("actuator");
        Sensor updatedSensor = sensorRepository.save(sensor);
        
        assertThat(updatedSensor.getName()).isEqualTo(sensor.getName());
        assertThat(updatedSensor.getType()).isEqualTo(sensor.getType());
    }
    
    @Test
    void shouldHandleEmptyCapabilities() {
        Sensor sensor = sensorRepository.save(new Sensor("NoCapabilities", "sensor", Set.of()));
        
        Optional<Sensor> foundSensor = sensorRepository.findById(sensor.getId());
        assertThat(foundSensor).isPresent();
        assertThat(foundSensor.get().getCapabilities()).isEmpty();
    }
    
    @Test
    void shouldReturnEmptyListWhenNoSensorsExist() {
		assertThat(sensorRepository.findAll()).isEmpty();
    }
    
    @Test
    void shouldReturnEmptyOptionalWhenSensorNotFound() {
		assertThat(sensorRepository.findById(999L)).isEmpty();
    }
}
```

## Running Tests

### Single Repository Test
```bash
mvn verify -Dit.test=JpaSensorRepositoryIT
```

### All Integration Tests
```bash
mvn verify
```

## Best Practices

### Data Isolation
- Always use `@BeforeEach` with `repository.deleteAll()` to ensure clean state
- Don't rely on test execution order
- Each test should be independent

### Assertions
- Use AssertJ's fluent assertions (`assertThat`)
- Test both positive and negative cases
- Verify not just existence but also data correctness

### Performance
- Use static container (default in `RepositoryIT`) for better performance
- Avoid unnecessary data setup in tests
- Keep test data minimal but sufficient

### Naming
- Use descriptive test method names: `shouldDoSomethingWhenCondition`
- Example: `shouldReturnEmptyListWhenNoSensorsExist`
- Test names should read like documentation

## Troubleshooting

### Container Not Starting
- Check Docker is running
- Verify network connectivity
- Check container logs in test output

### Flyway Migration Errors
- Ensure migrations are in `src/main/resources/db/migration/`
- Verify migration file naming: `V<timestamp>__description.sql`
- Check migration SQL syntax

### Test Failures
- Verify `@BeforeEach` cleanup is running
- Check for data dependencies between tests
- Review Hibernate SQL logs in test output
