# Devices Feature — Implementation Quality Evaluation

## Scoring Criteria

| # | Criterion | Description |
|---|-----------|-------------|
| 1 | **Completeness** | All required artifacts present and functional: migration, entity, repository, service, DTOs, controller, exception handler, configuration, AGENTS.md update |
| 2 | **Test Coverage** | JSON model tests, repository ITs, service unit tests, controller tests — all present, passing, and meaningful |
| 3 | **Compliance with AGENTS.md / Patterns** | Follows AGENTS.md rules and the linked pattern docs: no `@Service`/`@Component`, `@Configuration` wiring, package-protected repo, layering rule (SensorService not JpaSensorRepository), GDPR logging, no unnecessary interface, DTO only in controller layer |
| 4 | **Code Quality** | Naming conventions, proper imports (no FQN inline), efficient DB queries (no unnecessary duplicate reads, no `findAll().stream().filter()`), correct ID generation, correct timestamp type, correct migration filename format, `toEntity()` implemented on the DTO (AGENTS.md mandates this; outbound `toDto` direction is not required by AGENTS.md) |

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
- **DTO transformation placement** — AGENTS.md mandates a `toEntity()` method on inbound DTOs (e.g., `dto.toEntity()`). The outbound direction (`DeviceDto.from(entity)`) is a good practice but is not required by AGENTS.md and is not scored.

---

## Ranking Summary

| Rank | Model | Completeness | Test Coverage | Compliance | Code Quality | **Total** |
|------|-------|:------------:|:-------------:|:----------:|:------------:|:---------:|
| 1 | GPT 5.4 (GitHub Copilot) | 10 | 9 | 9 | 9 | **37** |
| 1 | Claude Sonnet 4.6 | 10 | 9 | 9 | 9 | **37** |
| 3 | GPT 5.4 (ChatGPT) | 10 | 10 | 8 | 8 | **36** |
| 3 | GPT 5.3 Codex | 9 | 10 | 9 | 8 | **36** |
| 5 | GLM 4.7 | 9 | 8 | 9 | 9 | **35** |
| 5 | GPT 5.3 Codex Extra-High | 10 | 9 | 7 | 9 | **35** |
| 7 | GPT 5.3 Codex High | 9 | 9 | 8 | 8 | **34** |
| 8 | GPT 5.3 Codex Low | 9 | 7 | 9 | 8 | **33** |
| 9 | Claude Opus 4.6 | 8 | 8 | 8 | 8 | **32** |
| 10 | Claude Haiku 4.5 | 8 | 8 | 7 | 8 | **31** |
| 11 | Kimi K2.5 | 8 | 5 | 9 | 7 | **29** |
| 12 | MiniMax 2.5 | 8 | 6 | 8 | 6 | **28** |
| 13 | Devstral | 6 | 7 | 6 | 6 | **25** |
| 14 | Devstral Small 2 (local) | 3 | 2 | 4 | 4 | **13** |
| 15 | Nemotron 3 Nano (local) | 6 | 1 | 2 | 3 | **12** |
| 16 | DeepSeek 3.2 | 2 | 1 | 2 | 3 | **8** |
| 17 | Qwen Turbo | 1 | 1 | 1 | 1 | **4** |

### Key Differences by Category

- **Top scorers**: GPT 5.4 (GitHub Copilot) and Claude Sonnet 4.6 tie at 37/40. Copilot has the strongest completeness and JPA relationship mapping but bypasses `SensorService` via `EntityManager.find(...)`. Sonnet is the only model to correctly use `SensorService` for cross-feature validation.
- **High-quality tier**: GPT 5.4 (ChatGPT) and GPT 5.3 Codex tie at rank 3 with 36/40. GPT 5.4 (ChatGPT) has the best JSON-model test coverage; GPT 5.3 Codex achieves perfect test coverage but bypasses `SensorService` via a JPQL query directly against the `Sensor` entity. GLM 4.7 and GPT 5.3 Codex Extra-High follow at 35/40; GLM is the strongest pay-per-use option with the richest JPA sensor graph; Codex Extra-High has the most thorough tests of any GPT model but carries a critical layering violation (`JpaSensorLookupRepository`). GPT 5.3 Codex High trails at 34/40.
- **Viable but clearly flawed**: Claude Opus 4.6, Claude Haiku 4.5, Kimi K2.5, MiniMax 2.5, and Devstral produce mostly complete implementations, but each has a significant weakness such as DB-exception-based validation, empty sensor responses, major performance issues, incomplete sensor payloads, or placeholder logic.
- **Not viable for this workflow**: Devstral Small 2, Nemotron 3 Nano, DeepSeek 3.2, and Qwen Turbo fail to deliver a complete, reliable feature. The dominant failure modes are partial implementations, missing tests, broken schema choices, invalid project structure, or tool-call breakdowns.

