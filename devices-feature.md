# Devices Feature Implementation Plan

## Goal

Add a new `devices` feature in its own package `de.sfl.devices` with a REST API for:

- creating devices
- assigning sensors to devices via an idempotent `PUT`
- updating devices
- deleting devices

Each device stores:

- `name`
- `description`

## API Design

Base URL: `/api/devices`

Planned endpoints:

- `POST /api/devices` to create a device
- `PUT /api/devices/{deviceId}` to update device name and description
- `PUT /api/devices/{deviceId}/sensors/{sensorId}` to assign a sensor to a device idempotently
- `DELETE /api/devices/{deviceId}` to delete a device

Suggested response behavior:

- `201 Created` for successful device creation
- `200 OK` for successful update
- `200 OK` for repeated sensor assignment when the relation already exists
- `204 No Content` for successful deletion
- `400 Bad Request` for invalid payloads
- `404 Not Found` when device or sensor does not exist

## Domain And Persistence

1. Create a new feature package `de.sfl.devices` and keep all device-specific controller, service, repository, DTO, exception, and configuration classes inside it.
2. Introduce a `Device` entity with fields for `id`, `name`, `description`, and timestamps consistent with the existing persistence style.
3. Model the device-to-sensor assignment as a many-to-many association using a dedicated join table such as `device_sensors`.
4. Decide and enforce device name uniqueness at the database and service level to match the existing sensor feature behavior.
5. Add a Flyway migration that creates the `devices` table and the `device_sensors` join table, including primary keys, foreign keys, and uniqueness constraints that guarantee idempotent assignments.

## Application Layer

1. Add a `DeviceService` with transaction boundaries on mutating operations.
2. Implement `createDevice` with validation and duplicate-name protection.
3. Implement `updateDevice` to load the existing device, apply changes, validate uniqueness when the name changes, and persist the update.
4. Implement `assignSensor` to load both aggregates, create the relation only when missing, and return the current device state without duplicating the assignment.
5. Implement `deleteDevice` to remove the device and clean up the join-table relationship through JPA mapping or database constraints.

## REST Layer

1. Add request and response DTOs such as `CreateDeviceDto`, `UpdateDeviceDto`, and `DeviceDto`.
2. Add bean validation for required fields, especially a non-blank `name`.
3. Add a `DeviceController` with OpenAPI annotations following the existing controller style.
4. Add feature-specific exception types such as `DeviceNotFoundException` and `DeviceAlreadyExistsException`.
5. Add a `DeviceExceptionHandler` that maps domain exceptions to the required HTTP status codes.

## Wiring

1. Add a `DeviceConfiguration` class that explicitly defines the device service and repository beans.
2. Reuse the existing sensor repository from the `sensors` feature only through an explicit dependency that preserves the layered architecture as much as possible.
3. If cross-feature repository access would violate the current rules, introduce a narrow sensor lookup service API in `de.sfl.sensors` and inject that into the device feature.
4. Update `AGENTS.md` to register the new feature with package `de.sfl.devices`, base URL `/api/devices`, and entity `Device`.

## Testing Plan

1. Add `DeviceControllerTest` covering create, update, assign sensor, delete, validation failures, duplicate-name conflicts, and missing-resource scenarios.
2. Add `DeviceServiceTest` covering happy paths, duplicate handling, idempotent sensor assignment, and delete behavior.
3. Add `JpaDeviceRepositoryIT` extending `RepositoryIT` to verify persistence, uniqueness rules, and join-table mapping.
4. Add JSON DTO marshalling tests for each new JSON DTO class.
5. Run focused tests during implementation and finish with `mvn verify` once the feature is complete.

## Implementation Order

1. Add the Flyway migration and the `Device` entity.
2. Add repository access and feature configuration.
3. Implement service-level create, update, assign, and delete behavior.
4. Add DTOs, controller, and exception handling.
5. Add and run controller, service, repository, and DTO tests.
6. Update `AGENTS.md` after the feature shape is finalized.

## Open Design Notes

- The assignment endpoint is planned as relation-oriented `PUT /api/devices/{deviceId}/sensors/{sensorId}` because it naturally expresses idempotency.
- The current project rule that services should not directly access repositories from other feature packages likely means sensor existence should be checked through a sensor service API rather than a direct repository dependency.
- If the API should return the updated device after assignment or update, `DeviceDto` should include assigned sensor IDs or nested sensor summaries; otherwise the controller can return a minimal success response.
