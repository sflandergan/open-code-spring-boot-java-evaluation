# Devices Feature — Implementation Quality Evaluation

## Scoring Criteria

| # | Criterion | Description |
|---|-----------|-------------|
| 1 | **Completeness** | All required artifacts present and functional: migration, entity, repository, service, DTOs, controller, exception handler, configuration, AGENTS.md update |
| 2 | **Test Coverage** | JSON model tests, repository ITs, service unit tests, controller tests — all present, passing, and meaningful |
| 3 | **Compliance with AGENTS.md / Patterns** | Follows AGENTS.md rules and the linked pattern docs: no `@Service`/`@Component`, `@Configuration` wiring, package-protected repo, layering rule (SensorService not JpaSensorRepository), keyset pagination, GDPR logging, no unnecessary interface, DTO only in controller layer |
| 4 | **Code Quality** | Naming conventions, proper imports (no FQN inline), efficient DB queries (no unnecessary duplicate reads, no `findAll().stream().filter()`), correct ID generation, correct timestamp type, correct migration filename format |

All scores are on a 1–10 scale.

---

## Key Reference Points (Ground Truth)

- **Migration filename**: `V<YYYYMMDDHHmm>__description.sql` with **double underscore** (existing: `V202511141138__create_sensor_tables.sql`).
- **ID type**: The prompt chose UUID for devices (all implementations consistent). `sensors` table uses `BIGSERIAL`/`Long`.
- **Timestamp type**: `TIMESTAMPTZ` — the existing migration uses `TIMESTAMPTZ`.
- **No `@Service`/`@Component`/`@Repository`** — beans wired via `@Configuration`.
- **No unnecessary interface** — `DeviceService` as a plain class, no `Impl` suffix.
- **Package-protected repositories** — no access modifier on `JpaDeviceRepository`.
- **Layering rule** — `DeviceService` must use `SensorService` (not `JpaSensorRepository`) to validate sensor existence.
- **DTOs only in controller layer** — service methods accept/return entities, not DTOs.
- **GDPR** — never log personal data or user identifiers (user IDs, emails, etc.); device and sensor IDs are technical identifiers and may be logged.
- **Keyset pagination** preferred over offset.
- **Test subclass pattern** (`ENTITY_TEST_DATA.md`) — use a test subclass or constructor, never reflection.
- **AssertJ** assertions in all tests.

---

## Ranking Summary

| Rank | Model | Completeness | Test Coverage | Compliance | Code Quality | **Total** |
|------|-------|:------------:|:-------------:|:----------:|:------------:|:---------:|
| 1 | Claude Sonnet 4.6 | 8 | 9 | 8 | 9 | **34** |
| 2 | GLM 4.7 | 8 | 8 | 8 | 9 | **33** |
| 3 | Claude Haiku 4.5 | 7 | 8 | 7 | 8 | **30** |
| 4 | Claude Opus 4.6 | 6 | 8 | 6 | 8 | **28** |
| 5 | Kimi K2.5 | 7 | 5 | 8 | 7 | **27** |
| 6 | MiniMax 2.5 | 7 | 6 | 7 | 6 | **26** |
| 7 | Devstral | 5 | 7 | 6 | 6 | **24** |
| 8 | Devstral Small 2 (local) | 3 | 2 | 4 | 4 | **13** |
| 9 | Nemotron 3 Nano (local) | 5 | 1 | 2 | 3 | **11** |
| 10 | DeepSeek 3.2 | 2 | 1 | 2 | 3 | **8** |
| 11 | Qwen Turbo | 1 | 1 | 1 | 1 | **4** |

> **Note on Opus**: Full implementation but used `DataIntegrityViolationException` as a proxy for `SensorNotFoundException` instead of calling `SensorService`, bypassing the layering rule.
>
> **Note on Devstral**: Architecturally complete but `getDeviceSensors()` explicitly returns a hardcoded empty list with a placeholder comment — sensors are never returned from any device response.

---

## Detailed Per-Implementation Evaluation

---

### 1. Claude Sonnet 4.6
**Total: 34 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository (with keyset pagination queries), Service, DTOs (Create, Update, Device with `Sensor` list), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- No `GET /api/devices` list endpoint — only `GET /{deviceId}`. A list with pagination is expected.
- `UpdateDeviceDto` passed into `DeviceService.updateDevice()` — DTO leaks into the service layer, violating the rule that services work with entities only.

