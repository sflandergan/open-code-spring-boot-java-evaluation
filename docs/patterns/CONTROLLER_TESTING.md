# Controller Testing

This document describes the patterns and guidelines for writing tests for REST controllers in the Spring Boot Java template.

## Overview

Every controller must have a corresponding test to ensure the web layer behaves correctly, validates input properly, handles errors appropriately, and integrates with services as expected.

## Test Class Naming

- **Pattern**: `<ControllerClassName>Test.java`
- **Examples**:
  - `SensorControllerTest.java` for `SensorController`
  - `UserControllerTest.java` for `UserController`

## Test Annotation

Use `@WebMvcTest` to test only the web layer:

```java
@WebMvcTest(SensorController.class)
class SensorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SensorService sensorService;
    
    // tests...
}
```

**Benefits of `@WebMvcTest`:**
- Loads only the web layer (controllers, filters, advice)
- Does not load services, repositories, or database configuration
- Fast test execution
- Forces proper layering through mocking

## Mocking Dependencies

- Use `@MockBean` to mock service dependencies
- Mock all services that the controller depends on
- Do not mock repositories directly - controllers should only interact with services

```java
@MockBean
private SensorService sensorService;

@MockBean
private UserService userService;
```

## Test Coverage Requirements

Test all aspects of controller behavior:

### 1. All Endpoints

Test every HTTP endpoint with various scenarios:

```java
@Test
void getAllSensors_shouldReturnListOfSensors() throws Exception {
    // Arrange
    var sensors = List.of(createSensor(1L), createSensor(2L));
    when(sensorService.findAll()).thenReturn(sensors);
    
    // Act & Assert
    mockMvc.perform(get("/api/sensors"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));
    
    verify(sensorService, times(1)).findAll();
}

@Test
void getSensorById_shouldReturnSensor_whenFound() throws Exception {
    // Arrange
    var sensor = createSensor(1L, "Sensor 1");
    when(sensorService.findById(1L)).thenReturn(sensor);
    
    // Act & Assert
    mockMvc.perform(get("/api/sensors/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Sensor 1"));
    
    verify(sensorService).findById(1L);
}

@Test
void createSensor_shouldCreateSensor_whenValidInput() throws Exception {
    // Arrange
    var sensorDto = new SensorDto("New Sensor", "sensor");
    var createdSensor = createSensor(1L, "New Sensor");
    when(sensorService.create(any())).thenReturn(createdSensor);
    
    // Act & Assert
    mockMvc.perform(post("/api/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensorDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("New Sensor"));
    
    verify(sensorService).create(any());
}

@Test
void deleteSensor_shouldRemoveSensor() throws Exception {
    // Arrange
    doNothing().when(sensorService).delete(1L);
    
    // Act & Assert
    mockMvc.perform(delete("/api/sensors/1"))
        .andExpect(status().isNoContent());
    
    verify(sensorService).delete(1L);
}
```

### 2. Input Validation

Test validation rules for request bodies and parameters:

```java
@Test
void createSensor_shouldReturn400_whenNameIsBlank() throws Exception {
    // Arrange
    var invalidDto = new SensorDto("", "sensor");
    
    // Act & Assert
    mockMvc.perform(post("/api/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());
    
    verify(sensorService, never()).create(any());
}

@Test
void createSensor_shouldReturn400_whenRequiredFieldIsMissing() throws Exception {
    // Arrange
    var invalidJson = "{\"name\": \"Sensor\"}"; // missing 'type'
    
    // Act & Assert
    mockMvc.perform(post("/api/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
}
```

### 3. Error Handling

Test exception handling and error responses:

```java
@Test
void getSensorById_shouldReturn404_whenSensorNotFound() throws Exception {
    // Arrange
    when(sensorService.findById(999L)).thenThrow(new SensorNotFoundException("Sensor not found"));
    
    // Act & Assert
    mockMvc.perform(get("/api/sensors/999"))
        .andExpect(status().isNotFound());
    
    verify(sensorService).findById(999L);
}

@Test
void createSensor_shouldReturn409_whenSensorAlreadyExists() throws Exception {
    // Arrange
    var sensorDto = new SensorDto("Existing Sensor", "sensor");
    when(sensorService.create(any())).thenThrow(new SensorAlreadyExistsException("Sensor already exists"));
    
    // Act & Assert
    mockMvc.perform(post("/api/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensorDto)))
        .andExpect(status().isConflict());
}

@Test
void createSensor_shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
    // Arrange
    var sensorDto = new SensorDto("Sensor", "sensor");
    when(sensorService.create(any())).thenThrow(new RuntimeException("Unexpected error"));
    
    // Act & Assert
    mockMvc.perform(post("/api/sensors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sensorDto)))
        .andExpect(status().isInternalServerError());
}
```

### 4. Pagination Parameters

Test pagination query parameters when applicable:

```java
@Test
void getAllSensors_shouldSupportPaginationParameters() throws Exception {
    // Arrange
    var page = Page.of(List.of(createSensor(1L)), 0, 10, 1);
    when(sensorService.findAll(anyInt(), anyInt())).thenReturn(page);
    
    // Act & Assert
    mockMvc.perform(get("/api/sensors?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalElements").value(1));
    
    verify(sensorService).findAll(0, 10);
}

@Test
void getAllSensors_shouldUseDefaultPagination_whenParametersNotProvided() throws Exception {
    // Arrange
    var page = Page.of(List.of(createSensor(1L)), 0, 20, 1);
    when(sensorService.findAll(anyInt(), anyInt())).thenReturn(page);
    
    // Act & Assert
    mockMvc.perform(get("/api/sensors"))
        .andExpect(status().isOk());
    
    verify(sensorService).findAll(0, 20); // default values
}
```

## Test Function Naming

Use descriptive test method names following the pattern:

```java
void methodName_shouldBehavior_whenCondition()
```

**Examples:**
```java
void getAllSensors_shouldReturnEmptyList_whenNoSensorsExist()
void createSensor_shouldReturn400_whenNameExceedsMaxLength()
void updateSensor_shouldUpdateSensor_whenValidInputProvided()
void deleteSensor_shouldReturn404_whenSensorNotFound()
```

## Verifying Service Calls

Always verify that service methods are called with expected parameters:

```java
verify(sensorService, times(1)).findAll();
verify(sensorService).findById(1L);
verify(sensorService, never()).create(any()); // should not be called
```

## MockMvc Usage

Use MockMvc's fluent API for readable tests:

```java
// GET request
mockMvc.perform(get("/api/sensors/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(1));

// POST request
mockMvc.perform(post("/api/sensors")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
    .andExpect(status().isCreated());

// PUT request
mockMvc.perform(put("/api/sensors/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
    .andExpect(status().isOk());

// DELETE request
mockMvc.perform(delete("/api/sensors/1"))
    .andExpect(status().isNoContent());
```

## JSON Assertions

Use `jsonPath` for asserting JSON response content:

```java
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.name").value("Sensor"))
.andExpect(jsonPath("$.items").isArray())
.andExpect(jsonPath("$.items.length()").value(3))
.andExpect(jsonPath("$.items[0].id").value(1))
.andExpect(jsonPath("$.active").value(true))
.andExpect(jsonPath("$.price").value(19.99))
.andExpect(jsonPath("$.tags").isEmpty());
```

## Complete Example

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensorController.class)
class SensorControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SensorService sensorService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void getAllSensors_shouldReturnListOfSensors() throws Exception {
        // Arrange
        var sensors = List.of(
            createSensor(1L, "Sensor 1"),
            createSensor(2L, "Sensor 2")
        );
        when(sensorService.findAll()).thenReturn(sensors);
        
        // Act & Assert
        mockMvc.perform(get("/api/sensors"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Sensor 1"));
        
        verify(sensorService, times(1)).findAll();
    }
    
    @Test
    void getSensorById_shouldReturn404_whenSensorNotFound() throws Exception {
        // Arrange
        when(sensorService.findById(999L)).thenThrow(new SensorNotFoundException("Sensor not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/sensors/999"))
            .andExpect(status().isNotFound());
        
        verify(sensorService).findById(999L);
    }
    
    @Test
    void createSensor_shouldCreateSensor_whenValidInput() throws Exception {
        // Arrange
        var sensorDto = new SensorDto("New Sensor", "sensor");
        var createdSensor = createSensor(1L, "New Sensor", "sensor");
        when(sensorService.create(any())).thenReturn(createdSensor);
        
        // Act & Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sensorDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("New Sensor"));
        
        verify(sensorService).create(any());
    }
    
    @Test
    void createSensor_shouldReturn400_whenNameIsBlank() throws Exception {
        // Arrange
        var invalidDto = new SensorDto("", "sensor");
        
        // Act & Assert
        mockMvc.perform(post("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest());
        
        verify(sensorService, never()).create(any());
    }
    
    @Test
    void deleteSensor_shouldRemoveSensor() throws Exception {
        // Arrange
        doNothing().when(sensorService).delete(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/sensors/1"))
            .andExpect(status().isNoContent());
        
        verify(sensorService).delete(1L);
    }
    
    private Sensor createSensor(Long id) {
        return createSensor(id, "Test Sensor", "sensor");
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

1. **Test the contract, not the implementation**: Focus on HTTP behavior, not internal logic
2. **Mock at the service layer**: Controllers should only depend on services, not repositories
3. **Test all HTTP methods**: GET, POST, PUT, PATCH, DELETE
4. **Test all status codes**: 200, 201, 204, 400, 404, 409, 500, etc.
5. **Verify service interactions**: Always use `verify()` to ensure services are called correctly
6. **Use ObjectMapper**: Serialize/deserialize DTOs consistently
7. **Test headers**: Verify Content-Type, Location (for POST), etc.
8. **Isolation**: Each test should be independent
9. **Use Mockito**: Use `@MockBean` for mocking and Mockito's `when()`, `verify()`, `times()`, `never()` for stubbing and verification

## Related Documentation

- **SERVICE_TESTING.md** - Service unit testing patterns
- **JSON-MODEL-TESTING.md** - Testing JSON marshalling/unmarshalling for DTOs
- **AGENTS.md** - Main project guidelines including AssertJ assertion patterns
