# Devices Feature Implementation Plan

## Overview
Create a new feature package `de.sfl.devices` for managing devices with REST API endpoints.

## Package Structure
```
de.sfl.devices/
├── Device.java                    # JPA Entity
├── JpaDeviceRepository.java       # Spring Data JPA Repository
├── DeviceService.java             # Business logic
├── DeviceController.java          # REST endpoints
├── DeviceConfiguration.java       # Bean configuration
├── DeviceExceptionHandler.java     # Exception handling
├── DeviceNotFoundException.java   # 404 exception
├── DeviceAlreadyExistsException.java # 409 exception
├── DeviceDto.java                 # Response DTO
├── CreateDeviceDto.java           # Request DTO for creation
├── UpdateDeviceDto.java           # Request DTO for update
├── AssignSensorsDto.java           # Request DTO for sensor assignment
```

## Database Schema
New migration file: `src/main/resources/db/migration/VYYYYMMDDHHMM__create_device_tables.sql`

```sql
CREATE TABLE devices (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_devices PRIMARY KEY (id)
);

CREATE TABLE device_sensors (
    device_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    PRIMARY KEY (device_id, sensor_id),
    CONSTRAINT fk_device_sensors_devices FOREIGN KEY (device_id) 
        REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_device_sensors_sensors FOREIGN KEY (sensor_id) 
        REFERENCES sensors(id) ON DELETE CASCADE
);

CREATE INDEX idx_devices_name ON devices(name);
CREATE INDEX idx_device_sensors_device_id ON device_sensors(device_id);
CREATE INDEX idx_device_sensors_sensor_id ON device_sensors(sensor_id);
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/devices | Create a new device |
| GET | /api/devices | Get all devices (with keyset pagination) |
| GET | /api/devices/{id} | Get device by ID |
| PUT | /api/devices/{id} | Update device (name, description) |
| DELETE | /api/devices/{id} | Delete device |
| PUT | /api/devices/{id}/sensors | Assign sensors to device (idempotent) |

## DTOs

### CreateDeviceDto
```java
record CreateDeviceDto(
    @NotBlank String name,
    String description
)
```

### UpdateDeviceDto
```java
record UpdateDeviceDto(
    @NotBlank String name,
    String description
)
```

### AssignSensorsDto
```java
record AssignSensorsDto(
    @NotNull Set<Long> sensorIds
)
```

### DeviceDto (Response)
```java
record DeviceDto(
    Long id,
    String name,
    String description,
    Set<Long> sensorIds
)
```

## Service Methods
- `createDevice(Device)` - Create new device (validate unique name)
- `getDevices(Long lastId, int pageSize)` - Keyset pagination
- `getDeviceById(Long id)` - Get single device
- `updateDevice(Long id, Device device)` - Update device details
- `deleteDevice(Long id)` - Delete device (cascades to device_sensors)
- `assignSensors(Long deviceId, Set<Long> sensorIds)` - Idempotent sensor assignment

## Exception Handling
- 400 Bad Request - Invalid input (validation)
- 404 Not Found - Device not found
- 409 Conflict - Device with same name already exists

## Testing
- `DeviceControllerTest` - Controller unit tests with MockMvc
- `DeviceServiceTest` - Service unit tests with Mockito
- `JpaDeviceRepositoryIT` - Repository integration tests

## Implementation Order

1. **Database Migration** - Create `V<timestamp>__create_device_tables.sql`
2. **Entity** - Create `Device.java` with JPA mappings
3. **Repository** - Create `JpaDeviceRepository.java`
4. **Exceptions** - Create `DeviceNotFoundException.java`, `DeviceAlreadyExistsException.java`
5. **DTOs** - Create `CreateDeviceDto.java`, `UpdateDeviceDto.java`, `AssignSensorsDto.java`, `DeviceDto.java`
6. **Service** - Create `DeviceService.java`
7. **Controller** - Create `DeviceController.java`
8. **Configuration** - Create `DeviceConfiguration.java`
9. **Exception Handler** - Create `DeviceExceptionHandler.java`
10. **Tests** - Create controller, service, and repository tests
