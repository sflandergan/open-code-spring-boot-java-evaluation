# Pagination Pattern

## Overview
Prefer **keyset pagination** (seek method) over offset-based pagination for better performance. Keyset pagination uses indexed columns in `WHERE` clauses for constant-time performance regardless of page depth.

## Keyset Pagination (Seek Method)

### Concept
- Use the last record's values from the current page as the starting point for the next page
- Requires stable, indexed sort columns (e.g., `score DESC, id ASC`)
- Build `WHERE` conditions using the last record's values: `WHERE (score, id) < (lastScore, lastId)`
- Always include a unique column (like `id`) in the sort to ensure deterministic ordering

### Advantages
- **Constant performance**: O(1) regardless of page depth
- **No skipped/duplicate records**: Even when data changes between requests
- **Efficient database queries**: Uses indexes effectively
- **Scalable**: Works well with millions of records

### Disadvantages
- **No random page access**: Can't jump to page 5 directly
- **More complex implementation**: Requires tracking cursor values
- **Client complexity**: Client must pass cursor values

## Spring Data Implementation

### Repository Layer

#### Simple Keyset Pagination (Single Sort Column)
```java
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    
    // First page - no cursor needed
    @Query("""
        SELECT d FROM Sensor d
        ORDER BY d.id ASC
        """)
    List<Sensor> findFirstPage(Pageable pageable);
    
    // Next pages - use last ID as cursor
    @Query("""
        SELECT d FROM Sensor d
        WHERE d.id > :lastId
        ORDER BY d.id ASC
        """)
    List<Sensor> findNextPage(@Param("lastId") Long lastId, Pageable pageable);
}
```

#### Complex Keyset Pagination (Multiple Sort Columns)
```java
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    // First page query
    @Query("""
        SELECT p FROM Player p 
        WHERE p.gameId = :gameId 
        ORDER BY p.score DESC, p.id DESC
        """)
    List<Player> findFirstPage(@Param("gameId") Long gameId, Pageable pageable);
    
    // Next page with composite cursor
    @Query("""
        SELECT p FROM Player p 
        WHERE p.gameId = :gameId 
        AND (p.score < :lastScore OR (p.score = :lastScore AND p.id < :lastId))
        ORDER BY p.score DESC, p.id DESC
        """)
    List<Player> findNextPage(
        @Param("gameId") Long gameId,
        @Param("lastScore") Integer lastScore,
        @Param("lastId") Long lastId,
        Pageable pageable
    );
}
```

### Service Layer

#### Simple Implementation
```java
public class SensorService {
    
    private final SensorRepository sensorRepository;
    
    public PageResult<SensorDto> getSensors(Long lastId, int pageSize) {
        // Request one extra item to determine if there are more pages
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        
        List<Sensor> sensors = (lastId == null)
            ? sensorRepository.findFirstPage(pageable)
            : sensorRepository.findNextPage(lastId, pageable);
        
        // Check if we got more items than requested
        boolean hasMore = sensors.size() > pageSize;
        
        // Limit the result to the requested page size
        List<SensorDto> dtos = sensors.stream()
            .limit(pageSize)
            .map(this::toDto)
            .toList();
        
        return new PageResult<>(dtos, hasMore);
    }
}
```

#### Complex Implementation
```java
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    public PageResult<PlayerDto> getPlayers(Long gameId, Integer lastScore, Long lastId, int pageSize) {
        // Request one extra item to determine if there are more pages
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        
        List<Player> players = (lastScore == null || lastId == null)
            ? playerRepository.findFirstPage(gameId, pageable)
            : playerRepository.findNextPage(gameId, lastScore, lastId, pageable);
        
        // Check if we got more items than requested
        boolean hasMore = players.size() > pageSize;
        
        // Limit the result to the requested page size
        List<PlayerDto> dtos = players.stream()
            .limit(pageSize)
            .map(this::toDto)
            .toList();
        
        return new PageResult<>(dtos, hasMore);
    }
}
```

### Response DTO
```java
public record PageResult<T>(List<T> items, boolean hasMore) {}
```

### REST Controller

#### Simple Pagination
```java
@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    
    private final SensorService sensorService;
    
    @GetMapping
    public PageResult<SensorDto> getSensors(
        @RequestParam(required = false) Long lastId,
        @RequestParam(defaultValue = "10") int pageSize) {
        
        return sensorService.getSensors(lastId, pageSize);
    }
}
```

#### Complex Pagination
```java
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    @GetMapping
    public PageResult<PlayerDto> getPlayers(
        @RequestParam Long gameId,
        @RequestParam(required = false) Integer lastScore,
        @RequestParam(required = false) Long lastId,
        @RequestParam(defaultValue = "10") int pageSize) {
        
        return playerService.getPlayers(gameId, lastScore, lastId, pageSize);
    }
}
```

## Mixed Sort Directions

For mixed ASC/DESC ordering (e.g., `score DESC, id ASC`), the WHERE clause becomes more complex:

```java
@Query("""
    SELECT p FROM Player p 
    WHERE p.gameId = :gameId 
    AND (p.score < :lastScore OR (p.score = :lastScore AND p.id > :lastId))
    ORDER BY p.score DESC, p.id ASC
    """)
List<Player> findNextPage(
    @Param("gameId") Long gameId,
    @Param("lastScore") Integer lastScore,
    @Param("lastId") Long lastId,
    Pageable pageable
);
```

