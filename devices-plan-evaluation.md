# Devices Feature — Implementation Plan Evaluation

## Scoring Criteria

| # | Criterion | Description |
|---|-----------|-------------|
| 1 | **AGENTS.md Compliance** | How well the plan follows the rules in `AGENTS.md` and the linked pattern docs |
| 2 | **Codebase Fit** | How well the plan aligns with the existing codebase (Sensor entity, migration style, `PageResult`, `RepositoryIT`, etc.) |
| 3 | **Completeness** | Whether all necessary steps are included (migration, entity, repo, service, DTOs, controller, config, exception handler, AGENTS.md update) |
| 4 | **Detail Level** | How concrete and actionable each step is (code snippets, field lists, method signatures, file paths) |

All scores are on a 1–10 scale. The **baseline** (`devices-feature.md` on the current branch) is used as a calibration reference and is not ranked.

---

## Key Reference Points (from Codebase & Pattern Docs)

Before evaluating, the following facts from the codebase were established as ground truth:

- **ID type**: `BIGSERIAL` / `Long` — the existing `sensors` table uses `BIGSERIAL`, not UUID. Plans proposing UUID are misaligned with the existing codebase.
- **Timestamp type**: `TIMESTAMPTZ`, not `TIMESTAMP`. The existing migration uses `TIMESTAMPTZ`.
- **Constraint naming**: Named constraints with prefixes `pk_`, `fk_`, `uq_`, `chk_` per `DATABASE_SCHEMA.md`.
- **No Lombok**: The existing `Sensor.java` entity does not use Lombok. `AGENTS.md` does not mention Lombok. Plans recommending Lombok deviate from the codebase style.
- **No `@Service`/`@Component`**: Beans must be wired via `@Configuration` classes — never annotated directly.
- **No interface when unnecessary**: `AGENTS.md` says "Only create interface when there will be multiple implementations." A `DeviceService` interface with a `DeviceServiceImpl` is a violation.
- **Package-protected repositories**: Repositories must have no access modifier.
- **Keyset pagination**: `AGENTS.md` and `PAGINATION.md` prefer keyset (seek) pagination over offset-based.
- **Service layer works with entities, not DTOs**: DTOs belong only in the controller layer.
- **Test subclass pattern**: `ENTITY_TEST_DATA.md` — use `protected Long id` and a test subclass, never reflection.
- **Repository IT extends `RepositoryIT`**: `REPOSITORY_TESTING.md`.
- **JSON model tests**: Required for every DTO per `JSON-MODEL-TESTING.md`.
- **AssertJ assertions** in tests (not JUnit `assertEquals`).
- **GDPR**: Never log IDs or personal data.
- **Migration filename**: `V<YYYYMMDDHHmm>__description.sql` with double underscore.
- **AGENTS.md must be updated** with the new feature entry.
- **Layering rule**: Services must not access repositories from other feature packages — they must call `SensorService` to validate sensor existence, not `JpaSensorRepository` directly.
- **DTO mapping style**: `AGENTS.md` requires `dto.toEntity()` for incoming mapping; plans that omit this are penalized. Outgoing mapping (`DeviceDto.from(entity)` or controller `toDto()`) is not required by AGENTS.md — plans that address it earn credit, but its absence is not a deduction.
- **Top-end completeness standard**: A production-complete device feature is expected to include a device list endpoint (`GET /api/devices`) using keyset pagination, and a way to unassign sensors from devices. These are treated as completeness expectations for high scores, not bonus credit.

---

## Ranking Summary

| Rank | Model | AGENTS.md Compliance | Codebase Fit | Completeness | Detail Level | **Total** |
|------|-------|:--------------------:|:------------:|:------------:|:------------:|:---------:|
| 1  | Claude Opus 4.6 | 10 | 10 | 9 | 9 | **38** |
| 2  | Claude Sonnet 4.6 | 9 | 10 | 6 | 9 | **34** |
| 3  | GLM 4.7 | 8 | 8 | 9 | 9 | **34** |
| 4  | GPT 5.3 Codex Extra-High | 9 | 8 | 7 | 7 | **31** |
| 4  | GPT 5.4 (GitHub Copilot) | 9 | 8 | 7 | 7 | **31** |
| 6  | Kimi K2.5 | 8 | 8 | 8 | 6 | **30** |
| 7  | GPT 5.3 Codex | 9 | 7 | 6 | 7 | **29** |
| 8  | GPT 5.4 Extra-High | 8 | 7 | 7 | 7 | **29** |
| 9  | GPT 5.4 (ChatGPT) | 8 | 7 | 6 | 7 | **28** |
| 10 | MiniMax 2.5 | 6 | 8 | 7 | 6 | **27** |
| 11 | GPT 5.3 Codex High | 8 | 6 | 5 | 6 | **25** |
| 12 | GPT 5.3 Codex Low | 7 | 6 | 5 | 6 | **24** |
| 12 | Devstral | 5 | 7 | 5 | 7 | **24** |
| —  | Claude Haiku 4.5 *(baseline)* | 5 | 4 | 7 | 8 | **24** |
| 14 | Nemotron 3 Nano 30B (local) | 3 | 2 | 5 | 4 | **14** |
| 15 | DeepSeek 3.2 | 3 | 2 | 4 | 3 | **12** |
| 15 | Devstral Small 2 25.12 (local) | 3 | 2 | 5 | 2 | **12** |
| 17 | GPT-OSS Safeguard 20B (local) | 2 | 1 | 3 | 3 | **9** |
| 18 | Qwen Turbo | 3 | 2 | 2 | 1 | **8** |

### Key Differences by Category

- **Top tier**: Claude Opus 4.6 (38) provides the strongest overall plan. Claude Sonnet 4.6 and GLM 4.7 (34) follow as high-quality plans with strong architecture and codebase alignment; Sonnet's main weakness is the missing `GET /api/devices` endpoint.
- **Strong mid-tier**: GPT 5.3 Codex Extra-High and GPT 5.4 (Copilot) tie at 31, followed by Kimi K2.5 at 30 and GPT 5.3 Codex plus GPT 5.4 Extra-High at 29. These plans are broadly solid, but missing list support and/or missing unassign support keep them below the top tier.
- **Useful but weaker**: GPT 5.4 (ChatGPT), MiniMax 2.5, GPT 5.3 Codex High, GPT 5.3 Codex Low, and Devstral cover substantial parts of the feature, but they are less complete or less codebase-aligned.
- **Not reliable enough**: the local plans, DeepSeek, GPT-OSS, and Qwen are too incomplete, too generic, or too inconsistent to serve as strong implementation guides.

> **Note:** The baseline (`devices-feature.md`, Claude Haiku 4.5) is included in the ranking row above for direct comparison. Local LLM plans are marked with *(local)*.

---

## Detailed Per-Plan Evaluation

---

### Baseline: Claude Haiku 4.5
**Total: 24 / 40** *(reference baseline)*

#### AGENTS.md Compliance — 5/10

**Strengths:**
- Package `de.sfl.devices` and base URL `/api/devices` correct.
- `@Configuration` for bean wiring mentioned.
- Package-protected repositories.
- `RepositoryIT` base class referenced.
- JSON model tests for all three DTOs planned.
- AGENTS.md update step included.
- Numbered implementation sequence.
- Constructor injection, `@Transactional` at class level noted.

**Issues (-5):**
- **UUID IDs** — the existing codebase uses `BIGSERIAL`/`Long`; UUID is a direct codebase misalignment. The plan even defends it ("Consistent with existing Sensor entities") which is factually wrong.
- **Lombok** (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) — not used in the codebase; `AGENTS.md` does not mention Lombok.
- **DTOs passed to service layer** — `createDevice(CreateDeviceDto dto)`, `updateDevice(UUID id, UpdateDeviceDto dto)` — violates the rule that services work with entities, not DTOs.
- **Wrong service boundary decision** — explicitly states "DeviceService does NOT call SensorService" as a design decision, which is the opposite of the layering rule.
- `@ControllerAdvice` not scoped to `DeviceController.class`.
- `TIMESTAMP` not `TIMESTAMPTZ`.
- No keyset pagination.

#### Codebase Fit — 4/10

**Strengths:**
- Migration SQL provided with two tables, indexes, FK constraints.
- `@PrePersist`/`@PreUpdate` lifecycle pattern.
- Separate junction table for many-to-many.

