# Devices Feature Implementation Plan

## Overview
Implement a new feature for managing devices with REST API endpoints for CRUD operations and sensor assignment.

## Feature Requirements

### Properties
- **Name** (required, unique)
- **Description** (optional)

### REST API Endpoints

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/api/devices` | Create a new device | 201, 400, 409 |
| PUT | `/api/devices/{id}` | Update device | 200, 400, 404 |
| DELETE | `/api/devices/{id}` | Delete device | 204, 404 |
| PUT | `/api/devices/{id}/sensors/{sensorId}` | Assign sensor to device (idempotent) | 204, 404 |

### Additional Endpoints (following existing patterns)
| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/api/devices` | List devices with keyset pagination | 200 |
| GET | `/api/devices/{id}` | Get device by ID | 200, 404 |

## Package Structure

Following the package-by-feature approach, create:
```
src/main/java/de/sfl/devices/
├── Device.java                           # Entity
├── DeviceDto.java                        # Response DTO (record)
├── CreateDeviceDto.java                  # Request DTO for creation
├── UpdateDeviceDto.java                  # Request DTO for updates
├── DeviceController.java                 # REST controller
├── DeviceService.java                    # Business logic
├── JpaDeviceRepository.java              # Data access
├── DeviceConfiguration.java              # Bean definitions
├── DeviceExceptionHandler.java           # Exception handling
├── DeviceNotFoundException.java          # 404 exception
└── DeviceAlreadyExistsException.java     # 409 exception
```

## Implementation Tasks

### 1. Database Migration
**File:** `src/main/resources/db/migration/V<timestamp>__create_device_tables.sql`

Create two tables:
- `devices` - Main device entity table
- `device_sensors` - Join table for device-sensor relationship

**Schema:**
```sql
-- devices table
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index on name for uniqueness constraint performance
CREATE INDEX idx_devices_name ON devices(name);

-- device_sensors join table
CREATE TABLE device_sensors (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    PRIMARY KEY (device_id, sensor_id),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (sensor_id) REFERENCES sensors(id) ON DELETE CASCADE
);

-- Indexes for the join table
CREATE INDEX idx_device_sensors_device_id ON device_sensors(device_id);
CREATE INDEX idx_device_sensors_sensor_id ON device_sensors(sensor_id);
```

### 2. Domain Layer

#### Device.java (Entity)
- JPA entity with `@Entity` and `@Table(name = "devices")`
- Fields: `id`, `name`, `description`, `createdAt`, `updatedAt`
- Many-to-many relationship with sensors via `@ManyToMany`
- Join table `device_sensors` with FK to sensors table
- Protected default constructor for JPA
- Public constructor: `Device(String name, String description)`
- Auto-generated timestamps using `@PrePersist` and `@PreUpdate`
- Identity-based equals/hashCode using only `id`
- Getter methods for all fields

#### Exceptions
- **DeviceNotFoundException** - Extends RuntimeException, for 404 scenarios
- **DeviceAlreadyExistsException** - Extends RuntimeException, for 409 scenarios

### 3. Data Access Layer

#### JpaDeviceRepository.java
- Package-private interface extending `JpaRepository<Device, Long>`
- Methods:
  - `existsByName(String name)` - Check name uniqueness
  - `findFirstPage(Pageable pageable)` - Custom query for first page
  - `findNextPage(Long lastId, Pageable pageable)` - Keyset pagination query

### 4. Service Layer

#### DeviceService.java
- Plain Java class with constructor-injected repository
- Methods:
  - `getDevices(Long lastId, int pageSize)` - Keyset pagination (read-only transaction)
  - `getDeviceById(Long id)` - Get device or throw DeviceNotFoundException (read-only)
  - `createDevice(Device device)` - Create device, check name uniqueness (write transaction)
  - `updateDevice(Long id, String name, String description)` - Update device (write transaction)
  - `deleteDevice(Long id)` - Delete device (write transaction)
  - `assignSensor(Long deviceId, Long sensorId)` - Assign sensor to device (write transaction)
- SLF4J logging at appropriate levels

### 5. API Layer (REST Controller)

#### DTOs

**DeviceDto.java** (Response DTO - record)
```java
public record DeviceDto(
    Long id,
    String name,
    String description,
    Set<Long> sensorIds,
    Instant createdAt,
    Instant updatedAt
) {}
```