#### Test Coverage — 9/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` (JSON model tests), `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT` base class. AssertJ throughout.

**Issues (-1):**
- No JSON model test for the nested `SensorDto` used inside `DeviceDto`.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` (not `JpaSensorRepository`) to validate sensor existence — correct layering.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` (RFC 7807) for error responses.
- AGENTS.md updated.
- Keyset pagination in repository.

**Issues (-2):**
- `UpdateDeviceDto` passed to `DeviceService` — DTO in service layer violates AGENTS.md.
- Migration filename `V202511141200__create_device_tables.sql` reuses the same timestamp as the existing `V202511141138__create_sensor_tables.sql` test migration (seconds differ but minute resolution is the same). Not a hard Flyway conflict but shows the timestamp was not genuinely generated.

#### Code Quality — 9/10

**Strengths:**
- Clean, readable method names matching AGENTS.md style.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate` pattern matching `Sensor.java`.
- `TIMESTAMPTZ` in migration.
- Proper imports (no FQN inline).
- Efficient: uses `existsByIdDeviceIdAndIdSensorId` before saving — no unnecessary read-back.

**Issues (-1):**
- `UpdateDeviceDto.applyToEntity()` is called inside the service, meaning the DTO logic subtly bleeds past the controller boundary.

---

### 2. Claude Haiku 4.5
**Total: 30 / 40**

#### Completeness — 7/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device, `SensorSummaryDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-3):**
- No `GET /api/devices` list endpoint.
- `SensorNotFoundException` is not handled in `DeviceExceptionHandler` — if a sensor is not found during `assignSensor`, the generic 500 handler catches it.
- `CreateDeviceDto` and `UpdateDeviceDto` are passed into `DeviceService` — DTOs in service layer.
- **Sensor data never returned**: despite having `SensorSummaryDto` and `@OneToMany` on `Device`, the controller's `toDto(device)` calls `new DeviceDto(device)` which uses the no-sensors constructor — every device response returns an empty sensor list regardless of assignments.

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout.

**Issues (-2):**
- No test for `SensorNotFoundException` handling path (the gap in the exception handler).
- No `SensorSummaryDtoTest` — `SensorSummaryDto` is a new JSON model class without its own marshalling test.

#### Compliance — 7/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.
- Keyset pagination in repository.

**Issues (-3):**
- DTOs passed to service layer (`createDevice(CreateDeviceDto dto)`, `updateDevice(UUID id, UpdateDeviceDto dto)`).
- Does not use `SensorService` to validate sensor existence — `assignSensor` only checks `deviceSensorRepository`, no cross-feature sensor validation.
- Missing `SensorNotFoundException` in exception handler.

#### Code Quality — 8/10

**Strengths:**
- Clean, readable code following `Sensor.java` pattern closely.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- No FQN inline imports.

**Issues (-2):**
- Migration filename `V202511141200__create_device_tables.sql` uses a past timestamp.
- Sensor data never surfaced in API response — `@OneToMany` relationship and `SensorSummaryDto` were defined but the controller ignores them, making the sensor assignment feature partially invisible to API consumers.

---

### 3. GLM 4.7
**Total: 33 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get + list), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- No `@GeneratedValue` on entity `@Id` — UUID must be assigned manually, causing null `id` on persist unless `@PrePersist` assigns it (not shown).
- `getAllDevices()` returns `List<Device>` with no pagination — keyset pagination was planned but not wired into the controller list endpoint.

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout.

**Issues (-2):**
- RepositoryITs failing — the missing `@GeneratedValue` causes null UUIDs on persist, breaking constraint checks in tests.
- Flyway wrongly-named schema migration (reported from your findings) prevents ITs from running.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-2):**
- DTOs (`UpdateDeviceDto`) passed into `DeviceService.updateDevice()` — DTO in service layer.
- No keyset pagination implemented in list endpoint.

#### Code Quality — 9/10

**Strengths:**
- Clean, readable code overall.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Good method decomposition.
- **Best relationship mapping**: `DeviceSensor` has a `@ManyToOne` back to `Sensor`, and the controller's `toDto()` walks `device.getSensors()` to build a full `SensorDto` list from the JPA graph — the richest sensor response of any implementation.

