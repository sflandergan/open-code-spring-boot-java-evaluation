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

---

## Ranking Summary

| Rank | Plan | Model | AGENTS.md Compliance | Codebase Fit | Completeness | Detail Level | **Total** |
|------|------|-------|:--------------------:|:------------:|:------------:|:------------:|:---------:|
| 1 | `gh-opus-4.6/devices-plan` | Claude Opus 4.6 | 9 | 9 | 9 | 9 | **36** |
| 2 | `gh-sonnet-4.6/device-plan` | Claude Sonnet 4.6 | 9 | 9 | 8 | 9 | **35** |
| 3 | `rq-glm-4.7/devices-plan` | GLM 4.7 | 8 | 8 | 9 | 9 | **34** |
| 4 | `rq-kimi-k2.5/devices-plan` | Kimi K2.5 | 7 | 8 | 8 | 6 | **29** |
| 5 | `rq-minimax-2.5/devices-plan` | MiniMax 2.5 | 6 | 8 | 7 | 6 | **27** |
| 6 | `rq-devstral/devices-plan` | Devstral | 5 | 7 | 7 | 7 | **26** |
| 7 | `rq-deepseek-3.2/devices-plan` | DeepSeek 3.2 | 3 | 2 | 4 | 3 | **12** |
| 8 | `rq-qwen-turbo/devices-plan` | Qwen Turbo | 3 | 2 | 2 | 1 | **8** |

> **Baseline** (`devices-feature.md`, current branch): AGENTS.md Compliance **5**, Codebase Fit **4**, Completeness **8**, Detail Level **8** → Total **25** (reference only)

---

## Detailed Per-Plan Evaluation

---

### 1. `gh-opus-4.6/devices-plan` — Claude Opus 4.6
**Total: 36 / 40**

#### AGENTS.md Compliance — 9/10

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

**Issues (-1):**
- `DeviceService` accepts `SensorService` as a dependency (correct) but the configuration bean snippet passes `SensorService` without clearly showing how it's injected into `DeviceConfiguration` — a minor clarity gap.

#### Codebase Fit — 9/10

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

**Issues (-1):**
- The `DeviceConfiguration` snippet doesn't show how `SensorService` is obtained to wire `DeviceService`, which could cause confusion during implementation since `SensorService` bean lives in the `de.sfl.sensors` package.

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

### 2. `rq-kimi-k2.5/devices-plan` — Kimi K2.5
**Total: 29 / 40**

#### AGENTS.md Compliance — 7/10

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

**Issues (-3):**
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

### 3. `gh-sonnet-4.6/device-plan` — Claude Sonnet 4.6
**Total: 35 / 40**

#### AGENTS.md Compliance — 9/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`, named constraints, FK indexes.
- Package-protected repository.
- `@Configuration` bean wiring.
- No unnecessary interface.
- Keyset pagination mentioned.
- Cross-cutting concerns section explicitly addresses: layering (calls `SensorService`, not `JpaSensorRepository`), idempotency, `@Transactional` on mutating methods, no `@Component`/`@Service`, DTOs in controller layer only.
- `AssignSensorsDto` with full-replace semantics — passing an empty or reduced set effectively supports unassigning sensors, covering a common real-world operation not explicitly required by the prompt but clearly useful.
- `ProblemDetail` (RFC 7807) for exception handler.
- GDPR logging note.
- AGENTS.md update step.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.

**Issues (-1):**
- Migration file timestamp uses `V<timestamp>` placeholder — correct guidance is given but no concrete example timestamp is shown.

#### Codebase Fit — 9/10

**Strengths:**
- `BIGSERIAL`, `TIMESTAMPTZ`, constraint naming matches existing migration.
- `Instant` timestamps.
- `protected Long id` for test subclass pattern.
- `PageResult<T>` usage.
- `RepositoryIT` base class.
- No Lombok.
- `DeviceDto` returns `Set<Long> sensorIds` — a legitimate design choice (lighter payload, avoids coupling) that the prompt does not prescribe against.

**Issues (-1):**
- No `existsByNameAndIdNot` method in the repository to support update uniqueness check — the plan mentions name uniqueness validation for updates in the service but the repository query is missing.

#### Completeness — 8/10

**Covered:** Migration, Entity, Repository, Exceptions, Service, DTOs (Create, Update, Assign, Device), Controller, Exception Handler, Configuration, JSON model tests (all DTOs), Repository IT, Service unit test, Controller test, AGENTS.md update, implementation order.

**Issues (-2):**
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

