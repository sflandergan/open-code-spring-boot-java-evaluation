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
- **GDPR** — never log IDs.
- **Keyset pagination** preferred over offset.
- **Test subclass pattern** (`ENTITY_TEST_DATA.md`) — use a test subclass or constructor, never reflection.
- **AssertJ** assertions in all tests.

---

## Ranking Summary

| Rank | Model | Completeness | Test Coverage | Compliance | Code Quality | **Total** |
|------|-------|:------------:|:-------------:|:----------:|:------------:|:---------:|
| 1 | Claude Sonnet 4.6 | 8 | 9 | 8 | 8 | **33** |
| 2 | Claude Haiku 4.5 | 8 | 8 | 7 | 8 | **31** |
| 3 | GLM 4.7 | 8 | 8 | 6 | 7 | **29** |
| 4 | Claude Opus 4.6 *(UUID/no SensorService)* | 6 | 8 | 5 | 7 | **26** |
| 5 | Kimi K2.5 | 7 | 5 | 7 | 6 | **25** |
| 5 | MiniMax 2.5 | 7 | 6 | 6 | 6 | **25** |
| 6 | Devstral *(sensors placeholder)* | 5 | 7 | 6 | 6 | **24** |
| 7 | Devstral Small 2 (local) | 3 | 2 | 4 | 4 | **13** |
| 8 | Nemotron 3 Nano (local) | 4 | 1 | 2 | 3 | **10** |
| 9 | DeepSeek 3.2 | 2 | 1 | 2 | 3 | **8** |
| 10 | GPT-OSS (local) | 1 | 1 | 1 | 2 | **5** |
| 11 | Qwen Turbo | 1 | 1 | 1 | 1 | **4** |

> **Note on Opus**: Full implementation but chose UUID device IDs (UUID is consistent end-to-end in its implementation) and used `DataIntegrityViolationException` as a proxy for `SensorNotFoundException` instead of calling `SensorService`, bypassing the layering rule.
>
> **Note on Devstral**: Architecturally complete but `getDeviceSensors()` explicitly returns a hardcoded empty list with a placeholder comment — sensors are never returned from any device response.

---

## Detailed Per-Implementation Evaluation

---

### 1. Claude Sonnet 4.6 (`gh-sonnet-4.6/device-feature`)
**Total: 33 / 40**

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

#### Code Quality — 8/10

**Strengths:**
- Clean, readable method names matching AGENTS.md style.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate` pattern matching `Sensor.java`.
- `TIMESTAMPTZ` in migration.
- Proper imports (no FQN inline).
- Efficient: uses `existsByIdDeviceIdAndIdSensorId` before saving — no unnecessary read-back.

**Issues (-2):**
- GDPR: `logger.info("Created device with id: {}, name: {}", ...)` — logs the device ID.
- `UpdateDeviceDto.applyToEntity()` is called inside the service, meaning the DTO logic subtly bleeds past the controller boundary.

---

### 2. Claude Haiku 4.5 (`gh-haiku-4.5/device-feature`)
**Total: 31 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device, `SensorSummaryDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- No `GET /api/devices` list endpoint.
- `SensorNotFoundException` is not handled in `DeviceExceptionHandler` — if a sensor is not found during `assignSensor`, the generic 500 handler catches it. (You noted this as a manually found issue.)
- `CreateDeviceDto` and `UpdateDeviceDto` are passed into `DeviceService` — DTOs in service layer.

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
- Does not use `SensorService` to validate sensor existence — `assignSensor` only checks `deviceSensorRepository`, no cross-feature sensor validation. The sensor FK constraint is relied on to catch invalid sensor IDs.
- Missing `SensorNotFoundException` in exception handler.

#### Code Quality — 8/10

**Strengths:**
- Clean, readable code following `Sensor.java` pattern closely.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- No FQN inline imports.

**Issues (-2):**
- GDPR: `logger.info("Created device with id: {}, name: {}", ...)` — logs the device ID.
- Migration filename `V202511141200__create_device_tables.sql` uses a past timestamp (same minute-resolution as if copied from sensor migration timestamp).

---

### 3. GLM 4.7 (`rq-glm-4.7/devices-features`)
**Total: 29 / 40**

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

#### Compliance — 6/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-4):**
- GDPR: Multiple `logger.info("Created device with id: {}", ...)` and `logger.debug("Retrieving device with id: {}", id)` — logs device IDs.
- DTOs (`UpdateDeviceDto`) passed into `DeviceService.updateDevice()` — DTO in service layer.
- No keyset pagination implemented in list endpoint.
- Flyway wrong schema name (reported as causing IT failure).

#### Code Quality — 7/10

**Strengths:**
- Clean, readable code overall.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Good method decomposition.

**Issues (-3):**
- Missing `@GeneratedValue` on entity `@Id` — critical for correct JPA behaviour.
- GDPR logging violations.
- `SensorDto` from the `sensors` package imported and reused in `DeviceController` — creates coupling to the `sensors` package's DTO directly in the `devices` controller. Should use a dedicated device-level sensor representation.

---

### 4. Kimi K2.5 (`rq-kimi-k2.5/devices-feature`)
**Total: 25 / 40**