**Issues (-1):**
- Missing `@GeneratedValue` on entity `@Id` — critical for correct JPA behaviour.

> **Note on Flyway schema name**: GLM's Flyway configuration references a wrong schema name, causing ITs to fail. This is counted once in Test Coverage (as the tests do not pass) but not as an additional Code Quality deduction.

---

### 4. Kimi K2.5
**Total: 27 / 40**

#### Completeness — 7/10

**Present:** Migration, Entity (Long ID with `@GeneratedValue(IDENTITY)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-3):**
- No `GET /api/devices` list endpoint.
- No JSON model DTO tests at all — `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` are absent.
- `UpdateDeviceDto` passed into `DeviceService` — DTO in service layer.

#### Test Coverage — 5/10

**Present:** `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`.

**Issues (-5):**
- No JSON model tests for DTOs (`CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` are absent).
- RepositoryITs failing due to transaction/static data handling issues.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-2):**
- `UpdateDeviceDto` passed to service layer.
- No keyset pagination in list operations.

#### Code Quality — 7/10

**Strengths:**
- `TIMESTAMPTZ` in migration — correct.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- `@GeneratedValue(IDENTITY)` used — correct for `BIGSERIAL` with `Long` ID.

**Issues (-3):**
- Uses `Long` for device ID (while all other models use UUID) — inconsistent with the feature design; the migration uses `BIGSERIAL` for device ID which conflicts with the many-to-many FK to a UUID-based sensor table.
- `getDeviceSensorIds()` uses `deviceSensorRepository.findAll().stream().filter()` — loads the **entire** device_sensors table into memory and filters in Java. This is a critical performance issue.
- Inconsistent sensor loading: some service methods load sensors via stream filter, others have no loading at all.

---

### 5. MiniMax 2.5
**Total: 26 / 40**

#### Completeness — 7/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-3):**
- No `@GeneratedValue` on `Device.id` — UUID must be assigned manually (same issue as GLM).
- No `GET /api/devices` list endpoint.
- No `JpaDeviceSensorRepositoryIT` — only `JpaDeviceRepositoryIT` present.

#### Test Coverage — 6/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`.

**Issues (-4):**
- No `JpaDeviceSensorRepositoryIT`.
- Missing `@GeneratedValue` will cause all ITs to fail on save (null UUID).
- Sensor mapping not loaded when returning device — `toDto()` only shows device fields, not assigned sensors, because `getDeviceSensors` is not called on the response path.

#### Compliance — 7/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-3):**
- `CreateDeviceDto` passed into `DeviceService.createDevice()` and `UpdateDeviceDto` passed into `updateDevice()` — DTOs in service layer.
- No keyset pagination.
- Missing `@GeneratedValue`.

#### Code Quality — 6/10

**Strengths:**
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.

**Issues (-4):**
- Missing `@GeneratedValue` — critical.
- Unnecessary read-back in `assignSensor`: after saving the assignment, performs `deviceRepository.findById(deviceId).orElseThrow()` to return a fresh device object — the device was already loaded at the top of the method and not mutated.
- Inconsistent indentation (4 spaces vs tabs mixed with the rest of the codebase).
- `DeviceDto.fromEntity()` maps `deviceSensors` to `SensorDto` but sets `name`, `type`, and `capabilities` all to `null` — the sensor data is structurally present in the JSON but entirely empty, making the sensor assignment feature misleading to API consumers.

---

### 6. Claude Opus 4.6
**Total: 28 / 40**

#### Completeness — 6/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device with nested `AssignedSensorDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-4):**
- No `GET /api/devices` list endpoint.
- No keyset pagination.
- `getDevice()` and `assignSensor()` make **two separate service calls** in the controller for device + sensors — results in two DB round-trips for what could be one.
- Sensor response only contains `AssignedSensorDto(sensorId, assignedAt)` — no sensor name, type, or capabilities returned. API consumers cannot identify a sensor from the device response without a separate lookup.

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Good scenario coverage in service and controller tests.

**Issues (-2):**
- `DeviceServiceTest` does not test the `getDeviceSensors` path thoroughly.
- `JpaDeviceRepositoryIT` only tests basic CRUD — no pagination or `existsByName` queries tested.

#### Compliance — 6/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-4):**
- Does **not** use `SensorService` to validate sensor existence — instead catches `DataIntegrityViolationException` from the FK constraint violation. This is an unconventional approach that bypasses the layering rule and couples service behaviour to DB exception types.
- `UpdateDeviceDto` passed into `DeviceService.updateDevice()` — DTO in service layer.
- No keyset pagination.
- No list endpoint.

#### Code Quality — 8/10

**Strengths:**
- Clean code structure, good method decomposition.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Proper imports — no FQN inline.
- `SensorNotFoundException` correctly handled in exception handler.

**Issues (-2):**
- `DataIntegrityViolationException` used as a proxy for sensor-not-found — fragile coupling to DB constraint behaviour.
- Migration file timestamp (`V202603151000`) appears to be today's date — which is fine operationally but confirms the model generated a future/arbitrary date rather than a real one.

---

### 7. Devstral
**Total: 24 / 40**

#### Completeness — 5/10

**Present:** Migration, Entity (UUID, no `@GeneratedValue`), Repository, Service (partially), DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-5):**
- **Critical**: `getDeviceSensors()` explicitly returns `List.of()` with a comment: *"This would need to fetch the actual sensor entities from a sensor service — For now, we'll return an empty list as a placeholder."* Sensors are never returned from the device response.
- No `@GeneratedValue` on `Device.id` — same issue as GLM and MiniMax.
- `TIMESTAMP` not `TIMESTAMPTZ` in migration.
- No `GET /api/devices` list endpoint.
- No `@Valid` on controller request bodies.

