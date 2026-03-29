# Devices Feature Implementation Plan

## Goal
Implement a new `devices` feature with a REST API that supports:
- creating devices
- assigning sensors to devices via idempotent `PUT`
- updating devices
- deleting devices

The feature will follow the existing package-by-feature architecture and Spring Boot conventions used in this repository.

## Feature Registration
After implementation, update `AGENTS.md` with:
- Package: `de.sfl.devices`
- Base URL: `/api/devices`
- Entities: `Device (id, name, description, sensors)`

## API Design

### Endpoints
| Method | Path | Purpose | Request Body | Response | Success Status |
|---|---|---|---|---|---|
| `POST` | `/api/devices` | Create device | `CreateDeviceDto` | `DeviceDto` | `201 Created` |
| `PUT` | `/api/devices/{deviceId}` | Update device name/description | `UpdateDeviceDto` | `DeviceDto` | `200 OK` |
| `PUT` | `/api/devices/{deviceId}/sensors/{sensorId}` | Assign sensor to device (idempotent) | none | none | `204 No Content` |
| `DELETE` | `/api/devices/{deviceId}` | Delete device | none | none | `204 No Content` |

### Error Handling Contract
- `400 Bad Request` for invalid JSON/validation failures.
- `404 Not Found` when device or sensor does not exist.
- `500 Internal Server Error` for unexpected failures.

### Idempotent Assignment Behavior
- First call to `PUT /api/devices/{deviceId}/sensors/{sensorId}` creates the association.
- Subsequent identical calls do not create duplicates and still return `204 No Content`.

## Domain and Persistence Design

### New Device Entity
- Create `Device` JPA entity in `de.sfl.devices` with:
  - `id` (`Long`, generated)
  - `name` (`String`, required)
  - `description` (`String`, required)
  - timestamps (`createdAt`, `updatedAt`) aligned with existing sensor pattern
  - collection of assigned sensors

### Relationship Model
- Model assignment with a join table `device_sensors`.
- Use a composite primary key (`device_id`, `sensor_id`) to enforce uniqueness and guarantee idempotency at the database level.
- Foreign keys:
  - `device_id -> devices.id` with `ON DELETE CASCADE`
  - `sensor_id -> sensors.id` (cascade delete from sensor side can be enabled for consistency)

### Flyway Migration
- Add migration in `src/main/resources/db/migration/`:
  - `devices` table
  - `device_sensors` join table
  - supporting indexes (for `name`, join columns)

## Package and Class Plan (`de.sfl.devices`)

### Interfaces Layer
- `DeviceController`
- `DeviceExceptionHandler`
- DTOs:
  - `CreateDeviceDto`
  - `UpdateDeviceDto`
  - `DeviceDto`

### Services Layer
- `DeviceService`
  - `createDevice(...)`
  - `updateDevice(...)`
  - `assignSensor(deviceId, sensorId)`
  - `deleteDevice(deviceId)`

### Infrastructure Layer
- `JpaDeviceRepository` (package-private, no stereotype annotation)

### Configuration
- `DeviceConfiguration` defining beans explicitly with `@Configuration` and `@Bean`.

### Exceptions
- `DeviceNotFoundException`
- Optional feature-specific `DeviceValidationException` if needed for richer domain errors.
- Reuse existing sensor not-found behavior for sensor lookup failures when assigning.

## Service-Level Behavior

### Create Device
- Validate input DTO (`@Valid`).
- Map DTO to entity and persist.
- Log operation outcome at `INFO` without personal data.

### Update Device
- Load device by id or throw `DeviceNotFoundException`.
- Update mutable fields (`name`, `description`).
- Persist and return updated representation.

### Assign Sensor to Device
- Load device by id or throw `DeviceNotFoundException`.
- Resolve sensor via service-layer collaboration with `SensorService` (do not access sensor repository directly from controller).
- If already assigned, do nothing.
- If not assigned, add relation and persist.
- Return successfully with no body (`204`).

### Delete Device
- Verify device exists.
- Delete device; join rows are removed by FK cascade.

## Testing Plan

### Controller Tests
- Add `DeviceControllerTest` using `@WebMvcTest`.
- Cover:
  - successful create/update/assign/delete
  - validation errors (`400`)
  - missing device/sensor (`404`)
  - idempotent assignment behavior (same `PUT` twice still `204`)

### Service Unit Tests
- Add `DeviceServiceTest` with mocked dependencies.
- Cover:
  - create/update/delete happy paths
  - not-found paths
  - assignment adds relation once
  - assignment no-op when relation already exists

### Repository Integration Tests
- Add `JpaDeviceRepositoryIT` extending `RepositoryIT`.
- Verify:
  - CRUD behavior
  - join-table persistence
  - uniqueness of (`device_id`, `sensor_id`)
  - cascade behavior on delete

### JSON Model Tests
- Add DTO marshalling/unmarshalling tests for:
  - `CreateDeviceDtoTest`
  - `UpdateDeviceDtoTest`
  - `DeviceDtoTest`

## Implementation Sequence
1. Add migration for `devices` and `device_sensors`.
2. Implement `Device` entity and `JpaDeviceRepository`.
3. Implement `DeviceService` with assignment idempotency rules.
4. Implement controller, DTOs, exception handler, and configuration.
5. Add/adjust OpenAPI annotations for all new endpoints.
6. Add unit, controller, JSON, and repository integration tests.
7. Update `AGENTS.md` feature list entry for devices.
8. Run verification commands:
   - `mvn test -Dtest=DeviceServiceTest,DeviceControllerTest,CreateDeviceDtoTest,UpdateDeviceDtoTest,DeviceDtoTest`
   - `mvn verify -Dit.test=JpaDeviceRepositoryIT`
   - `mvn verify`

## Assumptions
- A device can have multiple sensors.
- A sensor can be assigned to multiple devices.
- Device name uniqueness is not enforced unless explicitly requested later.
