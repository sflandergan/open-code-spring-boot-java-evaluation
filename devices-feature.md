# Device Management Feature - Implementation Plan

## Overview
This document outlines the implementation plan for the **Devices** feature, which provides REST API endpoints to manage devices and assign sensors to them.

---

## Feature Specification

### Package Structure
- **Package**: `de.sfl.devices`
- **Base URL**: `/api/devices`

### Core Entities
- **Device**
  - Properties: `id` (UUID), `name` (String), `description` (String)
  - Relationships: Can have multiple sensors assigned
  - Timestamps: `createdAt`, `updatedAt`

- **Device-Sensor Assignment** (Junction/Bridge Table)
  - Properties: `deviceId`, `sensorId`
  - Ensures a sensor can be assigned to multiple devices
  - Supports idempotent assignment (PUT operation)

---

## REST API Endpoints

### 1. Create Device
- **Method**: `POST`
- **URL**: `/api/devices`
- **Request Body**: `CreateDeviceDto` (name, description)
- **Response**: `DeviceDto` (201 Created)
- **Validation**: Name is required, both fields must be non-blank
- **Idempotency**: Not idempotent (creates new device each time)

### 2. Assign Sensor to Device
- **Method**: `PUT`
- **URL**: `/api/devices/{deviceId}/sensors/{sensorId}`
- **Response**: `DeviceDto` (200 OK)
- **Idempotency**: Fully idempotent - repeated calls return same result
- **Validation**: Both deviceId and sensorId must exist
- **Error Codes**:
  - 404 if device not found
  - 404 if sensor not found

### 3. Update Device
- **Method**: `PUT`
- **URL**: `/api/devices/{deviceId}`
- **Request Body**: `UpdateDeviceDto` (name, description)
- **Response**: `DeviceDto` (200 OK)
- **Validation**: Both fields optional but must be non-blank if provided
- **Error Codes**: 404 if device not found

### 4. Delete Device
- **Method**: `DELETE`
- **URL**: `/api/devices/{deviceId}`
- **Response**: No content (204 No Content)
- **Cascade**: All sensor assignments are deleted
- **Error Codes**: 404 if device not found

---

## Implementation Components

### 1. Database Layer

#### Migration: Create Device Tables
**File**: `src/main/resources/db/migration/V<timestamp>_create_device_tables.sql`

```sql
-- Device table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_name ON devices(name);

-- Device-Sensor assignment junction table
CREATE TABLE device_sensors (
    device_id UUID NOT NULL,
    sensor_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (device_id, sensor_id),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (sensor_id) REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_device_sensors_sensor_id ON device_sensors(sensor_id);
```

### 2. Entity Layer

#### `Device.java`
- JPA entity mapped to `devices` table
- Fields: id, name, description, createdAt, updatedAt
- Relationship: `@OneToMany` to `DeviceSensor` (cascade delete)
- Lombok: `@Entity`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Annotations: `@Table(name = "devices")`

#### `DeviceSensor.java` (Composite Key Entity)
- JPA entity for junction table `device_sensors`
- Composite key: DeviceSensorId (deviceId, sensorId)
- Fields: deviceId, sensorId, assignedAt
- Embedded ID class: `DeviceSensorId`

### 3. Repository Layer

#### `JpaDeviceRepository.java`
- Extends `JpaRepository<Device, UUID>`
- Package-protected (no access modifier)
- Methods:
  - Inherited CRUD methods (save, findById, delete, etc.)
  - Optional custom methods for complex queries

#### `JpaDeviceSensorRepository.java`
- Extends `JpaRepository<DeviceSensor, DeviceSensorId>`
- Package-protected
- Methods:
  - `existsByDeviceIdAndSensorId(UUID deviceId, UUID sensorId)` - check assignment exists

### 4. Service Layer