---

## Detailed Per-Implementation Evaluation

---

### 1. Claude Sonnet 4.6
**Total: 36 / 40**

#### Completeness — 10/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository (with keyset pagination queries), Service, DTOs (Create, Update, Device with `Sensor` list), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

#### Test Coverage — 9/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` (JSON model tests), `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT` base class. AssertJ throughout.

**Issues (-1):**
- No JSON model test for the nested `SensorDto` used inside `DeviceDto`.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` (not `JpaSensorRepository`) to validate sensor existence — correct layering.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` (RFC 7807) for error responses.
- AGENTS.md updated.
- Keyset pagination in repository.

**Issues (-1):**
- `UpdateDeviceDto` passed to `DeviceService` — DTO in service layer violates AGENTS.md.

#### Code Quality — 9/10

**Strengths:**
- Clean, readable method names matching AGENTS.md style.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate` pattern matching `Sensor.java`.
- `TIMESTAMPTZ` in migration.
- Proper imports (no FQN inline).
- Efficient: uses `existsByIdDeviceIdAndIdSensorId` before saving — no unnecessary read-back.

**Issues (-1):**
- Migration filename `V202511141200__create_device_tables.sql` reuses the same timestamp minute as the existing `V202511141138__create_sensor_tables.sql` — not a hard Flyway conflict but shows the timestamp was not genuinely generated.

---

### 2. Claude Haiku 4.5
**Total: 31 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity (UUID, `@GeneratedValue(UUID)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device, `SensorSummaryDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
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
- `DeviceDto(Device device, List<SensorSummaryDto> sensors)` constructor — transformation logic lives in the DTO, the controller's `toDto()` method is just a thin wrapper delegating to the DTO constructor.

**Issues (-2):**
- Migration filename `V202511141200__create_device_tables.sql` uses a past timestamp.
- Sensor data never surfaced in API response — `@OneToMany` relationship and `SensorSummaryDto` were defined but the controller ignores them by calling the no-sensors constructor, making the sensor assignment feature partially invisible to API consumers.

---

### 3. GLM 4.7
**Total: 35 / 40**

#### Completeness — 9/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get + list), Exception Handler, Configuration, AGENTS.md update.

**Issues (-1):**
- No `@GeneratedValue` on entity `@Id` — UUID must be assigned manually, causing null `id` on persist unless `@PrePersist` assigns it (not shown).

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout.

**Issues (-2):**
- RepositoryITs failing — the missing `@GeneratedValue` causes null UUIDs on persist, breaking constraint checks in tests.
- Flyway wrongly-named schema migration (reported from your findings) prevents ITs from running.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-1):**
- DTOs (`UpdateDeviceDto`) passed into `DeviceService.updateDevice()` — DTO in service layer.

#### Code Quality — 9/10

**Strengths:**
- Clean, readable code overall.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Good method decomposition.
- **Best relationship mapping**: `DeviceSensor` has a `@ManyToOne` back to `Sensor`, enabling navigation from device to full `SensorDto` list via the JPA graph — the richest sensor response of any implementation.

**Issues (-1):**
- Missing `@GeneratedValue` on entity `@Id` — critical for correct JPA behaviour.

> **Note on Flyway schema name**: GLM's Flyway configuration references a wrong schema name, causing ITs to fail. This is counted once in Test Coverage (as the tests do not pass) but not as an additional Code Quality deduction.

---

### 4. Kimi K2.5
**Total: 29 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity (Long ID with `@GeneratedValue(IDENTITY)`, `@PrePersist`/`@PreUpdate`, `Instant`), Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- No JSON model DTO tests at all — `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` are absent.
- `UpdateDeviceDto` passed into `DeviceService` — DTO in service layer.

#### Test Coverage — 5/10

**Present:** `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`.

**Issues (-5):**
- No JSON model tests for DTOs (`CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` are absent).
- RepositoryITs failing due to transaction/static data handling issues.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-1):**
- `UpdateDeviceDto` passed to service layer.

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
**Total: 28 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- No `@GeneratedValue` on `Device.id` — UUID must be assigned manually (same issue as GLM).
- No `JpaDeviceSensorRepositoryIT` — only `JpaDeviceRepositoryIT` present.

#### Test Coverage — 6/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`.

**Issues (-4):**
- No `JpaDeviceSensorRepositoryIT`.
- Missing `@GeneratedValue` will cause all ITs to fail on save (null UUID).
- Sensor mapping not loaded when returning device — `toDto()` only shows device fields, not assigned sensors, because `getDeviceSensors` is not called on the response path.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- Uses `SensorService` for sensor validation.
- AGENTS.md updated.
- `ProblemDetail` responses.

**Issues (-2):**
- `CreateDeviceDto` passed into `DeviceService.createDevice()` and `UpdateDeviceDto` passed into `updateDevice()` — DTOs in service layer.
- Missing `@GeneratedValue`.

