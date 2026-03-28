# Devices Feature Implementation Plan

## Goal

Add a new `devices` feature under `de.sfl.devices` with a REST API that supports:

- creating devices
- assigning sensors to a device through an idempotent `PUT` operation
- updating devices
- deleting devices

The device aggregate starts with these properties:

- `name`
- `description`

Base URL:

- `/api/devices`

## Proposed API

### Create device

- `POST /api/devices`
- request body: `CreateDeviceDto`
- response: `201 Created` with `DeviceDto`
- validation: reject blank `name`; decide whether `description` may be blank or null and validate consistently
- conflict handling: return `409 Conflict` when the chosen uniqueness rule is violated, most likely duplicate device name

### Assign sensors to device

- `PUT /api/devices/{deviceId}/sensors`
- request body: `AssignDeviceSensorsDto`
- recommended semantics: replace the complete assigned sensor set for the device, making repeated calls with the same payload idempotent
- response: `200 OK` with updated `DeviceDto`
- error handling:
  - `404 Not Found` when the device does not exist
  - `404 Not Found` when one or more referenced sensors do not exist
  - `400 Bad Request` when the payload is invalid

### Update device

- `PUT /api/devices/{id}`
- request body: `UpdateDeviceDto`
- response: `200 OK` with updated `DeviceDto`
- validation and conflict behavior should match create semantics where applicable

### Delete device

- `DELETE /api/devices/{id}`
- response: `204 No Content`
- return `404 Not Found` when the device does not exist

## Domain and persistence design

### New package

- create a dedicated feature package: `src/main/java/de/sfl/devices`
- keep controller, DTOs, service, repository, exceptions, and configuration inside this package

### Entity model

- introduce `Device` JPA entity
- fields:
  - `id`
  - `name`
  - `description`
  - timestamps consistent with the existing sensor model (`created_at`, `updated_at`)
- model assigned sensors through an association to existing `Sensor` entities

### Sensor assignment model

- recommended database design: `device_sensors` join table with `(device_id, sensor_id)` primary key
- service layer should treat assignment as set replacement to guarantee idempotent `PUT` behavior
- device logic should use `SensorService` to resolve sensors instead of accessing sensor repositories directly, preserving package boundaries

### Important domain decision

- clarify whether a sensor can belong to multiple devices or only one device
- recommended default for initial implementation: allow many-to-many assignment because it matches the requested API without adding hidden exclusivity rules
- if the business rule is one sensor per device, add a unique constraint on `sensor_id` in `device_sensors` and return `409 Conflict` on reassignment

## Application structure

### Main classes

- `Device`
- `DeviceController`
- `DeviceService`
- `JpaDeviceRepository`
- `DeviceConfiguration`
- `DeviceDto`
- `CreateDeviceDto`
- `UpdateDeviceDto`
- `AssignDeviceSensorsDto`
- `DeviceNotFoundException`
- `DeviceAlreadyExistsException`
- `DeviceExceptionHandler`

### Configuration

- register device beans in `DeviceConfiguration`
- keep repositories package-private
- use constructor injection via configuration, consistent with project rules

## Database migration plan

- add a new Flyway migration in `src/main/resources/db/migration/`
- create `devices` table with primary key, name, description, and timestamps
- create `device_sensors` join table with foreign keys to `devices(id)` and `sensors(id)`
- add indexes for lookup paths:
  - device name
  - `device_sensors.device_id`
  - `device_sensors.sensor_id`
- add uniqueness constraint for device name if that is the chosen conflict rule

## Service layer plan

### `DeviceService`

- `createDevice(Device device)`
- `updateDevice(Long id, Device device)`
- `assignSensors(Long deviceId, Set<Long> sensorIds)`
- `deleteDevice(Long id)`

### Service responsibilities

- enforce device existence checks
- enforce name uniqueness rule
- load referenced sensors through `SensorService`
- keep assignment updates transactional
- write GDPR-compliant logs without user-specific data

## Controller layer plan

- expose endpoints under `/api/devices`
- validate request DTOs with Bean Validation
- translate domain exceptions to `400`, `404`, and `409`
- document endpoints with Springdoc annotations, mirroring the sensor feature style
- return DTOs only from the controller layer

## Testing plan

### Controller tests

- add `DeviceControllerTest`
- cover:
  - create success and validation failures
  - update success, validation failures, duplicate-name conflict, and not-found
  - assign sensors success, invalid payload, missing device, and missing sensors
  - delete success and not-found

### Service tests

- add `DeviceServiceTest`
- cover:
  - create with unique and duplicate names
  - update with changed and unchanged names
  - assignment idempotency
  - assignment with missing sensors
  - delete behavior for existing and missing devices

### Repository integration tests

- add `JpaDeviceRepositoryIT`
- verify persistence of device data and sensor assignments
- verify join-table behavior and relevant uniqueness constraints

### DTO and JSON tests

- add JSON model tests for each new DTO class
- verify serialization, deserialization, and validation-related field expectations

## Documentation updates

- update `AGENTS.md` once implementation starts or lands:
  - Package: `de.sfl.devices`
  - Base URL: `/api/devices`
  - Entities: `Device (id, name, description)`
- document any final sensor-assignment rule if it becomes part of the public contract

## Suggested implementation order

1. Add the `devices` feature entry to planning notes and create the migration.
2. Implement the `Device` entity and repository.
3. Implement service logic for create, update, delete, and sensor assignment.
4. Add DTOs, controller, and exception handling.
5. Add controller, service, repository, and DTO tests.
6. Run focused tests first, then `mvn verify`.

## Acceptance criteria

- devices can be created, updated, and deleted through `/api/devices`
- sensor assignment uses an idempotent `PUT` endpoint
- project layering rules remain intact across `devices` and `sensors`
- Flyway migration creates the required schema
- automated tests cover controller, service, repository, and DTO behavior
