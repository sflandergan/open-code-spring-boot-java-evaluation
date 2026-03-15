# Devices Feature - Implementation Plan

## Overview

New feature for managing devices with a REST API supporting CRUD operations and sensor assignment. Devices have a name and description, and can have sensors assigned to them via an idempotent PUT operation.

**Package:** `de.sfl.devices`
**Base URL:** `/api/devices`
**Entities:** Device (id, name, description), DeviceSensor (join table)

---

## 1. Database Migration

**File:** `src/main/resources/db/migration/V202603151200__create_device_tables.sql`

Create two tables:

### `devices` table
| Column       | Type                | Constraints                          |
|--------------|---------------------|--------------------------------------|
| id           | BIGSERIAL           | PK (`pk_devices`)                    |
| name         | VARCHAR(255)        | NOT NULL                             |
| description  | TEXT                | nullable                             |
| created_at   | TIMESTAMPTZ         | NOT NULL DEFAULT CURRENT_TIMESTAMP   |
| updated_at   | TIMESTAMPTZ         | NOT NULL DEFAULT CURRENT_TIMESTAMP   |

- Index: `idx_devices_name` on `name`

### `device_sensors` join table
| Column    | Type   | Constraints                                              |
|-----------|--------|----------------------------------------------------------|
| device_id | BIGINT | NOT NULL, FK -> devices(id) ON DELETE CASCADE            |
| sensor_id | BIGINT | NOT NULL, FK -> sensors(id) ON DELETE CASCADE            |

- Composite PK: `(device_id, sensor_id)` named `pk_device_sensors`
- Foreign keys: `fk_device_sensors_devices`, `fk_device_sensors_sensors`
- Index: `idx_device_sensors_sensor_id` on `sensor_id` (device_id already indexed via PK)

---

## 2. Entity

**File:** `src/main/java/de/sfl/devices/Device.java`

- `@Entity`, `@Table(name = "devices")`
- Fields:
  - `protected Long id` — `@Id @GeneratedValue(IDENTITY)`
  - `String name` — `@Column(nullable = false)`
  - `String description` — `@Column`
  - `Set<Sensor> sensors` — `@ManyToMany` with `@JoinTable(name = "device_sensors", joinColumns = device_id, inverseJoinColumns = sensor_id)`
  - `Instant createdAt` — `@Column(name = "created_at", nullable = false, updatable = false)`
  - `Instant updatedAt` — `@Column(name = "updated_at", nullable = false)`
- `@PrePersist` / `@PreUpdate` lifecycle callbacks for timestamps
- Protected no-arg constructor (JPA)
- Public constructor: `Device(String name, String description)`
- Methods to manage sensor assignments: `addSensor(Sensor)`, `removeSensor(Sensor)`, `getSensors()`
- `equals`/`hashCode` based on `id`

---

## 3. Repository

**File:** `src/main/java/de/sfl/devices/JpaDeviceRepository.java`

- Package-private interface extending `JpaRepository<Device, Long>`
- Custom queries:
  - `boolean existsByName(String name)` — derived query for uniqueness check
  - `List<Device> findFirstPage(Pageable pageable)` — `@Query` keyset pagination
  - `List<Device> findNextPage(Long lastId, Pageable pageable)` — `@Query` keyset pagination

---

## 4. Service

**File:** `src/main/java/de/sfl/devices/DeviceService.java`

- Constructor injection: `JpaDeviceRepository`, `SensorService` (from sensors package, for looking up sensors)
- SLF4J logger
- Methods:
  - `getDevices(Long lastId, int pageSize)` — `@Transactional(readOnly = true)`, keyset pagination, returns `PageResult<Device>`
  - `getDeviceById(Long id)` — `@Transactional(readOnly = true)`, throws `DeviceNotFoundException`
  - `createDevice(Device device)` — `@Transactional`, validates name uniqueness, throws `DeviceAlreadyExistsException`
  - `updateDevice(Long id, String name, String description)` — `@Transactional`, finds device or throws 404, validates name uniqueness if changed, updates fields
  - `deleteDevice(Long id)` — `@Transactional`, finds device or throws 404, deletes
  - `assignSensor(Long deviceId, Long sensorId)` — `@Transactional`, idempotent, finds device and sensor or throws 404, adds sensor to device's set (no-op if already assigned)
- Private helpers: `findDeviceOrThrow(Long id)`, `validateDeviceNameUnique(String name)`
- Logging: DEBUG for reads, INFO for creates/updates/deletes/assignments

---

## 5. DTOs

### `DeviceDto.java` (Response)
- Java record: `Long id`, `String name`, `String description`, `Set<SensorDto> sensors`
- Uses `SensorDto` from the sensors package for nested sensor data

### `CreateDeviceDto.java` (Create Request)
- Java record: `String name`, `String description`
- Validation: `@NotBlank` on name
- `toEntity()` method returning `new Device(name, description)`