#### Code Quality — 6/10

**Strengths:**
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- `DeviceDto.fromEntity()` — transformation encapsulated as a static factory on the DTO, which is the correct style.

**Issues (-4):**
- Missing `@GeneratedValue` — critical.
- Unnecessary read-back in `assignSensor`: after saving the assignment, performs `deviceRepository.findById(deviceId).orElseThrow()` to return a fresh device object — the device was already loaded at the top of the method and not mutated.
- Inconsistent indentation (4 spaces vs tabs mixed with the rest of the codebase).
- `DeviceDto.fromEntity()` maps `deviceSensors` to `SensorDto` but sets `name`, `type`, and `capabilities` all to `null` — the sensor data is structurally present in the JSON but entirely empty, making the sensor assignment feature misleading to API consumers.

---

### 6. Claude Opus 4.6
**Total: 32 / 40**

#### Completeness — 8/10

**Present:** Migration, Entity, Repository, Service, DTOs (Create, Update, Device with nested `AssignedSensorDto`), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-2):**
- `getDevice()` and `assignSensor()` make **two separate service calls** in the controller for device + sensors — results in two DB round-trips for what could be one.
- Sensor response only contains `AssignedSensorDto(sensorId, assignedAt)` — no sensor name, type, or capabilities returned. API consumers cannot identify a sensor from the device response without a separate lookup.

#### Test Coverage — 8/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Good scenario coverage in service and controller tests.

**Issues (-2):**
- `DeviceServiceTest` does not test the `getDeviceSensors` path thoroughly.
- `JpaDeviceRepositoryIT` only tests basic CRUD — no `existsByName` queries tested.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-2):**
- Does **not** use `SensorService` to validate sensor existence — instead catches `DataIntegrityViolationException` from the FK constraint violation. This is an unconventional approach that bypasses the layering rule and couples service behaviour to DB exception types.
- `UpdateDeviceDto` passed into `DeviceService.updateDevice()` — DTO in service layer.

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
- DTO transformation is done inline in the controller: `new DeviceDto(device, sensors)` assembled directly in each handler method rather than via a `DeviceDto.from(entity)` factory. Not counted as a separate deduction since the controller-side build is the smaller of the quality issues here.

---

### 7. Devstral
**Total: 25 / 40**

#### Completeness — 6/10

**Present:** Migration, Entity (UUID, no `@GeneratedValue`), Repository, Service (partially), DTOs (Create, Update, Device), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update.

**Issues (-4):**
- **Critical**: `getDeviceSensors()` explicitly returns `List.of()` with a comment: *"This would need to fetch the actual sensor entities from a sensor service — For now, we'll return an empty list as a placeholder."* Sensors are never returned from the device response.
- No `@GeneratedValue` on `Device.id` — same issue as GLM and MiniMax.
- `TIMESTAMP` not `TIMESTAMPTZ` in migration.
- No `@Valid` on controller request bodies.

#### Test Coverage — 7/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`, `DeviceEntityTest`. Uses `RepositoryIT`. AssertJ throughout.

**Issues (-3):**
- `DeviceEntityTest` is a misplaced IT test (extends `RepositoryIT`) for entity persistence — unusual structure.
- `DeviceServiceTest` cannot meaningfully test sensor association since `getDeviceSensors` returns a hardcoded empty list.
- `JpaDeviceRepositoryIT` is sparse — only basic CRUD.

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
- `DeviceDto.fromEntityWithoutSensors()` — transformation encapsulated as a static factory on the DTO, which is the correct style.

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
**Total: 12 / 40**

#### Completeness — 6/10

**Present:** Migration (two files — one wrongly named), Entity, Repository, Service, Controller, DTOs (Create, Update, Device), `GlobalExceptionHandler` (in wrong package), `JpaDeviceSensorRepository`.

**Issues (-4):**
- No `DeviceConfiguration` — `@Service` annotation used instead, violating AGENTS.md.
- Two duplicate Flyway migration files: `V20260301_01_create_device_tables.sql` (wrong format) and `V202603012013__create_device_tables.sql` (correct format but both present) — Flyway will fail on the wrongly named file.
- No AGENTS.md update.
- No `DeviceExceptionHandler` scoped to device controller — uses a generic `GlobalExceptionHandler` placed in a separate `de.sfl.global` package (outside feature package).
- `sensorId` in `DeviceSensorId` is `UUID` — but `sensors.id` is `BIGINT` (`Long`). The FK type is fundamentally wrong.

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

### 12. GPT 5.4 (GitHub Copilot)
**Total: 37 / 40**

#### Completeness — 10/10