#### `DeviceService.java`
- `@Transactional` at class level
- Methods:
  - `createDevice(CreateDeviceDto dto): Device` - creates new device
  - `updateDevice(UUID id, UpdateDeviceDto dto): Device` - updates device fields
  - `deleteDevice(UUID id): void` - deletes device and all assignments
  - `assignSensor(UUID deviceId, UUID sensorId): Device` - assigns sensor (idempotent)
  - `getDevice(UUID id): Device` - retrieves device
  - `getDeviceSensors(UUID deviceId): List<Sensor>` - gets sensors assigned to device
- Exception handling:
  - Throw `DeviceNotFoundException` when device not found
  - Throw `SensorNotFoundException` when sensor not found (depends on sensor service)
- Logging:
  - INFO level: when device is created, updated, or deleted
  - DEBUG level: when device is retrieved

#### Dependencies:
- `JpaDeviceRepository` (injected via constructor)
- `JpaDeviceSensorRepository` (injected via constructor)
- Optional: `SensorService` if we need to validate sensor exists

### 5. DTO Layer

#### `CreateDeviceDto.java`
- Fields:
  - `@NotBlank String name`
  - `@NotBlank String description`
- Method: `toEntity(): Device` - converts to entity

#### `UpdateDeviceDto.java`
- Fields:
  - `@NotBlank(required=false) String name` (optional)
  - `@NotBlank(required=false) String description` (optional)
- Method: `applyToEntity(Device device): void` - updates entity fields

#### `DeviceDto.java`
- Fields:
  - `UUID id`
  - `String name`
  - `String description`
  - `List<SensorDto> sensors` (optional - only for get operations)
  - `LocalDateTime createdAt`
  - `LocalDateTime updatedAt`
- Constructor: Convert from entity with sensors

### 6. Controller Layer

#### `DeviceController.java`
- Base path: `/api/devices`
- Methods:
  - `POST /` - `createDevice(CreateDeviceDto dto)` → 201 Created, returns DeviceDto
  - `PUT /{id}` - `updateDevice(UUID id, UpdateDeviceDto dto)` → 200 OK, returns DeviceDto
  - `DELETE /{id}` - `deleteDevice(UUID id)` → 204 No Content
  - `PUT /{deviceId}/sensors/{sensorId}` - `assignSensor(UUID deviceId, UUID sensorId)` → 200 OK, returns DeviceDto
  - `GET /{id}` - `getDevice(UUID id)` → 200 OK, returns DeviceDto (optional for convenience)

- Annotations:
  - `@RestController`
  - `@RequestMapping("/api/devices")`
  - `@CrossOrigin` if needed
  - `@Valid` on request body DTOs
  - `@ExceptionHandler` or use global exception handler

- OpenAPI/Swagger documentation on each endpoint

### 7. Exception Handling

#### Custom Exceptions (in `devices` package)
- `DeviceNotFoundException extends RuntimeException`
  - Used when device ID doesn't exist
  - HTTP 404 response

- `DeviceAlreadyExistsException extends RuntimeException` (optional)
  - Used if implementing duplicate name check
  - HTTP 409 response

#### Exception Handler

