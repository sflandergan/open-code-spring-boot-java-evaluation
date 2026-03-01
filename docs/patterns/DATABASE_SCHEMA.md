# Database Schema Design Guidelines

This document provides comprehensive guidelines for designing database schemas in this Spring Boot application.

## Table Naming Conventions

### Use Plural Names
- **Tables should use plural names** to represent collections of entities
- Examples: `sensors`, `users`, `orders`, `order_items`
- Rationale: A table contains multiple rows, so plural naming is more intuitive

### Use Snake Case
- **All table names must use snake_case**
- Examples: `sensor_configurations`, `user_preferences`, `audit_logs`
- Never use camelCase or PascalCase for table names

## Column Naming Conventions

### Use Snake Case
- **All column names must use snake_case**
- Examples: `created_at`, `updated_at`, `sensor_name`, `user_email`
- This ensures consistency with PostgreSQL conventions and improves readability

### Primary Keys
- **Use `id` as the primary key column name** for single-column primary keys
- Type: `BIGSERIAL` (auto-incrementing 64-bit integer) or `UUID`
- Prefer `BIGSERIAL` for most cases unless you need distributed ID generation
- Example:
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL
  );
  ```

### Foreign Keys
- **Use `<referenced_table_singular>_id` pattern** for foreign key columns
- Examples: `sensor_id`, `user_id`, `order_id`
- Always add foreign key constraints with appropriate ON DELETE/ON UPDATE actions
- Example:
  ```sql
  CREATE TABLE sensor_readings (
      id BIGSERIAL PRIMARY KEY,
      sensor_id BIGINT NOT NULL,
      reading_value DECIMAL(10, 2) NOT NULL,
      CONSTRAINT fk_sensor FOREIGN KEY (sensor_id) 
          REFERENCES sensors(id) ON DELETE CASCADE
  );
  ```

### Timestamps
- **Always include audit timestamp columns**: `created_at` and `updated_at`
- Type: `TIMESTAMP WITH TIME ZONE` (or `TIMESTAMPTZ`)
- Set `created_at` with `DEFAULT CURRENT_TIMESTAMP`
- Update `updated_at` using triggers or application logic
- Example:
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
  );
  ```

### Boolean Columns
- **Use positive, descriptive names**: `is_active`, `is_enabled`, `has_permission`
- Avoid negative names like `is_not_active` or `disabled`
- Type: `BOOLEAN`
- Always set a default value: `DEFAULT FALSE` or `DEFAULT TRUE`

