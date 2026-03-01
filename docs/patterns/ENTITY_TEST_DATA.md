# Entity Test Data Creation Pattern

## Overview
This document describes the recommended approach for creating test entities with pre-set IDs without using reflection.

## The Problem
In tests, we often need to create entity instances with specific ID values to verify behavior. However, entity IDs are typically managed by JPA and should not be publicly settable.

## The Solution: Test Subclass Pattern

### Step 1: Make the `id` field `protected`
In your entity class, change the `id` field from `private` to `protected`:

```java
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;  // protected instead of private
    
    // ... rest of the entity
}
```

### Step 2: Create a Test Subclass
Within your test class, create a static nested subclass that extends the entity and sets the `id` in its constructor:

```java
@WebMvcTest(SensorController.class)
class SensorControllerTest {

    // ... test methods ...

    private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
        return new TestSensor(id, name, type, capabilities);
    }

    static class TestSensor extends Sensor {
        TestSensor(Long id, String name, String type, Set<String> capabilities) {
            super(name, type, capabilities);
            this.id = id;
        }
    }
}
```

## Benefits

- **No reflection**: Cleaner, more maintainable test code
- **Type-safe**: The compiler can verify the code
- **Encapsulation preserved**: The `id` field remains protected, not publicly settable
- **Clear intent**: The test subclass makes it explicit that this is test-specific behavior

## Anti-Pattern: Don't Use Reflection

❌ **Avoid this approach:**

```java
private Sensor createSensor(Long id, String name, String type, Set<String> capabilities) {
    var sensor = new Sensor(name, type, capabilities);
    try {
        var idField = Sensor.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(sensor, id);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    return sensor;
}
```

This is fragile, verbose, and bypasses type safety.

## When to Use This Pattern

Use this pattern in:
- **Controller tests** (`@WebMvcTest`) - when mocking service layer responses
- **Service tests** - when mocking repository responses
- **Any unit test** where you need entities with specific IDs

**Do NOT use this pattern in:**
- **Integration tests** - let the database assign IDs naturally
- **Production code** - IDs should only be set by JPA