**Issues (-6):**
- UUID primary key — `sensors` table uses `BIGSERIAL`; all FK references break if device IDs are UUID while sensor IDs are BIGINT.
- `TIMESTAMP` instead of `TIMESTAMPTZ`.
- No named FK/PK constraints (`CONSTRAINT pk_...`, `CONSTRAINT fk_...`).
- Lombok annotations on entity — diverges from `Sensor.java`.
- `LocalDateTime` in DTO — existing code uses `Instant`.
- Separate `DeviceSensorId` embedded key class — adds complexity inconsistent with the simpler `@ManyToMany` / `@JoinTable` pattern used by top plans.

#### Completeness — 7/10

**Strengths:**
- Migration SQL, Entity, Repository (two repos), Service (all methods), DTOs (three), Controller (5 endpoints), Exception Handler, Configuration, JSON model tests (3), Repository IT (two), Service test, Controller test, AGENTS.md update, full implementation sequence in 7 phases.

**Issues (-3):**
- No `mvn verify` instruction in the sequence.
- No `GET /api/devices` list endpoint is planned; pagination is deferred to future enhancements instead of being part of the feature plan.
- No way to unassign sensors from devices — only `PUT /{deviceId}/sensors/{sensorId}` assignment is planned.

#### Detail Level — 8/10

**Strengths:**
- SQL migration with both tables, FKs, and indexes.
- Service method signatures with return types.
- DTO field lists with validation annotations.
- Controller method-to-endpoint mapping table.
- Test scenarios listed per class.
- Design decisions section with explicit rationale per choice.
- Dependencies section (even if some are wrong).
- Numbered 7-phase implementation sequence.

**Issues (-2):**
- Entity described only in prose — no code snippet for `Device.java`.
- Repository described as bullet list — no interface code or query method signatures.

---

### 1. Claude Opus 4.6
**Total: 38 / 40**

#### AGENTS.md Compliance — 10/10

**Strengths:**
- Correct `BIGSERIAL`/`Long` ID, `TIMESTAMPTZ`, named constraints, proper FK indexes.
- Package-protected repository explicitly specified.
- `@Configuration` bean wiring — no `@Service`/`@Component`.
- No unnecessary interface: plain `DeviceService` class.
- Explicit cross-cutting concerns section covering layering rule (calls `SensorService`, not `JpaSensorRepository`).
- References `ENTITY_TEST_DATA.md` pattern explicitly (`TestDevice extends Device` inner class).
- `@RestControllerAdvice(assignableTypes = DeviceController.class)` — scoped advice.
- `ProblemDetail` (RFC 7807) for error responses.
- Keyset pagination with `PageResult<Device>`.
- GDPR-compliant logging explicitly called out.
- AGENTS.md update step included.

#### Codebase Fit — 10/10

**Strengths:**
- `BIGSERIAL` matches the existing `sensors` migration exactly.
- `TIMESTAMPTZ` matches.
- Named constraint style (`pk_devices`, `fk_device_sensors_devices`, `fk_device_sensors_sensors`) mirrors the `sensor_capabilities` pattern.
- `Instant` for timestamps matches `Sensor.java`.
- `@PrePersist` / `@PreUpdate` lifecycle pattern matches `Sensor.java`.
- `protected Long id` matches `Sensor.java`.
- `PageResult<T>` usage matches the existing `PageResult.java` utility class.
- Repository IT extends `RepositoryIT` base class.
- No Lombok.
- `SensorService` correctly used as a cross-package dependency — standard Spring injection.

#### Completeness — 9/10

**Covered:** Migration → Entity → Repository (with keyset pagination queries) → Exceptions → Service (all CRUD + `assignSensor`) → DTOs (Create, Update, Device, with `toEntity()`) → Controller (6 endpoints including GET list/by-id) → Exception Handler → Configuration → JSON model tests → Repository IT → Service unit test → Controller test → AGENTS.md update → Run `mvn verify`.

**Issues (-1):**
- No `AssignSensorsDto` — the `PUT /{id}/sensors/{sensorId}` takes path parameters only, matching the prompt's literal "assign" wording, but does not provide a way to unassign sensors. Plans using full-replace `PUT /{id}/sensors` with a body (Sonnet, GLM, MiniMax) cover this common operation.

#### Detail Level — 9/10

**Strengths:**
- Table-format database schema with column types and constraints.
- Service method signatures with return types and transaction annotations.
- All test class names, annotations, and test scenario descriptions.
- Implementation order numbered list.
- Cross-cutting concerns section with explicit rationale for layering decisions.

**Issues (-1):**
- DTO field lists are described in prose rather than code snippets, making them slightly less immediately actionable than code-level plans like GLM.

---

### 2. Claude Sonnet 4.6
**Total: 34 / 40**

#### AGENTS.md Compliance — 9/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`, named constraints, FK indexes.
- Package-protected repository.
- `@Configuration` bean wiring.
- No unnecessary interface.
- Cross-cutting concerns section explicitly addresses: layering (calls `SensorService`, not `JpaSensorRepository`), idempotency, `@Transactional` on mutating methods, no `@Component`/`@Service`, DTOs in controller layer only.
- `AssignSensorsDto` with full-replace semantics — passing an empty or reduced set effectively supports unassigning sensors, covering a common real-world operation not explicitly required by the prompt but clearly useful.
- `ProblemDetail` (RFC 7807) for exception handler.
- GDPR logging note.
- AGENTS.md update step.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.

**Issues (-1):**
- Migration file timestamp uses `V<timestamp>` placeholder — correct guidance is given but no concrete example timestamp is shown.

#### Codebase Fit — 10/10

**Strengths:**
- `BIGSERIAL`, `TIMESTAMPTZ`, constraint naming matches existing migration.
- `Instant` timestamps.
- `protected Long id` for test subclass pattern.
- `PageResult<T>` usage.
- `RepositoryIT` base class.
- No Lombok.
- `DeviceDto` returns `Set<Long> sensorIds` — a legitimate design choice (lighter payload, avoids coupling) that the prompt does not prescribe against.

#### Completeness — 6/10

**Covered:** Migration, Entity, Repository, Exceptions, Service, DTOs (Create, Update, Assign, Device), Controller, Exception Handler, Configuration, JSON model tests (all DTOs), Repository IT, Service unit test, Controller test, AGENTS.md update, implementation order.

**Issues (-4):**
- No `GET /api/devices` list endpoint is planned, so the feature is not production-complete under the completeness rubric.
- No `mvn verify` step at the end.
- No mention of `existsByNameAndIdNot` for update uniqueness, making the update path implementation-ready gap.

#### Detail Level — 9/10

**Strengths:**
- Java record code snippets for all DTOs.
- Full controller endpoint table with method, path, status, and description.
- Service method signatures and transaction annotations.
- Repository interface with derived query methods.
- Cross-cutting concerns section with rationale.
- Numbered implementation order.
- Per-test class scenario descriptions.
- `DeviceConfiguration` code snippet shown in full.

**Issues (-1):**
- Entity is described in prose only — no code snippet for the `Device` class itself.

---

### 3. GLM 4.7
**Total: 34 / 40**

#### AGENTS.md Compliance — 8/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`, named constraints, FK indexes including both `device_id` and `sensor_id` indexes on the join table.
- Unique constraint on `name` (`uniq_device_name`) explicitly included — good.
- Package-protected repository.
- `@Configuration` bean wiring.
- `protected Long id` explicitly shown.
- `@PrePersist`/`@PreUpdate` lifecycle pattern.
- `@ManyToMany` directly on the entity (no separate junction entity).
- `@EntityGraph` for eager fetch in repository — good practice.
- Keyset pagination methods.
- `ProblemDetail` responses.
- Test class names follow the pattern exactly.
- References `RepositoryIT`.
- `AssignSensorsDto` included — full-replace semantics support unassigning sensors by sending a reduced set, but `@NotEmpty` on the DTO means the plan does not support clearing all assignments in a single request.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- Per-scenario test descriptions.
- GDPR compliance note in Design Considerations ("Never log device IDs or personal data").

**Issues (-2):**
- `DeviceService` injects `JpaSensorRepository` directly, violating the cross-package repository access rule.
- Migration timestamp placeholder is not a concrete example.
- DTO records include `toEntity()` methods but service method signatures accept DTOs (`create(CreateDeviceDto dto)`, `update(Long id, UpdateDeviceDto dto)`, `assignSensors(Long deviceId, AssignSensorsDto dto)`), meaning DTOs leak into the service layer despite the DTO-owned conversion methods — a layering contradiction where `toEntity()` would be called inside the service rather than the controller.