### Enum Columns
- **Use VARCHAR with CHECK constraints** instead of PostgreSQL ENUM types
- Rationale: VARCHAR with CHECK is more flexible for schema evolution
- Example:
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      sensor_type VARCHAR(50) NOT NULL,
      CONSTRAINT chk_sensor_type CHECK (sensor_type IN ('SENSOR', 'ACTUATOR', 'GATEWAY'))
  );
  ```

## Indexing Strategy

### Primary Keys
- Primary keys automatically create a unique index
- No additional action needed

### Foreign Keys
- **Always create indexes on foreign key columns** for join performance
- Example:
  ```sql
  CREATE INDEX idx_sensor_readings_sensor_id ON sensor_readings(sensor_id);
  ```

### Unique Constraints
- **Use unique constraints for natural keys** (e.g., email, username, external IDs)
- Example:
  ```sql
  CREATE TABLE users (
      id BIGSERIAL PRIMARY KEY,
      email VARCHAR(255) NOT NULL,
      CONSTRAINT uq_users_email UNIQUE (email)
  );
  ```
- Unique constraints automatically create a unique index

### Query Performance
- **Create indexes for frequently queried columns**
- Consider composite indexes for multi-column queries
- Example:
  ```sql
  CREATE INDEX idx_sensors_type_status ON sensors(sensor_type, status);
  ```

### Partial Indexes
- **Use partial indexes for filtered queries** to save space
- Example:
  ```sql
  CREATE INDEX idx_sensors_active ON sensors(name) WHERE is_active = TRUE;
  ```

## Constraint Naming Conventions

Use consistent prefixes for constraint names:

- **Primary Key**: `pk_<table_name>`
  ```sql
  CONSTRAINT pk_sensors PRIMARY KEY (id)
  ```

- **Foreign Key**: `fk_<table_name>_<referenced_table>`
  ```sql
  CONSTRAINT fk_sensor_readings_sensors FOREIGN KEY (sensor_id) REFERENCES sensors(id)
  ```

- **Unique**: `uq_<table_name>_<column_name(s)>`
  ```sql
  CONSTRAINT uq_users_email UNIQUE (email)
  ```

- **Check**: `chk_<table_name>_<column_name>`
  ```sql
  CONSTRAINT chk_sensors_type CHECK (sensor_type IN ('SENSOR', 'ACTUATOR'))
  ```

- **Index**: `idx_<table_name>_<column_name(s)>`
  ```sql
  CREATE INDEX idx_sensors_type ON sensors(sensor_type);
  ```

## Data Types

### String Columns
- **Use `VARCHAR(n)` with appropriate length** for bounded strings
- **Use `TEXT`** for unbounded strings (descriptions, comments, JSON)
- Examples:
  - Names, emails: `VARCHAR(255)`
  - Short codes: `VARCHAR(50)`
  - Descriptions: `TEXT`

### Numeric Columns
- **Integers**: `SMALLINT` (2 bytes), `INTEGER` (4 bytes), `BIGINT` (8 bytes)
- **Decimals**: `DECIMAL(precision, scale)` for exact values (money, measurements)
- **Floating Point**: `REAL` or `DOUBLE PRECISION` for approximate values
- Choose the smallest type that fits your data range

### Date and Time
- **Always use `TIMESTAMPTZ`** (timestamp with time zone) for timestamps
- **Use `DATE`** only for dates without time component (birthdays, etc.)
- **Use `TIME`** only for time without date component (business hours, etc.)
- Never store timestamps as strings or integers

### JSON Data
- **Use `JSONB`** (not `JSON`) for JSON data
- JSONB is more efficient for querying and indexing
- Example:
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      capabilities JSONB NOT NULL DEFAULT '{}'::JSONB
  );
  ```

## Relationships

### One-to-Many
- Add foreign key column in the "many" side table
- Example: One sensor has many readings
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL
  );
  
  CREATE TABLE sensor_readings (
      id BIGSERIAL PRIMARY KEY,
      sensor_id BIGINT NOT NULL,
      reading_value DECIMAL(10, 2) NOT NULL,
      CONSTRAINT fk_sensor_readings_sensors FOREIGN KEY (sensor_id) 
          REFERENCES sensors(id) ON DELETE CASCADE
  );
  ```

### Many-to-Many
- Create a junction table with foreign keys to both tables
- Junction table name: `<table1_singular>_<table2_singular>` (alphabetically ordered)
- Example: Sensors can have many tags, tags can be on many sensors
  ```sql
  CREATE TABLE sensors (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL
  );
  
  CREATE TABLE tags (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(100) NOT NULL
  );
  
  CREATE TABLE sensor_tag (
      sensor_id BIGINT NOT NULL,
      tag_id BIGINT NOT NULL,
      PRIMARY KEY (sensor_id, tag_id),
      CONSTRAINT fk_sensor_tag_sensors FOREIGN KEY (sensor_id) 
          REFERENCES sensors(id) ON DELETE CASCADE,
      CONSTRAINT fk_sensor_tag_tags FOREIGN KEY (tag_id) 
          REFERENCES tags(id) ON DELETE CASCADE
  );
  ```

### One-to-One
- Add foreign key with unique constraint in either table
- Prefer adding to the dependent entity
- Example: User has one profile
  ```sql
  CREATE TABLE users (
      id BIGSERIAL PRIMARY KEY,
      email VARCHAR(255) NOT NULL
  );
  
  CREATE TABLE user_profiles (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL,
      bio TEXT,
      CONSTRAINT uq_user_profiles_user_id UNIQUE (user_id),
      CONSTRAINT fk_user_profiles_users FOREIGN KEY (user_id) 
          REFERENCES users(id) ON DELETE CASCADE
  );
  ```

## Referential Integrity

### ON DELETE Actions
Choose appropriate action based on business logic:

- **CASCADE**: Delete child records when parent is deleted
  - Use for dependent entities (e.g., order items when order is deleted)
  
- **RESTRICT**: Prevent deletion if child records exist
  - Use for important relationships (e.g., prevent deleting user with orders)
  
- **SET NULL**: Set foreign key to NULL when parent is deleted
  - Use when child can exist independently (e.g., optional category)

- **NO ACTION**: Similar to RESTRICT but checked at end of transaction
  - Default behavior, rarely used explicitly

Example:
```sql
-- Cascade: readings are meaningless without sensor
CONSTRAINT fk_sensor_readings_sensors FOREIGN KEY (sensor_id) 
    REFERENCES sensors(id) ON DELETE CASCADE