### Logic for Mixed Directions
- **First column DESC, second ASC**: `(score < lastScore OR (score = lastScore AND id > lastId))`
- **Both DESC**: `(score < lastScore OR (score = lastScore AND id < lastId))`
- **Both ASC**: `(score > lastScore OR (score = lastScore AND id > lastId))`

## Index Requirements

### Critical for Performance
Ensure composite indexes exist on sort columns:

```sql
-- For score DESC, id DESC
CREATE INDEX idx_player_game_score ON players(game_id, score DESC, id DESC);

-- For simple id ASC
CREATE INDEX idx_sensor_id ON sensors(id ASC);
```

### Index Best Practices
- Index column order must match the query's ORDER BY clause
- Include filter columns (e.g., `game_id`) at the start of the index
- Use DESC/ASC in index definition to match query direction
- Test query plans with EXPLAIN to verify index usage

## Client Usage Examples

### First Request
```http
GET /api/sensors?pageSize=10
```

Response:
```json
{
  "items": [
    {"id": 1, "name": "Sensor-001"},
    {"id": 2, "name": "Sensor-002"},
    ...
    {"id": 10, "name": "Sensor-010"}
  ],
  "hasMore": true
}
```

### Next Page Request
```http
GET /api/sensors?lastId=10&pageSize=10
```

Response:
```json
{
  "items": [
    {"id": 11, "name": "Sensor-011"},
    {"id": 12, "name": "Sensor-012"},
    ...
    {"id": 20, "name": "Sensor-020"}
  ],
  "hasMore": true
}
```

### Complex Pagination Example
```http
GET /api/players?gameId=123&pageSize=10
```

Response:
```json
{
  "items": [
    {"id": 5, "score": 1000, "name": "Player-005"},
    {"id": 3, "score": 950, "name": "Player-003"},
    ...
  ],
  "hasMore": true
}
```

Next page:
```http
GET /api/players?gameId=123&lastScore=850&lastId=7&pageSize=10
```

## Offset-Based Pagination

### When to Use
- Small datasets (< 10,000 records)
- Random page access required (e.g., page numbers in UI)
- Simpler client requirements

### Implementation
```java
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    // Spring Data provides this automatically
}

// Service
public Page<SensorDto> getSensors(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Sensor> sensorPage = sensorRepository.findAll(pageable);
    return sensorPage.map(this::toDto);
}

// Controller
@GetMapping
public Page<SensorDto> getSensors(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return sensorService.getSensors(page, size);
}
```

### Disadvantages
- **Performance degrades**: O(n) where n is the page number
- **Skipped/duplicate records**: When data changes between requests
- **Memory overhead**: Database must skip all previous records
- **Not scalable**: Poor performance with deep pagination

## Testing Pagination

### Repository Tests
```java
@Test
void shouldFindFirstPageOrderedById() {
    sensorRepository.save(new Sensor("Sensor-001", "sensor", Set.of("read")));
    sensorRepository.save(new Sensor("Sensor-002", "actuator", Set.of("write")));
    sensorRepository.save(new Sensor("Sensor-003", "sensor", Set.of("read", "write")));
    
    Pageable pageable = PageRequest.of(0, 2);
    List<Sensor> firstPage = sensorRepository.findFirstPage(pageable);
    
    assertThat(firstPage).hasSize(2);
    assertThat(firstPage.get(0).getId()).isLessThan(firstPage.get(1).getId());
}

@Test
void shouldFindNextPageAfterLastId() {
    Sensor saved1 = sensorRepository.save(new Sensor("Sensor-001", "sensor", Set.of("read")));
    Sensor saved2 = sensorRepository.save(new Sensor("Sensor-002", "actuator", Set.of("write")));
    Sensor saved3 = sensorRepository.save(new Sensor("Sensor-003", "sensor", Set.of("read", "write")));
    
    Pageable pageable = PageRequest.of(0, 2);
    List<Sensor> nextPage = sensorRepository.findNextPage(saved1.getId(), pageable);
    
    assertThat(nextPage).hasSize(2);
    assertThat(nextPage).extracting(Sensor::getId)
            .containsExactly(saved2.getId(), saved3.getId());
}
```

## Best Practices

### Always Include a Unique Column
```java
// Good - includes unique id
ORDER BY score DESC, id DESC

// Bad - score might not be unique
ORDER BY score DESC
```

### Validate Page Size
```java
public PageResult<SensorDto> getSensors(Long lastId, int pageSize) {
    if (pageSize < 1 || pageSize > 100) {
        throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
    // ... rest of implementation
}
```

### Document Cursor Parameters
```java
@GetMapping
@Operation(summary = "Get sensors with pagination")
public PageResult<SensorDto> getSensors(
    @Parameter(description = "ID of the last sensor from previous page")
    @RequestParam(required = false) Long lastId,
    @Parameter(description = "Number of items per page (1-100)")
    @RequestParam(defaultValue = "10") int pageSize) {
    return sensorService.getSensors(lastId, pageSize);
}
```

### Handle Edge Cases
- Empty result sets
- Last page (hasMore = false)
- Invalid cursor values
- Concurrent modifications
