# Devices Feature Implementation Plan

## 1. Goal

Implement a new **Device Management** feature in its own package with REST APIs to:

- Create devices
- Assign sensors to devices via an idempotent PUT operation
- Update devices
- Delete devices

Device properties:

- `name`
- `description`

## 2. Feature Registration

Update `AGENTS.md` under **Features** with:

- Feature: `Device Management`
- Package: `de.sfl.devices`
- Base URL: `/api/devices`
- Entities: `Device (id, name, description)`

## 3. API Design

Base URL: `/api/devices`

### Endpoints

1. `POST /api/devices`
   - Creates a new device
   - Request body: `CreateDeviceDto`
   - Response: `201 Created` with `DeviceDto`
   - Errors: `400` (validation), `409` (name conflict)

2. `PUT /api/devices/{deviceId}`
   - Updates device name/description
   - Request body: `UpdateDeviceDto`
   - Response: `200 OK` with `DeviceDto`
   - Errors: `400` (validation), `404` (device not found), `409` (name conflict)

3. `PUT /api/devices/{deviceId}/sensors/{sensorId}`
   - Assigns a sensor to a device
   - Idempotent behavior: repeated calls keep the same final state and do not create duplicates
   - Response: `200 OK` with updated `DeviceDto` (including assigned sensor ids)
   - Errors: `404` (device or sensor not found)

4. `DELETE /api/devices/{deviceId}`
   - Deletes a device
   - Response: `204 No Content`
   - Errors: `404` (device not found)

## 4. Package-by-Feature Structure

Create `src/main/java/de/sfl/devices/` with:

- `Device` (JPA entity)
- `JpaDeviceRepository` (package-private)
- `DeviceService`
- `DeviceController`
- `DeviceConfiguration`
- DTOs:
  - `CreateDeviceDto`
  - `UpdateDeviceDto`
  - `DeviceDto`
- Exceptions:
  - `DeviceNotFoundException`
  - `DeviceAlreadyExistsException`
  - `DeviceExceptionHandler` (`@ControllerAdvice`)

## 5. Data Model and Persistence

### Device table

- `id BIGSERIAL` primary key
- `name VARCHAR(...) NOT NULL`
- `description VARCHAR(...) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`
- Unique constraint on `name`

### Device-sensor relationship

Use a join table for many-to-many assignment:

- `device_sensors(device_id, sensor_id)`
- Composite primary key: `(device_id, sensor_id)` to enforce idempotency at DB level
- Foreign keys:
  - `device_id -> devices(id)` with `ON DELETE CASCADE`
  - `sensor_id -> sensors(id)` with `ON DELETE CASCADE`

### Flyway migration

Add a new migration in `src/main/resources/db/migration/`:

- Create `devices` table
- Create `device_sensors` join table
- Add indexes for lookup performance (name and FK columns)

## 6. Service Layer Behavior

Implement transactional service methods:

- `createDevice(Device)`
  - Validate unique name
  - Persist device
- `updateDevice(Long id, Device data)`
  - Load existing device or throw `DeviceNotFoundException`
  - Apply mutable fields (`name`, `description`)
  - Validate name uniqueness when changed
- `assignSensor(Long deviceId, Long sensorId)`
  - Load device
  - Validate sensor existence via `SensorService` (no direct cross-package repository access)
  - Add sensor association only if missing
  - Save device
- `deleteDevice(Long id)`
  - Validate existence
  - Delete

Logging should follow current policy (service layer, no personal data).

## 7. Validation and Error Handling

- DTO validation with Bean Validation:
  - `name`: not blank, length constraints
  - `description`: not blank, length constraints
- Map exceptions to status codes:
  - `DeviceAlreadyExistsException -> 409`
  - `DeviceNotFoundException -> 404`
  - Validation failures -> `400`
  - Unexpected failures -> `500`

## 8. Testing Plan

Add tests for all layers in the devices feature:

1. Controller tests (`DeviceControllerTest`)
   - Create/update/assign/delete success paths
   - Validation failures (`400`)
   - Not found (`404`) and conflict (`409`) scenarios

2. Service tests (`DeviceServiceTest`)
   - Name uniqueness checks
   - Update semantics
   - Idempotent sensor assignment behavior
   - Delete behavior and error paths

3. Repository integration tests (`JpaDeviceRepositoryIT`)
   - Persistence and retrieval
   - Unique name constraint behavior
   - Join-table assignment behavior

4. JSON model tests for DTO marshalling/unmarshalling as required by project conventions

## 9. Implementation Sequence

1. Add devices feature entry to `AGENTS.md`.
2. Create Flyway migration for `devices` and `device_sensors`.
3. Implement `Device` entity + repository.
4. Implement service logic (create, update, assign sensor, delete).
5. Implement DTOs + controller + exception handler.
6. Add/adjust OpenAPI annotations.
7. Implement tests (controller, service, repository, JSON models).
8. Run targeted tests, then `mvn verify`.

## 10. Definition of Done

- New feature exists only under `de.sfl.devices`.
- All requested endpoints implemented and documented.
- Sensor assignment via PUT is idempotent.
- Flyway migration applied successfully.
- Tests for controller/service/repository/JSON models are green.
- `mvn verify` passes.