#### Codebase Fit — 8/10

**Strengths:**
- Closely mirrors the existing sensor migration, including constraint naming and index pattern.
- `Instant` timestamps match `Sensor.java`.
- `protected Long id` matches.
- `PageResult<T>` referenced.
- No Lombok.
- `RepositoryIT` base class.

**Issues (-2):**
- `JpaSensorRepository` injection crosses package boundary.
- `Clock` injection in `DeviceService` is inconsistent with the existing codebase: `Sensor.java` uses `Instant.now()` inside `@PrePersist`/`@PreUpdate`, not a `Clock` bean. Introducing a `Clock` dependency here diverges from the established pattern.

#### Completeness — 9/10

**Covered:** Migration, Entity, Repository with keyset pagination and `@EntityGraph`, Exceptions, Service, DTOs (all 4), Controller (6 endpoints), Exception Handler, Configuration, JSON model tests (all 4 DTOs), Repository IT, Service unit test, Controller test, AGENTS.md update, implementation steps, `mvn test` commands, `mvn verify`.

**Issues (-1):**
- No mention of `existsByNameAndIdNot` for update uniqueness validation.

#### Detail Level — 9/10

**Strengths:**
- Full Java code blocks for entity, repository interface (with `@Query`/`@Param` annotations), all DTOs, controller (with OpenAPI annotations), configuration, exception handler, and exception classes.
- Service code shows `@Transactional(readOnly = true)` and `@Transactional` annotations with method signatures.
- Test scenarios listed per test class.
- Numbered implementation steps with `mvn test` and `mvn verify` commands.
- Design considerations section explaining key decisions.

**Issues (-1):**
- Service method bodies are comment stubs — the concrete logic (pagination, validation, assignment) is not shown, making it slightly less actionable than Opus for the service layer.

---

### 4. GPT 5.4 (GitHub Copilot)
**Total: 31 / 40**

#### AGENTS.md Compliance — 9/10

**Strengths:**
- Uses dedicated feature package `de.sfl.devices` and correct base URL `/api/devices`.
- Explicitly keeps the repository package-private and wires beans through `DeviceConfiguration`.
- Uses `SensorService` for cross-feature validation rather than direct repository access.
- Includes `AGENTS.md` update, controller tests, service tests, repository IT, and JSON model tests.
- Supports idempotent assignment semantics and keeps DTOs in the controller layer.

**Issues (-1):**
- DTO mapping guidance is implied rather than explicitly described. The plan states "return DTOs only from the controller layer" and service methods accept entities/primitives (correct layering), but no `toEntity()` methods are shown on the DTOs — the incoming mapping approach must be inferred.

#### Codebase Fit — 8/10

**Strengths:**
- Aligns well with package-by-feature structure and layered design.
- Reuses `SensorService` correctly and plans repository integration tests.
- Mentions timestamp alignment with the existing sensor feature.

**Issues (-2):**
- Leaves migration specifics generic instead of calling out named constraints in the established style.
- Omits `RepositoryIT` / `PageResult<T>`-level alignment details that the top plans include explicitly.

#### Completeness — 7/10

**Covered:** Feature registration, endpoints, entity, repository, service, DTOs, configuration, exceptions, migration, tests, AGENTS.md update, OpenAPI annotations, and verification commands.

**Issues (-3):**
- No list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- Relationship details and sensor cardinality remain somewhat under-specified.

#### Detail Level — 7/10

**Strengths:**
- Clear endpoint table, service behaviors, test scenarios, and implementation sequence.
- Verification commands are included.

**Issues (-3):**
- No concrete code snippets.
- Migration remains high level without code or named-constraint examples.
- DTO mapping guidance is implied rather than explicitly described.

---

### 5. GPT 5.3 Codex
**Total: 29 / 40**

#### AGENTS.md Compliance — 9/10

**Strengths:**
- Keeps all code in `de.sfl.devices` and plans explicit `DeviceConfiguration` bean wiring.
- Calls out `SensorService` instead of cross-package repository access.
- Includes DTOs, exception handler, tests, and `AGENTS.md` update.
- Ends with `mvn verify`.

**Issues (-1):**
- DTO mapping approach is not described. Service methods accept entity/primitives (correct layering), but no `toEntity()` method on DTOs is mentioned — the incoming mapping approach must be inferred.

#### Codebase Fit — 7/10

**Strengths:**
- Correct use of `BIGSERIAL`, `TIMESTAMPTZ`, join table, and feature-local package structure.
- Includes a dedicated migration, tests, and service/controller split matching the codebase.

**Issues (-3):**
- Generic migration guidance with no explicit named constraints.
- No explicit reference of extending `RepositoryIT`.
- Limited alignment with existing DTO transformation conventions.

#### Completeness — 6/10

**Covered:** Migration, package structure, entity, service, repository, DTOs, exception handler, controller, tests, AGENTS.md update, and verification.

**Issues (-4):**
- No GET list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- No way to unassign sensors from devices — the plan only proposes `PUT /api/devices/{deviceId}/sensors/{sensorId}`.
- Uniqueness handling is mentioned but not fully translated into repository/query details.

#### Detail Level — 7/10

**Strengths:**
- Good API breakdown, implementation order, and test coverage outline.
- Includes concrete verification steps.

**Issues (-3):**
- Mostly prose, few implementation-ready specifics.
- No code snippets.
- DTO mapping style is left implicit.

---

### 6. GPT 5.4 (ChatGPT)
**Total: 28 / 40**

#### AGENTS.md Compliance — 8/10

**Strengths:**
- Dedicated feature package, clear endpoint set, explicit `DeviceConfiguration`, and `AGENTS.md` update.
- Mentions using a narrow sensor service API when cross-package repository access would violate the rules.
- Covers controller, service, repository, and DTO tests.

**Issues (-2):**
- Leaves the layering decision slightly open-ended instead of committing cleanly to `SensorService`.
- DTO mapping is vague: mentions "request and response DTOs" and "response mapping helpers" but provides no `toEntity()` methods on the DTOs — the implementer must invent the incoming mapping approach.

#### Codebase Fit — 7/10

**Strengths:**
- Good package-by-feature alignment and awareness of layered architecture constraints.
- Includes migration, validation, exception handling, and tests in the expected shape.

**Issues (-3):**
- Stays generic on migration details rather than matching named-constraint and repository-test conventions closely.
- Does not explicitly anchor timestamps, repository style, or test base classes to the existing codebase.
- Leaves sensor cardinality and payload design partly open.

#### Completeness — 6/10

**Covered:** Endpoints, DTOs, service, repository, configuration, migration, tests, AGENTS.md update, and acceptance criteria.

**Issues (-4):**
- No list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- No way to unassign sensors from devices — the plan only proposes `PUT /api/devices/{deviceId}/sensors/{sensorId}`.
- Several implementation choices remain undecided rather than fully planned.

#### Detail Level — 7/10

**Strengths:**
- Clear structure, implementation order, and coverage of major artifacts.
- Includes acceptance criteria and testing areas.

**Issues (-3):**
- Lacks code snippets.
- Leaves important design choices as open notes.
- DTO mapping style is not spelled out.

---

### 7. GPT 5.4 Extra-High
**Total: 29 / 40**

#### AGENTS.md Compliance — 8/10

**Strengths:**
- Dedicated feature package `de.sfl.devices` and correct base URL `/api/devices`.
- Explicit `DeviceConfiguration` — no stereotype annotations.
- Uses `SensorService` for cross-feature collaboration rather than direct repository access.
- Package-protected repositories mentioned.
- AGENTS.md update step included.
- Controller, service, repository IT, and DTO JSON tests all planned.
- GDPR note: "keep logs GDPR-safe by avoiding device or sensor names in log messages."

**Issues (-2):**
- Explicitly recommends controller-side DTO mapping ("keep DTO-to-entity conversion in the controller layer where it remains simple") — this is the opposite of the preferred DTO-owned `toEntity()` pattern.
- Invents a description-trimming validation rule not in the requirements.

#### Codebase Fit — 7/10

**Strengths:**
- Good package-by-feature alignment and correct layered architecture.
- Proposes extending sensor feature with focused service methods (`assignSensorToDevice`, `clearAssignmentsForDevice`, `findAssignedSensorIds`) — practical and well-scoped.
- Suggests avoiding bidirectional JPA collection to reduce cross-feature coupling.
- "Recommended Defaults" section makes assumptions about sensor cardinality, name uniqueness, and delete behavior explicitly auditable.