-- Restrict: prevent deleting user with orders
CONSTRAINT fk_orders_users FOREIGN KEY (user_id) 
    REFERENCES users(id) ON DELETE RESTRICT

-- Set null: sensor can exist without category
CONSTRAINT fk_sensors_categories FOREIGN KEY (category_id) 
    REFERENCES categories(id) ON DELETE SET NULL
```

## Schema Evolution

### Adding Columns
- New columns should be nullable or have default values
- Example:
  ```sql
  ALTER TABLE sensors ADD COLUMN firmware_version VARCHAR(50);
  ALTER TABLE sensors ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
  ```

### Removing Columns
- Drop columns in separate migration from code changes
- Ensure no code references the column before dropping
- Example:
  ```sql
  ALTER TABLE sensors DROP COLUMN old_field;
  ```

### Renaming Columns
- Avoid renaming when possible (requires coordinated deployment)
- If necessary, use multi-step process:
  1. Add new column
  2. Copy data
  3. Update code to use new column
  4. Drop old column

### Changing Column Types
- Be cautious with type changes (may require data migration)
- Example:
  ```sql
  -- Safe: increasing VARCHAR length
  ALTER TABLE sensors ALTER COLUMN name TYPE VARCHAR(500);
  
  -- Risky: changing type may fail if data incompatible
  ALTER TABLE sensors ALTER COLUMN status TYPE VARCHAR(50);
  ```

## JPA Entity Mapping

### Table and Column Annotations
Map JPA entities to database schema using annotations:

```java
@Entity
@Table(name = "sensors")
public class Sensor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sensor_name", nullable = false, length = 255)
    private String sensorName;
    
    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // Constructors, getters, setters, etc.
}
```

### Naming Strategy
- Spring Boot uses `SpringPhysicalNamingStrategy` by default
- Converts camelCase to snake_case automatically
- You can omit `@Column(name = "...")` if field name matches after conversion
- Example: `sensorName` → `sensor_name` (automatic)

### Explicit Naming
- **Always use explicit `@Table(name = "...")` for clarity**
- Use explicit `@Column(name = "...")` when:
  - Database column name doesn't follow camelCase → snake_case pattern
  - You want to make the mapping explicit for documentation
  - Column name is a reserved keyword

## Best Practices Summary

1. **Use plural table names** in snake_case
2. **Use snake_case for all columns**
3. **Always include `id`, `created_at`, `updated_at`** columns
4. **Use `BIGSERIAL` for primary keys** (or UUID if needed)
5. **Follow `<table_singular>_id` pattern** for foreign keys
6. **Always create indexes on foreign keys**
7. **Use `TIMESTAMPTZ` for timestamps**, never strings or integers
8. **Use `JSONB` for JSON data**, not `JSON`
9. **Use VARCHAR with CHECK constraints** instead of ENUM types
10. **Choose appropriate ON DELETE actions** for foreign keys
11. **Name constraints consistently** with prefixes (pk_, fk_, uq_, chk_)
12. **Make new columns nullable or with defaults** for safe migrations
13. **Use explicit `@Table` annotations** in JPA entities
14. **Test migrations on a copy of production data** before deploying

## Migration Checklist

Before creating a new migration:

- [ ] Table names are plural and snake_case
- [ ] Column names are snake_case
- [ ] Primary key is `id BIGSERIAL`
- [ ] Foreign keys follow `<table>_id` pattern
- [ ] Timestamps are `created_at` and `updated_at` with `TIMESTAMPTZ`
- [ ] Foreign keys have appropriate ON DELETE actions
- [ ] Indexes created for foreign keys
- [ ] Unique constraints for natural keys
- [ ] Constraints have proper naming (pk_, fk_, uq_, chk_)
- [ ] New columns are nullable or have defaults
- [ ] Migration is idempotent when possible
