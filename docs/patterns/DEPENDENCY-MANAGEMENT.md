# Maven Dependency Management

This document describes the best practices for managing dependencies in this Maven project.

## Version Property Management

**Always define dependency versions as properties** in the `<properties>` section of pom.xml.

### Property Naming Convention

Use descriptive property names following the pattern: `<artifactId>.version`

### Property Organization

Group properties logically in the `<properties>` section:

```xml
<properties>
    <!-- dependency versions -->
    <okhttp.version>4.12.0</okhttp.version>
    <springdoc-openapi-starter-webmvc-ui.version>2.7.0</springdoc-openapi-starter-webmvc-ui.version>
    
    <!-- plugin versions -->
    <docker-maven-plugin.version>0.48.0</docker-maven-plugin.version>
</properties>
```

### Referencing Properties

Reference properties in dependencies using `${property.name}` syntax:

```xml
<dependencies>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>${okhttp.version}</version>
    </dependency>
</dependencies>
```

## When to Define Version Properties

### ✅ DO define version properties for:
- External libraries with explicit versions (e.g., `okhttp`, `commons-lang3`)
- Libraries where you want to control the version independently
- Maven plugins (e.g., `docker-maven-plugin`)
- Transitive dependency overrides

### ❌ DO NOT define version properties for:
- Dependencies inherited from Spring Boot parent POM (e.g., `spring-boot-starter-web`, `spring-boot-starter-validation`)
- Jackson modules managed by parent (e.g., `jackson-databind`, `jackson-datatype-jsr310`)
- Any dependency where version is omitted (parent manages it)

### How to check if a dependency needs a version property:
1. If the `<version>` tag is present in the dependency → move it to a property
2. If the `<version>` tag is absent → dependency is managed by parent, no property needed

## Adding a New Dependency

When adding a new dependency to pom.xml:

### Step 1: Check if version is needed
```bash
# Check if the dependency is managed by the parent POM
mvn help:effective-pom | grep -A 5 "<artifactId>your-artifact-id</artifactId>"
```

### Step 2: Add version property (if needed)
If the dependency requires an explicit version, add it to the `<properties>` section:

```xml
<properties>
    <!-- ...existing properties... -->
    
    <!-- dependency versions -->
    <your-artifact-id.version>1.2.3</your-artifact-id.version>
</properties>
```

### Step 3: Add dependency
```xml
<dependencies>
    <!-- ...existing dependencies... -->
    
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>your-artifact-id</artifactId>
        <version>${your-artifact-id.version}</version>
    </dependency>
</dependencies>
```

### Step 4: Verify
```bash
mvn dependency:tree
```

## Benefits

- **Centralized version management** - All versions in one place
- **Easy updates** - Update multiple dependencies by changing one property
- **Clear overview** - See all external library versions at a glance
- **Consistency** - Follow established patterns across the project
- **Reduced errors** - Avoid version conflicts and inconsistencies
- **Better maintenance** - Easier dependency upgrades

## Example: Current Dependencies

### With Version Properties (External Libraries)
```xml
<properties>
    <okhttp.version>4.12.0</okhttp.version>
    <springdoc-openapi-starter-webmvc-ui.version>2.7.0</springdoc-openapi-starter-webmvc-ui.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>${okhttp.version}</version>
    </dependency>
</dependencies>
```

### Without Version (Parent-Managed)
```xml
<dependencies>
    <!-- No version needed - managed by Spring Boot parent -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

## Troubleshooting

### Problem: Dependency version conflict
```bash
# View dependency tree to identify conflicts
mvn dependency:tree

# View effective POM to see resolved versions
mvn help:effective-pom
```

### Problem: Property not resolving
- Check property name matches exactly: `${artifactId.version}`
- Ensure property is defined in `<properties>` section
- Verify XML syntax is correct (no typos, proper closing tags)

### Problem: Unsure if parent manages dependency
```bash
# Check parent POM for dependency management
mvn help:effective-pom | grep -A 10 "dependencyManagement"
```

## Related Documentation
- [Maven POM Reference](https://maven.apache.org/pom.html)
- [Spring Boot Dependency Management](https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html)