#### Test Coverage — 7/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`, `DeviceEntityTest`. Uses `RepositoryIT`. AssertJ throughout.

**Issues (-3):**
- `DeviceEntityTest` is a misplaced IT test (extends `RepositoryIT`) for entity persistence — unusual structure.
- `DeviceServiceTest` cannot meaningfully test sensor association since `getDeviceSensors` returns a hardcoded empty list.
- `JpaDeviceRepositoryIT` is sparse — only basic CRUD, no pagination queries.

#### Compliance — 6/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- AGENTS.md updated.
- `ProblemDetail` responses.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.

**Issues (-4):**
- Does not use `SensorService` — no cross-feature sensor validation.
- `TIMESTAMP` not `TIMESTAMPTZ` in migration.
- Missing named FK/PK constraints in migration SQL (bare `FOREIGN KEY` syntax).
- DTOs (`CreateDeviceDto`, `UpdateDeviceDto`) passed into service layer.

#### Code Quality — 6/10

**Strengths:**
- Readable structure and method decomposition.
- Proper imports.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.

**Issues (-4):**
- `getDeviceSensors()` returns empty list — fundamentally broken sensor retrieval.
- Missing `@GeneratedValue`.
- Missing `@Valid` on controller inputs.
- Missing named constraints in migration.

---

### 8. Devstral Small 2
**Total: 13 / 40**

#### Completeness — 3/10

**Present:** Migration (partial), Entity, Repository (with `existsByName`, keyset pagination stubs), `JpaDeviceSensorRepository`.

**Missing:** No `DeviceService`, no `DeviceController`, no DTOs, no `DeviceConfiguration`, no `DeviceExceptionHandler`, no AGENTS.md update. Only the data layer was implemented.

**Issues (-7):**
- Only 6 source classes total — data layer only.
- No service, controller, or exception handling.
- Migration uses single underscore (`V202603071400_create_device_tables.sql`) — should be double underscore (`__`).
- Migration references `sensor_id UUID` in `device_sensors` — but `sensors.id` is `BIGINT`, so the FK type is wrong.
- `TIMESTAMP` not `TIMESTAMPTZ`.

#### Test Coverage — 2/10

**Present:** `JpaDeviceRepositoryIT` — extends `RepositoryIT`, AssertJ, tests basic CRUD + pagination + timestamps. Reasonably thorough for what it covers.

**Issues (-8):**
- Only one test class for an incomplete implementation.
- No service tests, controller tests, or JSON model tests (no DTOs exist).
- The pagination test methods reference `findFirstPage(Pageable)` and `findNextPage(UUID, Pageable)` which are not valid derived-query method names in Spring Data JPA — these are custom queries that need `@Query` annotations not present in the repo.

#### Compliance — 4/10