**Issues (-3):**
- Migration stays generic: no column types, no constraint names, no `BIGSERIAL`/`TIMESTAMPTZ` specifics — just prose like "add nullable `device_id` to `sensors`".
- Does not reference `RepositoryIT` base class.
- Does not use named constraint convention (`pk_`, `fk_`, `uq_`).

#### Completeness — 7/10

**Covered:** Endpoints (create, update, assign, delete), DTOs, service, repository, configuration, exceptions, controller advice, migration, tests (controller, service, repo IT, DTO JSON), AGENTS.md update, implementation order, assignment semantics, and sensor collaboration.

**Issues (-3):**
- No GET list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- Missing explicit unassign endpoint (only implicit via delete).

#### Detail Level — 7/10

**Strengths:**
- Clear API design with HTTP methods, paths, response codes, error conditions, and idempotency behavior.
- "Recommended Defaults" section upfront makes assumptions auditable before implementation.
- Explicit class list for the package.
- Service flow described step-by-step for each operation.
- Test scenarios enumerated concretely.
- Clear 6-step delivery order.

**Issues (-3):**
- No code snippets anywhere.
- Migration is prose, not SQL.
- DTO fields listed but no validation annotations shown.

---

### 8. GPT 5.3 Codex Extra-High
**Total: 31 / 40**

#### AGENTS.md Compliance — 9/10

**Strengths:**
- Dedicated feature package, explicit DTO/controller/service/repository split, and `DeviceConfiguration`.
- Uses `SensorService` rather than direct sensor repository access.
- Includes `AssignDeviceSensorsDto`, tests, migration, and AGENTS update.

**Issues (-1):**
- DTO mapping is not addressed. Service description says "Map DTO to entity and persist" but no `toEntity()` method is shown on the DTOs — the incoming conversion boundary between controller and service is left to the implementer.

#### Codebase Fit — 8/10

**Strengths:**
- Most schema-specific migration of any GPT plan: lists exact column types (`BIGSERIAL`, `VARCHAR(255)`, `TEXT`, `TIMESTAMPTZ`), composite PK, FK with `ON DELETE CASCADE`, and named indexes.
- Reasonable package structure and testing plan.
- Recognizes layered-architecture concerns.

**Issues (-2):**
- Does not reference `RepositoryIT` or other codebase-specific testing conventions explicitly.
- Does not use named constraints (`CONSTRAINT pk_...`, `CONSTRAINT fk_...`) despite providing detailed column definitions.

#### Completeness — 7/10

**Covered:** Migration, entity, repository, service, DTOs, configuration, controller, tests, AGENTS.md update, implementation order, and acceptance criteria.

**Issues (-3):**
- No GET list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- No way to unassign sensors from devices — the plan only proposes `PUT /api/devices/{deviceId}/sensors/{sensorId}`.

#### Detail Level — 7/10

**Strengths:**
- Clear API design, class list, service plan, and test coverage.
- Includes acceptance criteria and implementation order.

**Issues (-3):**
- No code snippets.
- Repository and service details remain abstract despite the strong schema section.
- DTO mapping guidance is absent.

---

### 9. Kimi K2.5
**Total: 30 / 40**

#### AGENTS.md Compliance — 8/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`, named constraints, FK indexes.
- Package-protected repository.
- `@Configuration` bean wiring — explicitly stated in section 6 and in the Notes ("Use explicit @Configuration for beans (no @Service/@Component)").
- No unnecessary interface for `DeviceService`.
- Keyset pagination with `PageResult<Device>`.
- `protected Long id` for test subclass pattern.
- Explicit logging rules (DEBUG reads, INFO mutations).
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- `ProblemDetail` responses.
- GDPR logging note.
- AGENTS.md update step.

**Issues (-2):**
- `DeviceService` is shown directly accessing `JpaSensorRepository` (passed via `DeviceConfiguration`) rather than going through `SensorService`. This breaks the layering rule from `AGENTS.md`.
- `DeviceConfiguration` wires `DeviceController` as a bean — controllers should not be manually configured as beans.
- No `AssignSensorsDto` — the assign endpoint uses `PUT /{id}/sensors/{sensorId}` with path parameters only, leaving no way to unassign sensors via the API.

#### Codebase Fit — 8/10

**Strengths:**
- SQL closely mirrors the existing sensor migration style.
- `Instant` timestamps, `@PrePersist`/`@PreUpdate` pattern.
- `protected Long id` matches `Sensor.java`.
- `PageResult<T>` usage.
- `RepositoryIT` base class usage.
- No Lombok.
- `existsByName` repository method.

**Issues (-2):**
- Injects `JpaSensorRepository` into `DeviceService` via `DeviceConfiguration`, crossing the package boundary.
- No `existsByNameAndIdNot` method — the plan only lists `existsByName`, leaving the update uniqueness check incomplete.

#### Completeness — 8/10

**Covered:** Migration, Entity, Repository with keyset pagination, Exceptions, Service, DTOs (3: `DeviceDto`, `CreateDeviceDto`, `UpdateDeviceDto`), Controller (6 endpoints), Exception Handler, Configuration, JSON model tests (3 DTOs), Repository IT, Service unit test, Controller test, AGENTS.md update.

**Issues (-2):**
- No `AssignSensorsDto` and no corresponding `AssignSensorsDtoTest`.
- No `mvn verify` step at the end to confirm everything passes.

#### Detail Level — 6/10

**Strengths:**
- Full DTO record code snippets with validation annotations and `toEntity()` method.
- Complete API Specification section with full request/response JSON examples for all endpoints.
- Service method list with read-only/write transaction labels (prose).
- Numbered implementation order.
- Error scenario table.

**Issues (-4):**
- Service is a prose bullet list of method names with transaction labels — no code block, no actual method signatures, no `@Transactional` annotations.
- Repository is a prose bullet list of method names — no code block, no `@Query`/`@Param` annotations.
- Controller is a prose bullet list of endpoints — no code block, no OpenAPI annotations shown.
- Exception handler, configuration, and entity are described only in prose — no code.
- Test section lists class names only — no per-scenario descriptions.

---

### 10. MiniMax 2.5
**Total: 27 / 40**

#### AGENTS.md Compliance — 6/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`, named constraints (`pk_devices`, `fk_device_sensors_devices`, `fk_device_sensors_sensors`), FK indexes on both columns of the join table.
- Package-protected repository.
- Keyset pagination methods in repository.
- No unnecessary interface.
- `AssignSensorsDto` with full-replace semantics.
- `AssignSensorsDto` with full-replace semantics — `PUT /{id}/sensors` with a body supports unassigning sensors by sending a reduced set, covering a common real-world operation.
- Service methods list with transaction annotations.
- GDPR compliance note.
- AGENTS.md update step.
- `RepositoryIT` base class referenced.

**Issues (-4):**
- Service injects `JpaSensorRepository` directly from another package — crosses package boundary.
- No mention of `protected Long id` / test subclass pattern from `ENTITY_TEST_DATA.md`.
- No mention of `ProblemDetail` — exception handler design is described only in terms of HTTP codes without mentioning the response body format.
- Migration timestamp placeholder is non-specific.
- `@Configuration` bean wiring is not stated — the plan names `DeviceConfiguration.java` in the file tree and implementation order, but never specifies `@Configuration` or confirms the absence of `@Service`/`@Component`.
- No DTO mapping methods described at all — DTOs are shown as records with fields but no `toEntity()` method is included, so the incoming mapping approach must be invented by the implementer.

#### Codebase Fit — 8/10

**Strengths:**
- Migration matches existing sensor migration style closely.
- DTO records usage consistent with Java 21 style.
- `PageResult<T>` usage.
- No Lombok.
- `RepositoryIT`.

**Issues (-2):**
- `JpaSensorRepository` cross-package access.
- No `Instant` mentioned for timestamps — described as "timestamps" only, leaving the type implicit.

#### Completeness — 7/10

**Covered:** Migration, Entity, Repository with keyset pagination, Exceptions, Service, DTOs (4), Controller (6 endpoints), Configuration, Exception Handler, JSON model tests, Repository IT, Service test, Controller test, AGENTS.md update, implementation order.

**Issues (-3):**
- No entity test data pattern mentioned for service/controller tests.
- Test descriptions are very brief — no per-scenario listing.
- Missing `mvn verify` instruction.

