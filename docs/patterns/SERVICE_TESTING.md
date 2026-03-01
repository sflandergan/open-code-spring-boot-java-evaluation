# Service Unit Testing

This document describes the patterns and guidelines for writing unit tests for service classes in the Spring Boot Java template.

## Overview

Every service class MUST have a corresponding unit test to ensure business logic is correct and maintainable. Service tests are pure unit tests that use mocks for all dependencies, allowing fast execution and focused testing of service logic.

## Test Class Naming

- **Pattern**: `<ServiceClassName>Test.java`
- **Examples**: 
  - `SensorServiceTest.java` for `SensorService`
  - `TaskAssignmentServiceTest.java` for `TaskAssignmentService`

## Test Structure

### Dependencies and Mocking

- Use **Mockito** for mocking dependencies
- Mock all service dependencies (repositories, other services, external clients)
- Use constructor injection to provide mocks to the service under test

### Clock Injection for Time-Dependent Logic

When services have time-dependent logic, inject a `Clock` instance to enable deterministic testing:

```java
class TaskAssignmentServiceTest {
    private final TaskAssignmentRepository repository = mock(TaskAssignmentRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
    private final TaskAssignmentService service = new TaskAssignmentService(repository, clock);
    
    @Test
    void findTaskHistory_shouldReturnTasksWithinDateRange() {
        // Arrange
        when(repository.findByMattermostUserIdAndDateRange(any(), any(), any())).thenReturn(List.of(task));
        
        // Act
        var result = service.findTaskHistory(userId, 14);
        
        // Assert
        assertThat(result).hasSize(1);
        verify(repository).findByMattermostUserIdAndDateRange(userId, startDate, endDate);
    }
}
```

## Test Coverage Requirements

Test all public methods with various scenarios:

### 1. Happy Path
- Test the normal, expected flow with valid inputs
- Verify correct return values and side effects

### 2. Edge Cases
- Empty lists or collections
- Null values (when applicable)
- Boundary conditions (min/max values, limits)
- Single vs. multiple items

### 3. Error Cases
- Exceptions thrown by dependencies
- Validation failures
- Business rule violations
- Resource not found scenarios

## Test Function Naming

Use descriptive test method names following the pattern:

```java
void methodName_shouldDoSomething_whenCondition()
```

**Examples**:
```java
void createSensor_shouldSaveSensor_whenValidInputProvided()
void findSensorById_shouldThrowException_whenSensorNotFound()
void updateSensor_shouldUpdateOnlyChangedFields()
void deleteSensor_shouldNotDelete_whenSensorHasActiveAssignments()
```

## Assertions

- **Always use AssertJ** for assertions (see main AGENTS.md for AssertJ patterns)
- Verify mock interactions using Mockito's `verify()` to ensure dependencies are called correctly
- Use `assertThat()` for fluent, readable assertions

## Example Test Class

```java
import org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SensorServiceTest {
    
    private final JpaSensorRepository sensorRepository = mock(JpaSensorRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
    private final SensorService service = new SensorService(sensorRepository, clock);
    
    @Test
    void findAll_shouldReturnAllSensors() {
        // Arrange
        var sensors = List.of(
            createSensor(1L, "Sensor 1"),
            createSensor(2L, "Sensor 2")
        );
        when(sensorRepository.findAll()).thenReturn(sensors);
        
        // Act
        var result = service.findAll();
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(sensors.get(0), sensors.get(1));
        verify(sensorRepository, times(1)).findAll();
    }
    
    @Test
    void findById_shouldReturnSensor_whenFound() {
        // Arrange
        var sensor = createSensor(1L, "Sensor 1");
        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        
        // Act
        var result = service.findById(1L);
        
        // Assert
        assertThat(result).isEqualTo(sensor);
        verify(sensorRepository).findById(1L);
    }
    
    @Test
    void findById_shouldThrowException_whenSensorNotFound() {
        // Arrange
        when(sensorRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(SensorNotFoundException.class);
        verify(sensorRepository).findById(999L);
    }
    
    @Test
    void create_shouldSaveSensorWithCurrentTimestamp() {
        // Arrange
        var sensor = createSensor("New Sensor");
        var expectedTimestamp = clock.instant();
        when(sensorRepository.save(any())).thenReturn(sensor);
        
        // Act
        var result = service.create(sensor);
        
        // Assert
        assertThat(result).isEqualTo(sensor);
        var captor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(expectedTimestamp);
    }
    
    @Test
    void delete_shouldRemoveSensor_whenExists() {
        // Arrange
        var sensor = createSensor(1L);
        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        doNothing().when(sensorRepository).delete(sensor);
        
        // Act
        service.delete(1L);
        
        // Assert
        verify(sensorRepository).findById(1L);
        verify(sensorRepository).delete(sensor);
    }
    
    private Sensor createSensor(Long id) {
        return createSensor(id, "Test Sensor", "sensor");
    }
    
    private Sensor createSensor(String name) {
        return createSensor(1L, name, "sensor");
    }
    
    private Sensor createSensor(Long id, String name) {
        return createSensor(id, name, "sensor");
    }
    
    private Sensor createSensor(Long id, String name, String type) {
        return new Sensor(id, name, type);
    }
}
```

## Best Practices

1. **Isolation**: Each test should be independent and not rely on other tests
2. **Arrange-Act-Assert**: Structure tests with clear sections for setup, execution, and verification
3. **One assertion per test**: Focus each test on a single behavior (though multiple `assertThat()` calls for the same result are fine)
4. **Mock verification**: Always verify that mocked dependencies are called with expected parameters
5. **Test data helpers**: Create helper functions (like `createSensor()`) to build test data consistently
6. **Avoid reflection**: Never use reflection to set entity fields in tests (see `ENTITY_TEST_DATA.md` for alternatives)

## Related Documentation

- **ENTITY_TEST_DATA.md** - Test subclass pattern for creating entity test data
- **REPOSITORY_TESTING.md** - Repository integration testing patterns
- **JSON-MODEL-TESTING.md** - Testing JSON marshalling/unmarshalling
- **AGENTS.md** - Main project guidelines including AssertJ assertion patterns