#### Completeness — 7/10

**Present:** Migration, Entity (Long ID with `@GeneratedValue(IDENTITY)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-3):**
- No `GET /api/devices` list endpoint.
- No JSON model DTO tests at all — `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` are absent.
- `UpdateDeviceDto` passed into `DeviceService` — DTO in service layer.

#### Test Coverage — 5/10

**Present:** `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`.

**Issues (-5):**
- No JSON model tests for any of the three DTOs.
- RepositoryITs are reported as failing — messed-up transaction and static test data handling in ITs.
- `JpaDeviceSensorRepositoryIT` missing (from the file list — only `JpaDeviceRepositoryIT` confirmed).

Wait — from the file list: both `JpaDeviceRepositoryIT` and `JpaDeviceSensorRepositoryIT` are present. Revising the IT test issue to focus on the transaction failures.

**Issues (-5):**
- No JSON model tests for DTOs.
- RepositoryITs failing due to transaction/static data handling issues.

#### Compliance — 7/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-3):**
- `UpdateDeviceDto` passed to service layer.
- GDPR: logs device IDs in service (`logger.info("Created device with id: {}", ...)`, `logger.info("Assigned sensor {} to device {}", ...)`).
- No keyset pagination in list operations.

#### Code Quality — 6/10

**Strengths:**
- `TIMESTAMPTZ` in migration — correct.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- `@GeneratedValue(IDENTITY)` used — correct for `BIGSERIAL` with `Long` ID.

**Issues (-4):**
- Uses `Long` for device ID (while all other models use UUID) — inconsistent with the feature design; the migration uses `BIGSERIAL` for device ID which conflicts with the many-to-many FK to a UUID-based sensor table.
- `getDeviceSensorIds()` uses `deviceSensorRepository.findAll().stream().filter()` — loads the **entire** device_sensors table into memory and filters in Java. This is a critical performance issue.
- GDPR logging violations.
- Inconsistent sensor loading: some service methods load sensors via stream filter, others have no loading at all.

---

### 5. MiniMax 2.5 (`rq-minimax-2.5/devices-feature`)
**Total: 25 / 40**

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

#### Compliance — 6/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-4):**
- `CreateDeviceDto` passed into `DeviceService.createDevice()` and `UpdateDeviceDto` passed into `updateDevice()` — DTOs in service layer.
- GDPR: logs device ID in service.
- No keyset pagination.
- Missing `@GeneratedValue`.

#### Code Quality — 6/10

**Strengths:**
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.

**Issues (-4):**
- Missing `@GeneratedValue` — critical.
- Unnecessary read-back in `assignSensor`: after saving the assignment, performs `deviceRepository.findById(deviceId).orElseThrow()` to return a fresh device object — the device was already loaded at the top of the method and not mutated.
- GDPR logging violations.
- Inconsistent indentation (4 spaces vs tabs mixed with the rest of the codebase).

---

### Claude Opus 4.6 (`gh-opus-4.6/devices-feature`)
**Total: 26 / 40** *(ranked separately — see note)*

#### Completeness — 6/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device with nested `AssignedSensorDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-4):**
- Uses **UUID** for device ID with `@GeneratedValue(strategy = GenerationType.UUID)` — while this works independently, the codebase uses `BIGSERIAL`/`Long` for sensors and the plan evaluation noted this as a key codebase fit issue. In the implementation, this design choice is at least consistent end-to-end.
- No `GET /api/devices` list endpoint.
- No keyset pagination.
- `getDevice()` and `assignSensor()` make **two separate service calls** in the controller for device + sensors — this is correct but results in two DB round-trips for what could be one.

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Good scenario coverage in service and controller tests.

**Issues (-2):**
- `DeviceServiceTest` does not test the `getDeviceSensors` path thoroughly.
- `JpaDeviceRepositoryIT` only tests basic CRUD — no pagination or `existsByName` queries tested.

#### Compliance — 5/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-5):**
- Does **not** use `SensorService` to validate sensor existence — instead catches `DataIntegrityViolationException` from the FK constraint violation. This is an unconventional approach that bypasses the layering rule and couples service behaviour to DB exception types.
- `UpdateDeviceDto` passed into `DeviceService.updateDevice()` — DTO in service layer.
- GDPR: logs device ID in exception message (`DeviceNotFoundException` contains the ID, logged via `ex.getMessage()`).
- No keyset pagination.
- No list endpoint.

#### Code Quality — 7/10

**Strengths:**
- Clean code structure, good method decomposition.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Proper imports — no FQN inline.
- `SensorNotFoundException` correctly handled in exception handler.

**Issues (-3):**
- `DataIntegrityViolationException` used as a proxy for sensor-not-found — fragile coupling to DB constraint behaviour.
- GDPR violation in exception handler logging (`ex.getMessage()` which contains the ID).
- Migration file timestamp (`V202603151000`) appears to be today's date — which is fine operationally but confirms the model generated a future/arbitrary date rather than a real one.

---

### Devstral (`rq-devstral/devices-feature`)
**Total: 24 / 40** *(listed separately — sensor loading is a placeholder)*

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
- GDPR: logs device and sensor IDs.

