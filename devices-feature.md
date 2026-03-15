# Devices Feature Implementation Plan

## Feature Overview
A feature for managing electronic devices in the system. Each device can have multiple sensors assigned to it, and sensors can be associated with devices.

## Requirements

### Core Requirements
1. Create devices with name and description
2. Assign sensors to devices (idempotent PUT operation)
3. Update device information
4. Delete devices
5. Unassign sensors from devices

### Domain Model
```
Device {
    Long id
    String name
    String description (optional)
    Set<Sensor> sensors (Many-to-Many relationship)
    Instant createdAt
    Instant updatedAt
    
    // Derived field for total number of sensors assigned
    Set<Long> assignedSensorIds
}

Sensor remains existing, but gains relationship to Device
```

### Device Properties
- **Name** (required, unique, 2-100 characters)
- **Description** (optional, up to 1000 characters)

## Implementation Plan

### Phase 1: Data Model & Entities ✓

#### 1.1 Database Schema Changes
- `devices` table:
  - id (BIGINT, PK)
  - name (VARCHAR 255, NOT NULL, UNIQUE)
  - description (TEXT, NULLABLE)
  - created_at (TIMESTAMP)
  - updated_at (TIMESTAMP)

- `device_assigned_sensors` (join table for many-to-many)
  - device_id (FK to devices.id)
  - sensor_id (FK to sensors.id)

#### 1.2 Device Entity
- Implement AuditModel class
- Support for soft deletes (logical deletes)
- One-to-many relationship with sensors via join table
- Transactiona constraints for constraints
- Timestamp tracking

### Phase 2: Core Domain Layer

#### 2.1 Device Entity Class
Investigate inheriting from a base framework class with:
- Business/synthetic ID
- Private or protected default constructor
- Entity constants and validation
- Protected visibility to control construction via `new new`

#### 2.2 Repository Layer
`JpaDeviceRepository` extending Spring Data JPA
- Basic CRUD operations + relationship management
- Custom queries for sensor assignments
- Archived device handling

#### 2.3 Service Layer
`DeviceService` responsible for:
- Basic CRUD operations
- Basic field validation in DTO layer
- Transaction propagation
- Business rule enforcement
- Address Data separation (purpose-driven JPA approach)

### Phase 3: API Layer

#### 3.1 DTO Strategy
Use DTO pattern to connect loosely on Data and Remote layers
```
DeviceRequestDto for API ↔ Domain object mapping
DeviceDto containing assigned device ID and nullable fields
```
- Enforce non-nullability/validation contracts
- Check object properties, throw early, fail fast before mapping to
    domain entities

#### 3.2 HTTP API Design

| Endpoint | HTTP Method | URL | Description |
|----------|------------|-----|-------------|
| Create Device | POST | /api/v1/devices | Create new device with validation |
| Get All Devices | GET | /api/v1/devices | Paginated list with filtering |
| Get Single | GET | /api/v1/devices/{id} | Retrieve by ID with 200 or 404 |
| Update Device | PUT/PATCH | /api/v1/devices/{id} | Full/partial update |
| Delete Device | DELETE | /api/v1/devices/{id} | Set active=false, log activities |
| List by criteria | GET | /api/v1/devices?type={type} | Optional filter for extended criteria |

#### 3.3 Request Examples

Requests must contain:
- Name (2-100 chars, unique)
- Optional description
- On creation returns 409 if name conflicts

### Sensor Assignment API
```
PUT /api/v1/devices/{deviceId}/sensors/{sensorId}
- Adds connection
- Mulitiple assignments from multiple requests
- Should avoid or eliminate possible infinite recursion cycles
- Return ID and basic ETag versioning not needed

DELETE /api/v1/devices/{deviceId}/sensors/{sensorId}
 - Idempotent, ignore conflicts like HTTP 204/SUBMIT_SPMHERE_2D
```

## Phase 4: Business Logic Layer
Focus on orchestration of state

Critical Rule: Sensors cannot belong to multiple devices simultaneously.
- Race condition detection via optimistic locking or `SELECT FOR UPDATE`

Lifecycle hooks:

### DB side:
- Default schema: violation treated as 400 with meaningful API errors

### Testing Plan Levels
1. Unit Tests: Model operations, SQL constraints (H2 database)
2. Integration Tests: Transactions, rollback handling, optimistic exceptions
   - eg. Device and sensors associated constraint matchers with cascade safeguards
3. System tests for full workflow validation
4. End-to-end scenarios: entire workflows simulated

**Edge Cases**:
- Device entity versioning for OCC (optimistic concurrency)
- Managing audit trails in entity lifecycles

### Additional Standards:
- Input validation via Bean Validation 2.0 (should fail for missing fields)
- Input sanitisation using JSR-380 with custom constraints for business logic

## Implementation Order
1. Migration scripts (with rollback)
2. Entity, service, repository layer
3. DTO mapping strategies (e.g. nr resolution, MapStruct)
4. Test data and validation tests
5. Publish API docs with Swagger/OpenAPI

## Notes for Implementation:
- Follow local patterns for error handling and Response codes
- Respect single flight principle for API state and idempotency
- Rollback handling on failure

## Dependencies
- Flyway SQL migration must migrate `device`-`sensor` relationships

```

This plan should be executable stepwise across incremental Pull Requests.

---
