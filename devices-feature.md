# Devices Feature — Implementation Plan

## Overview

A new `devices` feature following the existing `sensors` feature as reference. Devices can have sensors assigned to them. The feature lives entirely in the `de.sfl.devices` package.

---

## API Design

| Method | URL | Description | Success | Error |
|--------|-----|-------------|---------|-------|
| `POST` | `/api/devices` | Create a device | 201 | 400 (invalid), 409 (name conflict) |
| `PUT` | `/api/devices/{id}/sensors` | Assign sensors to a device (idempotent full replace) | 200 | 400 (invalid sensor ids), 404 (device not found) |
| `PUT` | `/api/devices/{id}` | Update a device | 200 | 400 (invalid), 404 (not found), 409 (name conflict) |
| `DELETE` | `/api/devices/{id}` | Delete a device | 204 | 404 (not found) |

---

## Entity Model

### `Device`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | Auto-generated (`BIGSERIAL`); `protected` for test subclass pattern |
| `name` | `String` | Required, unique |
| `description` | `String` | Optional |
| `sensors` | `Set<Sensor>` | Many-to-many join table `device_sensors` |
| `createdAt` | `Instant` | Set on `@PrePersist` |
| `updatedAt` | `Instant` | Set on `@PrePersist` and `@PreUpdate` |

---

## Database Migration

File: `src/main/resources/db/migration/V<timestamp>__create_device_tables.sql`

```sql
CREATE TABLE devices (
    id          BIGSERIAL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_devices PRIMARY KEY (id),
    CONSTRAINT uq_devices_name UNIQUE (name)
);

CREATE TABLE device_sensors (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    CONSTRAINT pk_device_sensors PRIMARY KEY (device_id, sensor_id),
    CONSTRAINT fk_device_sensors_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_sensors_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_device_sensors_device_id ON device_sensors(device_id);
CREATE INDEX idx_device_sensors_sensor_id ON device_sensors(sensor_id);
```

---

## Source Files

All files live under `src/main/java/de/sfl/devices/`.

### `Device.java` — JPA Entity

- `@Entity @Table(name = "devices")`
- Fields: `protected Long id`, `String name`, `String description`, `Set<Sensor> sensors` (`@ManyToMany`), timestamps
- Constructor: `Device(String name, String description)`
- `update(String name, String description)` method for partial mutation
- `assignSensors(Set<Sensor> sensors)` method for idempotent sensor replacement
- `@PrePersist` / `@PreUpdate` lifecycle callbacks for timestamps
- Equals/hashCode based on `id`

### `JpaDeviceRepository.java` — Repository (package-protected)

- Package-protected interface extending `JpaRepository<Device, Long>`
- Methods:
  - `existsByName(String name)`
  - `existsByNameAndIdNot(String name, Long id)` — for update uniqueness check

### `DeviceService.java` — Service

- Constructor-injected `JpaDeviceRepository` and `JpaSensorRepository`
  - Note: accessing `JpaSensorRepository` across packages requires exposing sensor lookup through `SensorService`; `DeviceService` will depend on `SensorService` (public API)
- `@Transactional` on mutating methods
- Methods:
  - `createDevice(Device device) → Device`
  - `updateDevice(Long id, String name, String description) → Device`
  - `assignSensors(Long deviceId, Set<Long> sensorIds) → Device`
  - `deleteDevice(Long id)`
- Throws `DeviceNotFoundException` (404), `DeviceAlreadyExistsException` (409)
- Validates that all provided sensor IDs exist before assigning; throws `SensorNotFoundException` for any missing ID
- Logging: DEBUG on reads, INFO on mutations

### `DeviceController.java` — REST Controller

- `@RestController @RequestMapping("/api/devices")`
- Endpoints:
  - `POST /api/devices` → `DeviceDto` 201
  - `PUT /api/devices/{id}` → `DeviceDto` 200
  - `PUT /api/devices/{id}/sensors` → `DeviceDto` 200
  - `DELETE /api/devices/{id}` → 204
- Private `toDto(Device)` method

### DTOs

#### `CreateDeviceDto.java` (record)
```java
public record CreateDeviceDto(
    @NotBlank String name,
    String description      // optional
) {
    public Device toEntity() { return new Device(name, description); }
}
```