**Strengths:**
- `@GeneratedValue(strategy = GenerationType.UUID)` on entity.
- Package-protected repositories.
- `Instant` timestamps.
- `@PrePersist`/`@PreUpdate` in entity.

**Issues (-6):**
- No `@Configuration` — no bean wiring possible with only data layer classes.
- No AGENTS.md update.
- Single underscore in migration filename.
- `TIMESTAMP` not `TIMESTAMPTZ`.
- No named FK/PK constraints.
- Wrong `sensor_id` type in migration (`UUID` vs `BIGINT`).

#### Code Quality — 4/10

**Strengths:**
- `@GeneratedValue` present.
- `Instant` timestamps.

**Issues (-6):**
- FQN usage: `private java.util.UUID id` — UUID used as FQN inline instead of imported.
- FQN usage: `public java.util.UUID getId()` — same issue.
- Wrong sensor_id type in migration.
- Single underscore in migration filename.
- Pagination queries (`findFirstPage`, `findNextPage`) are not valid derived-query names and would fail at runtime.

---

### 9. Nemotron 3 Nano
**Total: 11 / 40**

#### Completeness — 5/10

**Present:** Migration (two files — one wrongly named), Entity, Repository, Service, Controller, DTOs (Create, Update, Device), `GlobalExceptionHandler` (in wrong package), `JpaDeviceSensorRepository`.

**Issues (-5):**
- No `DeviceConfiguration` — `@Service` annotation used instead, violating AGENTS.md.
- Two duplicate Flyway migration files: `V20260301_01_create_device_tables.sql` (wrong format) and `V202603012013__create_device_tables.sql` (correct format but both present) — Flyway will fail on the wrongly named file.
- No AGENTS.md update.
- No `DeviceExceptionHandler` scoped to device controller — uses a generic `GlobalExceptionHandler` placed in a separate `de.sfl.global` package (outside feature package).
- `sensorId` in `DeviceSensorId` is `UUID` — but `sensors.id` is `BIGINT` (`Long`). The FK type is fundamentally wrong.
- No keyset pagination.

#### Test Coverage — 1/10

**Issues (-9):**
- No unit tests or integration tests of any kind.
- No JSON model tests.
- No repository ITs, service tests, or controller tests.

#### Compliance — 2/10

**Issues (-8):**
- `@Service` annotation on `DeviceService` — direct violation.
- `@RequiredArgsConstructor` (Lombok) used — not in the codebase.
- `GlobalExceptionHandler` in a separate `de.sfl.global` package — violates feature-package rule.
- `DeviceSensorId.sensorId` is `UUID` — should be `Long` to match `sensors.id BIGSERIAL`.
- Multiple Javadoc comments throughout — AGENTS.md says use them sparingly.
- `Device.builder()` pattern implies Lombok `@Builder` — not used in codebase.
- Duplicate Flyway files.
- No AGENTS.md update.

#### Code Quality — 3/10

**Issues (-7):**
- Lombok (`@Service`, `@RequiredArgsConstructor`, `@Builder`, `@Slf4j`) — not used in codebase.
- `sensorId` typed as `UUID` — wrong type relative to `sensors.id`.
- Duplicate migration files (one with wrong naming pattern).
- Verbose Javadoc comments contradict "self-documenting code" rule.
- `updateDevice()` does not return updated entity — void return makes API response impossible.
- `assignSensor` uses `UUID` sensorId parameter throughout, mismatching the `Long` type used everywhere else.

---

### 10. DeepSeek 3.2
**Total: 8 / 40**

#### Completeness — 2/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`), `DeviceSensor`, `DeviceSensorId`, `JpaDeviceRepository`, `CreateDeviceDto`, `UpdateDeviceDto`, `DeviceRepositoryInterface`.

**Missing:** No `DeviceService`, no `DeviceController`, no `DeviceDto`, no `DeviceConfiguration`, no `DeviceExceptionHandler`, no AGENTS.md update. Only partial data layer and a stray interface file.

**Issues (-8):**
- Aborted implementation — only 8 classes, less than half the required artifacts.
- `DeviceRepositoryInterface` is a confusingly named file — possibly the start of a service interface never completed.
- No AGENTS.md update.

#### Test Coverage — 1/10

**Issues (-9):**
- No test files of any kind.

#### Compliance — 2/10

