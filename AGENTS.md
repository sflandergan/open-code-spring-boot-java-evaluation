# Project structure
- Use a domain-driven design approach
- Keep track of the features in the AGENTS.md file (package, base URL, entities only)
- Use a package by feature approach bundling rest controllers, services, repositories, models, configurations
  - **Each feature MUST have its own dedicated package** under `de.sfl.<feature-name>`
  - **NEVER mix features in the same package** - each feature is completely self-contained
  - Package naming: use lowercase, plural or singular based on domain concept (e.g., `sensors`, `sensorgroups`, `users`, `orders`)
  - All feature code (controllers, services, repositories, DTOs, exceptions, configurations) goes in the feature package
  - Example: `sensors/` and `sensorgroups/` are separate packages, not mixed together
  - **When creating a new feature, ALWAYS create a new package** - do not add to existing feature packages
  - Exception: User may explicitly instruct to add functionality to an existing package if it extends that feature

## Features
- **Sensor Management**
  - Package: `de.sfl.sensors`
  - Base URL: `/api/sensors`
  - Entities: Sensor (id, name, type, capabilities)

# Managing AGENTS.md Size
- **Current status**: This file fits comfortably in AI context windows
- **When to split**: Consider restructuring when this file reaches **~800-1000 lines** or **~40-50K tokens**
- **Splitting strategy**:
    - Keep AGENTS.md as the high-level index with feature list and pointers
    - Move detailed patterns to `docs/patterns/` (already doing this well)
    - Always maintain clear pointers from AGENTS.md to detailed documentation

# Pattern Documentation
Detailed patterns and guidelines are documented in `docs/patterns/`:
- **CONTROLLER_TESTING.md** - Controller testing patterns with MockMvc, validation, and error handling
- **DATABASE_SCHEMA.md** - Database schema design guidelines (naming conventions, keys, indexing, data types, JPA entity mapping)
- **DEPENDENCY-MANAGEMENT.md** - Maven dependency and version management
- **ENTITY_TEST_DATA.md** - Test subclass pattern for creating entity test data without reflection
- **JSON-MODEL-TESTING.md** - Marshalling and unmarshalling tests for JSON model classes
- **PAGINATION.md** - Pagination implementation patterns
- **REPOSITORY_TESTING.md** - Repository integration testing patterns and base class usage
- **SERVICE_TESTING.md** - Service unit testing patterns with mocking and test coverage guidelines

# General
- When you've adapted a single class or implemented a new test, run the test with `mvn test -Dtest=YourTestClassName`
- When there is a corresponding integration test, run it with `mvn verify -Dit.test=YourIntegrationTestClassName`
- Run all tests with `mvn verify` when you've changed large parts of the code
- When you should reproduce a bug, write a test that fails and then fix the bug
- If `mvn verify` fails, check the container logs in `target/application-container.log` and `target/postgres-container.log` for debugging

# Code Style and Structure
- Write code like a book with clear narrative flow from high-level intent to low-level details:
  - **Short, focused methods**: Each method does one thing well
  - **Decompose long methods**: Break into smaller, well-named helper methods
  - **Orchestrating methods**: Main method reads like a table of contents, delegating to helpers
  - **Extract complex conditions**: Long boolean expressions become well-named methods
  - **Descriptive names**: Method names clearly express intent
- Use comments and JavaDoc sparsely - prefer self-documenting code with clear naming
- Comments should explain "why" not "what"
- Document class purpose when helpful, but well-named methods/parameters should be self-explanatory
- Clean up unused imports after code changes
- Only include imports actually used
- Organize imports: standard library, third-party, project imports
- Only create interface when there will be multiple implementations or requirements from a framework
- Do not add a `Impl` suffix to interface implementation. Use a name based on the technology used to implement the interface, e.g. `JpaUserRepository` or `InMemoryUserRepository` 

# Layered Architecture
- Follow a strict layered architecture: **Interfaces Layer** → calls → **Services Layer** → uses → **Infrastructure Layer**
- Services should not directly access repositories from other service packages 
- Interfaces should not directly access repositories; they must use services
- Repositories must be package-protected (no access modifier) to enforce they are only accessed within their feature package
- DTOs should only be used inside the Rest Controller layer
- Services should work with the entity model 