#### Detail Level — 6/10

**Strengths:**
- DTO record code snippets.
- SQL migration is complete and correct.
- Service method listing.
- Implementation steps numbered.

**Issues (-4):**
- Entity section is prose with no code snippet — only a partial skeleton.
- Controller section has no code snippet.
- Repository has no `@Query` examples shown.
- Exception handler shows only HTTP codes, no `ProblemDetail` usage or code.
- Test section is three lines with no detail.

---

### 11. GPT 5.3 Codex High
**Total: 25 / 40**

#### AGENTS.md Compliance — 8/10

**Strengths:**
- Correct package, base URL, explicit `SensorService` usage, and `DeviceConfiguration`.
- Includes DTOs, exception handling, tests, and AGENTS update.

**Issues (-2):**
- No explicit package-protected repository statement.
- DTO mapping is absent — the plan mentions "Map DTO to entity and persist" but no incoming `toEntity()` method is shown on the DTOs.

#### Codebase Fit — 6/10

**Strengths:**
- Uses `BIGSERIAL`, `TIMESTAMPTZ`, join table semantics, and service/controller layering.
- Includes duplicate-name handling and tests.

**Issues (-4):**
- Too generic compared with the codebase-specific detail in stronger plans.
- No named-constraint examples.
- No `RepositoryIT` reference.
- Leaves several persistence and mapping decisions at a conceptual level.

#### Completeness — 5/10

**Covered:** Core CRUD endpoints, repository, service, DTOs, tests, migration, and AGENTS update.

**Issues (-5):**
- No list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- No way to unassign sensors from devices — the plan only proposes `PUT /api/devices/{deviceId}/sensors/{sensorId}`.
- No explicit repository IT base-class alignment.
- Verification is weaker than in the stronger GPT plans.

#### Detail Level — 6/10

**Strengths:**
- Clear endpoint descriptions and implementation steps.
- Includes core tests and service responsibilities.

**Issues (-4):**
- Mostly generic prose.
- No code snippets.
- Limited codebase-specific persistence detail.
- DTO mapping style is not addressed.

---

### 12. GPT 5.3 Codex Low
**Total: 24 / 40**

#### AGENTS.md Compliance — 7/10

**Strengths:**
- Uses the correct feature package `de.sfl.devices` and base URL `/api/devices`.
- Explicitly says the `devices` feature should interact with sensors through service boundaries.
- Includes configuration, controller, service, repository, tests, and AGENTS.md update.
- Covers idempotent assignment semantics and main HTTP status codes.

**Issues (-3):**
- Introduces DTO names like `DeviceCreateRequestDto` / `DeviceUpdateRequestDto` instead of the codebase's `CreateDeviceDto` / `UpdateDeviceDto` style.
- Leaves the data model ambiguous and suggests a one-to-many `Sensor.device_id` approach as a primary option.
- Mentions "Implement mapping methods (`toEntity` where needed, plus response mapping helpers)" but provides no concrete code or approach — the mapping path is acknowledged but left entirely to the implementer.

#### Codebase Fit — 6/10

**Strengths:**
- Understands package-by-feature structure and the need for service-boundary interaction with sensors.
- Includes migration, repository IT, and explicit bean configuration.

**Issues (-4):**
- Uses generic `device` table naming instead of the established pluralized schema style.
- The proposed cardinality is under-specified and leans toward a one-to-many model that conflicts with the stronger plans and the evaluation baseline.
- No mention of named constraints, `TIMESTAMPTZ`, `RepositoryIT`, or `PageResult<T>`.
- DTO naming is less aligned with the codebase.

#### Completeness — 5/10

**Covered:** Feature package, API contract, entity, migration, service, repository, DTOs, configuration, tests, AGENTS.md update, and delivery sequence.

**Issues (-5):**
- No list endpoint planned — a significant gap that leaves the device API functionally incomplete (-2).
- No way to unassign sensors from devices — the plan only proposes `PUT /api/devices/{deviceId}/sensors/{sensorId}`.
- Persistence strategy for sensor assignment is not cleanly settled.
- Missing codebase-specific repository and migration details needed for implementation confidence.

#### Detail Level — 6/10

**Strengths:**
- Good structure, acceptance criteria, and phased delivery order.
- Covers testing and idempotency behavior explicitly.

**Issues (-4):**
- Mostly prose, no code snippets.
- Leaves multiple architectural choices open.
- Uses generic naming and persistence options instead of concrete repo-aligned decisions.
- DTO mapping style is not addressed.

---

### 13. Devstral
**Total: 24 / 40**

#### AGENTS.md Compliance — 5/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`.
- Package-protected repository.
- `@RestControllerAdvice` scoped to `DeviceController`.
- `ProblemDetail` responses.
- GDPR note.
- AGENTS.md update.
- Layering rule for `SensorService` access explicitly mentioned.
- `AssignSensorsDto`.

**Issues (-5):**
- **Critical violation**: Creates `DeviceService` as an interface with `DeviceServiceImpl` implementing it. `AGENTS.md` is explicit: "Only create interface when there will be multiple implementations or requirements from a framework. Do not add an `Impl` suffix." This is a direct violation.
- Named FK constraints are missing in the migration SQL — uses bare `FOREIGN KEY` syntax without `CONSTRAINT` names.
- `DeviceConfiguration` wires `DeviceController` as a bean — controllers should not be manually configured as beans.
- No mention of `existsByNameAndIdNot` for update uniqueness.
- `@Configuration` bean wiring is not explicitly stated — the plan names `DeviceConfiguration.java` but never specifies `@Configuration` or confirms the absence of `@Service`/`@Component`.

#### Codebase Fit — 7/10

**Strengths:**
- `BIGSERIAL`, `TIMESTAMPTZ`.
- `Instant` timestamps.
- `@PrePersist`/`@PreUpdate`.
- No Lombok.
- `RepositoryIT`.
- `protected Long id` mentioned.

**Issues (-3):**
- `DeviceServiceImpl` naming is incompatible with the codebase style.
- Missing named constraints in migration (FK/PK naming not following `pk_`, `fk_` prefix pattern).
- `DeviceConfiguration` wires controller explicitly which is wrong.

#### Completeness — 5/10

**Covered:** Migration, Entity, Repository, Exceptions, Service (via interface+impl), DTOs, Controller, Exception Handler, Configuration, JSON model tests, Repository IT, Service test, Controller test, AGENTS.md update.

**Issues (-5):**
- No `GET /api/devices` list endpoint is planned, so the feature is not production-complete under the completeness rubric.
- Creating an unnecessary interface doubles the artifact count without value.
- No `mvn verify` step.
- Full integration test class `DeviceControllerIT` mentioned but not planned in `CONTROLLER_TESTING.md`.

#### Detail Level — 7/10

**Strengths:**
- Detailed package structure file tree.
- SQL migration with both tables.
- API endpoint table with method, path, status codes.
- Detailed DTO records with validation annotations.
- Code snippets for exception classes.
- Numbered implementation order.
- API specification section with example request/response JSON.
- Error scenario table.

**Issues (-3):**
- Service and repository descriptions are brief bullet lists without method signatures or code snippets.
- Test descriptions are per class but without per-scenario detail.

---

### 14. Nemotron 3 Nano 30B a3b MLX 4bit (local)
**Total: 14 / 40**

#### AGENTS.md Compliance — 3/10

**Strengths:**
- Package `de.sfl.devices` correct.
- Base URL `/api/devices` correct.
- `RepositoryIT` base class referenced.
- `mvn verify` step included.
- `@ControllerAdvice` for exception handling mentioned.
- Centralized exception handler with correct HTTP codes.
- `SensorAssignmentRequest` body for sensor assignment endpoint.

**Issues (-7):**
- **No `@Configuration` bean wiring** — never mentioned; implies `@Service`/`@Component` approach.
- **Sub-packages** (`model/`, `repository/`, `service/`, `web/`) — `AGENTS.md` requires all feature code in the single feature package `de.sfl.devices`; sub-packages are not part of the pattern.
- **Wrong DTO naming**: `CreateDeviceRequest`, `UpdateDeviceRequest`, `SensorAssignmentRequest` — `AGENTS.md` requires `Dto` suffix (e.g., `CreateDeviceDto`).
- **`@SpringBootTest` with in-memory database** for integration tests — the project uses Testcontainers with PostgreSQL, not H2.
- No keyset pagination — `List<Device> getAllDevices()` implies full table scan.
- No `protected Long id` / test subclass pattern.
- No JSON model tests.
- No GDPR note.
- No AGENTS.md update step.
- `@ControllerAdvice` not scoped to `DeviceController.class`.
- Mentions Spring Security — not in the project.

