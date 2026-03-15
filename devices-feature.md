# Devices Feature Implementation Plan

## Overview
Implement a REST API for managing devices with the ability to assign sensors to devices.

## Requirements
- Create devices
- Assign sensors to devices (idempotent PUT operation)
- Update devices
- Delete devices

### Device Properties
- Name
- Description

## Package Structure
```
de.sfl.devices/
├── Device.java                      (entity)
├── DeviceDto.java                   (response DTO)
├── CreateDeviceDto.java             (create DTO with validation)
├── UpdateDeviceDto.java             (update DTO with validation)
├── JpaDeviceRepository.java         (package-protected repository interface)
├── DeviceService.java               (service)
├── DeviceController.java            (REST controller - /api/devices)
├── DeviceConfiguration.java         (@Configuration class)
├── DeviceNotFoundException.java     (custom exception)
├── DeviceAlreadyExistsException.java (custom exception)
└── DeviceExceptionHandler.java      (@RestControllerAdvice)
```

## Database Schema

### Tables

**devices**
```sql
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uniq_device_name UNIQUE (name)
);

CREATE INDEX idx_devices_name ON devices(name);
```

**devices_sensors** (junction table for many-to-many relationship)
```sql
CREATE TABLE devices_sensors (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    PRIMARY KEY (device_id, sensor_id),
    CONSTRAINT fk_devices_sensors_device FOREIGN KEY (device_id)
        REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_devices_sensors_sensor FOREIGN KEY (sensor_id)
        REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_devices_sensors_device ON devices_sensors(device_id);
CREATE INDEX idx_devices_sensors_sensor ON devices_sensors(sensor_id);
```

### Migration File
Location: `src/main/resources/db/migration/V202511140900__create_device_tables.sql`

## Entity Layer

### Device.java
```java
@Entity
@Table(name = "devices")
public class Device {

    protected Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    @ManyToMany
    @JoinTable(
        name = "devices_sensors",
        joinColumns = @JoinColumn(name = "device_id"),
        inverseJoinColumns = @JoinColumn(name = "sensor_id")
    )
    private Set<Sensor> sensors;

    // Constructor with required fields
    // Protected no-args constructor for JPA
    // Getters with defensive copies for collections
    // Setters (protected ID setter for test subclass pattern)
    // equals/hashCode based on ID
    // PrePersist/PreUpdate for timestamps
}
```

## DTO Layer

### DeviceDto.java (record)
```java
public record DeviceDto(
    Long id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt,
    List<Long> sensorIds
) {}
```

### CreateDeviceDto.java (record)
```java
public record CreateDeviceDto(
    @NotBlank(message = "Device name must not be blank")
    String name,
    String description
) {
    public Device toEntity() {
        return new Device(name, description);
    }
}
```

### UpdateDeviceDto.java (record)
```java
public record UpdateDeviceDto(
    @NotBlank(message = "Device name must not be blank")
    String name,
    String description
) {}
```

### AssignSensorsDto.java (record)
```java
public record AssignSensorsDto(
    @NotEmpty(message = "Sensor IDs list must not be empty")
    List<@NotNull Long> sensorIds
) {}
```

## Repository Layer

### JpaDeviceRepository.java (package-protected)
```java
interface JpaDeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT d FROM Device d WHERE d.id > :lastId ORDER BY d.id ASC")
    List<Device> findNextPage(@Param("lastId") Long lastId, Pageable pageable);

    @Query("SELECT d FROM Device d ORDER BY d.id ASC")
    List<Device> findFirstPage(Pageable pageable);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = "sensors")
    Optional<Device> findWithSensorsById(Long id);
}
```

## Service Layer

