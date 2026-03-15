# Devices Feature Implementation Plan

## 1. Feature Overview

Implement a REST API for managing devices with the following operations:
- Create devices
- Assign sensors to devices (idempotent PUT operation)
- Update devices
- Delete devices

A device has the following properties:
- Name
- Description

## 2. Package Structure

Create a new package `de.sfl.devices` for this feature.

## 3. REST API Endpoints

Define the following endpoints:
- `POST /api/devices` - Create a device
- `PUT /api/devices/{deviceId}/sensors/{sensorId}` - Assign a sensor to a device (idempotent)
- `PUT /api/devices/{deviceId}` - Update a device
- `DELETE /api/devices/{deviceId}` - Delete a device

## 4. Implementation Steps

1. Create the `de.sfl.devices` package.
2. Implement the `Device` entity with `name` and `description` fields.
3. Create a `DeviceRepository` interface for database operations.
4. Implement a `DeviceService` class to handle business logic.
5. Create a `DeviceController` class to handle HTTP requests.
6. Write unit tests for the service and controller layers.
7. Write integration tests for the REST API endpoints.
8. Add appropriate validation and error handling.
9. Document the API using SpringDoc OpenAPI.

## 5. Additional Considerations

- Use proper HTTP status codes for each operation (e.g., 201 for created, 404 for not found).
- Ensure the `PUT /api/devices/{deviceId}/sensors/{sensorId}` endpoint is idempotent.
- Implement proper logging and error handling.
- Follow the project's coding standards and best practices.