**Strengths:**
- Migration present with `TIMESTAMPTZ`.
- Package structure correct.

**Issues (-8):**
- No `@Configuration`, no service, no controller — compliance cannot be evaluated beyond the data layer.
- `DeviceRepositoryInterface` suggests an interface-based service design was started — violation of the no-unnecessary-interface rule.
- No AGENTS.md update.
- Migration filename `V202503203454` uses a timestamp from March 2025 — not following the `YYYYMMDDHHmm` convention strictly (seconds digit `54` suggests `YYYYMMDDHHmmSS` was used).

#### Code Quality — 3/10

**Strengths:**
- `@GeneratedValue(UUID)` present.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps.

**Issues (-7):**
- Implementation is too incomplete to evaluate end-to-end quality.
- `TIMESTAMP` not `TIMESTAMPTZ` in device_sensors table (only devices table uses TIMESTAMPTZ).
- No named constraints in migration.
- FQN-style wildcard `jakarta.persistence.*` import.

---

### 11. Qwen Turbo
**Total: 4 / 40**

The model produced two structurally distinct, mutually incompatible attempts within the same branch, neither of which is placed in a valid Maven source tree.

#### Completeness — 1/10

**What was generated (in wrong locations):**

The model generated code across three separate directory roots, none of which correspond to a valid Maven project layout:

1. **`de/sfl/devices/` (root-level)** — A minimal CRUD-only implementation with no sensor-assignment capability: `Device.java` (entity), `DeviceDto.java`, `DeviceRepository.java`, `DeviceController.java`, `DeviceService.java`, three exception classes, `DevicesApplicationTests.java`. This set is missing a `DeviceConfiguration` and no migration file was written for this version.

2. **`src/main/java/de/sfl/devices/` (correct Maven root, wrong package depth)** — A second, entirely different implementation: `Device.java` (different model with `serialNumber`, `model` fields instead of `name`/`type`/`capabilities`), `DeviceSensor.java`, and a second `@SpringBootApplication` class `DevicesApplication.java`. This duplicate main class breaks the Spring Boot build — the project already has `OpenCodeSpringApplication` as its entry point, and having two `@SpringBootApplication` classes in the same scan path causes an ambiguous startup error.

3. **`de/sfl/sensors/` (root-level)** — A partial reimplementation of the existing sensors feature: `SensorService.java` (interface), `SensorServiceImpl.java`, `SensorRepository.java`, `SensorConfig.java`, `Sensor.java`. These duplicate (and conflict with) the existing `de.sfl.sensors` package already in the project.

The migration file (`V202511140900__create_devices_table.sql`) is placed correctly under `src/main/resources/db/migration/` but uses `TIMESTAMP` instead of `TIMESTAMPTZ` and embeds two `CREATE TABLE` statements in a single file — the second statement (`device_sensor_relationships`) is pasted inline rather than in a separate migration.

**Issues (-9):**
- No code is in a valid Maven source root. The primary output (`de/sfl/...`) was written to the repository root, not `src/main/java/` or `src/test/java/`.
- No sensor-assignment feature: neither implementation contains an assign-sensor endpoint, a `DeviceSensor` join entity in the correct package, or a `JpaDeviceSensorRepository`.
- No `DeviceConfiguration` — `@Autowired` field injection and `@Service`/`@Repository` annotations used throughout.
- A second `@SpringBootApplication` class (`DevicesApplication`) introduced into the live source tree breaks the build.
- The sensors feature was partially reimplemented from scratch, duplicating and conflicting with existing production code.
- No `GET /api/devices` list endpoint in the primary implementation (the `src/main/java` version has a different, incompatible domain model).
- No AGENTS.md update.

#### Test Coverage — 1/10

Three test files were generated, all non-functional:

- `DevicesApplicationTests.java` — a context-load smoke test placed in package `de.sfl.devices` (wrong package for the main application).
- `DeviceIntegrationTest.java` (in a `integration-test` sub-package) — asserts that a GET to `/api/devices/1` contains the literal string `"expectedContent"`, which is a placeholder and will never pass.
- `DeviceServiceTest.java` — attempts to mock `DeviceRepository` via `@MockBean` but the class references unresolved symbols (`DeviceRepository`, `Device`) with incorrect package paths; the method body calls `when(deviceRepository.findById(1L)).thenReturn(new Device())` but `thenReturn` expects an `Optional`, not a bare entity.

