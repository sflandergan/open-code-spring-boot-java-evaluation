# Devices Feature Implementation Plan

## Goal
Introduce a new **Device Management** feature with a dedicated package and REST API that supports:

- Creating devices
- Assigning sensors to devices via an idempotent `PUT` operation
- Updating devices
- Deleting devices

Device properties:

- `name`
- `description`

## 1. Domain and Package Design

1. Create a new dedicated feature package: `de.sfl.devices`.
2. Keep all Device feature classes self-contained inside this package:
   - REST controllers
   - DTOs
   - services
   - repositories
   - configurations
   - exceptions
3. Keep Sensor feature in `de.sfl.sensors` unchanged and interact with it through service-layer boundaries only.

## 2. API Contract (Base URL)

Base URL: `/api/devices`

Planned endpoints:

1. `POST /api/devices`
   - Creates a device.
   - Returns `201 Created` with created representation.
2. `PUT /api/devices/{deviceId}`
   - Updates device name/description.
   - Returns `200 OK` with updated representation.
   - Returns `404 Not Found` if device does not exist.
3. `PUT /api/devices/{deviceId}/sensors/{sensorId}`
   - Assigns a sensor to a device.
   - Must be idempotent (repeating same request keeps same final state).
   - Returns `200 OK` (or `204 No Content`, choose one and keep consistent).
   - Returns `404 Not Found` if device or sensor does not exist.
4. `DELETE /api/devices/{deviceId}`
   - Deletes a device.
   - Returns `204 No Content`.
   - Returns `404 Not Found` if device does not exist.

Validation and error handling:

- Return `400 Bad Request` for invalid payloads.
- Return `409 Conflict` for resource conflicts (e.g., duplicate device name if uniqueness is required).
- Use `@ControllerAdvice` with typed exceptions.

## 3. Data Model and Persistence

1. Add `Device` entity with fields:
   - `id`
   - `name`
   - `description`
2. Implement relationship for sensor assignment:
   - If one sensor belongs to one device: `Device (1) -> (N) Sensor`.
   - If multiple devices per sensor are required in future, re-evaluate as many-to-many.
3. Create Flyway migration in `src/main/resources/db/migration/`:
   - `device` table with proper constraints/indexes.
   - Foreign key strategy for sensor assignment (e.g., `sensor.device_id`) based on chosen cardinality.
4. Keep migration idempotent where possible and do not modify existing migration files.

## 4. Application Layers

### Interfaces Layer (REST)

1. Create `DeviceController`.
2. Create DTOs:
   - `DeviceCreateRequestDto`
   - `DeviceUpdateRequestDto`
   - `DeviceResponseDto`
3. Add Bean Validation annotations for request DTOs.
4. Implement mapping methods (`toEntity` where needed, plus response mapping helpers).

### Service Layer

1. Create `DeviceService` for orchestration and transaction boundaries.
2. Service responsibilities:
   - create device
   - update device
   - delete device
   - assign sensor to device idempotently
3. Enforce business rules and raise domain-specific exceptions.
4. Add compliant logging:
   - INFO for modifications
   - DEBUG for retrieval operations
   - no personal data / no user identifiers

### Infrastructure Layer

1. Create package-protected repository interfaces/classes in `de.sfl.devices`.
2. Add device persistence operations.
3. Handle sensor-assignment persistence mechanics within allowed package/service boundaries.

## 5. Bean Configuration

1. Do not use `@Component`, `@Service`, `@Repository`.
2. Add explicit `@Configuration` class(es) in `de.sfl.devices` to wire:
   - controller dependencies
   - service beans
   - repository adapters
   - exception handler beans if needed

## 6. Idempotent Sensor Assignment Semantics

Implementation expectations for `PUT /api/devices/{deviceId}/sensors/{sensorId}`:

1. If sensor is already assigned to the same device, return success without side effects.
2. If sensor is unassigned, assign it and return success.
3. If sensor is assigned to a different device, decide and document policy:
   - either reassign (last-write-wins), or
   - reject with `409 Conflict`.

Recommended default: reject cross-device reassignment with `409 Conflict` to avoid accidental ownership changes.

## 7. Testing Plan

1. Controller tests (`@WebMvcTest`):
   - create/update/delete endpoints
   - idempotent sensor assignment endpoint
   - validation failures (`400`)
   - not found and conflict paths (`404`, `409`)
2. Service unit tests (Mockito):
   - happy paths
   - idempotent assignment behavior
   - conflict and missing-resource scenarios
3. Repository integration tests:
   - extend `RepositoryIT`
   - verify CRUD and assignment persistence behavior
4. Migration verification:
   - ensure schema boots and constraints behave as expected

Execution guidance:

- Run targeted tests while implementing (`mvn test -Dtest=<ClassName>`)
- Run integration verification for repository tests (`mvn verify -Dit.test=<ClassName>`)
- Run full verification at the end (`mvn verify`)

## 8. Documentation and Feature Registry Updates

1. Update `AGENTS.md` feature list:
   - Feature: Device Management
   - Package: `de.sfl.devices`
   - Base URL: `/api/devices`
   - Entities: Device (`id`, `name`, `description`)
2. Optionally add short package-level documentation for device-specific rules.

## 9. Suggested Delivery Sequence

1. Define API contract and exception model.
2. Add entity + Flyway migration.
3. Implement repository and service logic.
4. Implement controller + DTO mappings + validation.
5. Add/update tests at each layer.
6. Update `AGENTS.md` and finalize with `mvn verify`.

## 10. Acceptance Criteria

1. Device CRUD operations work according to contract.
2. Sensor assignment endpoint is idempotent.
3. Proper status codes returned for validation, conflict, and missing resources.
4. Architecture rules respected (feature package isolation, layered access, explicit bean configuration).
5. Tests exist for controller, service, and repository layers and pass in CI.