#### Codebase Fit — 2/10

**Strengths:**
- `Long` ID type correct.
- Junction table mentioned.
- `RepositoryIT` reference.

**Issues (-8):**
- No SQL schema — cannot verify `BIGSERIAL`, `TIMESTAMPTZ`, or named constraints.
- Sub-package structure contradicts the codebase convention.
- Spring Security not in project.
- In-memory database for integration tests contradicts Testcontainers setup.
- No `PageResult<T>` reference.
- No `Instant` timestamps.
- No `@PrePersist`/`@PreUpdate`.
- No Lombok avoidance mentioned.

#### Completeness — 5/10

**Strengths:**
- All major layers present: migration (step only), entity, repo, service, controller, DTOs, exception handler, `@ControllerAdvice`, tests (unit/controller/integration/repository), `mvn verify`.
- API endpoint table with 6 endpoints.
- Acceptance criteria section.
- Numbered implementation phases.

**Issues (-5):**
- No JSON model tests.
- No AGENTS.md update step.
- No configuration class (`DeviceConfiguration`).
- No pagination planned.
- In-memory DB for integration tests is structurally wrong.
- Test section has minimal detail — no per-scenario descriptions.

#### Detail Level — 4/10

**Strengths:**
- Service method signatures with return types (best of the three local plans).
- API endpoint table with 6 rows.
- Implementation phase table with task breakdowns.
- Acceptance criteria section.
- Project structure directory tree (though with wrong sub-packages).
- `mvn verify` instruction.

**Issues (-6):**
- No SQL migration code.
- No entity code snippet.
- No repository interface code.
- No DTO field definitions.
- No controller code.
- No configuration class.
- No test scenarios — just class-type mentions.

---

### 15. DeepSeek 3.2
**Total: 12 / 40**

#### AGENTS.md Compliance — 3/10

**Issues (-7):**
- **Critical**: Uses `/api/v1/` as base URL instead of `/api/` — directly conflicts with the existing `/api/sensors` pattern and `AGENTS.md` which specifies `/api/devices`.
- **Critical**: Recommends Lombok, MapStruct, "AuditModel base class" — none of these exist in the codebase.
- **Critical**: Proposes soft deletes (`active=false`, `logical deletes`) — not requested and inconsistent with how the sensors feature handles deletion.
- **Critical**: Proposes that "Sensors cannot belong to multiple devices simultaneously" and adds optimistic locking / `SELECT FOR UPDATE` — an invented business constraint not in the requirements.
- Proposes an external "H2 database" for unit tests — the project uses Testcontainers with PostgreSQL.
- `@Service`, `@Repository` annotations likely implied (uses "implement AuditModel", "repository layer" loosely).
- No `@Configuration` bean wiring mentioned.
- No keyset pagination mentioned.
- No mention of `protected Long id` / test subclass pattern.
- No JSON model tests.
- GDPR note absent.
- AGENTS.md update absent.
- HTTP method sections are vague and inconsistent with AGENTS.md HTTP status code requirements.

#### Codebase Fit — 2/10

**Issues (-8):**
- `BIGINT` used for PK (not `BIGSERIAL`), and `TIMESTAMP` not `TIMESTAMPTZ`.
- No FK constraint names.
- Table naming convention is questionable (`device_assigned_sensors` vs. `device_sensors`).
- H2 database usage contradicts Testcontainers PostgreSQL setup.
- MapStruct is not in the project.
- References ETag versioning and optimistic concurrency control — irrelevant to the codebase context.
- The plan uses `DeviceRequestDto` and `DeviceDto` naming that suggests confusion about DTO conventions.

#### Completeness — 4/10

**Issues (-6):**
- Many phases are described at a vague, conceptual level ("investigate inheriting from base framework class").
- No concrete file paths or file names.
- No JSON model tests.
- No mention of AGENTS.md update.
- Testing plan mentions "Unit Tests: Model operations, SQL constraints (H2 database)" — structurally wrong.
- Implementation order is incomplete and inconsistent.

#### Detail Level — 3/10

**Issues (-7):**
- Almost no code snippets.
- Entity design is described as "investigate..." — not actionable.
- Service and repository descriptions use vague phrases ("Address Data separation", "purpose-driven JPA approach").
- API table uses `/api/v1/` paths but has formatting errors (e.g., "SUBMIT_SPMHERE_2D").
- Test section descriptions are non-specific.
- Dependencies section mentions "Flyway SQL migration must migrate device-sensor relationships" which is incomplete.

---

### 16. Devstral Small 2 25.12 (local)
**Total: 12 / 40**

#### AGENTS.md Compliance — 3/10

**Strengths:**
- Package `de.sfl.devices` correct.
- Base URL `/api/devices` correct.
- Package-protected repository mentioned.
- AGENTS.md update step included.

**Issues (-7):**
- **Critical violation**: Creates `DeviceService` as an **interface** with `JpaDeviceService` implementing it — directly violates `AGENTS.md` ("Only create interface when there will be multiple implementations. Do not add an `Impl` suffix." — using a technology-based name like `JpaDeviceService` for an interface impl is the exact antipattern warned against).
- No mention of `@Configuration` bean wiring — implies the standard `@Service`/`@Component` approach.
- No keyset pagination.
- No `protected Long id` / test subclass pattern.
- No JSON model tests — only "DeviceDtoTest" with no marshalling/unmarshalling specifics.
- No GDPR note.
- No `ProblemDetail`.
- No `@RestControllerAdvice` scoping.
- No layering discussion for `SensorService` vs. `JpaSensorRepository`.

#### Codebase Fit — 2/10

**Strengths:**
- Mentions Flyway migration step and DATABASE_SCHEMA.md reference.
- Mentions `JpaRepository` extension.

**Issues (-8):**
- No SQL schema at all — cannot evaluate `BIGSERIAL`, `TIMESTAMPTZ`, or named constraints.
- No mention of `Instant` timestamps.
- No mention of `@PrePersist`/`@PreUpdate`.
- No `PageResult<T>` reference.
- No `RepositoryIT` base class reference (only "DeviceRepositoryIT" without indicating the base class).
- Lombok not explicitly avoided.
- Plan is too sparse to demonstrate any codebase-specific alignment.

#### Completeness — 5/10

**Strengths:**
- All major layers present: migration, entity, repo, service, controller, DTOs, config, tests, AGENTS.md update.
- Implementation order numbered (10 steps).

**Issues (-5):**
- Only one DTO test planned (`DeviceDtoTest`) — no `CreateDeviceDtoTest` or `UpdateDeviceDtoTest`.
- No JSON marshalling/unmarshalling test specifics.
- No exception handler class mentioned.
- No `ProblemDetail` or `@RestControllerAdvice`.
- No `mvn verify` step.
- No pagination endpoint planned.
- No GET endpoints defined (no list, no get-by-id).
- Service layer defined as interface only — no indication of how beans are wired.

#### Detail Level — 2/10

**Issues (-8):**
- No code snippets of any kind.
- All steps are 1-3 bullet lines of prose.
- No method signatures, no field lists, no SQL.
- No repository query methods described.
- No controller endpoint details beyond a 4-bullet list.
- No exception class names.
- No test scenarios — just class name mentions.
- 73 lines total — the shortest plan in the evaluation.

---

### 17. GPT-OSS Safeguard 20B MLX MXFP4 (local)
**Total: 9 / 40**

#### AGENTS.md Compliance — 2/10

**Strengths:**
- Base URL `/api/devices` correct.
- `AssignSensorsDto` equivalent implied (body with sensor IDs).
- `@WebMvcTest` for controller tests.

**Issues (-8):**
- **Critical**: Explicitly uses `@Repository` annotation on the repository interface — `AGENTS.md` forbids `@Repository`/`@Service`/`@Component`.
- **Critical**: Explicitly uses `@Service` annotation on `DeviceService` — direct violation.
- No `@Configuration` bean wiring mentioned.
- No keyset pagination.
- No `protected Long id` / test subclass pattern.
- No JSON model tests.
- No GDPR note.
- No AGENTS.md update step.
- No `RepositoryIT` base class reference.
- No `ProblemDetail`.
- No `@RestControllerAdvice` scoping.
- No exception handler class.
- No named constraints, no migration SQL.
- Wrong relationship: `@OneToMany(mappedBy = "device")` on `Sensor` — the existing `Sensor` entity has no `device` field; this models the relationship from the wrong side.