No JSON model tests, no repository integration tests extending `RepositoryIT`, no meaningful assertions. None of the test files would compile or pass.

#### Compliance — 1/10

**Issues (-9):**
- `@Autowired` field injection used in both `DeviceService` and `DeviceController` — violates the constructor injection requirement.
- `@Service` on `DeviceService` and `@Repository` on `DeviceRepository` — direct AGENTS.md violations; no `@Configuration` class exists.
- `DeviceRepository` is a public interface — not package-protected.
- `@RestControllerAdvice` on `DeviceExceptionHandler` is not scoped to `DeviceController.class`.
- Error responses return `ResponseEntity<String>` with plain text — not `ProblemDetail` (RFC 7807).
- No cross-feature sensor validation; the sensor-assignment feature is entirely absent.
- The exception classes carry `@ResponseStatus` annotations alongside the `@RestControllerAdvice` handler — redundant and inconsistent.
- Excessive Javadoc on exception classes and the exception handler — contradicts the "sparingly" rule.
- AGENTS.md not updated.
- `DeviceTestData.java` is placed in a package named `de.sfl.devices.test-data` — a hyphen is not a valid Java package name segment; this file would not compile.

#### Code Quality — 1/10

**Issues (-9):**
- The two implementations use incompatible domain models: the `src/de/...` version uses `name`/`type`/`capabilities` (matching the sensor entity); the `src/main/java/...` version uses `serialNumber`/`model` — both exist in the branch simultaneously.
- `javax.persistence.*` imports used in the `src/de/...` entity — the project uses Jakarta EE (`jakarta.persistence.*`), not the legacy `javax` namespace. This would cause a compilation failure on Java 17+/Spring Boot 3.x.
- `LocalDateTime` used for timestamps — the codebase standard is `Instant` with `TIMESTAMPTZ` in the DB.
- `TIMESTAMP` (not `TIMESTAMPTZ`) in the migration.
- Two `CREATE TABLE` statements in a single migration file; the second table (`device_sensor_relationships`) is not a valid devices-feature migration — it references `sensors(id)` with no `BIGINT` type alignment guard and uses bare `FOREIGN KEY` syntax without named constraints.
- Migration timestamp `V202511140900` collides with the existing sensor migration (`V202511141138`) namespace, and the timestamp is not a genuine generation time.
- `getAllDevices()` in `DeviceService` uses `deviceRepository.findAll().stream().map(...)` — no pagination.
- `updateDevice()` returns `null` on not-found rather than throwing a typed exception — the exception classes defined (`DeviceNotFoundException`) are never used in the service or controller.
- `DeviceTestData.createDevice()` calls a three-argument constructor `new Device("Test Device", "TypeA", "1234567890")` but the `Device` entity has no such constructor.
- The `src/main/java/de/sfl/devices/Device.java` model calls `sensors.add(sensor)` in `addSensor()` without null-checking the `sensors` set, which will throw `NullPointerException` at runtime since `sensors` is never initialised.
- Verbose `toString()`, `equals()`, `hashCode()` generated manually on the entity — inconsistent with the codebase style using records or omitting these methods.

---

## Cross-Implementation Analysis

### Common Strengths Across Top Implementations (Sonnet, Haiku, GLM, Kimi, MiniMax)

- All chose UUID for device IDs — a reasonable independent choice given the feature was new.
- All used `TIMESTAMPTZ` in migration (except Devstral which used `TIMESTAMP`).
- All used `@Configuration` for bean wiring — no `@Service`/`@Component`.
- All updated `AGENTS.md`.
- All followed the `@PrePersist`/`@PreUpdate` and `Instant` pattern from `Sensor.java`.
- All provided `ProblemDetail` RFC 7807 responses.
- All scoped `@RestControllerAdvice` to `DeviceController.class`.

### Recurring Issues Across Multiple Implementations

