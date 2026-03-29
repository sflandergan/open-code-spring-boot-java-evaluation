# Device Management Feature

## Overview
The *Device* feature provides CRUD capability for **devices** and allows assigning sensors to a device.  The API is intentionally REST‑style with clear separation of concerns: controller → service → repository.

## Domain Model
```java
@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // One‑to‑many relationship to sensors
    @OneToMany(mappedBy = "device")
    private Set<Sensor> sensors = new HashSet<>();
}
```

### DTOs
- `DeviceDto` for create/update requests.
- `DeviceResponseDto` for responses.

## Repository Layer
```java
@Repository
interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByName(String name);
}
```

## Service Layer
```
@Service
public class DeviceService {
    public Device create(DeviceDto dto) { … }
    public Device update(Long id, DeviceDto dto) { … }
    public void delete(Long id) { … }
    public Device assignSensors(Long deviceId, Set<Long> sensorIds) { … }
}
```

## REST API (Spring MVC)
| HTTP Method | Path | Description |
|------------|------|-----------|
| POST | `/api/devices` | Create a new device |
| GET | `/api/devices/{id}` | Retrieve a single device |
| PUT | `/api/devices/{id}` | Update a device |
| DELETE | `/api/devices/{id}` | Delete a device |
| PUT | `/api/devices/{id}/sensors` | Assign sensors to a device (idempotent) |

## Idempotent PUT for assigning sensors
* The request body contains a list of sensor ids.  The operation must be idempotent: repeating the same request keeps the same result.

## Validation Rules
- `name` must be non‑blank and unique per user |
- `description` optional |

## Tests
* Unit tests for service – mock repository |
* Integration tests for controller using `@WebMvcTest` |

## Deployment Notes
- Add API docs via SpringDoc.|