# Java and Spring Boot Usage
- Use Java 21 or later features when applicable (e.g., records, sealed classes, pattern matching)
- Use var when assigning a variable to a constructor call
- Don't use var when you can directly assign to a field
- Leverage Spring Boot 3.x features and best practices
- Use Spring Boot starters for quick project setup and dependency management
- **When adding new dependencies** - see `docs/patterns/DEPENDENCY-MANAGEMENT.md` for version property management

# Bean configuration
- Use constructor injection over field injection for better testability
- **Never use `@Component`, `@Service`, or `@Repository` annotations** on classes
- **Always use `@Configuration` classes** to explicitly define beans and manage their lifecycles
- This provides explicit control over bean creation, dependencies, and conditional logic
- Reuse configuration classes in tests if possible

# Transaction Management
- Transactions should be started on service layer 
- Multiple service calls can participate in the same transaction

# Rest Controllers
- Use DTOs for request and response
- Name DTOs with `Dto` suffix (e.g., `UserDto`, `OrderDto`)
- Implement input validation using Bean Validation (e.g., @Valid, custom validators)
- Implement proper exception handling using @ControllerAdvice and @ExceptionHandler
- Apply a RESTful API design (proper use of HTTP methods, status codes, etc.)
- Return a http status 409 when a resource already exists
- Return a http status 404 when a resource does not exist
- Return a http status 400 when a request is invalid
- Return a http status 500 when an internal server error occurs
- Use Springdoc OpenAPI (formerly Swagger) for API documentation
- Implement a toEntity method in the DTOs to convert the DTO to an entity if needed
- **When working with JSON DTOs** - see `docs/patterns/JSON-MODEL-TESTING.md` for testing JSON serialization/deserialization

## Pagination
- Prefer **keyset pagination** (seek method) over offset-based pagination for better performance
- See `docs/patterns/PAGINATION.md` for detailed implementation patterns and examples

# Configuration and Properties
- Use application.yaml for configuration.
- Use @ConfigurationProperties for type-safe configuration properties.

# HTTP Client Usage
- Use Spring's `RestClient` (Spring Boot 3.2+) or `WebClient` for calling external HTTP services
- Create HTTP client beans in a `@Configuration` class within the feature package

## Timeout Configuration
- **Always set timeouts** to prevent indefinite blocking
- Configure three types of timeouts:
  - **Connection timeout**: Time to establish connection (e.g., 5 seconds)
  - **Read timeout**: Time to read response (e.g., 10 seconds)
  - **Response timeout**: Overall request timeout (e.g., 15 seconds)
- Make timeout values configurable via `@ConfigurationProperties`
- Example configuration in `application.yaml`:
  ```yaml
  external-service:
    base-url: https://api.example.com
    timeout:
      connection: 5s
      read: 10s
      response: 15s
  ```

## Error Handling
- **Ask the user** how to handle errors for each external service integration:
  - **Option 1: Fallback strategy** - Use cached data, default values, or alternative service
  - **Option 2: Propagate error** - Throw custom exception and let caller handle it
  - **Option 3: Circuit breaker** - Use Resilience4j to prevent cascading failures
  - **Option 4: Retry** - Use Resilience4j to retry failed requests
- Log all external service errors at ERROR level with context (URL, status code, error message)
- Wrap external service exceptions in custom domain exceptions (e.g., `ExternalServiceException`)
- Never expose raw HTTP client exceptions to the REST API layer
- Return appropriate HTTP status codes:
  - 503 Service Unavailable when external service is down
  - 504 Gateway Timeout when external service times out
  - 500 Internal Server Error for unexpected errors

## Best Practices
- Create a dedicated client class per external service (e.g., `WeatherApiClient`)
- Place client classes in the infrastructure layer within the feature package
- Use `@ConfigurationProperties` for external service configuration
- Add retry logic using Resilience4j `@Retry` annotation when appropriate
- Test HTTP clients using WireMock or MockWebServer in integration tests
- Document external service dependencies in feature package documentation

