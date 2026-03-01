# Spring Boot Template

A production-ready Spring Boot template following domain-driven design principles with a package-by-feature approach. This template provides a solid foundation for building RESTful web services with best practices baked in.

## Table of Contents

- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Creating a New Project from This Template](#creating-a-new-project-from-this-template)
  - [Running the Application Locally](#running-the-application-locally)
- [Technologies](#technologies)
- [Building and Testing](#building-and-testing)
- [Working with AGENTS.md](#working-with-agentsmd)
- [Developing Features with AI Assistants](#developing-features-with-ai-assistants)
- [Features](#features)

## Architecture

This template follows a **layered architecture** with clear separation of concerns:

- **Interfaces Layer** (REST Controllers) - Handles HTTP requests/responses
- **Services Layer** - Contains business logic
- **Infrastructure Layer** (Repositories) - Handles data persistence

The project uses a **package-by-feature** approach, where each feature is self-contained in its own package with all related components (controllers, services, repositories, DTOs, configurations).

## Getting Started

### Prerequisites

- **Java 21** or later
- **Maven 3.9+**
- **Docker** (for integration tests and containerization)

### Running the Application Locally

1. **Start the PostgreSQL database** using Maven:
   ```bash
   mvn docker:start -Ddocker.filter=postgres
   ```
   
   This starts a PostgreSQL container with the configuration from `pom.xml` (database: `testdb`, user: `testuser`, password: `testpass`).

2. **Configure database connection** (optional):
   
   The default configuration in `config/application.yml` works with the PostgreSQL container started above.

3. **Run the application**:

   **Option A: Using Maven**
   ```bash
   mvn spring-boot:run
   ```

   **Option B: From your IDE**
   - Open the main application class (e.g., `TemplateApplication.java`)
   - Run it as a Java application (usually via right-click → Run)

4. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Actuator Health: http://localhost:8080/actuator/health

5. **Stop the database** when done:
   ```bash
   mvn docker:stop -Ddocker.filter=postgres
   ```

## Technologies

### Java 21

This project uses Java 21 with modern language features:
- **Records** for immutable data carriers (DTOs, entities)
- **Sealed classes** for controlled type hierarchies
- **Pattern matching** for cleaner conditional logic
- **Text blocks** for multi-line strings

### Spring Boot 3.5.7

The application leverages Spring Boot 3.x features:
- **Auto-configuration** for rapid development
- **Dependency injection** via constructor injection
- **Spring Boot Starters** for streamlined dependency management
- **Configuration properties** with `@ConfigurationProperties` for type-safe configuration

### Spring Web MVC

RESTful API implementation using Spring Web MVC:
- **REST Controllers** with `@RestController` and `@RequestMapping`
- **Request validation** using Bean Validation (`@Valid`, `@NotNull`, etc.)
- **Exception handling** with `@ControllerAdvice` and `@ExceptionHandler`
- **Content negotiation** for JSON responses
- **Springdoc OpenAPI** for automatic API documentation at `/swagger-ui.html`

### PostgreSQL 17

PostgreSQL is used as the primary relational database:
- **Spring Data JPA** for ORM and repository abstraction
- **JDBC driver** for database connectivity
- **Connection pooling** via HikariCP (default in Spring Boot)

### Flyway

Database migration management with Flyway:
- **Version-controlled schema changes** in `src/main/resources/db/migration/`
- **Migration naming convention**: `VYYYYMMDDHHmm_description.sql` (e.g., `V202511140900_add_sensor_table.sql`)
- **Automatic migration execution** on application startup
- **Idempotent migrations** for safe re-execution

### TestContainers

Integration testing with real database instances:
- **PostgreSQL TestContainers** for repository integration tests
- **Automatic container lifecycle management** (start/stop)
- **Isolated test environments** with no shared state
- **Base class `RepositoryIT`** for consistent test setup
- See `docs/patterns/REPOSITORY_TESTING.md` for detailed patterns

### Docker Maven Plugin

Fabric8 Docker Maven Plugin for containerization and integration testing:
- **Multi-architecture builds** (linux/amd64, linux/arm64) using BuildKit
- **Automated container orchestration** during Maven lifecycle
- **PostgreSQL container** for integration tests
- **Application container** with debug port (5005) enabled
- **Container logs** captured in `target/postgres-container.log` and `target/application-container.log`
- **Health checks** via Spring Boot Actuator endpoints

## Building and Testing

### Build the Application

```bash
# Compile and package (skips tests)
mvn clean package -DskipTests

# Build Docker image
mvn clean package
```

The Docker image will be built as `sensorinsight/template:latest` and `sensorinsight/template:0.1.0-SNAPSHOT`.

### Running Tests

#### Unit Tests Only

```bash
# Run all unit tests
mvn test

# Run a specific test class
mvn test -Dtest=SensorControllerTest

# Run a specific test method
mvn test -Dtest=SensorControllerTest#createSensor_shouldReturn201
```

#### Integration Tests

Integration tests use TestContainers to spin up real PostgreSQL instances:

```bash
# Run all tests (unit + integration)
mvn verify

# Run a specific integration test
mvn verify -Dit.test=JpaSensorRepositoryIT
```

#### Full Build with Docker Integration Tests

```bash
# Run all tests including Docker-based integration tests
mvn clean verify
```

This will:
1. Compile the application
2. Run unit tests
3. Package the application as a JAR
4. Build a Docker image
5. Start PostgreSQL and application containers
6. Run integration tests against the containerized application
7. Stop and remove containers

**Note**: Container logs are saved to:
- `target/postgres-container.log` - PostgreSQL container logs
- `target/template-container.log` - Application container logs

These logs are invaluable for debugging test failures.

### Debugging Integration Tests

If `mvn verify` fails, check the container logs:

```bash
# View PostgreSQL logs
cat target/postgres-container.log

# View application logs
cat target/template-container.log
```

### Manual Docker Container Management

```bash
# Start containers manually
mvn docker:start

# Stop containers manually
mvn docker:stop

# View running containers
docker ps
```

## Working with AGENTS.md

### For Developers

The `AGENTS.md` file contains comprehensive instructions for AI coding assistants (like GitHub Copilot, Cursor, Windsurf or other LLM-based tools). This file helps maintain consistency when AI assistants generate or modify code.

**Important Guidelines:**

1. **Keep AGENTS.md Updated**: When you introduce new technologies, architectural decisions, or implementation patterns, update `AGENTS.md` accordingly.

2. **Document Key Decisions**: Include:
   - New frameworks or libraries
   - Architectural patterns (e.g., event-driven, CQRS)
   - Naming conventions
   - Testing strategies
   - Error handling approaches
   - Security practices

3. **Be Concise**: AGENTS.md has size limitations (typically 8-16KB depending on the AI tool). Focus on:
   - High-level architectural decisions
   - Non-obvious patterns and conventions
   - Common pitfalls and how to avoid them
   - Links to detailed documentation in `docs/patterns/`

4. **Reference External Docs**: For detailed patterns, create separate files in `docs/patterns/` and reference them from AGENTS.md. Examples:
   - `docs/patterns/PAGINATION.md` - Keyset pagination implementation
   - `docs/patterns/REPOSITORY_TESTING.md` - Repository testing guidelines

5. **Review Regularly**: As the project evolves, review AGENTS.md to ensure it reflects current practices and remove outdated information.

### What to Include in AGENTS.md

✅ **Do Include:**
- Project structure and package organization
- Layered architecture rules
- Code style preferences (method length, naming, etc.)
- Testing requirements (when to write tests, naming conventions)
- Technology-specific guidelines (Spring Boot, JPA, etc.)
- Common commands (build, test, run)

❌ **Don't Include:**
- Detailed API documentation (use Swagger/OpenAPI)
- Step-by-step tutorials (use README.md or separate docs)
- Boilerplate code examples (link to pattern docs instead)
- Project history or changelog (use Git history)

### Size Management Tips

If AGENTS.md grows too large:
1. Move detailed patterns to `docs/patterns/` and link them
2. Remove redundant information
3. Use bullet points instead of paragraphs
4. Focus on "what" and "why", not "how" (code examples in pattern docs)

## Developing Features with AI Assistants

This template is designed to work seamlessly with AI coding assistants. You can develop complete features with a single, well-structured prompt.

### Sample Prompt

Here's an example prompt that demonstrates how to request a complete feature implementation:

```
Add a new feature for managing sensor groups.
It should have a REST API to:
- Create sensor groups
- Assign sensors to sensor groups (idempotent put operation)
- Update sensor groups
- Delete sensor groups
A sensor group has the following properties:
- Name
```

The AI assistant will:
1. Create the feature package following the package-by-feature structure
2. Implement REST controllers with proper HTTP methods and status codes
3. Create service layer with business logic
4. Set up JPA repositories with appropriate queries
5. Define DTOs for requests and responses
6. Create database migration scripts
7. Write controller tests and repository integration tests
8. Update AGENTS.md with the new feature documentation

### Tips for Effective Prompts

- **Be specific about the API operations** you need (CRUD, custom queries, etc.)
- **List entity properties** clearly, including data types if they're not obvious
- **Mention relationships** between entities (e.g., "A sensor group can contain multiple sensors")
- **Specify validation rules** if you have specific requirements (e.g., "Name must be unique")
- **Request additional features** like pagination, filtering, or sorting if needed

The AI assistant will follow all guidelines in `AGENTS.md` to ensure consistency with the project's architecture and coding standards.

## Features

This template includes an example features to demonstrate the architecture:

### Sensor Management
- **Package**: `sfl.sensorinsight.template.sensors`
- **Base URL**: `/api/sensors`
- **Entities**: Sensor (id, name, type, capabilities)

Each feature follows the same structure:
- REST Controller for HTTP endpoints
- Service for business logic
- Repository for data access
- DTOs for request/response
- Integration tests for repositories
- Unit tests for controllers

## Additional Resources

- **API Documentation**: http://localhost:8080/swagger-ui.html (when running)
- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Spring Data JPA**: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- **Flyway Documentation**: https://flywaydb.org/documentation/
- **TestContainers**: https://www.testcontainers.org/