**Present:** Migration (named constraints, `TIMESTAMPTZ`, UUID FK types), Entity (UUID, `@GeneratedValue`, `@PrePersist`/`@PreUpdate`, `Instant`), `DeviceSensor` with `@MapsId` and `@ManyToOne` back to `Sensor`, Repository (package-protected), Service, DTOs (Create, Update, Device with full `SensorDto` list), Controller (CRUD + assign + get), Exception Handler (scoped, `ProblemDetail`), Configuration, AGENTS.md update. `findDetailedById` with `JOIN FETCH` for eager sensor loading.

#### Test Coverage — 9/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` (JSON model tests), `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT` base class. AssertJ throughout. Test subclass pattern for entity ID assignment.

**Issues (-1):**
- No `SensorDtoTest` — the local `SensorDto` record (used inside `DeviceDto`) has no JSON marshalling test.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` (RFC 7807) responses.
- AGENTS.md updated.
- Service works with entities — no DTO leakage into service layer.
- Test subclass pattern.

**Issues (-1):**
- Does **not** use `SensorService` for cross-feature sensor validation — uses `EntityManager.find(Sensor.class, sensorId)` to load sensor entities directly. While this avoids accessing `JpaSensorRepository` (which is package-protected), it bypasses the service boundary that AGENTS.md requires.

#### Code Quality — 9/10

**Strengths:**
- Richest JPA relationship mapping of the GPT group: `@OneToMany` on Device → DeviceSensor, `@ManyToOne` with `@MapsId` back to `Sensor`.
- `JOIN FETCH` query in repository for eager sensor loading — efficient single-query approach.
- Full sensor details (id, name, type, capabilities) returned in API response via `DeviceDto.from()` — transformation encapsulated in the DTO, not the controller.
- Named constraints in migration (`CONSTRAINT pk_devices`, `CONSTRAINT fk_device_sensors_device`, etc.).
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Clean, readable method decomposition.

**Issues (-1):**
- `EntityManager.find` for sensor lookup is unconventional in a Spring Data JPA codebase — typically cross-feature validation goes through the service layer.

---

### 13. GPT 5.4 (ChatGPT)
**Total: 36 / 40**

#### Completeness — 10/10

**Present:** Migration (named constraints, `TIMESTAMPTZ`), Entity (`@MapsId`, `@ManyToOne` back to `Sensor`), Repository (package-protected), Service, DTOs (Create, Update, Device with full local `SensorDto` list), Controller (CRUD + assign + get), Exception Handler (scoped, `ProblemDetail`), Configuration, AGENTS.md update. `findDetailedById` with `JOIN FETCH` for eager sensor loading.

#### Test Coverage — 10/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest`, `SensorDtoTest` (JSON model tests — including the nested `SensorDto`, which the GPT 5.4 (Copilot) run missed), `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT` base class. AssertJ throughout. Test subclass pattern for entity ID assignment. Good scenario coverage across all paths.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` (RFC 7807) responses.
- AGENTS.md updated.
- Test subclass pattern.
- Local `SensorDto` in `devices` package avoids importing across feature packages.

**Issues (-2):**
- `UpdateDeviceDto` passed into `DeviceService.updateDevice()` — DTO in service layer.
- Does **not** use `SensorService` for cross-feature sensor validation — uses `EntityManager.find(Sensor.class, sensorId)`, same approach as the GPT 5.4 (Copilot) run.

#### Code Quality — 8/10

**Strengths:**
- Rich JPA model: `@OneToMany` on Device → DeviceSensor, `@ManyToOne` with `@MapsId` back to `Sensor`.
- `JOIN FETCH` query in repository for eager sensor loading.
- Full sensor details (id, name, type, capabilities) returned in API response via DTO factory methods — transformation encapsulated in the DTO.
- Named constraints in migration.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- `@Transactional(readOnly = true)` at class level with explicit `@Transactional` on mutating methods — correct transaction hygiene.
- `hasSensor(Long sensorId)` domain method and `assignSensor(Sensor)` on `Device` entity — richer domain model than the GPT 5.4 (Copilot) run.

**Issues (-2):**
- Missing `@GeneratedValue` on `Device.id` — UUID is assigned via a null-guard in `@PrePersist` (`if (id == null) id = UUID.randomUUID()`), which works but is less idiomatic than `@GeneratedValue(strategy = GenerationType.UUID)`.
- `id UUID` in migration without `NOT NULL` — the GPT 5.4 (Copilot) run used `id UUID NOT NULL`.

> **Comparison with GPT 5.4 (Copilot) (37/40)**: The two implementations are architecturally near-identical — same `EntityManager.find` strategy, same `JOIN FETCH` repository query, same DTO structures and test structure. The GPT 5.4 (ChatGPT) run trades the DTO-in-service leak (Compliance) and missing `@GeneratedValue` (Code Quality) for a richer domain model (idempotency logic in the entity, `@Transactional(readOnly = true)` class-level annotation) and a more complete test suite (adds `SensorDtoTest`). The net result is 36/40 vs 37/40 — GPT 5.4 (ChatGPT) is one point behind due to the DTO-in-service compliance deduction and missing `@GeneratedValue`, despite being marginally stronger on test coverage and transaction management.

---

### 14. GPT 5.3 Codex Extra-High
**Total: 35 / 40**

#### Completeness — 10/10

**Present:** Migration (named constraints, `TIMESTAMPTZ`), Entity (UUID, `@GeneratedValue`), `DeviceSensor` with `(Device, Long sensorId)` constructor, Repository (package-protected), Service, DTOs (Create, Update, Device with full `SensorDto` list), Controller (CRUD + assign + get), Exception Handler (scoped, `ProblemDetail`), Configuration, AGENTS.md update. Full sensor details returned via `sensorLookupRepository.findByIdIn()`.

#### Test Coverage — 9/10

**Present:** Most thorough test coverage of all GPT models — symmetry tests, comprehensive DTO tests (marshalling and unmarshalling), rich repository IT coverage, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Test subclass pattern.

**Issues (-1):**
- Minor redundancy across some test scenarios.

#### Compliance — 7/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- `ProblemDetail` responses.
- AGENTS.md updated.
- Named constraints in migration.

**Issues (-3):**
- **Critical layering violation**: Creates `JpaSensorLookupRepository` — a Spring Data repository for the `Sensor` entity — inside the `devices` package. This explicitly violates AGENTS.md: *"Services should not directly access repositories from other service packages."* Creating a new repository in your own package for another feature's entity is worse than the spirit of the violation.
- Mixed indentation (4-space/tab inconsistency from codebase standard).

#### Code Quality — 8/10

**Strengths:**
- `TIMESTAMPTZ` in migration.
- Named constraints.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Full sensor details in API response (richest response alongside GPT 5.4 and Codex Low).
- `findByIdIn()` for batch sensor loading — efficient.

**Issues (-2):**
- Mixed indentation (tabs vs. 4 spaces inconsistent with codebase).
- `JpaSensorLookupRepository` creates an unauthorized access path to another feature's entity.

---

### 14. GPT 5.3 Codex
**Total: 36 / 40**

#### Completeness — 9/10

**Present:** Migration (`TIMESTAMPTZ`), Entity (UUID, `@GeneratedValue`), `DeviceSensor`, Repository (package-protected), Service (with `getDeviceSensorIds()`), DTOs (Create, Update, Device with `List<Long> sensorIds`), Controller (CRUD + assign + get), Exception Handler (scoped, `ProblemDetail`), Configuration, AGENTS.md update.

**Issues (-1):**
- Migration missing named PK constraint (`id UUID PRIMARY KEY` inline instead of `CONSTRAINT pk_devices`).

#### Test Coverage — 10/10

**Present:** `CreateDeviceDtoTest`, `DeviceDtoTest`, `UpdateDeviceDtoTest` (JSON model tests), `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Test subclass pattern.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` responses.
- AGENTS.md updated.

**Issues (-1):**
- Does **not** use `SensorService` for sensor validation — uses a custom `sensorExists()` JPQL query (`SELECT COUNT(s) > 0 FROM Sensor s WHERE s.id = :sensorId`) on `JpaDeviceSensorRepository`. This queries the `Sensor` entity directly from the devices package, bypassing the service boundary.

#### Code Quality — 8/10

**Strengths:**
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- Proper imports — no FQN inline.
- Clean method decomposition.

**Issues (-2):**
- Missing named PK constraint in migration.
- Returns only `sensorIds` — no full sensor details in API response. API consumers cannot identify sensors without a separate lookup.

---

### 15. GPT 5.3 Codex Low
**Total: 33 / 40**

#### Completeness — 9/10

**Present:** Migration (`TIMESTAMPTZ`), Entity (UUID, `@GeneratedValue`), Device with `@OneToMany` to `DeviceSensor`, Repository (package-protected), Service, DTOs (Create, Update, Device with `AssignedSensorDto` containing id, name, type, capabilities), Controller (CRUD + assign + get), Exception Handler, Configuration, AGENTS.md update. Full sensor details returned.

**Issues (-1):**
- `@GeneratedValue` without explicit strategy — relies on Hibernate default UUID generation. Works but less explicit than `@GeneratedValue(strategy = GenerationType.UUID)`.

#### Test Coverage — 7/10

**Present:** All test types — JSON model tests, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Test subclass pattern.

**Issues (-3):**
- `JpaDeviceSensorRepositoryIT` is sparse — only 1 test method.
- `@AssertTrue` validation pattern on `UpdateDeviceDto` may not be thoroughly tested in controller tests.
- Overall test depth is lighter than GPT 5.4 or Codex Extra-High.

#### Compliance — 9/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- No unnecessary interface.
- `ProblemDetail` responses.
- AGENTS.md updated.
- Full sensor details in response — richest API output among the Codex effort-level variants.
- Custom `AssignedSensorNotFoundException` avoids cross-package dependency on `de.sfl.sensors.SensorNotFoundException`.

**Issues (-1):**
- Does **not** use `SensorService` — uses `EntityManager.find(Sensor.class, sensorId)` for sensor validation, same approach as GPT 5.4. Bypasses the service boundary.

#### Code Quality — 8/10

**Strengths:**
- Full sensor details (id, name, type, capabilities) returned via `AssignedSensorDto`.
- `@OneToMany` on Device — rich JPA relationship model.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.

**Issues (-2):**
- `@GeneratedValue` without explicit strategy — less readable than explicit `GenerationType.UUID`.
- `@AssertTrue` validation pattern on `UpdateDeviceDto` is unconventional — standard `@Pattern` or `@NotBlank` constraints are more typical and self-documenting.

---

### 16. GPT 5.3 Codex High
**Total: 34 / 40**

#### Completeness — 9/10

**Present:** Migration (named constraints, `TIMESTAMPTZ`), Entity (UUID, `@GeneratedValue`), `DeviceSensor` with `@ManyToOne` to Device only (no `@ManyToOne` to `Sensor`), Repository (package-protected), Service, DTOs (Create, Update, Device with `List<Long> sensorIds`), Controller (CRUD + assign + get), Exception Handler (scoped, `ProblemDetail`), Configuration, AGENTS.md update.

**Issues (-1):**
- Returns `List<Long> sensorIds` only — no full sensor details. API consumers cannot identify sensors from the device response.

#### Test Coverage — 9/10

**Present:** All test types with comprehensive coverage — JSON model tests, `JpaDeviceRepositoryIT`, `JpaDeviceSensorRepositoryIT`, `DeviceServiceTest`, `DeviceControllerTest`. Uses `RepositoryIT`. AssertJ throughout. Test subclass pattern. Good scenario coverage across all test types.

**Issues (-1):**
- Native SQL sensor validation not extensively tested for edge cases.

#### Compliance — 8/10

**Strengths:**
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- Package-protected repositories.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` responses.
- AGENTS.md updated.
- Named constraints in migration.