### `UpdateDeviceDto.java` (Update Request)
- Java record: `String name`, `String description`
- Validation: `@NotBlank` on name

---

## 6. Controller

**File:** `src/main/java/de/sfl/devices/DeviceController.java`

- `@RestController`, `@RequestMapping("/api/devices")`
- Constructor injection: `DeviceService`
- OpenAPI annotations: `@Tag`, `@Operation`, `@ApiResponse`
- Endpoints:

| Method | Path                              | Status | Description                          |
|--------|-----------------------------------|--------|--------------------------------------|
| GET    | `/api/devices`                    | 200    | List devices (keyset pagination)     |
| GET    | `/api/devices/{id}`               | 200    | Get device by ID                     |
| POST   | `/api/devices`                    | 201    | Create device                        |
| PUT    | `/api/devices/{id}`               | 200    | Update device                        |
| DELETE | `/api/devices/{id}`               | 204    | Delete device                        |
| PUT    | `/api/devices/{id}/sensors/{sensorId}` | 204 | Assign sensor to device (idempotent) |

- Private `toDto(Device)` helper to convert entity to response DTO

---

## 7. Exception Handling

### Custom Exceptions
- `DeviceNotFoundException extends RuntimeException` — message: `"Device with id: '<id>' not found"`
- `DeviceAlreadyExistsException extends RuntimeException` — message: `"Device with name: '<name>' already exists"`

### Exception Handler
**File:** `src/main/java/de/sfl/devices/DeviceExceptionHandler.java`

- `@RestControllerAdvice(assignableTypes = DeviceController.class)`
- Returns `ProblemDetail` (RFC 7807)
- Handlers:

| Exception                         | HTTP Status | Title                   |
|-----------------------------------|-------------|-------------------------|
| `DeviceNotFoundException`         | 404         | "Device Not Found"      |
| `DeviceAlreadyExistsException`    | 409         | "Device Already Exists" |
| `MethodArgumentNotValidException` | 400         | "Validation Failed"     |
| `Exception` (catch-all)           | 500         | "Internal Server Error" |

---

## 8. Configuration

**File:** `src/main/java/de/sfl/devices/DeviceConfiguration.java`

```java
@Configuration
public class DeviceConfiguration {
    @Bean
    public DeviceService deviceService(JpaDeviceRepository deviceRepository, SensorService sensorService) {
        return new DeviceService(deviceRepository, sensorService);
    }
}
```

---

## 9. Tests

### 9a. JSON Model Tests

- **`DeviceDtoTest.java`** — marshalling/unmarshalling of `DeviceDto` including nested sensors
- **`CreateDeviceDtoTest.java`** — marshalling/unmarshalling + `toEntity()` test
- **`UpdateDeviceDtoTest.java`** — marshalling/unmarshalling

### 9b. Repository Integration Test

**File:** `src/test/java/de/sfl/devices/JpaDeviceRepositoryIT.java`

- Extends `RepositoryIT`
- Tests: CRUD, pagination queries, sensor assignments via entity, timestamps, edge cases
- `@BeforeEach` cleans data

### 9c. Service Unit Test

**File:** `src/test/java/de/sfl/devices/DeviceServiceTest.java`

- `@ExtendWith(MockitoExtension.class)`
- Mocks: `JpaDeviceRepository`, `SensorService`
- `TestDevice extends Device` inner class for ID setting
- Tests: all service methods, happy paths, error cases (not found, already exists), pagination logic, idempotent sensor assignment

### 9d. Controller Test

**File:** `src/test/java/de/sfl/devices/DeviceControllerTest.java`

- `@WebMvcTest(DeviceController.class)`
- `@MockBean DeviceService`
- Tests all 6 endpoints with valid/invalid inputs
- Tests validation (400 for blank name)
- Tests error handling (404, 409)
- Verifies service calls with `verify()`

---

## 10. AGENTS.md Update

Add the new feature entry:

```markdown
- **Device Management**
  - Package: `de.sfl.devices`
  - Base URL: `/api/devices`
  - Entities: Device (id, name, description, sensors)
```

---

## Implementation Order

1. Database migration (Flyway SQL)
2. Entity (`Device.java`)
3. Repository (`JpaDeviceRepository.java`)
4. Exceptions (`DeviceNotFoundException`, `DeviceAlreadyExistsException`)
5. Service (`DeviceService.java`)
6. DTOs (`DeviceDto`, `CreateDeviceDto`, `UpdateDeviceDto`)
7. Controller (`DeviceController.java`)
8. Exception handler (`DeviceExceptionHandler.java`)
9. Configuration (`DeviceConfiguration.java`)
10. Tests (DTO tests, repository IT, service test, controller test)
11. Update `AGENTS.md`
12. Run `mvn verify` to confirm everything passes
