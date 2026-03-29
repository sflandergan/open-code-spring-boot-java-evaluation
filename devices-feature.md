# Devices Feature Implementation Plan

## Goal

Add a new `de.sfl.devices` feature package with REST endpoints to create, update, delete, and manage sensor assignment for devices.

## Recommended Defaults

- Base package: `de.sfl.devices`
- Base URL: `/api/devices`
- Device model: `id`, `name`, `description`, `createdAt`, `updatedAt`
- Assignment model: one device can have many sensors, and one sensor can belong to at most one device
- Assignment endpoint: `PUT /api/devices/{deviceId}/sensors/{sensorId}`
- Idempotency rule: reassigning a sensor to the same device is a no-op and still returns success
- Delete behavior: deleting a device unassigns its sensors instead of deleting them
- Validation rule: `name` is required and unique; `description` is optional but should be trimmed

If product rules differ on sensor cardinality, name uniqueness, or delete behavior, adjust the persistence model before implementation.

## API Scope

### 1. Create device

- `POST /api/devices`
- Request body: `CreateDeviceDto`
- Response: `201 Created` with `DeviceDto`
- Validation: reject blank `name`
- Conflict: return `409 Conflict` when a device with the same name already exists

### 2. Update device

- `PUT /api/devices/{id}`
- Request body: `UpdateDeviceDto`
- Response: `200 OK` with `DeviceDto`
- Errors: `404 Not Found` for missing device, `400 Bad Request` for invalid payload, `409 Conflict` for duplicate name

### 3. Assign sensor to device

- `PUT /api/devices/{deviceId}/sensors/{sensorId}`
- No request body required
- Response: `200 OK` with `DeviceDto` including assigned `sensorIds`
- Errors: `404 Not Found` when device or sensor does not exist
- Idempotency: repeated calls must not create duplicate assignments or side effects

### 4. Delete device

- `DELETE /api/devices/{id}`
- Response: `204 No Content`
- Errors: `404 Not Found` for missing device
- Side effect: clear device assignment from linked sensors before deleting the device

## Package and Class Design

Create a dedicated feature package under `src/main/java/de/sfl/devices/`:

- `Device`
- `DeviceController`
- `DeviceService`
- `JpaDeviceRepository`
- `DeviceConfiguration`
- `DeviceDto`
- `CreateDeviceDto`
- `UpdateDeviceDto`
- `DeviceNotFoundException`
- `DeviceAlreadyExistsException`
- `DeviceExceptionHandler`

Keep repositories package-protected. Register beans through `DeviceConfiguration` instead of stereotype annotations.

## Persistence Design

### Device table

Create a new Flyway migration in `src/main/resources/db/migration/`:

- `devices` table with `id`, `name`, `description`, `created_at`, `updated_at`
- unique constraint or unique index on `name`
- supporting index for lookup by name

### Sensor assignment

Recommended approach:

- add nullable `device_id` to `sensors`
- add foreign key from `sensors.device_id` to `devices.id`
- add index on `sensors.device_id`

This keeps assignment idempotent and simple without introducing a join table.

## Service Collaboration

`DeviceService` should own the device use cases and collaborate with `SensorService` instead of accessing sensor repositories directly.

Planned flow:

- `createDevice` validates unique name and saves the new device
- `updateDevice` loads the device, applies changes, validates uniqueness if name changes, and saves
- `assignSensor` verifies the device exists, then delegates sensor reassignment to `SensorService`
- `deleteDevice` clears sensor assignments through `SensorService`, then deletes the device

To support this, extend the sensor feature with focused service methods such as:

- `assignSensorToDevice(sensorId, deviceId)`
- `clearAssignmentsForDevice(deviceId)`
- `findAssignedSensorIds(deviceId)`

Avoid a bidirectional JPA collection on `Device` unless it becomes necessary. Keeping the relationship owned by `Sensor` reduces cross-feature coupling.

## DTO Design

Recommended DTOs:

- `CreateDeviceDto(String name, String description)`
- `UpdateDeviceDto(String name, String description)`
- `DeviceDto(Long id, String name, String description, Set<Long> sensorIds)`

Use Bean Validation on input DTOs and keep DTO-to-entity conversion in the controller layer where it remains simple.

## Error Handling

Add a device-specific `@RestControllerAdvice` mapped to `DeviceController`.

Handle at least:

- `DeviceNotFoundException` -> `404 Not Found`
- `DeviceAlreadyExistsException` -> `409 Conflict`
- `SensorNotFoundException` bubbling from assignment -> `404 Not Found`
- `MethodArgumentNotValidException` -> `400 Bad Request`
- unexpected exceptions -> `500 Internal Server Error`

## Testing Plan

Add or update the following tests:

- `CreateDeviceDtoTest`
- `UpdateDeviceDtoTest`
- `DeviceDtoTest`
- `DeviceControllerTest`
- `DeviceServiceTest`
- `JpaDeviceRepositoryIT`
- `SensorServiceTest` for assignment behavior
- `JpaSensorRepositoryIT` for persisted assignment and unassignment behavior

Cover these scenarios:

- create succeeds
- create rejects blank name
- create rejects duplicate name
- update succeeds
- update returns 404 for missing device
- assign succeeds
- assign is idempotent for repeated calls
- assign returns 404 for missing device
- assign returns 404 for missing sensor
- delete unassigns sensors and removes the device

## Documentation and Housekeeping

- add the new feature entry to `AGENTS.md` once implementation is in place
- document the chosen assignment semantics in the device package if business rules become more complex
- keep logs GDPR-safe by avoiding device or sensor names in log messages

## Suggested Delivery Order

1. Create the migration and `Device` persistence model.
2. Implement repository, service, configuration, exceptions, and controller advice.
3. Add DTOs and controller endpoints for create, update, assign, and delete.
4. Extend the sensor feature to support assignment and unassignment.
5. Add controller, service, repository, and DTO tests.
6. Run focused tests, then finish with `mvn verify`.