#### Codebase Fit — 1/10

**Strengths:**
- `Long` ID in entity snippet (correct type).

**Issues (-9):**
- No SQL migration at all — cannot verify `BIGSERIAL`, `TIMESTAMPTZ`, or named constraints.
- Wrong relationship model: `@OneToMany` from `Device` to `Sensor` implies adding a `device` FK column to the `sensors` table, which contradicts both the existing schema and the many-to-many requirement.
- `@Service`/`@Repository` annotations are explicitly shown.
- No `PageResult<T>` reference.
- No `Instant` timestamps.
- No `RepositoryIT`.
- Generic enough to apply to any Spring Boot project — no codebase-specific alignment.
- 69 lines with no real implementation guidance.

#### Completeness — 3/10

**Strengths:**
- API endpoint table with 5 endpoints.
- Mentions unit tests and `@WebMvcTest` controller tests.
- `Deployment Notes` mentions SpringDoc.

**Issues (-7):**
- No migration SQL or schema.
- No exception handler class or exception classes.
- No configuration class.
- No JSON model tests.
- No AGENTS.md update.
- No implementation order.
- No `RepositoryIT`.
- DTOs only named (no fields, no code).
- No GDPR note.
- No pagination.

#### Detail Level — 3/10

**Strengths:**
- Entity code snippet (with errors).
- Repository interface snippet (with `@Repository` violation).
- Service method signatures (with `@Service` violation).
- API endpoint table.

**Issues (-7):**
- All snippets contain critical annotation violations, making them worse than no snippet at all for implementation guidance.
- No SQL migration.
- No DTO field definitions.
- No controller code.
- No exception handler.
- No test scenarios — just a two-line mention.
- No implementation sequence.
- 69 lines total.

---

### 18. Qwen Turbo
**Total: 8 / 40**

#### AGENTS.md Compliance — 3/10

**Issues (-7):**
- **Critical**: No mention of `@Configuration` for bean wiring — implies the standard `@Service`/`@Repository` approach which `AGENTS.md` explicitly forbids.
- **Critical**: "Create a `DeviceRepository` interface" — no `Jpa` prefix, no package-protected modifier mentioned.
- No keyset pagination.
- No mention of package-protected repositories.
- No JSON model tests.
- GDPR note absent.
- AGENTS.md update absent.
- No mention of `RepositoryIT` base class.
- No entity test data / test subclass pattern.
- No `@RestControllerAdvice` scoping.
- No exception handler class defined.
- No `ProblemDetail`.
- Constructor injection not explicitly mentioned.

#### Codebase Fit — 2/10

**Issues (-8):**
- No SQL code or schema — cannot evaluate migration compliance.
- No mention of `BIGSERIAL`, `TIMESTAMPTZ`, named constraints.
- No reference to existing `PageResult<T>`.
- No reference to `RepositoryIT`.
- Generic enough that it could apply to any Spring Boot project, not specifically this one.

#### Completeness — 2/10

**Issues (-8):**
- Only 50 lines total — barely a skeleton.
- No DTOs defined (no Create/Update/Response DTO specifics).
- No configuration class mentioned.
- No exception handler class.
- No JSON model tests.
- No AGENTS.md update.
- Vague "Additional Considerations" replaces actual steps.
- No implementation sequence with numbered steps.

#### Detail Level — 1/10

**Issues (-9):**
- No code snippets of any kind.
- No file paths beyond a one-line mention of `de.sfl.devices`.
- No method signatures.
- No field definitions.
- No database schema — the 5-point "Additional Considerations" is the entirety of the guidance.
- Essentially a rough outline, not an implementation plan.

---

## Cross-Plan Analysis

### Common Strengths Across Top Plans (Opus, Sonnet, GLM)
- All use `BIGSERIAL`/`Long` IDs consistent with the existing codebase.
- All use `TIMESTAMPTZ`.
- All specify package-protected repositories.
- All use `@Configuration` bean wiring.
- Opus and GLM include keyset pagination; Sonnet is otherwise very strong but omits the list endpoint entirely.
- All include JSON model tests, Repository IT, service unit test, and controller test.
- All include an AGENTS.md update step.

### Recurring Issues Across Multiple Plans
| Issue | Plans Affected |
|-------|---------------|
| Cross-package `JpaSensorRepository` injection (layering violation) | Kimi, GLM, MiniMax |
| UUID instead of BIGSERIAL | Baseline (Haiku 4.5) |
| Lombok usage | Baseline (Haiku 4.5) |
| `TIMESTAMP` instead of `TIMESTAMPTZ` | Baseline, DeepSeek |
| Missing named FK/PK constraint naming | Devstral, DeepSeek, partial in others |
| Generic DTO naming (`*RequestDto`) | GPT 5.3 Codex Low, Nemotron 3 Nano 30B |
| `DeviceController` wired manually as a bean in `@Configuration` | Devstral, Kimi |
| `DeviceService` interface + `DeviceServiceImpl` / `JpaDeviceService` | Devstral (cloud), Devstral Small 2 (local) |
| DTOs passed to service layer | Baseline |
| DTO-owned mapping not stated explicitly | GPT 5.4 (gh), GPT 5.4 (oai), GPT 5.4 Extra-High, GPT 5.3 Codex, GPT 5.3 Codex High, GPT 5.3 Codex Extra-High, GPT 5.3 Codex Low |
| No production-complete listing (`GET /api/devices` with keyset pagination) | Sonnet, GPT 5.4 (gh), GPT 5.4 (oai), GPT 5.4 Extra-High, GPT 5.3 Codex, GPT 5.3 Codex High, GPT 5.3 Codex Extra-High, GPT 5.3 Codex Low, Devstral, Baseline (Haiku 4.5) |
| Outgoing (entity→DTO) mapping completely absent from plan *(not penalized — AGENTS.md does not require it, but plans that address it earn credit)* | MiniMax, Kimi, GPT 5.3 Codex High, GPT 5.3 Codex Extra-High, Nemotron 3 Nano, Devstral Small 2, GPT-OSS Safeguard, Qwen Turbo, DeepSeek |
| No JSON model tests | DeepSeek, Qwen, all three local plans |
| No AGENTS.md update | DeepSeek, Qwen, partial; all three local plans |
| `@Service`/`@Repository` annotations explicitly used | GPT-OSS Safeguard 20B |
| Sub-packages within feature package | Nemotron 3 Nano 30B |
| Wrong DTO naming convention (`Request` instead of `Dto`) | Nemotron 3 Nano 30B |
| In-memory database for integration tests (instead of Testcontainers) | Nemotron 3 Nano 30B |

