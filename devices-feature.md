# Devices Feature Implementation Plan

## Overview
This document outlines the implementation plan for the Devices feature, which will allow managing devices and assigning sensors to them through a REST API.

## Feature Requirements

### Device Properties
- **Name**: String, required
- **Description**: String, optional

### API Endpoints
1. **Create Device**: `POST /api/devices`
   - Request Body: Device DTO (name, description)
   - Response: Created Device DTO with ID
   - Status Code: 201 Created

2. **Assign Sensors to Device**: `PUT /api/devices/{deviceId}/sensors`
   - Request Body: List of Sensor IDs
   - Response: Updated Device DTO with assigned sensors
   - Status Code: 200 OK
   - Idempotent operation

3. **Update Device**: `PUT /api/devices/{deviceId}`
   - Request Body: Device DTO (name, description)
   - Response: Updated Device DTO
   - Status Code: 200 OK

4. **Delete Device**: `DELETE /api/devices/{deviceId}`
   - Response: 204 No Content
   - Status Code: 204 No Content

## Implementation Steps

### 1. Create Device Entity
- Define `Device` entity with properties: `id`, `name`, `description`
- Add JPA annotations for database mapping
- Place in `de.sfl.devices` package

### 2. Create Device Repository
- Define `DeviceRepository` interface extending `JpaRepository`
- Add custom methods if needed
- Place in `de.sfl.devices` package

### 3. Create Device DTOs
- Define `DeviceDto` for request/response
- Include validation annotations
- Place in `de.sfl.devices` package

### 4. Create Device Service
- Define `DeviceService` interface
- Implement `DeviceServiceImpl` with business logic
- Methods: `createDevice`, `assignSensorsToDevice`, `updateDevice`, `deleteDevice`
- Place in `de.sfl.devices` package

### 5. Create Device Controller
- Define `DeviceController` with REST endpoints
- Use `@RestController` and `@RequestMapping("/api/devices")`
- Place in `de.sfl.devices` package

### 6. Create Device Configuration
- Define `DeviceConfiguration` for bean definitions
- Place in `de.sfl.devices` package

### 7. Create Database Migration
- Add Flyway migration for `devices` table
- Define columns: `id`, `name`, `description`
- Place in `src/main/resources/db/migration/`

### 8. Create Tests
- Unit tests for `DeviceService`
- Integration tests for `DeviceRepository`
- Controller tests for `DeviceController`
- JSON model tests for `DeviceDto`

### 9. Update AGENTS.md
- Add Devices feature to the feature list
- Document package, base URL, and entities

## Database Schema

### devices Table
| Column       | Type       | Constraints          |
|--------------|------------|----------------------|
| id           | BIGINT     | PRIMARY KEY, AUTO_INCREMENT |
| name         | VARCHAR(255)| NOT NULL             |
| description  | TEXT       | NULLABLE              |

### device_sensors Table (Join Table)
| Column       | Type       | Constraints          |
|--------------|------------|----------------------|
| device_id    | BIGINT     | FOREIGN KEY (devices.id) |
| sensor_id    | BIGINT     | FOREIGN KEY (sensors.id) |
| PRIMARY KEY (device_id, sensor_id) |

## Error Handling
- Return 400 Bad Request for invalid input
- Return 404 Not Found when device does not exist
- Return 409 Conflict when device already exists
- Return 500 Internal Server Error for unexpected errors

## Validation
- Validate device name is not blank
- Validate device name length (e.g., max 255 characters)
- Validate sensor IDs exist when assigning sensors

## Testing Strategy
- Unit tests for service layer
- Integration tests for repository layer
- Controller tests for REST endpoints
- JSON model tests for DTOs
- Test edge cases and error scenarios

## Timeline
- Day 1: Create entity, repository, DTOs, and service
- Day 2: Create controller, configuration, and database migration
- Day 3: Write tests and verify functionality
- Day 4: Update documentation and finalize

## Dependencies
- Spring Boot Starter Data JPA
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Flyway Core
- Lombok (optional, for reducing boilerplate)

## Notes
- Follow existing code conventions and patterns
- Ensure all tests pass before marking as complete
- Update AGENTS.md with feature details