**CreateDeviceDto.java** (Request DTO - record with validation)
```java
public record CreateDeviceDto(
    @NotBlank String name,
    String description
) {
    public Device toEntity() {
        return new Device(name, description);
    }
}
```

**UpdateDeviceDto.java** (Request DTO - record with validation)
```java
public record UpdateDeviceDto(
    @NotBlank String name,
    String description
) {}
```

#### DeviceController.java
- Base URL: `/api/devices`
- Constructor-injected DeviceService
- Endpoints:
  - `GET /api/devices` - List with keyset pagination (lastId, pageSize params)
  - `GET /api/devices/{id}` - Get device by ID
  - `POST /api/devices` - Create device (@Valid CreateDeviceDto)
  - `PUT /api/devices/{id}` - Update device (@Valid UpdateDeviceDto)
  - `DELETE /api/devices/{id}` - Delete device
  - `PUT /api/devices/{id}/sensors/{sensorId}` - Assign sensor
- Returns appropriate HTTP status codes (200, 201, 204, 400, 404, 409)
- OpenAPI annotations for documentation

### 6. Configuration

#### DeviceConfiguration.java
- `@Configuration` class
- Bean definitions:
  - `deviceService(JpaDeviceRepository)`
  - `deviceController(DeviceService)`
  - `deviceExceptionHandler()`

#### DeviceExceptionHandler.java
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`
- Handles:
  - `DeviceNotFoundException` -> 404
  - `DeviceAlreadyExistsException` -> 409
  - `MethodArgumentNotValidException` -> 400
  - Generic `Exception` -> 500
- Returns `ProblemDetail` responses

### 7. Test Implementation

Following patterns in `docs/patterns/`:

#### Unit Tests
- **DeviceTest.java** - Entity unit tests
- **DeviceServiceTest.java** - Service unit tests with Mockito
- **DeviceControllerTest.java** - Controller tests with @WebMvcTest

#### DTO Tests
- **DeviceDtoTest.java** - JSON marshalling/unmarshalling
- **CreateDeviceDtoTest.java** - JSON tests with validation
- **UpdateDeviceDtoTest.java** - JSON tests

#### Integration Tests
- **JpaDeviceRepositoryIT.java** - Repository tests extending RepositoryIT
- **DeviceControllerIT.java** - Full integration tests with Testcontainers

## Dependencies

No new dependencies required. Uses existing:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Flyway
- PostgreSQL

## API Specification

### Create Device
```
POST /api/devices
Content-Type: application/json

{
  "name": "Living Room Device",
  "description": "Main device for living room"
}

Response: 201 Created
Location: /api/devices/{id}
{
  "id": 1,
  "name": "Living Room Device",
  "description": "Main device for living room",
  "sensorIds": [],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Update Device
```
PUT /api/devices/{id}
Content-Type: application/json

{
  "name": "Updated Device Name",
  "description": "Updated description"
}

Response: 200 OK
{
  "id": 1,
  "name": "Updated Device Name",
  "description": "Updated description",
  "sensorIds": [1, 2],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

### Delete Device
```
DELETE /api/devices/{id}

Response: 204 No Content
```

### Assign Sensor (Idempotent)
```
PUT /api/devices/{id}/sensors/{sensorId}

Response: 204 No Content
```

### List Devices (Keyset Pagination)
```
GET /api/devices?pageSize=10&lastId=5

Response: 200 OK
{
  "items": [...],
  "hasMore": true
}
```

## Validation Rules

1. **Name**: Required, non-blank, unique
2. **Description**: Optional, no specific constraints
3. **Sensor Assignment**: Device must exist, sensor must exist

## Error Scenarios

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Device not found | 404 | ProblemDetail with type DEVICE_NOT_FOUND |
| Device name already exists | 409 | ProblemDetail with type DEVICE_ALREADY_EXISTS |
| Invalid request body | 400 | ProblemDetail with validation errors |
| Sensor not found | 404 | ProblemDetail with type SENSOR_NOT_FOUND |

## Notes

- Follow existing sensor management patterns exactly
- Use package-private repositories (no public modifier)
- Use explicit @Configuration for beans (no @Service/@Component)
- Implement keyset pagination for list endpoint
- Use Test subclass pattern for entity tests (see docs/patterns/ENTITY_TEST_DATA.md)
- Ensure proper error handling with ProblemDetail responses
- Log at appropriate levels (INFO for writes, DEBUG for reads)
- Never log user IDs or personal information (GDPR compliance)