##### `DeviceExceptionHandler.java`
- `@ControllerAdvice`
- `@ExceptionHandler(DeviceNotFoundException.class)` → 404
- `@ExceptionHandler(SensorNotFoundException.class)` → 404 (when assigning sensor)
- `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400
- `@ExceptionHandler(Exception.class)` → 500

### 8. Configuration

#### `DeviceConfiguration.java`
- `@Configuration`
- Bean definitions:
  - `DeviceService` bean (constructor-injected dependencies)
  - Optionally: Spring Data repositories are auto-configured

---

## Testing Strategy

### 1. Unit Tests

#### `DeviceServiceTest.java`
- Test all service methods with mocked repositories
- Test happy paths and error scenarios
- Test idempotency of sensor assignment
- Test validation logic
- Test exception throwing

#### `DeviceControllerTest.java`
- Use `@WebMvcTest`
- Mock `DeviceService`
- Test all 5 endpoints
- Test request validation (400 errors)
- Test 404 errors
- Test 201, 200, 204 responses
- Test request/response serialization

### 2. Integration Tests

#### `JpaDeviceRepositoryIT.java`
- Extends `RepositoryIT` base class
- Test CRUD operations
- Test data persistence
- Test cascade delete behavior

#### `JpaDeviceSensorRepositoryIT.java`
- Test junction table operations
- Test composite key behavior
- Test foreign key constraints

### 3. JSON Model Tests

#### `CreateDeviceDtoTest.java`
- Marshalling test: DTO → JSON
- Unmarshalling test: JSON → DTO
- Validation test: required fields

#### `UpdateDeviceDtoTest.java`
- Marshalling and unmarshalling tests
- Test optional fields

#### `DeviceDtoTest.java`
- Marshalling and unmarshalling tests
- Test nested sensors list

---

## Implementation Sequence

### Phase 1: Foundation (Database & Entities)
1. Create Flyway migration for device tables
2. Implement `Device.java` entity
3. Implement `DeviceSensor.java` and `DeviceSensorId.java` entities
4. Create database integration tests

### Phase 2: Repository Layer
5. Implement `JpaDeviceRepository.java`
6. Implement `JpaDeviceSensorRepository.java`
7. Create repository integration tests

### Phase 3: DTOs & Validation
8. Implement `CreateDeviceDto.java`
9. Implement `UpdateDeviceDto.java`
10. Implement `DeviceDto.java`
11. Create JSON model tests for all DTOs

### Phase 4: Service Layer
12. Implement `DeviceService.java`
13. Create service unit tests

### Phase 5: REST API & Exception Handling
14. Implement `DeviceExceptionHandler.java`
15. Implement `DeviceController.java`
16. Create controller tests
17. Add OpenAPI/Swagger documentation

### Phase 6: Configuration
18. Implement `DeviceConfiguration.java`
19. Integration testing of entire feature

### Phase 7: Documentation
20. Add feature entry to AGENTS.md
21. Add API documentation (OpenAPI/Swagger)

---

## Design Decisions

### 1. Sensor Assignment Strategy
- **Junction Table Pattern**: Uses `DeviceSensor` junction table for many-to-many relationship
- **Rationale**: Allows sensors to be assigned to multiple devices, and devices to have multiple sensors
- **Idempotency**: Checking existence before insertion ensures PUT is fully idempotent

### 2. UUID vs Auto-increment
- **Decision**: Use UUID for device IDs
- **Rationale**: Consistent with existing Sensor entities, better for distributed systems

### 3. Timestamps
- **Decision**: Include `createdAt` and `updatedAt` on Device entity
- **Rationale**: Audit trail and common best practice

### 4. Cascade Delete
- **Decision**: Deleting a device cascades to delete all sensor assignments
- **Rationale**: Data integrity - orphaned assignments should not exist

### 5. Service Boundaries
- **Decision**: `DeviceService` does NOT call `SensorService` directly
- **Rationale**: Services should not cross package boundaries; validation happens at repository/FK level
- **Exception**: Could optionally inject `SensorService` if we want to enrich response with sensor details

---

## Future Enhancements

1. **Pagination**: Add list/search endpoints for devices with pagination
2. **Filtering**: Filter devices by name, creation date, etc.
3. **Bulk Operations**: Bulk assign sensors to a device
4. **Device Groups**: Group devices and manage them collectively
5. **Audit Trail**: Track all modifications with user info
6. **Soft Delete**: Support soft-deletes for compliance/recovery

---

## Dependencies
- Spring Boot 3.x
- Spring Data JPA
- Lombok
- Flyway (for migrations)
- JUnit 5
- Mockito
- Spring Boot Test
- Springdoc OpenAPI (for API documentation)

No new dependencies required; all are already in the project.

---

## Compliance & Guidelines
- Follows domain-driven design with package-by-feature approach
- Adheres to strict layered architecture (Controller → Service → Repository)
- Uses constructor injection for all dependencies
- Uses `@Configuration` classes for bean management
- Includes comprehensive test coverage
- Follows naming conventions (DTOs with `Dto` suffix)
- GDPR compliant logging (no personal data logged)
- Proper HTTP status codes (201, 200, 204, 404, 409, 400, 500)