### 4. `rq-glm-4.7/devices-plan` — GLM 4.7
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
- `AssignSensorsDto` included — full-replace semantics implicitly support unassigning sensors by sending a reduced set.
- `@RestControllerAdvice(assignableTypes = DeviceController.class)`.
- Per-scenario test descriptions.
- GDPR compliance note in Design Considerations ("Never log device IDs or personal data").

**Issues (-2):**
- `DeviceService` injects `JpaSensorRepository` directly, violating the cross-package repository access rule.
- Migration timestamp placeholder is not a concrete example.

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

### 5. `rq-minimax-2.5/devices-plan` — MiniMax 2.5
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

### 6. `rq-devstral/devices-plan` — Devstral
**Total: 26 / 40**

#### AGENTS.md Compliance — 5/10

**Strengths:**
- Correct `BIGSERIAL`, `TIMESTAMPTZ`.
- Package-protected repository.
- Keyset pagination explicitly noted.
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
- `PageResult<T>`.
- No Lombok.
- `RepositoryIT`.
- `protected Long id` mentioned.

**Issues (-3):**
- `DeviceServiceImpl` naming is incompatible with the codebase style.
- Missing named constraints in migration (FK/PK naming not following `pk_`, `fk_` prefix pattern).
- `DeviceConfiguration` wires controller explicitly which is wrong.

#### Completeness — 7/10

**Covered:** Migration, Entity, Repository, Exceptions, Service (via interface+impl), DTOs, Controller, Exception Handler, Configuration, JSON model tests, Repository IT, Service test, Controller test, AGENTS.md update.

**Issues (-3):**
- Creating an unnecessary interface doubles the artifact count without value.
- No `DeviceTest.java` (entity unit test mentioned but entity-level testing is unusual and suggests possible confusion).
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

### 7. `rq-deepseek-3.2/devices-plan` — DeepSeek 3.2
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

### 8. `rq-qwen-turbo/devices-plan` — Qwen Turbo
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

### Common Strengths Across Top Plans (Opus, Kimi, Sonnet, GLM)
- All use `BIGSERIAL`/`Long` IDs consistent with the existing codebase.
- All use `TIMESTAMPTZ`.
- All specify package-protected repositories.
- All use `@Configuration` bean wiring.
- All include keyset pagination.
- All include JSON model tests, Repository IT, service unit test, and controller test.
- All include an AGENTS.md update step.

### Recurring Issues Across Multiple Plans
| Issue | Plans Affected |
|-------|---------------|
| Cross-package `JpaSensorRepository` injection (layering violation) | Kimi, GLM, MiniMax |
| UUID instead of BIGSERIAL | Baseline |
| Lombok usage | Baseline |
| `TIMESTAMP` instead of `TIMESTAMPTZ` | Baseline, DeepSeek |
| Missing named FK/PK constraint naming | Devstral, DeepSeek, partial in others |
| `DeviceController` wired manually as a bean in `@Configuration` | Devstral, Kimi |
| `DeviceService` interface + `DeviceServiceImpl` | Devstral |
| DTOs passed to service layer | Baseline |
| No JSON model tests | DeepSeek, Qwen |
| No AGENTS.md update | DeepSeek, Qwen, partial |

### Key Differentiators
- **Opus** excels at the cross-cutting concerns section, making architectural rationale explicit and actionable. Along with GLM, it uses `@ManyToMany` directly on the entity with `@JoinTable` — no separate junction entity class.
- **Sonnet** has the most complete `DeviceConfiguration` code, the cleanest bulk-assign API design (`PUT /{id}/sensors` with body enabling unassignment), and the strongest cross-cutting concerns discussion.
- **GLM** provides the most complete Java code examples overall, including transaction details, controller methods with OpenAPI annotations, `@Query`/`@Param` repository methods, configuration, and exception handling — the most immediately actionable plan for implementation. Like Opus, uses `@ManyToMany` directly on the entity.
- **GLM** is the only plan to include `@EntityGraph` for avoiding N+1 queries on the sensor relationship and a unique constraint on device name in the DB schema.
- **Kimi** stands out for its detailed API Specification section with full request/response JSON examples for all endpoints. However, most implementation sections (service, repository, controller, entity) are prose bullet lists with no code.
- **MiniMax** is the most concise correct plan — covers all required artifacts but lacks implementation detail beyond DTOs.
- **DeepSeek** introduces multiple invented requirements (optimistic locking, soft deletes, sensor exclusivity constraint) that would derail implementation.
- **Qwen** is effectively unusable as an implementation guide.
