# JSON Model Testing

## Overview
Every JSON model class (POJOs or records with Jackson annotations) must have comprehensive marshalling and unmarshalling tests to ensure proper serialization and deserialization.

## Purpose
- Verify correct mapping between JSON field names (often snake_case) and Java properties (camelCase)
- Validate Jackson annotations (`@JsonProperty`, `@JsonIgnoreProperties`, etc.)
- Ensure data integrity during serialization/deserialization cycles
- Catch breaking changes in JSON structure early
- Document the expected JSON format

## Test Structure

### File Organization
- Place tests in the same package as the model class under `src/test/java/`
- Name test files as `{ModelClassName}Test.java`
- Example: `MattermostPost.java` → `MattermostPostTest.java`

### Required Tests
Each model class should have at least two test methods:

1. **Marshalling Test**: `marshal{ModelName}_shouldSerializeToJsonCorrectly`
   - Creates a model instance with representative data
   - Serializes to JSON string
   - Compares against expected JSON structure
   
2. **Unmarshalling Test**: `unmarshalJson_shouldDeserializeTo{ModelName}Correctly`
   - Defines JSON string input
   - Deserializes to model instance
   - Asserts equality with expected object

## Test Template

```java
package com.example.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YourModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void marshalYourModel_shouldSerializeToJsonCorrectly() throws Exception {
        var model = new YourModel(
            "123",
            "value",
            42
        );
        
        var json = objectMapper.writeValueAsString(model);
        
        var expectedJson = """
            {
                "id":"123",
                "property_name":"value",
                "another_property":42
            }
            """;
        
        assertThat(objectMapper.readTree(json))
            .isEqualTo(objectMapper.readTree(expectedJson));
    }

    @Test
    void unmarshalJson_shouldDeserializeToYourModelCorrectly() throws Exception {
        var json = """
            {
                "id":"123",
                "property_name":"value",
                "another_property":42
            }
            """;
        
        var expected = new YourModel(
            "123",
            "value",
            42
        );
        
        var result = objectMapper.readValue(json, YourModel.class);
        
        assertThat(result).isEqualTo(expected);
    }
}
```

## Best Practices

### JSON Formatting
- **Multi-line format**: Always format JSON with one attribute per line for readability
- **Use text blocks**: Java 15+ text blocks automatically handle indentation
- **Consistent indentation**: Use tabs or spaces consistently (prefer tabs)

**Good Example:**
```java
var expectedJson = """
    {
        "id":"123",
        "property_name":"value",
        "nested":{
            "key":"value"
        }
    }
    """;
```

**Bad Example:**
```java
var expectedJson = "{\"id\":\"123\",\"property_name\":\"value\",\"nested\":{\"key\":\"value\"}}";
```

### Field Naming
- **Use `@JsonProperty`** on fields or record components to map JSON field names
  - This ensures proper serialization/deserialization
  - Example: `@JsonProperty("user_id") String userId` maps snake_case to camelCase
- Verify that `@JsonProperty` annotations correctly map snake_case JSON fields to camelCase Java properties
- Include all fields that are serialized, including computed properties

### Test Data
- Use realistic but simple test data
- Include all required fields
- Consider optional fields (test with and without them)
- Test edge cases like empty strings, null values, empty collections

### Computed Properties
If your model has computed properties (methods that are serialized):
```java
@Test
void marshalModel_shouldIncludeComputedProperties() throws Exception {
    var model = new Model("D");
    
    var json = objectMapper.writeValueAsString(model);
    
    var expectedJson = """
        {
            "type":"D",
            "isDirectMessage":true
        }
        """;
    
    assertThat(objectMapper.readTree(json))
        .isEqualTo(objectMapper.readTree(expectedJson));
}

@Test
void unmarshalJson_shouldComputeProperties() throws Exception {
    var json = "{\"type\":\"D\"}";
    
    var result = objectMapper.readValue(json, Model.class);
    
    assertThat(result.getType()).isEqualTo("D");
    assertThat(result.isDirectMessage()).isTrue();
}
```

### Nested Objects and Collections
For complex nested structures:
```java
@Test
void marshalParent_shouldIncludeNestedStructures() throws Exception {
    var model = new Parent(
        "1",
        List.of(
            new Child("child1"),
            new Child("child2")
        ),
        Map.of("key1", "value1", "key2", "value2")
    );
    
    var json = objectMapper.writeValueAsString(model);
    
    var expectedJson = """
        {
            "id":"1",
            "children":[
                {"name":"child1"},
                {"name":"child2"}
            ],
            "metadata":{
                "key1":"value1",
                "key2":"value2"
            }
        }
        """;
    
    assertThat(objectMapper.readTree(json))
        .isEqualTo(objectMapper.readTree(expectedJson));
}
```

### Assertion Strategy
- Use `objectMapper.readTree()` for comparison to handle JSON formatting differences
- This approach ignores whitespace and attribute ordering
- For stricter testing, compare JSON strings directly

```java
// Flexible comparison (recommended)
assertThat(objectMapper.readTree(json))
    .isEqualTo(objectMapper.readTree(expectedJson));

// Strict comparison (use when order matters)
assertThat(json).isEqualTo(expectedJson);
```

## Running Tests

### Individual Test
```bash
mvn test -Dtest=YourModelTest
```

### All Model Tests
```bash
mvn test -Dtest=*ModelTest
```

### Pattern-based Tests
```bash
mvn test -Dtest=Mattermost*Test
```

## Common Issues and Solutions

### Issue: Test fails with "UnrecognizedPropertyException"
**Cause**: JSON field name doesn't match `@JsonProperty` annotation

**Solution**: Verify JSON field names match the annotations exactly
```java
public record User(
    @JsonProperty("user_id")  // Must match JSON: "user_id"
    String userId
) {}
```

### Issue: Extra fields in serialized JSON
**Cause**: Computed properties or methods are being serialized

**Solution**: 
1. Include them in expected JSON, or
2. Use `@JsonIgnore` to exclude them
```java
public class Model {
    private String type;
    
    @JsonIgnore
    public boolean isSpecial() {
        return "S".equals(type);
    }
}
```

### Issue: Assertion fails due to field ordering
**Cause**: JSON field order differs between expected and actual

**Solution**: Use `readTree()` comparison instead of string comparison
```java
// Good - ignores field order
assertThat(objectMapper.readTree(json))
    .isEqualTo(objectMapper.readTree(expectedJson));

// Bad - sensitive to field order
assertThat(json).isEqualTo(expectedJson);
```

### Issue: Build cache causing test failures
**Cause**: Maven cache contains outdated compiled classes

**Solution**: Clean build before running tests
```bash
mvn clean test -Dtest=YourModelTest
```

## Examples

See existing test files for reference:
- `MattermostPostTest.java` - Basic model with multiple fields
- `MattermostUserTest.java` - Model with optional fields
- `MattermostChannelTest.java` - Model with computed properties
- `MattermostWebSocketEventTest.java` - Model with nested maps

## Checklist

Before considering tests complete, verify:

- [ ] Both marshalling and unmarshalling tests exist
- [ ] JSON is formatted with one attribute per line
- [ ] All serialized fields are included in expected JSON
- [ ] Test data is realistic and representative
- [ ] Tests pass with `mvn clean test -Dtest=YourModelTest`
- [ ] Field name mapping (snake_case ↔ camelCase) is correct
- [ ] Optional fields are tested appropriately
- [ ] Computed properties are handled correctly
- [ ] Nested structures are properly validated

