# Devices Feature Implementation Plan

## Overview
This document outlines the implementation plan for a new Devices feature that will manage devices and their associated sensors.

## Feature Requirements
- REST API for device management (CRUD operations)
- Device properties: Name, Description
- Assign sensors to devices using idempotent PUT operation

## Implementation Steps

### 1. Create Device Package Structure
- Create new package: `de.sfl.devices`
- Follow domain-driven design with separate packages for each feature

### 2. Device Entity
- Create `Device` entity with:
  - id (Long, auto-generated)
  - name (String, required)
  - description (String, optional)
- Add relationship to Sensor entity
- Implement proper JPA mappings following DATABASE_SCHEMA.md guidelines

### 3. Device DTOs
- Create `DeviceDto` for request/response
- Implement validation annotations (@NotBlank, @Size)
- Add `toEntity()` method for conversion

### 4. Device Repository
- Create `DeviceRepository` interface extending JpaRepository
- Add custom query methods if needed
- Implement package-protected access

### 5. Device Service
- Create `DeviceService` interface with methods:
  - createDevice()
  - updateDevice()
  - deleteDevice()
  - assignSensorsToDevice() (idempotent)
- Create `JpaDeviceService` implementation
- Add transaction management
- Implement proper error handling

### 6. Device Controller
- Create `DeviceController` with endpoints:
  - POST /api/devices - Create device
  - PUT /api/devices/{id} - Update device
  - DELETE /api/devices/{id} - Delete device
  - PUT /api/devices/{deviceId}/sensors - Assign sensors (idempotent)
- Implement proper HTTP status codes
- Add OpenAPI documentation

### 7. Database Migration
- Create Flyway migration for devices table
- Add foreign key to sensors table if needed
- Follow naming conventions from DATABASE_SCHEMA.md

### 8. Testing
- Create unit tests for each layer:
  - DeviceDtoTest (JSON marshalling/unmarshalling)
  - DeviceServiceTest
  - DeviceControllerTest
- Create integration test: DeviceRepositoryIT
- Follow testing patterns from documentation

### 9. Configuration
- Add any needed configuration properties
- Follow @ConfigurationProperties pattern

### 10. Documentation
- Update AGENTS.md with new feature information
- Add any specific patterns or considerations to docs/patterns/