**Issues (-2):**
- Does **not** use `SensorService` — uses a native SQL query (`SELECT EXISTS (SELECT 1 FROM sensors WHERE id = :sensorId)`) for sensor validation. This directly queries the `sensors` table from the devices package, bypassing both `SensorService` and `JpaSensorRepository`.
- Creates own `SensorNotFoundException` in `de.sfl.devices` instead of calling `SensorService` to validate.

#### Code Quality — 8/10

**Strengths:**
- Named constraints in migration — among the best migration quality.
- `TIMESTAMPTZ` in migration.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate`.
- `DeviceSensor` constructor takes `(Device, Long sensorId)` — simple and explicit.
- Comprehensive test structure.

**Issues (-2):**
- Native SQL query for sensor validation is fragile — tied to table and column names, no compile-time safety.
- `DeviceSensor` has no `@ManyToOne` to `Sensor` entity — the JPA relationship is one-sided, making it impossible to navigate from device to full sensor details without a separate query.

---

## Cross-Implementation Analysis

### Common Strengths Across Top Implementations (Sonnet, Haiku, GLM, Kimi, MiniMax, GPT 5.4, GPT 5.3 variants)

- All chose UUID for device IDs — a reasonable independent choice given the feature was new.
- All used `TIMESTAMPTZ` in migration (except Devstral which used `TIMESTAMP`).
- All used `@Configuration` for bean wiring — no `@Service`/`@Component`.
- All updated `AGENTS.md`.
- All followed the `@PrePersist`/`@PreUpdate` and `Instant` pattern from `Sensor.java`.
- All provided `ProblemDetail` RFC 7807 responses.
- All scoped `@RestControllerAdvice` to `DeviceController.class`.
- All GPT models used the test subclass pattern for entity ID assignment in tests.

### Recurring Issues Across Multiple Implementations

| Issue | Implementations Affected |
|-------|--------------------------|
| No `SensorService` for cross-feature validation | All GPT models (5.4 gh, 5.4 oai, Codex, Codex Low, Codex High, Codex xHigh), Haiku, Opus, Devstral |
| DTOs passed to service layer | Sonnet, Haiku, GLM, Kimi, MiniMax, Devstral, Opus, GPT 5.4 (oai) |
| Missing `@GeneratedValue` on UUID entity | GLM, MiniMax, Devstral, GPT 5.4 (oai) |
| `TIMESTAMP` instead of `TIMESTAMPTZ` | Devstral, Devstral-Small |
| Missing named FK/PK constraints | Devstral, Devstral-Small, DeepSeek, GPT 5.3 Codex |
| Single underscore in migration filename | Devstral-Small (single `_`) |
| Wrong `sensorId` type in junction table | Nemotron, Devstral-Small |
| Sensor loading returns empty list | Devstral |
| No JSON model DTO tests | Kimi |
| `@Service`/Lombok | Nemotron |
| Layering violation via custom repository | GPT 5.3 Codex Extra-High (`JpaSensorLookupRepository`) |
| Native SQL for cross-feature query | GPT 5.3 Codex High |

### Sensor Response Quality

A key differentiator across implementations is whether assigned sensor data is meaningfully surfaced in the API response. There is a clear spectrum:

| Quality | Model | Sensor data returned |
|---------|-------|----------------------|
| Full sensor details | Sonnet, GLM | Full `SensorDto` (id, name, type, capabilities) via `SensorService` or JPA graph traversal |
| Full sensor details | GPT 5.4 (gh), GPT 5.4 (oai), GPT 5.3 Codex Low | Full `SensorDto`/`AssignedSensorDto` via `EntityManager.find` and JPA relationship |
| Full sensor details | GPT 5.3 Codex Extra-High | Full `SensorDto` via `JpaSensorLookupRepository` (layering violation) |
| Partial data | Opus | `AssignedSensorDto(sensorId, assignedAt)` — identifies which sensor but no details |
| IDs only | GPT 5.3 Codex, GPT 5.3 Codex High | `List<Long> sensorIds` — no sensor details |
| IDs only | Kimi | `List<Long> sensorIds` — no sensor details |
| Broken / empty | Haiku | `@OneToMany` defined but `toDto()` ignores it — always returns empty list |
| Broken / empty | MiniMax | Sensor IDs mapped but name/type/capabilities all null |
| Broken / placeholder | Devstral | `List.of()` hardcoded in service with an explicit TODO comment |

**Sonnet** resolves sensors through `SensorService`, which is the architecturally correct approach (respects the cross-feature service boundary). **GLM** uses JPA relationship traversal (`@ManyToOne` on `DeviceSensor` → `Sensor`), which is also a valid approach and produces the richest graph in a single DB query. Both are valid; Sonnet's approach is more aligned with the AGENTS.md layering rules since it goes through the service boundary.

### DTO Transformation Placement

AGENTS.md mandates a `toEntity()` method on inbound DTOs. The outbound direction (`DeviceDto.from(entity)`) is a recommended practice but not required by AGENTS.md and was not scored. The table below documents each model's approach for reference.

| Approach | Models | Notes |
|----------|--------|-------|
| **DTO-owned factory/constructor** (`DeviceDto.from()`, `dto.toEntity()`, DTO constructor) | GPT 5.4 (gh), GPT 5.4 (oai), Haiku, MiniMax, Devstral | Cleaner style; controller delegates to the DTO |
| **Controller-side inline mapping** (`toDto(entity)` with field extraction on controller) | Sonnet, GLM, Opus, Nemotron | Mapping logic lives in the controller, not the DTO — not penalized |
| **Controller wrapper → DTO factory** (controller `toDto()` delegates to `DeviceDto.from()`) | GPT 5.3 Codex, Codex Low, Codex High, Codex XHigh | Functionally equivalent to DTO-owned since the mapping logic is in the DTO, but the wrapper adds indirection |

**GPT 5.4 (Copilot)** is the clearest example of the DTO-owned style — `DeviceDto.from(entity)` with all mapping logic in the DTO. **Haiku** also uses DTO-owned transformation — its controller `toDto()` wrapper delegates to `new DeviceDto(device)`, keeping mapping logic inside the DTO constructor. **Sonnet**, **GLM**, **Opus**, and **Nemotron** placed the field-extraction logic directly in the controller — this is a style observation, not a scored deduction, since AGENTS.md only mandates `toEntity()`. **MiniMax** used `DeviceDto.fromEntity()` — the right pattern — but the implementation was broken because the mapped fields were null. **Devstral** used `DeviceDto.fromEntityWithoutSensors()` — DTO-owned factory — though the name reflects the incomplete sensor retrieval. The four GPT 5.3 Codex variants use a controller `toDto()` wrapper that delegates to `DeviceDto.from()` — functionally equivalent to DTO-owned since the mapping logic is in the DTO.

### Key Differentiators

- **GPT 5.4 (GitHub Copilot)** is the top scorer at 37/40 — the richest JPA model (`@MapsId`, `JOIN FETCH`, full `@OneToMany`), no DTO-in-service leakage, DTO-owned transformation via `DeviceDto.from()`, `@GeneratedValue` present, and comprehensive tests. Its `EntityManager.find` approach for sensor validation is unconventional but avoids accessing package-protected repositories.
- **GPT 5.4 (ChatGPT)** ranks 3rd at 36/40. Near-identical architecture to the GPT 5.4 (Copilot) run; the one-point gap vs. Copilot reflects the DTO-in-service compliance deduction and missing `@GeneratedValue`, offset by an additional `SensorDtoTest` and richer domain model.
- **GLM 4.7** ranks 4th at 35/40. Its showstopper bugs (missing `@GeneratedValue`, Flyway schema misconfiguration) prevent ITs from passing, but the JPA relationship model and sensor response quality are among the strongest — `@ManyToOne` on `DeviceSensor` → `Sensor` gives the richest graph in a single DB query.
- **Claude Sonnet 4.6** ties GPT 5.4 (Copilot) at 37/40 — the only model to correctly use `SensorService` for cross-feature sensor validation, with 10/10 Completeness and 9/10 across Test Coverage and Compliance.
- **GPT 5.4 (ChatGPT) and GPT 5.3 Codex** tie at rank 3 with 36/40. GPT 5.4 (ChatGPT) is near-identical to the Copilot run with the best JSON-model test coverage. GPT 5.3 Codex achieves 10/10 test coverage and 9/10 compliance — its single deduction is bypassing `SensorService` via a JPQL query directly against the `Sensor` entity.
- **GPT 5.3 Codex Extra-High** ranks 5th at 35/40. It invested the most effort time (20 min) and produced the most thorough test suite of any GPT model, but created `JpaSensorLookupRepository` — the most explicit layering violation in the evaluation. Codex High trails at 34/40. Codex Low (33/40) returns full sensor details, unlike Default and High which return only IDs.
- **GPT 5.3 Codex Low** scored 33/40. Each Codex variant chose a different sensor validation strategy (JPQL, EntityManager, native SQL) — all bypass `SensorService`. This suggests GPT models internalized the "services work with entities" rule but consistently missed the cross-feature layering rule.
- **Claude Opus 4.6** is 9th at 32/40. Architecturally unusual — it chose a `DataIntegrityViolationException` catch as a proxy for sensor validation, which is fragile and bypasses the layering rule. Despite being a top-tier plan producer, the implementation reflects less codebase alignment than Sonnet or GPT 5.4.
- **Haiku** is 10th at 31/40. The primary quality issue is a subtle but critical bug: the controller always calls the no-sensors DTO constructor despite a full `@OneToMany` relationship being defined — every device response returns an empty sensor list regardless of assignments. This is a functional gap that makes the sensor assignment feature invisible to API consumers.
- **Kimi** (29/40) suffers from a critical performance bug (`findAll().stream().filter()`) and missing DTO tests, but the core structure is otherwise sound.
- **MiniMax** (28/40) has the unnecessary-read-back bug in `assignSensor`, missing `@GeneratedValue`, and a misleading sensor response that includes structurally correct but data-empty sensor objects.
- **Devstral (cloud)** (25/40) produced working code structure but with the sensor retrieval left as a placeholder — a fundamental functional gap acknowledged in the code itself.
- **Local models (Devstral-Small, Nemotron, Qwen)** all failed to produce complete implementations. Devstral-Small produced a reasonable data layer; Nemotron produced a structurally broken implementation with wrong types; Qwen produced two incompatible partial implementations in invalid directory locations — neither compiled, both violated foundational AGENTS.md rules, and the model partially reimplemented the existing sensors feature from scratch using the wrong Jakarta namespace.

### Migration Filename Compliance

All models that produced a migration used the double-underscore separator correctly **except Devstral-Small** (`V202603071400_create_device_tables.sql` — single underscore). The timestamp format was generally correct though some used past dates suggesting copy-paste from existing migrations rather than generating a fresh timestamp. All five GPT models used the correct double-underscore format.

### The DTO-in-Service Anti-Pattern

The most pervasive single violation across the evaluation models is passing DTOs into the service layer. Every non-GPT model that produced a service leaked at least one DTO type into it. Only **GPT 5.4 (Copilot)** and the GPT 5.3 Codex models consistently avoided the pattern; **Sonnet** used `UpdateDeviceDto.applyToEntity()` from the service, which is a boundary violation even though the transformation logic itself is correctly encapsulated in the DTO.

**The GPT 5.4 (Copilot) run avoids the DTO-in-service pattern** — its service methods accept entity parameters or primitive types, not DTOs. The **GPT 5.4 (ChatGPT) run**, however, did pass `UpdateDeviceDto` into the service, suggesting this is not a guaranteed property of the model but rather a run-to-run variation. The five GPT 5.3 Codex models also generally avoided DTOs in service methods. Regardless, both GPT 5.4 runs share a universal violation: **neither uses `SensorService`** for cross-feature sensor validation, opting instead for `EntityManager.find`. This suggests that while GPT 5.4 internalized the "services work with entities" rule, it consistently missed the cross-feature layering rule that services should go through other feature services, not directly access other features' entities or tables.