### Key Differentiators
- **Opus** excels at the cross-cutting concerns section, making architectural rationale explicit and actionable. Along with GLM, it uses `@ManyToMany` directly on the entity with `@JoinTable` — no separate junction entity class. Provides the cleanest DTO mapping path: `CreateDeviceDto.toEntity()` for incoming and a private `toDto(Device)` helper in the controller for outgoing.
- **Sonnet** has the most complete `DeviceConfiguration` code, the cleanest bulk-assign API design (`PUT /{id}/sensors` with body enabling unassignment), and the strongest cross-cutting concerns discussion. Like Opus, shows both incoming `toEntity()` on DTOs and outgoing `toDto()` in the controller. Its main score-limiting gap is the missing list endpoint.
- **GLM** provides the most complete Java code examples overall, including transaction details, controller methods with OpenAPI annotations, `@Query`/`@Param` repository methods, configuration, and exception handling — the most immediately actionable plan for implementation. Like Opus, uses `@ManyToMany` directly on the entity. However, the DTO mapping path is contradictory: DTOs include `toEntity()` methods but service method signatures accept DTOs directly, meaning DTOs leak into the service layer.
- **GLM** is the only plan to include `@EntityGraph` for avoiding N+1 queries on the sensor relationship and a unique constraint on device name in the DB schema.
- **GPT 5.4 (GitHub Copilot)** is the strongest GPT planner: good layering, explicit configuration, practical testing plan, and a clear feature outline, with less codebase-specific detail than Opus, Sonnet, and GLM.
- **GPT 5.3 Codex** is the cleanest ChatGPT Codex planning result: solid structure, correct technology choices, and a useful verification flow, but generic on pagination, mapping style, and migration specifics.
- **GPT 5.4 (ChatGPT)** is similar in overall quality to Kimi: sensible and complete, but more abstract than the top GPT plans and less anchored to existing repository conventions.
- **GPT 5.4 Extra-High** includes a useful "Recommended Defaults" section that makes assumptions auditable, and it includes GDPR logging guidance. Its main gaps are the missing list endpoint, missing unassign path, lack of code snippets, and generic migration guidance.
- **GPT 5.3 Codex Extra-High** has the most specific migration schema of any GPT plan (exact column types, `TIMESTAMPTZ`, named indexes), with completeness limited by the missing list endpoint and missing unassign path.
- **GPT 5.3 Codex High** is serviceable but notably generic, making it less implementation-ready than the stronger GPT plans.
- **GPT 5.3 Codex Low** is fast and reasonably structured, but drifts on DTO naming and leaves the sensor-assignment persistence model too open to rank above the baseline.
- **Kimi** stands out for its detailed API Specification section with full request/response JSON examples for all endpoints. However, most implementation sections (service, repository, controller, entity) are prose bullet lists with no code.
- **MiniMax** is the most concise correct plan — covers all required artifacts but lacks implementation detail beyond DTOs.
- **DeepSeek** introduces multiple invented requirements (optimistic locking, soft deletes, sensor exclusivity constraint) that would derail implementation.
- **Qwen** is effectively unusable as an implementation guide.
- **All three local LLM plans** score in the 9–14 range. The primary failure modes are: no SQL schema, missing `@Configuration` bean wiring, no JSON model tests, no GDPR note, no AGENTS.md update, and insufficient detail to guide implementation. **Devstral Small 2** and **GPT-OSS Safeguard 20B** are the weakest, comparable to Qwen Turbo. **Nemotron 3 Nano 30B** is the best local plan because it includes service method signatures, a full API table, `mvn verify`, and a `RepositoryIT` reference.
- **The Haiku 4.5 baseline**, despite UUID/Lombok/layering violations, provides stronger implementation guidance than the local plans because it includes a detailed SQL migration, DTO field definitions, a full implementation sequence, and a comprehensive test plan.

### DTO Mapping Path Analysis

A key quality signal is how plans handle the two directions of DTO-to-entity conversion:

1. **Incoming (DTO → Entity)**: How request DTOs like `CreateDeviceDto` are converted to `Device` entities before reaching the service layer.
2. **Outgoing (Entity → DTO)**: How `Device` entities returned by the service are converted to `DeviceDto` response objects.

The `AGENTS.md` rule is clear: "DTOs should only be used inside the Rest Controller layer" and "Services should work with the entity model." For incoming mapping, `AGENTS.md` explicitly requires `dto.toEntity()` on the DTO. 
For outgoing mapping, `AGENTS.md` gives no guidance — approaches like `DeviceDto.from(entity)` or a controller `toDto()` helper are a useful addition but not a requirement. 
Plans that pass DTOs into the service layer violate the layering rule regardless of direction.

#### Incoming Mapping (DTO → Entity)

| Approach | Plans |
|----------|-------|
| **`toEntity()` method on DTO — code shown** | Opus, Sonnet, GLM, Kimi, Devstral Small 2 (mention only) |
| **`toEntity()` method on DTO — mentioned but no code** | GPT 5.4 (gh), GPT 5.3 Codex Low |
| **Service accepts entity, controller does mapping (implicit)** | GPT 5.3 Codex, GPT 5.3 Codex Extra-High |
| **Service accepts DTO directly (layering violation)** | Baseline (Haiku 4.5), Devstral (cloud), GLM (service methods accept DTOs despite `toEntity()` on record) |
| **"Mapper methods" or "MapStruct" mentioned, no concrete approach** | DeepSeek, Nemotron 3 Nano |
| **Controller-side mapping explicitly recommended** | GPT 5.4 Extra-High |
| **Not addressed** | MiniMax, GPT 5.4 (oai), GPT 5.3 Codex High, GPT-OSS Safeguard, Qwen Turbo |

**Key findings:**
- Opus and Sonnet show the cleanest incoming pattern: `CreateDeviceDto.toEntity()` is code-level explicit, and the service signature takes `Device` entities or primitive parameters (`Long id, String name, String description`).
- GLM includes `toEntity()` on the DTO record but also shows service methods accepting DTOs directly (`create(CreateDeviceDto dto)`, `update(Long id, UpdateDeviceDto dto)`), creating a contradiction within its own plan.
- GPT 5.4 Extra-High uniquely states "keep DTO-to-entity conversion in the controller layer where it remains simple" — this explicitly recommends **against** the DTO-owned `toEntity()` pattern, which is a weaker approach per `AGENTS.md` guidelines.
- Kimi shows `CreateDeviceDto.toEntity()` in full code, making it one of the stronger plans for incoming mapping despite other weaknesses.

#### Outgoing Mapping (Entity → DTO)

| Approach | Plans |
|----------|-------|
| **Controller `toDto(Device)` private helper — explicitly described** | Opus, Sonnet, GLM |
| **Controller returns DTOs, mapping implied but not detailed** | GPT 5.4 (gh), GPT 5.4 (oai), GPT 5.3 Codex, GPT 5.3 Codex Low |
| **"response mapping helpers" mentioned, no code** | GPT 5.4 (ChatGPT), GPT 5.3 Codex Low |
| **`DeviceDto.from(entity)` factory method** | None (no plan uses this pattern) |
| **DTO constructor from entity** | Baseline (Haiku 4.5) — `DeviceDto` has "Constructor: Convert from entity with sensors" |
| **Not addressed at all** | MiniMax, Kimi, GPT 5.3 Codex High, GPT 5.3 Codex Extra-High, GPT 5.4 Extra-High, Devstral (cloud), all local models, DeepSeek, Qwen Turbo |

**Key findings:**
- **No plan proposes `DeviceDto.from(entity)` as a static factory method.** Every plan that addresses outgoing mapping uses a private `toDto(Device)` helper in the controller instead. Since `AGENTS.md` does not prescribe an outgoing mapping approach, this is not a deficiency — but plans that explicitly address outgoing mapping demonstrate more thorough thinking about the full DTO lifecycle.
- The Baseline (Haiku 4.5) is actually the only plan to propose outgoing mapping owned by the DTO itself (a constructor taking the entity), though it does so alongside layering violations that undermine the benefit.
- Opus, Sonnet, and GLM explicitly describe a private controller `toDto()` helper — this is the most common concrete approach across all strong plans.
- Nine plans leave outgoing mapping completely unspecified, including several GPT variants and all models below 27/40.

#### Combined Assessment

| Tier | Plans | Incoming | Outgoing |
|------|-------|----------|----------|
| **Strong** | Opus, Sonnet | `toEntity()` on DTO with code | Controller `toDto()` with description |
| **Good incoming, adequate outgoing** | GLM, Kimi | `toEntity()` on DTO with code | GLM: controller helper; Kimi: unspecified |
| **Adequate** | GPT 5.4 (gh), GPT 5.3 Codex | Implied or mentioned | Implied from "DTOs in controller only" |
| **Weak or contradictory** | GPT 5.4 Extra-High, GPT 5.3 Codex Low | Controller-side mapping or wrong naming | Mentioned but vague |
| **Absent or violated** | MiniMax, GPT 5.4 (oai), GPT 5.3 Codex High, GPT 5.3 Codex Extra-High, Devstral, Baseline, all local models, DeepSeek, Qwen | Missing, vague, or layering violation | Missing or not addressed |

**Note on GLM:** The GLM plan contains a contradiction. The DTO records include `toEntity()` methods (correct), but the service method signatures accept DTOs: `create(CreateDeviceDto dto)`, `update(Long id, UpdateDeviceDto dto)`, `assignSensors(Long deviceId, AssignSensorsDto dto)`. This means the `toEntity()` method would be called inside the service, not the controller — a subtle layering violation where DTOs leak into the service layer. This is reflected in GLM's AGENTS.md Compliance deduction.

**Note on Devstral (cloud):** Similar to GLM, Devstral shows `CreateDeviceDto.toEntity()` on the DTO record but also shows service methods accepting DTOs directly. The existing evaluation flags this as a DTOs-passed-to-service violation under the Baseline but not under Devstral's own section.