| Issue | Implementations Affected |
|-------|--------------------------|
| DTOs passed to service layer | Sonnet, Haiku, GLM, Kimi, MiniMax, Devstral, Opus |
| No `GET /api/devices` list endpoint | Sonnet, Haiku, Kimi, MiniMax, Opus, Devstral |
| Missing `@GeneratedValue` on UUID entity | GLM, MiniMax, Devstral |
| `TIMESTAMP` instead of `TIMESTAMPTZ` | Devstral, Devstral-Small |
| Missing named FK/PK constraints | Devstral, Devstral-Small, DeepSeek |
| Single underscore in migration filename | Devstral-Small (single `_`) |
| Wrong `sensorId` type in junction table | Nemotron, Devstral-Small |
| Sensor loading returns empty list | Devstral |
| No JSON model DTO tests | Kimi |
| `@Service`/Lombok | Nemotron |

### Sensor Response Quality

A key differentiator across implementations is whether assigned sensor data is meaningfully surfaced in the API response. There is a clear spectrum:

| Quality | Model | Sensor data returned |
|---------|-------|----------------------|
| Full sensor details | Sonnet, GLM | Full `SensorDto` (id, name, type, capabilities) via `SensorService` or JPA graph traversal |
| Partial data | Opus | `AssignedSensorDto(sensorId, assignedAt)` — identifies which sensor but no details |
| Broken / empty | Haiku | `@OneToMany` defined but `toDto()` ignores it — always returns empty list |
| Broken / empty | MiniMax | Sensor IDs mapped but name/type/capabilities all null |
| Broken / placeholder | Devstral | `List.of()` hardcoded in service with an explicit TODO comment |
| IDs only | Kimi | `List<Long> sensorIds` — no sensor details |

**Sonnet** resolves sensors through `SensorService`, which is the architecturally correct approach (respects the cross-feature service boundary). **GLM** uses JPA relationship traversal (`@ManyToOne` on `DeviceSensor` → `Sensor`), which is also a valid approach and produces the richest graph in a single DB query. Both are valid; Sonnet's approach is more aligned with the AGENTS.md layering rules since it goes through the service boundary.

### Key Differentiators

- **Sonnet** is the most complete working implementation. Its main weakness is the DTO-in-service pattern (present in nearly all models). It is the only model that correctly used `SensorService` for cross-feature sensor validation.
- **GLM** rose to #2 on the strength of its relationship mapping (full JPA graph, richest sensor response) and broad test coverage. Its showstopper bugs (missing `@GeneratedValue`, Flyway schema misconfiguration) prevent the tests from passing, but the code structure and design quality are high.
- **Haiku** fell to #3 due to a subtle but critical bug: despite defining `SensorSummaryDto` and a full `@OneToMany` relationship, the controller always calls the no-sensors constructor — the feature is architecturally present but functionally broken for API consumers.
- **Opus** is architecturally unusual — it chose a `DataIntegrityViolationException` catch as a proxy for sensor validation, which is fragile and bypasses the layering rule. Despite being a top-tier plan producer, the implementation reflects less codebase alignment than Sonnet or GLM.
- **Kimi** suffers from a critical performance bug (`findAll().stream().filter()`) and missing DTO tests, but the core structure is otherwise sound.
- **MiniMax** has the unnecessary-read-back bug in `assignSensor`, missing `@GeneratedValue`, and a misleading sensor response that includes structurally correct but data-empty sensor objects.
- **Devstral (cloud)** produced working code structure but with the sensor retrieval left as a placeholder — a fundamental functional gap acknowledged in the code itself.
- **Local models (Devstral-Small, Nemotron, Qwen)** all failed to produce complete implementations. Devstral-Small produced a reasonable data layer; Nemotron produced a structurally broken implementation with wrong types; Qwen produced two incompatible partial implementations in invalid directory locations — neither compiled, both violated foundational AGENTS.md rules, and the model partially reimplemented the existing sensors feature from scratch using the wrong Jakarta namespace.

### Migration Filename Compliance

All models that produced a migration used the double-underscore separator correctly **except Devstral-Small** (`V202603071400_create_device_tables.sql` — single underscore). The timestamp format was generally correct though some used past dates suggesting copy-paste from existing migrations rather than generating a fresh timestamp.

### The DTO-in-Service Anti-Pattern

The most pervasive single violation across all models is passing DTOs into the service layer. Only **Sonnet** used `SensorService` correctly for cross-feature validation. Every model that produced a service leaked at least one DTO type into it. This suggests that while models read and understood the instruction *"Services should work with the entity model"*, they did not consistently apply it to all service methods — particularly `updateDevice`.