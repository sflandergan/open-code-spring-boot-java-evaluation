# Devices Feature Implementation Plan

## Goal
Implement a new feature package for managing devices with REST endpoints to create, update, delete, and assign sensors using an idempotent PUT operation.

## Scope and Assumptions
- New feature package: `de.sfl.devices`
- Base URL: `/api/devices`
- Device fields: `name`, `description`
- Sensor assignment model: one device can have many sensors; one sensor can be assigned to at most one device
- Idempotent assignment behavior: repeating the same assignment request has no additional effect

## API Contract

### 1) Create device
- Method/Path: `POST /api/devices`
- Request body: `CreateDeviceDto` (`name`, `description`)
- Responses:
  - `201 Created` with `DeviceDto`
  - `400 Bad Request` for validation errors
  - `409 Conflict` if uniqueness rule is violated (if name is enforced unique)

### 2) Update device
- Method/Path: `PUT /api/devices/{deviceId}`
- Request body: `UpdateDeviceDto` (`name`, `description`)
- Responses:
  - `200 OK` with updated `DeviceDto`
  - `400 Bad Request` for validation errors
  - `404 Not Found` when device does not exist
  - `409 Conflict` if update violates uniqueness rule (if name is enforced unique)

### 3) Assign sensor to device (idempotent)
- Method/Path: `PUT /api/devices/{deviceId}/sensors/{sensorId}`
- Request body: none
- Responses:
  - `204 No Content` when assignment is created
  - `204 No Content` when assignment already exists (idempotent no-op)
  - `404 Not Found` when device or sensor does not exist
  - `409 Conflict` when sensor is already assigned to a different device

### 4) Delete device
- Method/Path: `DELETE /api/devices/{deviceId}`
- Responses:
  - `204 No Content` when deleted
  - `404 Not Found` when device does not exist

## Package and Class Design

### Production code (`src/main/java/de/sfl/devices`)
- `Device` (JPA entity)
- `DeviceController` (REST API)
- `DeviceService` (business logic, transaction boundary)
- `JpaDeviceRepository` (package-private Spring Data repository)
- `DeviceDto` (response DTO)
- `CreateDeviceDto` (create request DTO)
- `UpdateDeviceDto` (update request DTO)
- `DeviceExceptionHandler` (`@RestControllerAdvice` scoped to `DeviceController`)
- Exceptions:
  - `DeviceNotFoundException`
  - `DeviceAlreadyExistsException` (if uniqueness is implemented)
  - `SensorAlreadyAssignedException`
- `DeviceConfiguration` (`@Configuration` with explicit `@Bean` wiring)

### Existing feature interactions
- Reuse `de.sfl.sensors.SensorService` to validate/fetch sensors
- Do not access sensor repositories directly from `devices` package
- Keep repositories package-protected inside each feature package

## Database and Migration Plan

### New migration (`src/main/resources/db/migration`)
Create a migration file named with timestamp convention, for example:
- `V<YYYYMMDDHHmm>__create_device_tables.sql`

### Schema changes
1. `devices` table
   - `id BIGSERIAL` primary key
   - `name VARCHAR(255) NOT NULL`
   - `description TEXT NOT NULL`
   - `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
   - `updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
2. `device_sensors` table
   - `device_id BIGINT NOT NULL`
   - `sensor_id BIGINT NOT NULL`
   - Primary key `(device_id, sensor_id)` for idempotent assignment
   - Foreign key to `devices(id)` with `ON DELETE CASCADE`
   - Foreign key to `sensors(id)` with `ON DELETE CASCADE`
   - Unique constraint on `sensor_id` to enforce one-device-per-sensor rule
3. Indexes
   - `idx_devices_name` (and optional unique index/constraint if enforcing unique names)
   - `idx_device_sensors_device_id`
   - `idx_device_sensors_sensor_id`

## Service Behavior Details
- `createDevice`: validate input, enforce business constraints, persist, log at INFO
- `updateDevice`: load-or-404, apply fields, enforce constraints, persist, log at INFO
- `assignSensorToDevice`:
  - verify device exists
  - verify sensor exists via `SensorService`
  - create assignment when missing
  - return no-op when already assigned to same device
  - throw conflict when sensor belongs to another device
- `deleteDevice`: load-or-404, delete, rely on cascade for assignment cleanup, log at INFO

## Test Plan

### Controller tests (`@WebMvcTest`)
Create `DeviceControllerTest` covering:
- create success + validation errors
- update success + 404 + validation errors
- assign success + idempotent second call + 404 (device/sensor) + 409 conflict
- delete success + 404

### Service tests (Mockito)
Create `DeviceServiceTest` covering:
- create/update/delete happy paths
- not-found and conflict scenarios
- idempotent assignment behavior and cross-device conflict handling

### Repository integration tests
Create `JpaDeviceRepositoryIT` (extends `RepositoryIT`) covering:
- CRUD for devices
- assignment persistence and uniqueness rules
- cascade delete behavior for `device_sensors`

### JSON model tests
Create tests for DTO serialization/deserialization:
- `CreateDeviceDtoTest`
- `UpdateDeviceDtoTest`
- `DeviceDtoTest`

## Documentation Updates
- Update `AGENTS.md` feature list:
  - Package: `de.sfl.devices`
  - Base URL: `/api/devices`
  - Entities: `Device (id, name, description)`

## Implementation Sequence
1. Add Flyway migration for `devices` and `device_sensors`
2. Implement device entity/repository and configuration
3. Implement service logic for create/update/delete/assign
4. Implement controller, DTOs, and exception handler
5. Add unit/integration/JSON tests
6. Update `AGENTS.md`
7. Run verification commands:
   - `mvn test -Dtest=DeviceControllerTest,DeviceServiceTest,CreateDeviceDtoTest,UpdateDeviceDtoTest,DeviceDtoTest`
   - `mvn verify -Dit.test=JpaDeviceRepositoryIT`