### DeviceService.java
```java
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    private final JpaDeviceRepository deviceRepository;
    private final JpaSensorRepository sensorRepository;
    private final Clock clock;

    // Constructor injection

    @Transactional(readOnly = true)
    public PageResult<Device> findAll(Long lastId, int pageSize) {
        // Implement keyset pagination
    }

    @Transactional(readOnly = true)
    public Device findById(Long id) {
        // Returns device with sensors loaded
    }

    @Transactional
    public Device create(CreateDeviceDto dto) {
        // Validate name uniqueness
        // Create device
        // log INFO
    }

    @Transactional
    public Device update(Long id, UpdateDeviceDto dto) {
        // Check device exists
        // Validate name uniqueness (excluding current device)
        // Update device
        // log INFO
    }

    @Transactional
    public Device assignSensors(Long deviceId, AssignSensorsDto dto) {
        // Load device with sensors
        // Validate all sensor IDs exist
        // Idempotent: replace existing sensors with new set
        // Return updated device
        // log INFO
    }

    @Transactional
    public void delete(Long id) {
        // Check device exists
        // Delete device (cascade to devices_sensors)
        // log INFO
    }

    // Private helper methods for common operations
}
```

## Controller Layer

### DeviceController.java
Base URL: `/api/devices`

```java
@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Device management API")
public class DeviceController {

    private final DeviceService deviceService;

    // Constructor injection

    @GetMapping
    @Operation(summary = "Get all devices with pagination")
    public PageResult<DeviceDto> getAllDevices(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int pageSize) {
        // Return paginated devices
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    public DeviceDto getDevice(@PathVariable Long id) {
        // Return device with sensor IDs
    }

    @PostMapping
    @Operation(summary = "Create a new device")
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceDto dto) {
        // Return 201 created
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing device")
    public DeviceDto updateDevice(@PathVariable Long id, @Valid @RequestBody UpdateDeviceDto dto) {
        // Return updated device
    }

    @PutMapping("/{id}/sensors")
    @Operation(summary = "Assign sensors to device (idempotent)")
    public DeviceDto assignSensors(@PathVariable Long id, @Valid @RequestBody AssignSensorsDto dto) {
        // Return updated device with sensors
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable Long id) {
        // Delete device
    }

    // toDto() helper method
}
```

## Configuration Layer

### DeviceConfiguration.java
```java
@Configuration
public class DeviceConfiguration {

    @Bean
    public DeviceService deviceService(
            JpaDeviceRepository deviceRepository,
            JpaSensorRepository sensorRepository,
            Clock clock) {
        return new DeviceService(deviceRepository, sensorRepository, clock);
    }
}
```

## Exception Handling

### DeviceExceptionHandler.java
```java
@RestControllerAdvice(assignableTypes = DeviceController.class)
public class DeviceExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDeviceNotFound(DeviceNotFoundException ex) {
        // Return 404
    }

    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleDeviceAlreadyExists(DeviceAlreadyExistsException ex) {
        // Return 409
    }

    @ExceptionHandler(SensorNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSensorNotFound(SensorNotFoundException ex) {
        // Return 404 (when assigning non-existent sensor)
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Return 400 with validation errors
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        // Return 500
    }
}
```

## Custom Exceptions

### DeviceNotFoundException.java
```java
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(Long id) {
        super("Device not found with id: " + id);
    }
}
```

### DeviceAlreadyExistsException.java
```java
public class DeviceAlreadyExistsException extends RuntimeException {
    public DeviceAlreadyExistsException(String name) {
        super("Device already exists with name: " + name);
    }
}
```

## Testing

### Controller Test: DeviceControllerTest.java
- `@WebMvcTest(DeviceController.class)`
- `@MockBean` for DeviceService
- Test all endpoints:
  - GET /api/devices (with pagination variations)
  - GET /api/devices/{id}
  - POST /api/devices (success, validation errors, conflict)
  - PUT /api/devices/{id} (success, not found, validation, conflict)
  - PUT /api/devices/{id}/sensors (success, not found sensor, not found device)
  - DELETE /api/devices/{id} (success, not found)
