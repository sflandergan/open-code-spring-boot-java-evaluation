# Device Management Feature Plan

## 1. Package Structure

- Create a new package: `de.sfl.devices`
  - This will contain all code related to device management.

## 2. Entities

- Define the `Device` entity:
  - Properties:
    - `id`: Long (primary key, auto-generated)
    - `name`: String (required)
    - `description`: String

## 3. Repository

- Create a `DeviceRepository` interface extending `JpaRepository`:
  - Responsible for database operations related to devices.

## 4. Service Layer

- Create a `DeviceService` interface:
  - Methods:
    - `createDevice(Device device)`: Creates a new device.
    - `assignSensorsToDevice(Long deviceId, List<Long> sensorIds)`: Assigns sensors to a device (idempotent).
    - `updateDevice(Long deviceId, Device updatedDevice)`: Updates an existing device.
    - `deleteDevice(Long deviceId)`: Deletes a device.
    - `getDeviceById(Long id)`: Retrieves a device by its ID

## 5. REST Controller

- Create a `DeviceController`:
  - Endpoints:
    - `POST /api/devices`: Creates a device.
    - `PUT /api/devices/{deviceId}/sensors`: Assigns sensors to a device.
    - `PUT /api/devices/{deviceId}`: Updates a device.
    - `DELETE /api/devices/{deviceId}`: Deletes a device.

## 6. DTOs

- Define `CreateDeviceDto`, `UpdateDeviceDto`:
  - Used for request bodies in the REST controller.

## 7. Configuration

- Create a `DeviceConfiguration` class:
  - Define beans for the `DeviceService`, `DeviceRepository`.

## 8. Testing

- Unit tests for `DeviceService`
- Integration tests for `DeviceRepository`
- Controller tests for `DeviceController`

## 9. Documentation

- Update AGENTS.md with the new feature details (package, base URL, entities).