#### `UpdateDeviceDto.java` (record)
```java
public record UpdateDeviceDto(
    @NotBlank String name,
    String description
) {}
```

#### `AssignSensorsDto.java` (record)
```java
public record AssignSensorsDto(@NotNull Set<Long> sensorIds) {}
```

#### `DeviceDto.java` (record)
```java
public record DeviceDto(
    Long id,
    String name,
    String description,
    Set<Long> sensorIds
) {}
```

### `DeviceConfiguration.java` — Bean wiring

```java
@Configuration
public class DeviceConfiguration {
    @Bean
    public DeviceService deviceService(JpaDeviceRepository deviceRepository, SensorService sensorService) {
        return new DeviceService(deviceRepository, sensorService);
    }
}
```

### `DeviceExceptionHandler.java` — Error handling

- `@RestControllerAdvice(assignableTypes = DeviceController.class)`
- Handles: `DeviceNotFoundException` → 404, `DeviceAlreadyExistsException` → 409, `SensorNotFoundException` → 404, `MethodArgumentNotValidException` → 400, `Exception` → 500
- Returns `ProblemDetail` (RFC 7807)

### `DeviceNotFoundException.java`

```java
public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(Long id) { super("Device with id: '" + id + "' not found"); }
}
```

### `DeviceAlreadyExistsException.java`

```java
public class DeviceAlreadyExistsException extends RuntimeException {
    public DeviceAlreadyExistsException(String name) { super("Device with name: '" + name + "' already exists"); }
}
```

---

## Test Files

All test files live under `src/test/java/de/sfl/devices/`.

### `CreateDeviceDtoTest.java`
- Marshalling + unmarshalling tests for `CreateDeviceDto`
- Tests `toEntity()` conversion

### `UpdateDeviceDtoTest.java`
- Marshalling + unmarshalling tests for `UpdateDeviceDto`

### `AssignSensorsDtoTest.java`
- Marshalling + unmarshalling tests for `AssignSensorsDto`

### `DeviceDtoTest.java`
- Marshalling + unmarshalling tests for `DeviceDto`

### `DeviceServiceTest.java`
- Pure Mockito unit tests, no Spring context
- Mocks `JpaDeviceRepository` and `SensorService`
- Covers: create, update, assignSensors, delete — happy path + error paths

### `DeviceControllerTest.java`
- `@WebMvcTest(DeviceController.class)` + `@MockBean DeviceService`
- Covers all endpoints, validation (400), 404/409 error cases, 204 for delete

### `JpaDeviceRepositoryIT.java`
- Extends `RepositoryIT`
- Covers: save, findById, existsByName, existsByNameAndIdNot, deleteById

---

## Implementation Order

1. Database migration SQL file
2. `Device.java` entity
3. `JpaDeviceRepository.java`
4. Domain exceptions: `DeviceNotFoundException`, `DeviceAlreadyExistsException`
5. `DeviceService.java`
6. DTOs: `CreateDeviceDto`, `UpdateDeviceDto`, `AssignSensorsDto`, `DeviceDto`
7. `DeviceController.java`
8. `DeviceExceptionHandler.java`
9. `DeviceConfiguration.java`
10. Tests: `DeviceServiceTest`, `DeviceControllerTest`, `JpaDeviceRepositoryIT`, DTO JSON tests
11. Update `AGENTS.md` to register the new feature

---

## Cross-Cutting Concerns

- **Layered architecture**: `DeviceController` → `DeviceService` → `JpaDeviceRepository`. For sensor lookup, `DeviceService` calls `SensorService` (public service API), never `JpaSensorRepository` directly — this respects the package-protected repository rule.
- **Idempotency of sensor assignment**: `PUT /api/devices/{id}/sensors` replaces the full set of assigned sensors. Calling it twice with the same body has no additional effect.
- **Transactions**: All mutating service methods are `@Transactional`. Sensor assignment loads the device, replaces the `sensors` collection, and saves — all within one transaction.
- **No `@Component`/`@Service`/`@Repository`** annotations — all beans wired via `DeviceConfiguration`.
- **DTOs in controller layer only** — `DeviceService` works exclusively with `Device` entities and `Long` IDs.