- Verify service calls with `verify()`

### Service Test: DeviceServiceTest.java
- `@ExtendWith(MockitoExtension.class)`
- Mock JpaDeviceRepository and JpaSensorRepository
- Test all public methods:
  - findAll: various pagination scenarios
  - findById: found, not found
  - create: success, duplicate name
  - update: success, not found, duplicate name
  - assignSensors: success, not found device, not found sensor, idempotency
  - delete: success, not found

### Repository Integration Test: JpaDeviceRepositoryIT.java
- Extend `RepositoryIT` base class
- Test CRUD operations
- Test custom query methods
- Test pagination (first page, next page)
- Test device-sensor relationship
- Test cascade delete behavior
- Test timestamp behavior

### JSON Model Tests
- **DeviceDtoTest.java**
  - Serialization to JSON
  - Deserialization from JSON
  - Round-trip test
  - Edge cases (null fields, empty sensor list)

- **CreateDeviceDtoTest.java**
  - JSON marshalling/unmarshalling
  - Validation tests (@NotBlank)
  - toEntity() method tests

- **UpdateDeviceDtoTest.java**
  - JSON marshalling/unmarshalling
  - Validation tests

- **AssignSensorsDtoTest.java**
  - JSON marshalling/unmarshalling
  - Validation tests (@NotEmpty, @NotNull)

## Implementation Steps

1. **Create database migration** - `V202511140900__create_device_tables.sql`
2. **Create entity** - `Device.java` with many-to-many relationship to Sensor
3. **Create DTOs** - `DeviceDto.java`, `CreateDeviceDto.java`, `UpdateDeviceDto.java`, `AssignSensorsDto.java`
4. **Create repository** - `JpaDeviceRepository.java` (package-protected)
5. **Create custom exceptions** - `DeviceNotFoundException.java`, `DeviceAlreadyExistsException.java`
6. **Create service** - `DeviceService.java` with business logic
7. **Create configuration** - `DeviceConfiguration.java`
8. **Create exception handler** - `DeviceExceptionHandler.java`
9. **Create controller** - `DeviceController.java` with REST endpoints
10. **Update AGENTS.md** - Add devices feature entry
11. **Create JSON model tests** - All DTOs must have marshalling/unmarshalling tests
12. **Create repository integration test** - `JpaDeviceRepositoryIT.java`
13. **Create service unit test** - `DeviceServiceTest.java`
14. **Create controller test** - `DeviceControllerTest.java`

## Testing Workflow

After each implementation step:
- Run the newly created test: `mvn test -Dtest=ClassName`
- After completing the feature: `mvn verify` to run all tests

## Design Considerations

### Many-to-Many Relationship
- Devices and sensors have a many-to-many relationship
- Junction table `devices_sensors` with composite primary key
- `ON DELETE CASCADE` for both foreign keys
- Load sensors lazily by default, eager fetch only when needed

### Idempotent PUT for Sensors
- `PUT /api/devices/{id}/sensors` completely replaces the device's sensor assignments
- Idempotent: calling with same sensor IDs multiple times yields same result
- Validates all sensor IDs exist before assignment
- Returns device with updated sensor list

### Naming Convention
- Use singular: `devices` (unlike `sensors` which uses singular for package)
- Follow existing pattern: `de.sfl.devices`

### GDPR Compliance
- Never log device IDs or personal data
- Log only technical information: "Device created", "Device updated", etc.

### HTTP Status Codes
- 200 OK: GET requests, PUT updates
- 201 Created: POST create
- 204 No Content: DELETE
- 400 Bad Request: Validation errors
- 404 Not Found: Resource not found
- 409 Conflict: Duplicate name
- 500 Internal Server Error: Unexpected errors

## Dependencies
No new dependencies required. Using existing:
- Spring Boot Starter Data JPA
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Flyway
- Testcontainers (for integration tests)
- Springdoc OpenAPI