# Database Schema and Migrations
- **Follow database schema design guidelines** - see `docs/patterns/DATABASE_SCHEMA.md` for comprehensive rules on naming conventions, keys, indexing, data types, and JPA entity mapping
- Use Flyway for database schema versioning and migrations
- Name migration files: `V<timestamp with format VYYYYMMDDHHmm>_description.sql` (e.g., `V202511140900_add_asset_table.sql`)
- Place migration files in `src/main/resources/db/migration/`
- Keep migrations idempotent when possible
- Never modify existing migration files after they've been applied

# Testing
- Write unit tests using JUnit 5 and Spring Boot Test
- Use MockMvc for testing web layers
- **Always create tests when implementing new features**
- **Always update tests when modifying existing code**

## Entity Test Data Creation
- **When you need to create entity instances with specific field values in tests** (e.g., entities with private setters, immutable fields, or generated IDs)
- **Never use reflection** to set entity fields in tests
- See `docs/patterns/ENTITY_TEST_DATA.md` for the recommended test subclass pattern and detailed examples

## Controller Tests
- **Every controller must have a controller test** - see `docs/patterns/CONTROLLER_TESTING.md` for comprehensive patterns
- Test class name: `<ControllerName>Test`
- Use `@WebMvcTest` to test only the web layer
- Use `@MockBean` to mock service dependencies
- Test all endpoints (GET, POST, PUT, DELETE)
- Test validation (400 Bad Request for invalid input)
- Test error handling (404 Not Found, 409 Conflict, etc.)
- Test pagination parameters
- Verify service method calls using `verify()`
- Use descriptive test method names: `methodName_shouldDoSomething_whenCondition`
- Example: `createSensor_shouldReturn400WhenNameIsBlank`

## Service Tests
- **Every service must have a unit test** - see `docs/patterns/SERVICE_TESTING.md` for comprehensive patterns
- Test class name: `<ServiceName>Test`
- Use Mockito for mocking dependencies
- Test all public methods with happy path, edge cases, and error scenarios
- Use descriptive test method names: `methodName_shouldDoSomething_whenCondition`

## Repository Integration Tests
- **Every repository must have an integration test** - see `docs/patterns/REPOSITORY_TESTING.md` for details
- All repository integration tests must extend the `RepositoryIT` abstract base class
- Test class name: `<RepositoryName>IT`

## JSON Model Classes
- **Every JSON model class** must have marshalling and unmarshalling tests
- See `docs/patterns/JSON-MODEL-TESTING.md` for detailed guidance, templates, and examples

# Logging and Monitoring
- Use SLF4J for logging.
- Keep logging in the service layer 
- Write INFO level logs when entities are modified
- Write DEBUG level logs when entities are retrieved
- Write WARN level logs when recoverable errors occur
- Write ERROR level logs when unrecoverable errors occur
- Use Spring Boot Actuator for application monitoring and metrics.
- **Use trace IDs to follow requests** - leverage Spring Boot's built-in tracing (Micrometer Tracing) to track the flow of a single request across multiple services and log statements
- Include trace IDs in log output using MDC (Mapped Diagnostic Context) or Spring Boot's automatic trace ID propagation
- Trace IDs allow debugging and monitoring without exposing user information

## GDPR Compliance
- **Never log personal data or user information** to ensure GDPR compliance
- **Never log user IDs** - this includes primary keys, usernames, email addresses, or any other user identifiers
- Log only technical information: entity types, operation types, counts, status codes, error types
- Example compliant log: `"Sensor created successfully"` or `"Failed to update sensor: validation error"`
- Example non-compliant log: `"Sensor created for user 12345"` or `"User john.doe@example.com logged in"`
- When debugging is necessary, use anonymized identifiers or aggregate metrics instead of real user data

# Maven Dependency Management
- **⚠️ REQUIRED: Read `docs/patterns/DEPENDENCY-MANAGEMENT.md` before adding new dependencies**
- Always define dependency versions as properties in the `<properties>` section of pom.xml
- Use the naming pattern: `<artifactId>.version` for property names
- Reference properties using `${property.name}` syntax in dependency declarations
- Dependencies managed by Spring Boot parent (like `spring-boot-starter-web`) don't need explicit version properties
- See `docs/patterns/DEPENDENCY-MANAGEMENT.md` for detailed guidelines, examples, and troubleshooting