#### Code Quality — 6/10

**Strengths:**
- Readable structure and method decomposition.
- Proper imports.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.

**Issues (-4):**
- `getDeviceSensors()` returns empty list — fundamentally broken sensor retrieval.
- Missing `@GeneratedValue`.
- Missing `@Valid` on controller inputs.
- GDPR logging violations.
- Missing named constraints in migration.

---

### 7. Devstral Small 2 (`devstral-small-2/device-feature`)
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

### 8. Nemotron 3 Nano (`nemotron-3-nano/devices-feature`)
**Total: 10 / 40**

#### Completeness — 4/10

**Present:** Migration (two files — one wrongly named), Entity, Repository, Service, Controller, DTOs (Create, Update, Device), `GlobalExceptionHandler` (in wrong package), `JpaDeviceSensorRepository`.

**Issues (-6):**
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

### 9. DeepSeek 3.2 (`rq-deepseek-3.2/devices-feature`)
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

### 10. GPT-OSS (`gpt-oss/devices-feature`)
**Total: 5 / 40**

#### Completeness — 1/10

**Issues (-9):**
- Branch contains only a `docs/devices-feature.md` plan file. No source code at all.
- You noted "5 partially implemented classes" — this may refer to code in the plan document rather than actual Java files. Git diff confirms no `src/` changes.

#### Test Coverage — 1/10
No code, no tests.

#### Compliance — 1/10
No code to evaluate.

#### Code Quality — 2/10
The plan document exists but no implementation was produced.

---

### 11. Qwen Turbo (`rq-qwen-turbo/devices-feature`)
**Total: 4 / 40**

#### Completeness — 1/10

**Issues (-9):**
- Branch has no changes to `src/` at all — no Java files, no migration.
- You noted "Wrote source code partially to root instead of src / Added second main application breaking build" — confirming the implementation is structurally broken and no valid code reached the feature branch.

#### Test Coverage — 1/10
No code, no tests.

#### Compliance — 1/10
No code to evaluate.

#### Code Quality — 1/10
No code to evaluate.

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
| GDPR: logging device/sensor IDs | Sonnet, Haiku, GLM, Kimi, MiniMax, Devstral |
| No `GET /api/devices` list endpoint | Sonnet, Haiku, Kimi, MiniMax, Opus, Devstral |
| Missing `@GeneratedValue` on UUID entity | GLM, MiniMax, Devstral |
| `TIMESTAMP` instead of `TIMESTAMPTZ` | Devstral, Devstral-Small |
| Missing named FK/PK constraints | Devstral, Devstral-Small, DeepSeek |
| Single underscore in migration filename | Devstral-Small (single `_`) |
| Wrong `sensorId` type in junction table | Nemotron, Devstral-Small |
| Sensor loading returns empty list | Devstral |
| No JSON model DTO tests | Kimi |
| `@Service`/Lombok | Nemotron |

### Key Differentiators

- **Sonnet** is the most complete working implementation. Its main weaknesses are the DTO-in-service pattern (present in nearly all models) and the GDPR logging violation. It is the only model that correctly used `SensorService` for cross-feature sensor validation.
- **Haiku** is close behind Sonnet and demonstrates that the model learned from the plan's mistakes (no Lombok, UUID IDs). Its main gap is the missing `SensorNotFoundException` handler and the DTO-in-service pattern.
- **GLM** has excellent test coverage breadth but the missing `@GeneratedValue` is a showstopper bug that will break all repository ITs. The GDPR logging is the most extensive across all models.
- **Opus** is architecturally unusual — it chose a `DataIntegrityViolationException` catch as a proxy for sensor validation, which is fragile and bypasses the layering rule. Despite being a top-tier plan producer, the implementation reflects less codebase alignment than Sonnet or Haiku.
- **Kimi** suffers from a critical performance bug (`findAll().stream().filter()`) and missing DTO tests, but the core structure is otherwise sound.
- **MiniMax** has the unnecessary-read-back bug in `assignSensor` and missing `@GeneratedValue`, but otherwise follows patterns correctly.
- **Devstral (cloud)** produced working code structure but with the sensor retrieval left as a placeholder — a fundamental functional gap acknowledged in the code itself.
- **Local models (Devstral-Small, Nemotron, GPT-OSS)** all failed to produce complete implementations. Devstral-Small produced a reasonable data layer; Nemotron produced a structurally broken implementation with wrong types; GPT-OSS and Qwen produced no working code.

### Migration Filename Compliance

All models that produced a migration used the double-underscore separator correctly **except Devstral-Small** (`V202603071400_create_device_tables.sql` — single underscore). The timestamp format was generally correct though some used past dates suggesting copy-paste from existing migrations rather than generating a fresh timestamp.

### The DTO-in-Service Anti-Pattern

The most pervasive single violation across all models is passing DTOs into the service layer. Only **Sonnet** used `SensorService` correctly for cross-feature validation. Every model that produced a service leaked at least one DTO type into it. This suggests that while models read and understood the instruction *"Services should work with the entity model"*, they did not consistently apply it to all service methods — particularly `updateDevice`.
